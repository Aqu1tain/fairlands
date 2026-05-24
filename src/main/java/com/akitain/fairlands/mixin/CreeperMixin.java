package com.akitain.fairlands.mixin;

import com.akitain.fairlands.rule.FairlandsGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Creeper.class)
public abstract class CreeperMixin {
    @ModifyArg(
            method = "explodeCreeper",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)V"
            ),
            index = 4
    )
    private float fairlands$increaseCreeperExplosionRadius(float radius) {
        ServerLevel level = fairlands$serverLevel();
        if (level == null) {
            return radius;
        }

        return radius + FairlandsGameRules.creeperExplosionRadiusBonus(level);
    }

    @ModifyArg(
            method = "explodeCreeper",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)V"
            ),
            index = 5
    )
    private Level.ExplosionInteraction fairlands$disableCreeperBlockDamage(Level.ExplosionInteraction interaction) {
        ServerLevel level = fairlands$serverLevel();
        if (level == null || FairlandsGameRules.creeperBlockDamageEnabled(level)) {
            return interaction;
        }

        return Level.ExplosionInteraction.NONE;
    }

    private ServerLevel fairlands$serverLevel() {
        Level level = ((Creeper) (Object) this).level();
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel;
        }

        return null;
    }
}
