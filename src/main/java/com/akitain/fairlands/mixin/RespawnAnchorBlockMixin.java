package com.akitain.fairlands.mixin;

import com.akitain.fairlands.spawn.SpawnSetupRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RespawnAnchorBlock.class)
public abstract class RespawnAnchorBlockMixin {
    @Redirect(
            method = "useWithoutItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/server/level/ServerPlayer$RespawnConfig;Z)V"
            )
    )
    private void fairlands$restrictAnchorRespawnSetup(ServerPlayer player, ServerPlayer.RespawnConfig respawnConfig, boolean showMessage) {
        SpawnSetupRules.trySetRespawnPosition(player, respawnConfig, showMessage);
    }
}
