package com.akitain.fairlands.mixin;

import com.akitain.fairlands.alchemy.FairlandsAlchemy;
import com.akitain.fairlands.health.BonusHeartsRules;
import com.akitain.fairlands.health.BonusHeartsShake;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// At the bonus heart cap the draught simply cannot be drunk: the hearts jitter instead of a message.
@Mixin(Item.class)
public class TidalDraughtUseMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void fairlands$refuseTidalDraughtAtCap(Level level, Player player, InteractionHand hand,
                                                   CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isTidalDraught(stack) || !isAtCap(player)) {
            return;
        }

        if (level.isClientSide()) {
            BonusHeartsShake.trigger();
        }

        cir.setReturnValue(InteractionResult.FAIL);
    }

    @Unique
    private static boolean isTidalDraught(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.potion()
                .map(potion -> potion.is(FairlandsAlchemy.TIDAL_DRAUGHT)).orElse(false);
    }

    @Unique
    private static boolean isAtCap(Player player) {
        return player.getAttachedOrElse(BonusHeartsRules.BONUS_HEARTS, 0) >= BonusHeartsRules.maxHearts();
    }
}
