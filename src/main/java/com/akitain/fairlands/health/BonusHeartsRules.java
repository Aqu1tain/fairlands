package com.akitain.fairlands.health;

import com.akitain.fairlands.Fairlands;
import com.akitain.fairlands.config.FairlandsConfig;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// Bonus hearts are earned by consuming vitality shards and are wiped by death: the attachment is not
// copied to the respawned player, which is exactly the intended penalty.
public final class BonusHeartsRules {
    private static final Identifier MODIFIER_ID = Fairlands.id("bonus_hearts");
    private static final double HEALTH_PER_HEART = 2.0;

    public static final AttachmentType<Integer> BONUS_HEARTS = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT)
            .initializer(() -> 0)
            .buildAndRegister(Fairlands.id("bonus_hearts"));

    private BonusHeartsRules() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> applyModifier(handler.getPlayer()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> applyModifier(newPlayer));
    }

    public static int getHearts(ServerPlayer player) {
        return player.getAttachedOrElse(BONUS_HEARTS, 0);
    }

    public static int maxHearts() {
        return Math.max(0, FairlandsConfig.bonusHeartsMax);
    }

    public static boolean isFull(ServerPlayer player) {
        return getHearts(player) >= maxHearts();
    }

    /** Grants one bonus heart. Returns false when the player is already at the cap. */
    public static boolean grantHeart(ServerPlayer player) {
        int current = getHearts(player);
        if (current >= maxHearts()) {
            return false;
        }

        player.setAttached(BONUS_HEARTS, current + 1);
        applyModifier(player);
        player.setHealth(player.getMaxHealth());
        return true;
    }

    private static void applyModifier(ServerPlayer player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        maxHealth.removeModifier(MODIFIER_ID);
        int hearts = Math.min(getHearts(player), maxHearts());
        if (hearts > 0) {
            maxHealth.addOrUpdateTransientModifier(new AttributeModifier(
                    MODIFIER_ID, hearts * HEALTH_PER_HEART, AttributeModifier.Operation.ADD_VALUE));
        }

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }
}
