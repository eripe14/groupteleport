package pl.kdronia.groupteleport.random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import pl.kdronia.groupteleport.config.impl.PluginConfig;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class RandomTeleportService {

    private final PluginConfig pluginConfig;

    public RandomTeleportService(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    public CompletableFuture<Optional<Location>> getRandomLocation(World world) {
        return this.findRandomLocationWithAttempts(world, 0);
    }

    private CompletableFuture<Optional<Location>> findRandomLocationWithAttempts(World world, int attempt) {
        if (attempt >= this.pluginConfig.maxTeleportAttempts) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        int randomX = this.generateRandomX();
        int randomZ = this.generateRandomZ();

        return world.getChunkAtAsync(randomX >> 4, randomZ >> 4)
                .thenCompose(chunk -> {
                    Location safeLocation = this.findSafeLocation(world, randomX, randomZ);

                    if (safeLocation != null) {
                        return CompletableFuture.completedFuture(Optional.of(safeLocation));
                    }

                    return this.findRandomLocationWithAttempts(world, attempt + 1);
                });
    }

    private int generateRandomX() {
        return ThreadLocalRandom.current().nextInt(
                this.pluginConfig.randomTeleportMinX,
                this.pluginConfig.randomTeleportMaxX + 1
        );
    }

    private int generateRandomZ() {
        return ThreadLocalRandom.current().nextInt(
                this.pluginConfig.randomTeleportMinZ,
                this.pluginConfig.randomTeleportMaxZ + 1
        );
    }

    private Location findSafeLocation(World world, int x, int z) {
        int highestY = world.getHighestBlockYAt(x, z);

        if (highestY <= world.getMinHeight()) {
            return null;
        }

        int standBlockHeight = highestY + 1;

        Block groundBlock = world.getBlockAt(x, highestY, z);
        Block feetBlock = world.getBlockAt(x, standBlockHeight, z);
        Block headBlock = world.getBlockAt(x, highestY + 2, z);

        if (!groundBlock.getType().isSolid()) {
            return null;
        }

        if (!feetBlock.getType().isAir() || !headBlock.getType().isAir()) {
            return null;
        }

        return new Location(world, x + 0.5, standBlockHeight, z + 0.5, 0f, 0f);
    }
}