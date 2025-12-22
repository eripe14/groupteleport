package pl.kdronia.groupteleport;

import eu.okaeri.configs.OkaeriConfig;

public class GroupTeleportConfig extends OkaeriConfig {

    private String id;
    private String worldGuardRegionId;
    private String destinationWorld;
    private int requiredPlayers;
    private int teleportTime;

    private GroupTeleportConfig() {}

    public GroupTeleportConfig(String id, String worldGuardRegionId, String destinationWorld, int requiredPlayers, int teleportTime) {
        this.id = id;
        this.worldGuardRegionId = worldGuardRegionId;
        this.destinationWorld = destinationWorld;
        this.requiredPlayers = requiredPlayers;
        this.teleportTime = teleportTime;
    }

    public String getId() {
        return id;
    }

    public String getWorldGuardRegionId() {
        return worldGuardRegionId;
    }

    public String getDestinationWorld() {
        return destinationWorld;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public int getTeleportTime() {
        return teleportTime;
    }
}