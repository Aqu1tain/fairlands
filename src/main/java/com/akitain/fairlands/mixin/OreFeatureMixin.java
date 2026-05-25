package com.akitain.fairlands.mixin;

import com.akitain.fairlands.world.WorldProgressionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreFeature.class)
public abstract class OreFeatureMixin {
    @Unique
    private static final ThreadLocal<Boolean> FAIRLANDS_PLACING_BONUS_ORE = ThreadLocal.withInitial(() -> false);

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void fairlands$scaleOreVeinAttempts(FeaturePlaceContext<OreConfiguration> context, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (FAIRLANDS_PLACING_BONUS_ORE.get()) {
            return;
        }

        if (WorldProgressionRules.shouldSkipOreVein(context.level(), context.origin(), context.config(), context.random())) {
            callbackInfo.setReturnValue(false);
        }
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void fairlands$placeFarOreBonus(FeaturePlaceContext<OreConfiguration> context, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (FAIRLANDS_PLACING_BONUS_ORE.get() || !callbackInfo.getReturnValue()) {
            return;
        }

        if (!WorldProgressionRules.shouldPlaceBonusOreVein(context.level(), context.origin(), context.config(), context.random())) {
            return;
        }

        FAIRLANDS_PLACING_BONUS_ORE.set(true);
        try {
            ((OreFeature) (Object) this).place(offsetContext(context));
        } finally {
            FAIRLANDS_PLACING_BONUS_ORE.set(false);
        }
    }

    @Unique
    private FeaturePlaceContext<OreConfiguration> offsetContext(FeaturePlaceContext<OreConfiguration> context) {
        BlockPos offsetOrigin = context.origin().offset(
                context.random().nextInt(17) - 8,
                context.random().nextInt(5) - 2,
                context.random().nextInt(17) - 8
        );
        return new FeaturePlaceContext<>(
                context.topFeature(),
                context.level(),
                context.chunkGenerator(),
                context.random(),
                offsetOrigin,
                context.config()
        );
    }
}
