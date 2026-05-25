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

public final class WorldProgressionDebugCommand {
    private WorldProgressionDebugCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("fairlands_debug")
                        .requires(PermissionPredicates.require(Fairlands.id("debug"), PermissionLevel.GAMEMASTERS))
                        .then(Commands.literal("world_progression")
                                .then(Commands.literal("here")
                                        .executes(context -> reportPosition(context.getSource(), BlockPos.containing(context.getSource().getPosition()))))
                                .then(Commands.literal("at")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(context -> reportPosition(
                                                                context.getSource(),
                                                                new BlockPos(
                                                                        IntegerArgumentType.getInteger(context, "x"),
                                                                        context.getSource().getLevel().getSeaLevel(),
                                                                        IntegerArgumentType.getInteger(context, "z")
                                                                )
                                                        )))))
                                .then(Commands.literal("samples")
                                        .executes(context -> reportSamples(context.getSource()))))
        ));
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
}
