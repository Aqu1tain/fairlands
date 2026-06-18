package com.akitain.fairlands.spawn;

import com.akitain.fairlands.rule.FairlandsGameRules;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class SpawnProtectionRules {
    private static final Component PROTECTED_SPAWN_MESSAGE = Component.literal("Spawn is protected.");

    private SpawnProtectionRules() {
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(SpawnProtectionRules::canDamageEntity);
    }

    public static boolean protectsExplosion(ServerLevel level, double x, double y, double z) {
        return explosionProtectionEnabled(level) && isProtected(level, BlockPos.containing(x, y, z));
    }

    public static boolean blocksWitherSpawn(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        return isProtected(serverLevel, pos);
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

    private static boolean explosionProtectionEnabled(ServerLevel level) {
        return FairlandsGameRules.spawnProtectionEnabled(level) && FairlandsGameRules.spawnProtectionExplosionsEnabled(level);
    }

    private static boolean isProtected(ServerLevel level, BlockPos pos) {
        if (!SpawnAreaRules.isOverworld(level) || !FairlandsGameRules.spawnProtectionEnabled(level)) {
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
