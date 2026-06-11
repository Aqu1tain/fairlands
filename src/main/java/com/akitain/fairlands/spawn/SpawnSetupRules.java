package com.akitain.fairlands.spawn;

import com.akitain.fairlands.rule.FairlandsGameRules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelData;

public final class SpawnSetupRules {
    private SpawnSetupRules() {
    }

    public static void trySetRespawnPosition(ServerPlayer player, ServerPlayer.RespawnConfig respawnConfig, boolean showMessage) {
        if (canSetRespawnPosition(player, respawnConfig)) {
            player.setRespawnPosition(respawnConfig, showMessage);
            return;
        }

        sendBlockedRespawnMessage(player);
    }

    private static boolean canSetRespawnPosition(ServerPlayer player, ServerPlayer.RespawnConfig respawnConfig) {
        if (respawnConfig == null) {
            return true;
        }

        ServerLevel level = player.level();
        if (!FairlandsGameRules.respawnSetupRestrictionEnabled(level)) {
            return true;
        }

        return isWithinAllowedRadius(level, respawnConfig.respawnData());
    }

    private static void sendBlockedRespawnMessage(ServerPlayer player) {
        int radius = FairlandsGameRules.respawnSetupRadius(player.level());
        player.sendSystemMessage(Component.literal("Respawn point unchanged: it must be within " + radius + " blocks of world spawn."));
    }

    private static boolean isWithinAllowedRadius(ServerLevel level, LevelData.RespawnData respawnData) {
        int radius = FairlandsGameRules.respawnSetupRadius(level);
        return SpawnAreaRules.isWithinRadius(level, respawnData.pos(), radius);
    }
}
