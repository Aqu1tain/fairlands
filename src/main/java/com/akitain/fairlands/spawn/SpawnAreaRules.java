package com.akitain.fairlands.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class SpawnAreaRules {
    private SpawnAreaRules() {
    }

    public static boolean isWithinRadius(ServerLevel level, BlockPos pos, int radius) {
        if (radius <= 0) {
            return false;
        }

        BlockPos worldSpawn = level.getServer().overworld().getRespawnData().pos();
        long maxDistanceSquared = (long) radius * radius;
        return horizontalDistanceSquared(worldSpawn, pos) <= maxDistanceSquared;
    }

    private static long horizontalDistanceSquared(BlockPos origin, BlockPos target) {
        long xOffset = (long) target.getX() - origin.getX();
        long zOffset = (long) target.getZ() - origin.getZ();
        return xOffset * xOffset + zOffset * zOffset;
    }
}
