package com.akitain.fairlands.mixin;

import com.akitain.fairlands.spawn.SpawnSetupRules;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "setRespawnPosition", at = @At("HEAD"), cancellable = true)
    private void fairlands$restrictRespawnSetup(ServerPlayer.RespawnConfig respawnConfig, boolean sendMessage, CallbackInfo callbackInfo) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (SpawnSetupRules.canSetRespawnPosition(player, respawnConfig)) {
            return;
        }

        if (sendMessage) {
            SpawnSetupRules.sendBlockedRespawnMessage(player);
        }

        callbackInfo.cancel();
    }
}
