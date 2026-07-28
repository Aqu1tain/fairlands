package com.akitain.fairlands.alchemy;

import com.akitain.fairlands.health.BonusHeartsRules;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

// The whole point of the potion: drinking it grants one permanent bonus heart. The effect itself is
// instantaneous, it only exists so vanilla brewing and drinking handle everything else for us.
public class TidalBlessingEffect extends MobEffect {
    public TidalBlessingEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectStarted(LivingEntity mob, int amplifier) {
        if (!(mob instanceof ServerPlayer player)) {
            return;
        }

        if (BonusHeartsRules.grantHeart(player)) {
            actionBar(player, Component.translatable("effect.fairlands.tidal_blessing.gained",
                    BonusHeartsRules.getHearts(player)));
        } else {
            actionBar(player, Component.translatable("effect.fairlands.tidal_blessing.full",
                    BonusHeartsRules.maxHearts()));
        }
    }

    private static void actionBar(ServerPlayer player, Component message) {
        player.connection.send(new ClientboundSetActionBarTextPacket(message));
    }
}
