package com.akitain.fairlands.mixin;

import com.akitain.fairlands.rule.FairlandsGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "getBaseExperienceReward", at = @At("RETURN"), cancellable = true)
    private void fairlands$capDeathExperienceDrop(ServerLevel level, CallbackInfoReturnable<Integer> callbackInfo) {
        if (!FairlandsGameRules.partialKeepInventoryEnabled(level)) {
            return;
        }

        Player player = (Player) (Object) this;
        int uncappedReward = player.experienceLevel * 7;
        callbackInfo.setReturnValue(Math.min(uncappedReward, FairlandsGameRules.deathXpDropCap(level)));
    }
}
