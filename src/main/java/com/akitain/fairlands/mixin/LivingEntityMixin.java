package com.akitain.fairlands.mixin;

import com.akitain.fairlands.combat.CombatRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
    private float fairlands$capEndCrystalPlayerDamage(float amount, ServerLevel level, DamageSource source) {
        if (!((Object) this instanceof ServerPlayer)) {
            return amount;
        }

        float cappedAmount = CombatRules.capEndCrystalPlayerDamage(level, source, amount);
        cappedAmount = CombatRules.capRespawnAnchorPlayerDamage(level, source, cappedAmount);
        cappedAmount = CombatRules.capTntMinecartPlayerDamage(level, source, cappedAmount);
        cappedAmount = CombatRules.capMaceSmashPlayerDamage(level, source, cappedAmount);
        return CombatRules.capSpearPlayerDamage(level, source, cappedAmount);
    }
}
