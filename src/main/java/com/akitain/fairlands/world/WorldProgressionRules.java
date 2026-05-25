package com.akitain.fairlands.world;

import com.akitain.fairlands.rule.FairlandsGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public final class WorldProgressionRules {
    private WorldProgressionRules() {
    }

    public static boolean shouldSkipOreVein(WorldGenLevel level, BlockPos origin, OreConfiguration config, RandomSource random) {
        if (!affectsOreGeneration(level, config)) {
            return false;
        }

        int veinPercent = oreVeinPercent(level.getLevel(), origin);
        return random.nextInt(100) >= veinPercent;
    }

    public static boolean shouldPlaceBonusOreVein(WorldGenLevel level, BlockPos origin, OreConfiguration config, RandomSource random) {
        if (!affectsOreGeneration(level, config)) {
            return false;
        }

        ServerLevel serverLevel = level.getLevel();
        if (!isBeyondNormalRadius(serverLevel, origin)) {
            return false;
        }

        return random.nextInt(100) < FairlandsGameRules.oreProgressionFarBonusPercent(serverLevel);
    }

    private static boolean affectsOreGeneration(WorldGenLevel level, OreConfiguration config) {
        ServerLevel serverLevel = level.getLevel();
        return serverLevel.dimension() == Level.OVERWORLD
                && FairlandsGameRules.worldProgressionEnabled(serverLevel)
                && isOreConfig(config);
    }

    private static int oreVeinPercent(ServerLevel level, BlockPos origin) {
        int innerRadius = FairlandsGameRules.oreProgressionInnerRadius(level);
        int normalRadius = FairlandsGameRules.oreProgressionNormalRadius(level);
        int innerPercent = FairlandsGameRules.oreProgressionInnerVeinPercent(level);
        double distance = Math.sqrt(horizontalDistanceSquared(level, origin));

        if (distance <= innerRadius) {
            return innerPercent;
        }

        if (normalRadius <= innerRadius || distance >= normalRadius) {
            return 100;
        }

        double progress = (distance - innerRadius) / (normalRadius - innerRadius);
        return (int) Math.round(innerPercent + (100 - innerPercent) * progress);
    }

    private static boolean isBeyondNormalRadius(ServerLevel level, BlockPos origin) {
        int normalRadius = FairlandsGameRules.oreProgressionNormalRadius(level);
        return horizontalDistanceSquared(level, origin) > (long) normalRadius * normalRadius;
    }

    private static boolean isOreConfig(OreConfiguration config) {
        return config.targetStates.stream().anyMatch(target -> target.state.is(BlockTags.COAL_ORES)
                || target.state.is(BlockTags.COPPER_ORES)
                || target.state.is(BlockTags.DIAMOND_ORES)
                || target.state.is(BlockTags.EMERALD_ORES)
                || target.state.is(BlockTags.GOLD_ORES)
                || target.state.is(BlockTags.IRON_ORES)
                || target.state.is(BlockTags.LAPIS_ORES)
                || target.state.is(BlockTags.REDSTONE_ORES));
    }

    private static long horizontalDistanceSquared(ServerLevel level, BlockPos target) {
        BlockPos spawn = level.getServer().overworld().getRespawnData().pos();
        long xOffset = (long) target.getX() - spawn.getX();
        long zOffset = (long) target.getZ() - spawn.getZ();
        return xOffset * xOffset + zOffset * zOffset;
    }
}
