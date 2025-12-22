package pl.kdronia.groupteleport.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;

import java.util.List;

public final class RegionUtil {

    private RegionUtil() {
    }

    public static List<String> getRegionsIdsAtLocation(Location location) {
        com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        return query.getApplicableRegions(wgLocation, RegionQuery.QueryOption.COMPUTE_PARENTS).getRegions().stream()
                .map(ProtectedRegion::getId)
                .toList();
    }

}