package com.akitain.fairlands.world;

import com.akitain.fairlands.Fairlands;
import com.akitain.fairlands.rule.FairlandsGameRules;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

public final class WorldProgressionDebugCommand {
    private WorldProgressionDebugCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var progression = Commands.literal("world_progression")
                    .then(Commands.literal("here")
                            .executes(context -> reportPosition(context.getSource(), BlockPos.containing(context.getSource().getPosition()))))
                    .then(Commands.literal("at")
                            .then(Commands.argument("x", IntegerArgumentType.integer())
                                    .then(Commands.argument("z", IntegerArgumentType.integer())
                                            .executes(context -> reportPosition(
                                                    context.getSource(),
                                                    commandPosition(context.getSource(), IntegerArgumentType.getInteger(context, "x"), IntegerArgumentType.getInteger(context, "z"))
                                            )))))
                    .then(Commands.literal("samples")
                            .executes(context -> reportSamples(context.getSource())))
                    .then(Commands.literal("count")
                            .then(Commands.literal("here")
                                    .then(Commands.argument("chunkRadius", IntegerArgumentType.integer(0, 2))
                                            .executes(context -> countPosition(
                                                    context.getSource(),
                                                    BlockPos.containing(context.getSource().getPosition()),
                                                    IntegerArgumentType.getInteger(context, "chunkRadius")
                                            ))))
                            .then(Commands.literal("at")
                                    .then(Commands.argument("x", IntegerArgumentType.integer())
                                            .then(Commands.argument("z", IntegerArgumentType.integer())
                                                    .then(Commands.argument("chunkRadius", IntegerArgumentType.integer(0, 2))
                                                            .executes(context -> countPosition(
                                                                    context.getSource(),
                                                                    commandPosition(context.getSource(), IntegerArgumentType.getInteger(context, "x"), IntegerArgumentType.getInteger(context, "z")),
                                                                    IntegerArgumentType.getInteger(context, "chunkRadius")
                                                            ))))))
                            .then(Commands.literal("samples")
                                    .then(Commands.argument("chunkRadius", IntegerArgumentType.integer(0, 1))
                                            .executes(context -> countSamples(
                                                    context.getSource(),
                                                    IntegerArgumentType.getInteger(context, "chunkRadius")
                                            )))));

