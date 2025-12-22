package pl.kdronia.groupteleport.random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import pl.kdronia.groupteleport.config.impl.PluginConfig;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class RandomTeleportService {

    private final Random random;
    private final PluginConfig pluginConfig;

    public RandomTeleportService(PluginConfig pluginConfig) {
        this.random = new Random();
        this.pluginConfig = pluginConfig;
    }

    public CompletableFuture<Location> getRandomLocation(World world) {
        return CompletableFuture.supplyAsync(() -> {
            int randomX = this.generateRandomX();
            int randomZ = this.generateRandomZ();

            return world.getChunkAtAsync(new Location(world, randomX, 100, randomZ))
                    .thenApply(chunk -> this.findSafeLocation(world, randomX, randomZ))
                    .thenCompose(location -> {
                        if (this.isLocationSafe(location)) {
                            return CompletableFuture.completedFuture(location);
                        }
                        return this.getRandomLocation(world);
                    })
                    .join();
        });
    }

    private int generateRandomX() {
        return this.random.nextInt(
                this.pluginConfig.randomTeleportMaxX - this.pluginConfig.randomTeleportMinX + 1
        ) + this.pluginConfig.randomTeleportMinX;
    }

    private int generateRandomZ() {
        return this.random.nextInt(
                this.pluginConfig.randomTeleportMaxZ - this.pluginConfig.randomTeleportMinZ + 1
        ) + this.pluginConfig.randomTeleportMinZ;
    }

    private Location findSafeLocation(World world, int x, int z) {
        int highestY = world.getHighestBlockYAt(x, z);

        // Jeśli miejsce się nie załadowało, zwróć warunkową lokację
        if (highestY <= 0) {
            return null;
        }

        Location location = new Location(world, x + 0.5, highestY + 1, z + 0.5);

        // Upewnij się że są puste bloki powyżej
        Block headBlock = location.add(0, 1, 0).getBlock();
        location.subtract(0, 1, 0);

        if (headBlock.getType() == Material.AIR) {
            return location;
        }

        return null;
    }

    private boolean isLocationSafe(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        Block feetBlock = location.subtract(0, 1, 0).getBlock();
        location.add(0, 1, 0);

        Block headBlock = location.getBlock();
        Block chestBlock = location.add(0, 1, 0).getBlock();
        location.subtract(0, 1, 0);

        // Feet muszą być na czymś solidnym
        if (!feetBlock.getType().isSolid()) {
            return false;
        }

        // Głowa i klatka piersiowa muszą być puste
        return headBlock.getType() == Material.AIR && chestBlock.getType() == Material.AIR;
    }
}