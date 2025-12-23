package pl.kdronia.groupteleport.config.impl;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;
import pl.kdronia.groupteleport.GroupTeleportConfig;

import java.util.Map;

public class PluginConfig extends OkaeriConfig {

    public Material activationPlateMaterial = Material.STONE_PRESSURE_PLATE;

    public String worldGuardRegionIdPrefix = "teparka_";

    public int randomTeleportMinX = -1000;
    public int randomTeleportMaxX = 1000;

    public int randomTeleportMinZ = -1000;
    public int randomTeleportMaxZ = 1000;

    public int maxTeleportAttempts = 50;

    public boolean notifyPlayerAboutTeleportationFailure = true;

    public Map<String, GroupTeleportConfig> groupTeleports = Map.of(
            "tp_1", new GroupTeleportConfig(
                    "tp_1",
                    "teparka_1v1",
                    "world",
                    2,
                    5
            ),
            "tp_2", new GroupTeleportConfig(
                    "tp_2",
                    "teparka_1v1v1",
                    "world",
                    3,
                    10
            )
    );

}