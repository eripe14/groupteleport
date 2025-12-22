package pl.kdronia.groupteleport;

import net.raidstone.wgevents.events.RegionLeftEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.kdronia.groupteleport.config.impl.PluginConfig;
import pl.kdronia.groupteleport.util.RegionUtil;

import java.util.Optional;

public class GroupTeleportController implements Listener {

    private final PluginConfig pluginConfig;
    private final GroupTeleportService groupTeleportService;

    public GroupTeleportController(PluginConfig pluginConfig, GroupTeleportService groupTeleportService) {
        this.pluginConfig = pluginConfig;
        this.groupTeleportService = groupTeleportService;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    void onRegionLeft(RegionLeftEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        String leftRegionId = event.getRegion().getId();

        if (!this.isGroupTeleportRegion(leftRegionId)) {
            return;
        }

        String teleportId = this.findTeleportIdByRegion(leftRegionId).orElse(null);

        if (teleportId == null) {
            return;
        }

        this.groupTeleportService.removePlayer(teleportId, player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    void onPressurePlateActivate(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        if (clickedBlock.getType() != this.pluginConfig.activationPlateMaterial) {
            return;
        }

        if (event.getAction() != Action.PHYSICAL) {
            return;
        }

        Optional<String> regionIdOptional = this.getRegionIdForTeleport(clickedBlock.getLocation());

        if (regionIdOptional.isEmpty()) {
            return;
        }

        String regionId = regionIdOptional.get();

        if (!this.isGroupTeleportRegion(regionId)) {
            return;
        }

        Player player = event.getPlayer();
        this.groupTeleportService.addPlayer(regionId, player.getUniqueId());
    }

    private boolean isGroupTeleportRegion(String regionId) {
        return this.pluginConfig.groupTeleports.values().stream()
                .anyMatch(config -> config.getWorldGuardRegionId().equalsIgnoreCase(regionId));
    }

    private Optional<String> findTeleportIdByRegion(String regionId) {
        return this.pluginConfig.groupTeleports.values().stream()
                .filter(config -> config.getWorldGuardRegionId().equalsIgnoreCase(regionId))
                .map(GroupTeleportConfig::getId)
                .findFirst();
    }

    private Optional<String> getRegionIdForTeleport(Location location) {
        return RegionUtil.getRegionsIdsAtLocation(location).stream()
                .filter(id -> id.startsWith(this.pluginConfig.worldGuardRegionIdPrefix))
                .findFirst();
    }

}