            dispatcher.register(Commands.literal("fairlands_debug")
                    .requires(PermissionPredicates.require(Fairlands.id("debug"), PermissionLevel.GAMEMASTERS))
                    .then(progression));
        });
    }

    private static BlockPos commandPosition(CommandSourceStack source, int x, int z) {
        return new BlockPos(x, source.getLevel().getSeaLevel(), z);
    }

    private static int reportPosition(CommandSourceStack source, BlockPos pos) {
        ServerLevel level = source.getLevel();
        source.sendSuccess(() -> Component.literal(formatProgressionLine(level, pos)), false);
        return 1;
    }

    private static int reportSamples(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos spawn = WorldProgressionRules.worldSpawn(level);
        int innerRadius = level.getGameRules().get(FairlandsGameRules.ORE_PROGRESSION_INNER_RADIUS);
        int normalRadius = level.getGameRules().get(FairlandsGameRules.ORE_PROGRESSION_NORMAL_RADIUS);

        source.sendSuccess(() -> Component.literal(formatProgressionLine(level, spawn)), false);
        source.sendSuccess(() -> Component.literal(formatProgressionLine(level, spawn.offset(innerRadius, 0, 0))), false);
        source.sendSuccess(() -> Component.literal(formatProgressionLine(level, spawn.offset((innerRadius + normalRadius) / 2, 0, 0))), false);
        source.sendSuccess(() -> Component.literal(formatProgressionLine(level, spawn.offset(normalRadius, 0, 0))), false);
        source.sendSuccess(() -> Component.literal(formatProgressionLine(level, spawn.offset(normalRadius + 1000, 0, 0))), false);
        return 5;
    }

    private static int countPosition(CommandSourceStack source, BlockPos pos, int chunkRadius) {
        ServerLevel level = source.getLevel();
        OreScanResult result = scanOres(level, pos, chunkRadius);
        source.sendSuccess(() -> Component.literal(formatScanLine(level, pos, result)), false);
        return result.totalOres();
    }

    private static int countSamples(CommandSourceStack source, int chunkRadius) {
        ServerLevel level = source.getLevel();
        BlockPos spawn = WorldProgressionRules.worldSpawn(level);
        int innerRadius = level.getGameRules().get(FairlandsGameRules.ORE_PROGRESSION_INNER_RADIUS);
        int normalRadius = level.getGameRules().get(FairlandsGameRules.ORE_PROGRESSION_NORMAL_RADIUS);
        BlockPos[] samples = {
                spawn,
                spawn.offset(innerRadius, 0, 0),
                spawn.offset((innerRadius + normalRadius) / 2, 0, 0),
                spawn.offset(normalRadius, 0, 0),
                spawn.offset(normalRadius + 1000, 0, 0)
        };

        for (BlockPos sample : samples) {
            OreScanResult result = scanOres(level, sample, chunkRadius);
            source.sendSuccess(() -> Component.literal(formatScanLine(level, sample, result)), false);
        }

        return samples.length;
    }

    private static String formatProgressionLine(ServerLevel level, BlockPos pos) {
        return "Fairlands ore progression at "
                + pos.getX()
                + ", "
                + pos.getZ()
                + " | distance="
                + Math.round(WorldProgressionRules.distanceFromSpawn(level, pos))
                + " | vein="
                + WorldProgressionRules.oreVeinPercent(level, pos)
                + "%"
                + " | far_bonus="
                + WorldProgressionRules.oreBonusPercent(level, pos)
                + "%"
                + " | expected="
                + WorldProgressionRules.expectedOreAttemptPercent(level, pos)
                + "%";
    }

    private static OreScanResult scanOres(ServerLevel level, BlockPos center, int chunkRadius) {
        ChunkPos centerChunk = ChunkPos.containing(center);
        int chunksScanned = 0;
        int totalOres = 0;
        int coal = 0;
        int copper = 0;
        int iron = 0;
        int gold = 0;
        int redstone = 0;
        int lapis = 0;
        int emerald = 0;
        int diamond = 0;

        for (int chunkX = centerChunk.x() - chunkRadius; chunkX <= centerChunk.x() + chunkRadius; chunkX++) {
            for (int chunkZ = centerChunk.z() - chunkRadius; chunkZ <= centerChunk.z() + chunkRadius; chunkZ++) {
                ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                level.getChunk(chunkX, chunkZ);
                chunksScanned++;

                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
                    for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                        for (int y = level.getMinY(); y < level.getMaxY(); y++) {
                            pos.set(x, y, z);
                            BlockState state = level.getBlockState(pos);
                            if (state.is(BlockTags.COAL_ORES)) {
                                coal++;
                                totalOres++;
                            } else if (state.is(BlockTags.COPPER_ORES)) {
                                copper++;
                                totalOres++;
                            } else if (state.is(BlockTags.IRON_ORES)) {
                                iron++;
                                totalOres++;
                            } else if (state.is(BlockTags.GOLD_ORES)) {
                                gold++;
                                totalOres++;
                            } else if (state.is(BlockTags.REDSTONE_ORES)) {
                                redstone++;
                                totalOres++;
                            } else if (state.is(BlockTags.LAPIS_ORES)) {
                                lapis++;
                                totalOres++;
                            } else if (state.is(BlockTags.EMERALD_ORES)) {
                                emerald++;
                                totalOres++;
                            } else if (state.is(BlockTags.DIAMOND_ORES)) {
                                diamond++;
                                totalOres++;
                            }
                        }
                    }
                }
            }
        }

        return new OreScanResult(chunksScanned, totalOres, coal, copper, iron, gold, redstone, lapis, emerald, diamond);
    }

    private static String formatScanLine(ServerLevel level, BlockPos pos, OreScanResult result) {
        return formatProgressionLine(level, pos)
                + " | chunks="
                + result.chunksScanned()
                + " | ores="
                + result.totalOres()
                + " | per_chunk="
                + String.format("%.1f", result.oresPerChunk())
                + " | coal="
                + result.coal()
                + " copper="
                + result.copper()
                + " iron="
                + result.iron()
                + " gold="
                + result.gold()
                + " redstone="
                + result.redstone()
                + " lapis="
                + result.lapis()
                + " emerald="
                + result.emerald()
                + " diamond="
                + result.diamond();
    }

    private record OreScanResult(
            int chunksScanned,
            int totalOres,
            int coal,
            int copper,
            int iron,
            int gold,
            int redstone,
            int lapis,
            int emerald,
            int diamond
    ) {
        private double oresPerChunk() {
            if (chunksScanned == 0) {
                return 0;
            }

            return (double) totalOres / chunksScanned;
        }
    }
}
