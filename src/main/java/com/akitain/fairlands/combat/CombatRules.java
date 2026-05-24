package com.akitain.fairlands.combat;

import com.akitain.fairlands.rule.FairlandsGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

public final class CombatRules {
    private CombatRules() {
    }

    public static float capEndCrystalPlayerDamage(ServerLevel level, DamageSource source, float amount) {
        if (!isEndCrystalDamage(source)) {
            return amount;
        }

        return Math.min(amount, FairlandsGameRules.endCrystalPlayerDamageCap(level));
    }

    public static float capRespawnAnchorPlayerDamage(ServerLevel level, DamageSource source, float amount) {
        if (!source.is(DamageTypes.BAD_RESPAWN_POINT)) {
            return amount;
        }

        return Math.min(amount, FairlandsGameRules.respawnAnchorPlayerDamageCap(level));
    }

    private static boolean isEndCrystalDamage(DamageSource source) {
        return source.getDirectEntity() instanceof EndCrystal || source.getEntity() instanceof EndCrystal;
    }
}
