package pl.kdronia.groupteleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import pl.kdronia.groupteleport.config.impl.PluginConfig;
import pl.kdronia.groupteleport.notice.NoticeService;
import pl.kdronia.groupteleport.random.RandomTeleportService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class GroupTeleportService {

    private final Plugin plugin;
    private final Server server;
    private final PluginConfig pluginConfig;
    private final RandomTeleportService randomTeleportService;
    private final NoticeService noticeService;
    private final GroupTeleportStateService stateManager;

    public GroupTeleportService(
            Plugin plugin,
            Server server,
            PluginConfig pluginConfig,
            RandomTeleportService randomTeleportService,
            NoticeService noticeService
    ) {
        this.plugin = plugin;
        this.server = server;
        this.pluginConfig = pluginConfig;
        this.randomTeleportService = randomTeleportService;
        this.noticeService = noticeService;
        this.stateManager = new GroupTeleportStateService();
    }

    public CompletableFuture<Boolean> teleportGroup(String teleportId, World world) {
        Set<UUID> playerIds = this.stateManager.removePendingTeleport(teleportId);

        if (playerIds == null || playerIds.isEmpty()) {
            this.stateManager.removeActiveTeleport(teleportId);
            return CompletableFuture.completedFuture(false);
        }

        return this.randomTeleportService.getRandomLocation(world)
                .thenApplyAsync(locationOptional -> this.executeTeleportation(playerIds, locationOptional))
                .exceptionally(ex -> {
                    this.plugin.getLogger().severe("Failed to teleport group '" + teleportId + "': " + ex.getMessage());
                    return false;
                })
                .whenComplete((result, ex) -> this.stateManager.removeActiveTeleport(teleportId));
    }

    private boolean executeTeleportation(Set<UUID> playerIds, Optional<Location> locationOptional) {
        List<Player> onlinePlayers = this.getOnlinePlayers(playerIds);

        if (onlinePlayers.isEmpty()) {
            return false;
        }

        if (locationOptional.isEmpty()) {
            this.notifyTeleportFailure(onlinePlayers);
            return false;
        }

        Location destination = locationOptional.get();
        onlinePlayers.forEach(player -> player.teleportAsync(destination));

        return true;
    }

    private void notifyTeleportFailure(List<Player> players) {
        if (!this.pluginConfig.notifyPlayerAboutTeleportationFailure) {
            return;
        }

        List<UUID> playerIds = players.stream()
                .map(Player::getUniqueId)
                .toList();

        this.noticeService.create()
                .notice(messages -> messages.teleportationFailed)
                .players(playerIds)
                .send();
    }

    private List<Player> getOnlinePlayers(Set<UUID> playerIds) {
        return playerIds.stream()
                .map(this.server::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    public void addPlayer(String regionId, UUID playerId) {
        this.findTeleportIdByRegion(regionId).ifPresent(teleportId -> {
            if (this.stateManager.isPlayerPending(teleportId, playerId)) {
                return;
            }

            this.stateManager.addPendingPlayer(teleportId, playerId);
            this.tryActivateTeleport(teleportId);
        });
    }

    public void removePlayer(String teleportId, UUID playerId) {
        this.stateManager.removePendingPlayer(teleportId, playerId);

        if (this.stateManager.isActiveTeleport(teleportId)) {
            this.cancelTeleport(teleportId);
        }
    }

    public void cancelTeleport(String teleportId) {
        Set<UUID> players = this.stateManager.getPendingPlayers(teleportId);

        if (players != null && !players.isEmpty()) {
            this.noticeService.create()
                    .notice(messages -> messages.teleportCancelled)
                    .players(players)
                    .send();
        }

        this.stateManager.cancelTeleport(teleportId);
    }

    private void tryActivateTeleport(String teleportId) {
        if (this.stateManager.isActiveTeleport(teleportId)) {
            return;
        }

        GroupTeleportConfig config = this.pluginConfig.groupTeleports.get(teleportId);
        if (config == null) {
            this.plugin.getLogger().warning("Teleport configuration not found: " + teleportId);
            return;
        }

        int pendingCount = this.stateManager.getPendingPlayerCount(teleportId);
        if (pendingCount != config.getRequiredPlayers()) {
            return;
        }

        World world = Bukkit.getWorld(config.getDestinationWorld());
        if (world == null) {
            this.plugin.getLogger().severe("Destination world not found: " + config.getDestinationWorld());
            return;
        }

        this.startCountdown(teleportId, world, config.getTeleportTime());
    }

    private void startCountdown(String teleportId, World world, int countdownSeconds) {
        Set<UUID> players = this.stateManager.getPendingPlayers(teleportId);

        if (players == null || players.isEmpty()) {
            return;
        }

        CountdownTimer timer = new CountdownTimer(countdownSeconds);
        GroupTeleportRunnable runnable = new GroupTeleportRunnable(
                teleportId,
                players,
                world,
                timer,
                this,
                this.noticeService,
                this.server
        );

        BukkitTask task = this.server.getScheduler().runTaskTimer(this.plugin, runnable, 0L, 1L);
        runnable.setTask(task);

        this.stateManager.setActiveTeleport(teleportId, task);
    }

    private Optional<String> findTeleportIdByRegion(String regionId) {
        return this.pluginConfig.groupTeleports.values().stream()
                .filter(config -> config.getWorldGuardRegionId().equals(regionId))
                .map(GroupTeleportConfig::getId)
                .findFirst();
    }

    public void shutdown() {
        this.stateManager.shutdown();
    }
}