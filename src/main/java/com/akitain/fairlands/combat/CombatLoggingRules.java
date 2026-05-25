package com.akitain.fairlands.combat;

import com.akitain.fairlands.Fairlands;
import com.akitain.fairlands.rule.FairlandsGameRules;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class CombatLoggingRules {
    private static final net.minecraft.resources.Identifier PENALTY_MODIFIER_ID = Fairlands.id("combat_log_health_penalty");
    private static final Map<UUID, CombatTag> COMBAT_TAGS = new HashMap<>();
    private static final Map<UUID, Long> ACTIVE_PENALTIES = new HashMap<>();
    private static final Map<UUID, Integer> PENDING_PENALTY_SECONDS = new HashMap<>();

    private CombatLoggingRules() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(CombatLoggingRules::tagPvPCombat);
        ServerLivingEntityEvents.AFTER_DEATH.register(CombatLoggingRules::clearCombatTagOnDeath);
        ServerTickEvents.END_SERVER_TICK.register(CombatLoggingRules::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> handleDisconnect(handler.getPlayer(), server));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> applyPendingPenalty(handler.getPlayer(), server));
    }

    private static void tagPvPCombat(LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked) {
        if (!(entity instanceof ServerPlayer victim) || damageTaken <= 0.0F) {
            return;
        }

        Entity attacker = source.getEntity();
        if (!(attacker instanceof ServerPlayer player) || player == victim) {
            return;
        }

        if (!FairlandsGameRules.pvpCombatLoggingEnabled(victim.level())) {
            return;
        }

        long tagTicks = FairlandsGameRules.pvpCombatTagSeconds(victim.level()) * 20L;
        long expiresAtTick = victim.level().getServer().getTickCount() + tagTicks;
        tagPlayer(victim, expiresAtTick, tagTicks);
        tagPlayer(player, expiresAtTick, tagTicks);
    }

    private static void tagPlayer(ServerPlayer player, long expiresAtTick, long tagTicks) {
        CombatTag tag = COMBAT_TAGS.computeIfAbsent(player.getUUID(), uuid -> new CombatTag(createCombatBossBar(player)));
        tag.expiresAtTick = expiresAtTick;
        tag.tagTicks = tagTicks;
        tag.bossBar.addPlayer(player);
        updateCombatBossBar(player.level().getServer(), tag);
    }

    private static ServerBossEvent createCombatBossBar(ServerPlayer player) {
        ServerBossEvent bossBar = new ServerBossEvent(
                UUID.randomUUID(),
                Component.literal("In combat"),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );
        bossBar.addPlayer(player);
        return bossBar;
    }

    private static void clearCombatTagOnDeath(LivingEntity entity, DamageSource source) {
        if (entity instanceof ServerPlayer player) {
            clearCombatTag(player.getUUID());
        }
    }

    private static void handleDisconnect(ServerPlayer player, MinecraftServer server) {
        pauseActivePenalty(player, server);
        punishCombatLogout(player, server);
    }

    private static void punishCombatLogout(ServerPlayer player, MinecraftServer server) {
        CombatTag tag = COMBAT_TAGS.remove(player.getUUID());
        if (tag == null) {
            return;
        }

        tag.bossBar.removeAllPlayers();
        if (tag.expiresAtTick <= server.getTickCount()) {
            return;
        }

        int penaltySeconds = FairlandsGameRules.pvpCombatLogPenaltySeconds(player.level());
        PENDING_PENALTY_SECONDS.put(player.getUUID(), penaltySeconds);
    }

    private static void pauseActivePenalty(ServerPlayer player, MinecraftServer server) {
        Long penaltyUntilTick = ACTIVE_PENALTIES.remove(player.getUUID());
        if (penaltyUntilTick == null) {
            return;
        }

        long remainingTicks = penaltyUntilTick - server.getTickCount();
        if (remainingTicks <= 0) {
            removeHealthPenalty(player);
            return;
        }

        int remainingSeconds = (int) Math.max(1, (remainingTicks + 19) / 20);
        PENDING_PENALTY_SECONDS.merge(player.getUUID(), remainingSeconds, Math::max);
        removeHealthPenalty(player);
    }

    private static void applyPendingPenalty(ServerPlayer player, MinecraftServer server) {
        Integer pendingSeconds = PENDING_PENALTY_SECONDS.remove(player.getUUID());
        if (pendingSeconds == null) {
            Long penaltyUntilTick = ACTIVE_PENALTIES.get(player.getUUID());
            if (penaltyUntilTick != null && penaltyUntilTick > server.getTickCount()) {
                applyHealthPenalty(player);
                return;
            }

            ACTIVE_PENALTIES.remove(player.getUUID());
            removeHealthPenalty(player);
            return;
        }

        long penaltyUntilTick = server.getTickCount() + pendingSeconds * 20L;
        ACTIVE_PENALTIES.put(player.getUUID(), penaltyUntilTick);
        applyHealthPenalty(player);
        int healthPercent = FairlandsGameRules.pvpCombatLogHealthPercent(player.level());
        player.sendSystemMessage(Component.literal(
                "You disconnected during PVP. Your max health is reduced to " + healthPercent + "% for " + formatDuration(pendingSeconds) + "."
        ));
    }

    private static void tick(MinecraftServer server) {
        tickCombatTags(server);
        tickPenalties(server);
    }

    private static void tickCombatTags(MinecraftServer server) {
        Iterator<Map.Entry<UUID, CombatTag>> iterator = COMBAT_TAGS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, CombatTag> entry = iterator.next();
            CombatTag tag = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null || tag.expiresAtTick <= server.getTickCount()) {
                tag.bossBar.removeAllPlayers();
                iterator.remove();
                continue;
            }

            updateCombatBossBar(server, tag);
        }
    }

    private static void tickPenalties(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Long>> iterator = ACTIVE_PENALTIES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (entry.getValue() > server.getTickCount()) {
                if (player != null) {
                    applyHealthPenalty(player);
                }
                continue;
            }

            iterator.remove();
            if (player != null) {
                removeHealthPenalty(player);
                player.sendSystemMessage(Component.literal("Combat log penalty expired."));
            }
        }
    }

    private static void updateCombatBossBar(MinecraftServer server, CombatTag tag) {
        long remainingTicks = Math.max(0, tag.expiresAtTick - server.getTickCount());
        long totalTicks = Math.max(1, tag.tagTicks);
        long remainingSeconds = (remainingTicks + 19) / 20;
        tag.bossBar.setName(Component.literal("In combat: " + remainingSeconds + "s"));
        tag.bossBar.setProgress(Math.clamp((float) remainingTicks / totalTicks, 0.0F, 1.0F));
    }

    private static String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        }

        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        if (remainingSeconds == 0) {
            return minutes + " minutes";
        }

        return minutes + " minutes " + remainingSeconds + " seconds";
    }

    private static void clearCombatTag(UUID playerId) {
        CombatTag tag = COMBAT_TAGS.remove(playerId);
        if (tag != null) {
            tag.bossBar.removeAllPlayers();
        }
    }

    private static void applyHealthPenalty(ServerPlayer player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        int healthPercent = FairlandsGameRules.pvpCombatLogHealthPercent(player.level());
        double amount = healthPercent / 100.0 - 1.0;
        maxHealth.removeModifier(PENALTY_MODIFIER_ID);
        maxHealth.addOrUpdateTransientModifier(new AttributeModifier(PENALTY_MODIFIER_ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void removeHealthPenalty(ServerPlayer player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(PENALTY_MODIFIER_ID);
        }
    }

    private static final class CombatTag {
        private final ServerBossEvent bossBar;
        private long expiresAtTick;
        private long tagTicks;

        private CombatTag(ServerBossEvent bossBar) {
            this.bossBar = bossBar;
        }
    }
}
