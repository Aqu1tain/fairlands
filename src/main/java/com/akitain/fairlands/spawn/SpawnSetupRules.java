package com.akitain.fairlands.spawn;

import com.akitain.fairlands.rule.FairlandsGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelData;

public final class SpawnSetupRules {
    private SpawnSetupRules() {
    }

    public static boolean canSetRespawnPosition(ServerPlayer player, ServerPlayer.RespawnConfig respawnConfig) {
        if (respawnConfig == null) {
            return true;
        }

        ServerLevel level = player.level();
        if (!FairlandsGameRules.respawnSetupRestrictionEnabled(level)) {
            return true;
        }

        return isWithinAllowedRadius(level, respawnConfig.respawnData());
    }

    public static void sendBlockedRespawnMessage(ServerPlayer player) {
        int radius = FairlandsGameRules.respawnSetupRadius(player.level());
        player.sendSystemMessage(Component.literal("Respawn point unchanged: it must be within " + radius + " blocks of world spawn."));
    }

    private static boolean isWithinAllowedRadius(ServerLevel level, LevelData.RespawnData respawnData) {
        BlockPos worldSpawn = level.getServer().overworld().getRespawnData().pos();
        BlockPos requestedSpawn = respawnData.pos();
        int radius = FairlandsGameRules.respawnSetupRadius(level);
        long maxDistanceSquared = (long) radius * radius;
        long distanceSquared = horizontalDistanceSquared(worldSpawn, requestedSpawn);

        return distanceSquared <= maxDistanceSquared;
    }

    private static long horizontalDistanceSquared(BlockPos origin, BlockPos target) {
        long xOffset = (long) target.getX() - origin.getX();
        long zOffset = (long) target.getZ() - origin.getZ();
        return xOffset * xOffset + zOffset * zOffset;
    }
}
