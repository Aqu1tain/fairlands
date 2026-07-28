package com.akitain.fairlands.mixin.client;

import com.akitain.fairlands.Fairlands;
import com.akitain.fairlands.health.BonusHeartsRules;
import com.akitain.fairlands.health.BonusHeartsShake;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Draws the bonus hearts in navy over the vanilla red ones. Runs after vanilla has laid the row out and
// reuses the very same placement maths, so it stays aligned without reading any local variable.
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

    @Inject(method = "extractHearts", at = @At("TAIL"))
    private void fairlands$paintBonusHearts(
            GuiGraphicsExtractor graphics,
            Player player,
            int xLeft,
            int yLineBase,
            int healthRowHeight,
            int heartOffsetIndex,
            float maxHealth,
            int currentHealth,
            int oldHealth,
            int absorption,
            boolean blink,
            CallbackInfo ci) {
        int bonusHearts = player.getAttachedOrElse(BonusHeartsRules.BONUS_HEARTS, 0);
        if (bonusHearts <= 0) {
            return;
        }

        int healthContainerCount = Mth.ceil(maxHealth / 2.0);
        int firstBonusContainer = Math.max(0, healthContainerCount - bonusHearts);

        for (int containerIndex = firstBonusContainer; containerIndex < healthContainerCount; containerIndex++) {
            int halves = containerIndex * 2;
            if (halves >= currentHealth) {
                continue;
            }

            int row = containerIndex / 10;
            int column = containerIndex % 10;
            int restingX = xLeft + column * 8;
            int yo = yLineBase - row * healthRowHeight;
            if (containerIndex == heartOffsetIndex) {
                yo -= 2;
            }

            // The vanilla red heart is still painted underneath, so while the heart is jittering we first
            // repaint the empty container over it, otherwise red bleeds out from behind the offset sprite.
            int shake = BonusHeartsShake.offset();
            if (shake != 0) {
                boolean hardcore = player.level().getLevelData().isHardcore();
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                        hardcore ? CONTAINER_HARDCORE : CONTAINER, restingX, yo, 9, 9);
            }

            boolean half = halves + 1 == currentHealth;
            Identifier sprite = blink
                    ? (half ? NAVY_HALF_BLINKING : NAVY_FULL_BLINKING)
                    : (half ? NAVY_HALF : NAVY_FULL);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, restingX + shake, yo, 9, 9);
        }
    }
}
