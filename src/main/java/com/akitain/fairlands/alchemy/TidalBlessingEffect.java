package com.akitain.fairlands.alchemy;

import com.akitain.fairlands.health.BonusHeartsRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

// The whole point of the potion: drinking it grants one permanent bonus heart. The effect is
// instantaneous and only exists so vanilla brewing and drinking handle everything else for us.
public class TidalBlessingEffect extends MobEffect {
    public TidalBlessingEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectStarted(LivingEntity mob, int amplifier) {
        if (mob instanceof ServerPlayer player) {
            BonusHeartsRules.grantHeart(player);
        }
    }
}
