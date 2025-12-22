package pl.kdronia.groupteleport;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import pl.kdronia.groupteleport.config.impl.PluginConfig;
import pl.kdronia.groupteleport.notice.NoticeService;
import pl.kdronia.groupteleport.random.RandomTeleportService;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
        return CompletableFuture.supplyAsync(
                () -> this.executeTeleportation(teleportId, world),
                this.server.getScheduler().getMainThreadExecutor(this.plugin)
        );
    }

    private boolean executeTeleportation(String teleportId, World world) {
        Set<UUID> players = this.stateManager.removePendingTeleport(teleportId);

        if (players == null || players.isEmpty()) {
            return false;
        }

        this.randomTeleportService.getRandomLocation(world).whenComplete((location, ex) -> {
            if (ex != null) {
                this.plugin.getLogger().severe("Failed to get random location: " + ex.getMessage());
                return;
            }

            if (location == null) {
                return;
            }

            List<Player> onlinePlayers = this.getOnlinePlayersForTeleport(players);
            if (onlinePlayers.isEmpty()) {
                return;
            }

            for (Player player : onlinePlayers) {
                player.teleportAsync(location);
            }
        });

        this.stateManager.removeActiveTeleport(teleportId);
        return true;
    }

    private List<Player> getOnlinePlayersForTeleport(Set<UUID> playerIds) {
        return playerIds.stream()
                .map(this.server::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .toList();
    }

    public void addPlayer(String regionId, UUID playerId) {
        this.findTeleportIdByRegion(regionId).ifPresent(teleportId -> {
            if (!this.stateManager.isPlayerPending(teleportId, playerId)) {
                this.stateManager.addPendingPlayer(teleportId, playerId);
                this.activateTeleport(teleportId);
            }
        });
    }

    private Optional<String> findTeleportIdByRegion(String regionId) {
        return this.pluginConfig.groupTeleports.values().stream()
                .filter(config -> config.getWorldGuardRegionId().equals(regionId))
                .map(GroupTeleportConfig::getId)
                .findFirst();
    }

    public void removePlayer(String teleportId, UUID playerId) {
        if (this.stateManager.isActiveTeleport(teleportId)) {
            this.cancelTeleport(teleportId);
        }

        this.stateManager.removePendingPlayer(teleportId, playerId);
    }

    public void activateTeleport(String teleportId) {
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
            throw new IllegalStateException("Destination world not found: " + config.getDestinationWorld());
        }

        this.startTeleportCountdown(teleportId, world, config.getTeleportTime());
    }

    private void startTeleportCountdown(String teleportId, World world, int countdownSeconds) {
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
                this.server,
                this.plugin
        );

        BukkitTask task = this.server.getScheduler().runTaskTimer(this.plugin, runnable, 0L, 1L);
        runnable.setTask(task);

        this.stateManager.setActiveTeleport(teleportId, task);
    }

    public void cancelTeleport(String teleportId) {
        Set<UUID> players = this.stateManager.getPendingPlayers(teleportId);

        if (players != null) {
            this.notifyPlayersAboutCancellation(players);
        }

        this.stateManager.cancelTeleport(teleportId);
    }

    private void notifyPlayersAboutCancellation(Set<UUID> players) {
        this.noticeService.create()
                .notice(messages -> messages.teleportCancelled)
                .players(players)
                .send();
    }

    public void shutdown() {
        this.stateManager.shutdown();
    }
}