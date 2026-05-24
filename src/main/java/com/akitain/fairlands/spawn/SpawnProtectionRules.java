package com.akitain.fairlands.spawn;

import com.akitain.fairlands.rule.FairlandsGameRules;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class SpawnProtectionRules {
    private static final Component PROTECTED_SPAWN_MESSAGE = Component.literal("Spawn is protected.");

    private SpawnProtectionRules() {
    }

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> canBreakBlock(level, player, pos));
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> canUseBlock(level, player, hand, hitResult.getBlockPos()) ? InteractionResult.PASS : InteractionResult.FAIL);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(SpawnProtectionRules::canDamageEntity);
    }

    public static boolean protectsExplosion(ServerLevel level, double x, double y, double z) {
        return griefProtectionEnabled(level) && isProtected(level, BlockPos.containing(x, y, z));
    }

    private static boolean canBreakBlock(Level level, Player player, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || !griefProtectionEnabled(serverLevel) || !isProtected(serverLevel, pos)) {
            return true;
        }

        sendProtectedSpawnMessage(player);
        return false;
    }

    private static boolean canUseBlock(Level level, Player player, net.minecraft.world.InteractionHand hand, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || !griefProtectionEnabled(serverLevel)) {
            return true;
        }

        Item item = player.getItemInHand(hand).getItem();
        if (!canGriefWithItem(item)) {
            return true;
        }

        if (!isProtected(serverLevel, pos)) {
            return true;
        }

        sendProtectedSpawnMessage(player);
        return false;
    }

    private static boolean canGriefWithItem(Item item) {
        return item instanceof BlockItem
                || item == Items.FLINT_AND_STEEL
                || item == Items.FIRE_CHARGE
                || item == Items.LAVA_BUCKET
                || item == Items.WATER_BUCKET
                || item == Items.POWDER_SNOW_BUCKET
                || item == Items.TNT_MINECART
                || item == Items.END_CRYSTAL;
    }

    private static boolean canDamageEntity(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayer victim)) {
            return true;
        }

        ServerLevel level = victim.level();
        if (!FairlandsGameRules.spawnProtectionPvpEnabled(level) || !isProtected(level, victim.blockPosition())) {
            return true;
        }

        Entity attacker = source.getEntity();
        if (!(attacker instanceof ServerPlayer player) || player == victim) {
            return true;
        }

        sendProtectedSpawnMessage(player);
        return false;
    }

    private static boolean griefProtectionEnabled(ServerLevel level) {
        return FairlandsGameRules.spawnProtectionEnabled(level) && FairlandsGameRules.spawnProtectionGriefEnabled(level);
    }

    private static boolean isProtected(ServerLevel level, BlockPos pos) {
        if (!FairlandsGameRules.spawnProtectionEnabled(level)) {
            return false;
        }

        int radius = FairlandsGameRules.spawnProtectionRadius(level);
        return SpawnAreaRules.isWithinRadius(level, pos, radius);
    }

    private static void sendProtectedSpawnMessage(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(PROTECTED_SPAWN_MESSAGE);
        }
    }
}
