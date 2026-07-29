package com.akitain.fairlands.mixin.client;

import com.akitain.fairlands.Fairlands;
import com.akitain.fairlands.health.BonusHeartsRules;
import com.akitain.fairlands.health.BonusHeartsShake;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Bonus hearts are drawn entirely by this mixin: vanilla is stopped from painting them so the whole
// heart, dark outline included, can drift with the wave instead of the fill sliding inside a fixed frame.
@Mixin(Gui.class)
public abstract class BonusHeartsHudMixin {
    @Unique
    private static final Identifier NAVY_FULL = Fairlands.id("hud/heart/navy_full");
    @Unique
    private static final Identifier NAVY_HALF = Fairlands.id("hud/heart/navy_half");
    @Unique
    private static final Identifier NAVY_FULL_BLINKING = Fairlands.id("hud/heart/navy_full_blinking");
    @Unique
    private static final Identifier NAVY_HALF_BLINKING = Fairlands.id("hud/heart/navy_half_blinking");
    @Unique
    private static final Identifier CONTAINER = Identifier.withDefaultNamespace("hud/heart/container");
    @Unique
    private static final Identifier CONTAINER_HARDCORE = Identifier.withDefaultNamespace("hud/heart/container_hardcore");

    // A slow swell that travels along the row, lifting each heart only as its crest passes.
    @Unique
    private static final double WAVE_PERIOD_MS = 1100.0;
    @Unique
    private static final double WAVE_PHASE_PER_HEART = 0.8;
    @Unique
    private static final double WAVE_CREST = 0.65;

    @Unique
    private final LongSet fairlands$bonusSlots = new LongOpenHashSet();

    @Inject(method = "extractHearts", at = @At("HEAD"))
    private void fairlands$claimBonusSlots(
            GuiGraphicsExtractor graphics, Player player, int xLeft, int yLineBase, int healthRowHeight,
            int heartOffsetIndex, float maxHealth, int currentHealth, int oldHealth, int absorption,
            boolean blink, CallbackInfo ci) {
        fairlands$bonusSlots.clear();

        int bonusHearts = player.getAttachedOrElse(BonusHeartsRules.BONUS_HEARTS, 0);
        if (bonusHearts <= 0) {
            return;
        }

        int healthContainers = Mth.ceil(maxHealth / 2.0);
        int firstBonus = Math.max(0, healthContainers - bonusHearts);
        boolean hardcore = player.level().getLevelData().isHardcore();

        for (int index = firstBonus; index < healthContainers; index++) {
            int halves = index * 2;
            if (halves >= currentHealth) {
                continue; // empty slots keep vanilla's static container
            }

            int x = xLeft + (index % 10) * 8;
            int y = yLineBase - (index / 10) * healthRowHeight - (index == heartOffsetIndex ? 2 : 0);
            fairlands$bonusSlots.add(fairlands$key(x, y));

            int lift = fairlands$waveOffset(index);
            int drawX = x + BonusHeartsShake.offset();
            int drawY = y + lift;

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    hardcore ? CONTAINER_HARDCORE : CONTAINER, drawX, drawY, 9, 9);

            boolean half = halves + 1 == currentHealth;
            Identifier sprite = blink
                    ? (half ? NAVY_HALF_BLINKING : NAVY_FULL_BLINKING)
                    : (half ? NAVY_HALF : NAVY_FULL);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, drawX, drawY, 9, 9);
        }
    }

    // Vanilla must not paint anything in a slot we have taken over, otherwise its red heart and static
    // outline stay behind the drifting one.
    @Inject(method = "extractHeart", at = @At("HEAD"), cancellable = true)
    private void fairlands$skipClaimedSlots(
            GuiGraphicsExtractor graphics, @Coerce Object type, int xo, int yo,
            boolean isHardcore, boolean blinks, boolean half, CallbackInfo ci) {
        if (!fairlands$bonusSlots.isEmpty() && fairlands$bonusSlots.contains(fairlands$key(xo, yo))) {
            ci.cancel();
        }
    }

    @Unique
    private static long fairlands$key(int x, int y) {
        return ((long) x << 32) ^ (y & 0xFFFFFFFFL);
    }

    @Unique
    private static int fairlands$waveOffset(int containerIndex) {
        double phase = System.currentTimeMillis() / WAVE_PERIOD_MS * (Math.PI * 2.0)
                - containerIndex * WAVE_PHASE_PER_HEART;
        return Math.sin(phase) > WAVE_CREST ? -1 : 0;
    }
}
