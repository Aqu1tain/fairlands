package com.akitain.fairlands.item;

import com.akitain.fairlands.health.BonusHeartsRules;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VitalityShardItem extends Item {
    public VitalityShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (!BonusHeartsRules.grantHeart(serverPlayer)) {
            actionBar(serverPlayer, Component.translatable("item.fairlands.vitality_shard.full", BonusHeartsRules.maxHearts()));
            return InteractionResult.FAIL;
        }

        stack.consume(1, player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.4F);
        actionBar(serverPlayer, Component.translatable("item.fairlands.vitality_shard.gained", BonusHeartsRules.getHearts(serverPlayer)));
        return InteractionResult.SUCCESS;
    }

    private static void actionBar(ServerPlayer player, Component message) {
        player.connection.send(new ClientboundSetActionBarTextPacket(message));
    }
}
