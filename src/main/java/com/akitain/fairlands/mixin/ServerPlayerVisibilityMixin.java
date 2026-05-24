package com.akitain.fairlands.mixin;

import com.akitain.fairlands.visibility.TabVisibilityRules;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerVisibilityMixin {
    @Inject(method = "updateInvisibilityStatus", at = @At("TAIL"))
    private void fairlands$syncTabVisibility(CallbackInfo callbackInfo) {
        TabVisibilityRules.syncPlayer((ServerPlayer) (Object) this);
    }
}
