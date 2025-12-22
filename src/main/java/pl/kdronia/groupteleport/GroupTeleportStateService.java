package pl.kdronia.groupteleport;

import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GroupTeleportStateService {

    private final Map<String, Set<UUID>> pendingTeleports = new ConcurrentHashMap<>();
    private final Map<String, BukkitTask> activeTeleports = new ConcurrentHashMap<>();

    public void addPendingPlayer(String teleportId, UUID playerId) {
        this.pendingTeleports.computeIfAbsent(teleportId, k -> ConcurrentHashMap.newKeySet()).add(playerId);
    }

    public void removePendingPlayer(String teleportId, UUID playerId) {
        Set<UUID> players = this.pendingTeleports.get(teleportId);
        if (players != null && players.remove(playerId) && players.isEmpty()) {
            this.pendingTeleports.remove(teleportId);
        }
    }

    public Set<UUID> removePendingTeleport(String teleportId) {
        return this.pendingTeleports.remove(teleportId);
    }

    public Set<UUID> getPendingPlayers(String teleportId) {
        return this.pendingTeleports.getOrDefault(teleportId, Collections.emptySet());
    }

    public int getPendingPlayerCount(String teleportId) {
        Set<UUID> players = this.pendingTeleports.get(teleportId);
        return players != null ? players.size() : 0;
    }

    public boolean isPlayerPending(String teleportId, UUID playerId) {
        Set<UUID> players = this.pendingTeleports.get(teleportId);
        return players != null && players.contains(playerId);
    }

    public void setActiveTeleport(String teleportId, BukkitTask task) {
        this.activeTeleports.put(teleportId, task);
    }

    public boolean isActiveTeleport(String teleportId) {
        return this.activeTeleports.containsKey(teleportId);
    }

    public void removeActiveTeleport(String teleportId) {
        this.activeTeleports.remove(teleportId);
    }

    public void cancelTeleport(String teleportId) {
        BukkitTask task = this.activeTeleports.remove(teleportId);
        if (task != null) {
            task.cancel();
        }
        this.pendingTeleports.remove(teleportId);
    }

    public void shutdown() {
        this.activeTeleports.values().forEach(BukkitTask::cancel);
        this.activeTeleports.clear();
        this.pendingTeleports.clear();
    }

}