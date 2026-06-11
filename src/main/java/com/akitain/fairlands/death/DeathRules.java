package com.akitain.fairlands.death;

import com.akitain.fairlands.config.FairlandsConfig;
import com.akitain.fairlands.rule.FairlandsGameRules;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DeathRules {
    private static final Map<UUID, PendingDeathRules> PENDING_DEATH_RULES = new HashMap<>();

    private DeathRules() {
    }

    public static void register() {
        ServerPlayerEvents.ALLOW_DEATH.register((player, damageSource, damageAmount) -> {
            if (!willDeathProtectionTrigger(player, damageSource)) {
                captureKeptItems(player);
            }
            return true;
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            restoreKeptItems(newPlayer);
            applyDeathPenalty(newPlayer);
        });

        ServerPlayerEvents.JOIN.register(DeathRules::restoreKeptItems);
        ServerPlayerEvents.LEAVE.register(DeathRules::restoreKeptItems);
    }

    private static boolean willDeathProtectionTrigger(ServerPlayer player, DamageSource damageSource) {
        if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }

        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand).has(DataComponents.DEATH_PROTECTION)) {
                return true;
            }
        }

        return false;
    }

    private static void captureKeptItems(ServerPlayer player) {
        restorePendingItems(player);

        if (!FairlandsGameRules.partialKeepInventoryEnabled(player.level())) {
            return;
        }

        Inventory inventory = player.getInventory();
        List<KeptItem> keptItems = new ArrayList<>();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !shouldKeepItem(slot, stack)) {
                continue;
            }

            keptItems.add(new KeptItem(slot, stack.copy()));
            inventory.setItem(slot, ItemStack.EMPTY);
        }

        int keptExperience = calculateKeptExperience(player);
        if (keptItems.isEmpty() && keptExperience <= 0) {
            return;
        }

        inventory.setChanged();
        PENDING_DEATH_RULES.put(player.getUUID(), new PendingDeathRules(keptItems, keptExperience));
    }

    private static boolean shouldKeepItem(int slot, ItemStack stack) {
        if (isAllowlisted(stack) || stack.is(FairlandsItemTags.KEEP_ON_DEATH)) {
            return true;
        }

        if (FairlandsConfig.keepEquippedArmor && isEquippedArmor(slot)) {
            return true;
        }

        return FairlandsConfig.keepToolsAndWeapons && isToolOrWeapon(stack);
    }

    private static boolean isEquippedArmor(int slot) {
        EquipmentSlot equipmentSlot = Inventory.EQUIPMENT_SLOT_MAPPING.get(slot);
        return equipmentSlot != null && equipmentSlot.isArmor();
    }

    private static boolean isToolOrWeapon(ItemStack stack) {
        return stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES)
                || stack.is(ItemTags.SPEARS)
                || stack.is(ItemTags.BOW_ENCHANTABLE)
                || stack.is(ItemTags.CROSSBOW_ENCHANTABLE)
                || stack.is(ItemTags.TRIDENT_ENCHANTABLE)
                || stack.is(ItemTags.MACE_ENCHANTABLE)
                || stack.getItem() == Items.SHIELD
                || stack.getItem() == Items.SHEARS
                || stack.getItem() == Items.FLINT_AND_STEEL
                || stack.getItem() == Items.FISHING_ROD
                || stack.getItem() == Items.BRUSH
                || stack.getItem() == Items.WATER_BUCKET
                || stack.getItem() == Items.CARROT_ON_A_STICK
                || stack.getItem() == Items.WARPED_FUNGUS_ON_A_STICK;
    }

    private static boolean isAllowlisted(ItemStack stack) {
        for (String configuredItem : FairlandsConfig.keepOnDeathAllowlist) {
            Identifier itemId = Identifier.tryParse(configuredItem);
            if (itemId == null) {
                continue;
            }

            if (BuiltInRegistries.ITEM.getOptional(itemId).filter(item -> stack.getItem() == item).isPresent()) {
                return true;
            }
        }

        return false;
    }

    private static void restoreKeptItems(ServerPlayer player) {
        PendingDeathRules pendingRules = PENDING_DEATH_RULES.remove(player.getUUID());
        if (pendingRules == null) {
            return;
        }

        restoreKeptItems(player, pendingRules.keptItems());
        restoreKeptExperience(player, pendingRules.keptExperience());
    }

    private static void restorePendingItems(ServerPlayer player) {
        PendingDeathRules pendingRules = PENDING_DEATH_RULES.remove(player.getUUID());
        if (pendingRules == null) {
            return;
        }

        restoreKeptItems(player, pendingRules.keptItems());
    }

    private static void restoreKeptItems(ServerPlayer player, List<KeptItem> keptItems) {
        Inventory inventory = player.getInventory();
        for (KeptItem keptItem : keptItems) {
            restoreKeptItem(player, inventory, keptItem);
        }

        inventory.setChanged();
    }

    private static int calculateKeptExperience(ServerPlayer player) {
        int keptPercent = FairlandsGameRules.deathXpKeepPercent(player.level());
        int percentageKeptExperience = Math.floorDiv(player.totalExperience * keptPercent, 100);
        int maximumKeptExperience = player.totalExperience - expectedDeathExperienceDrop(player);
        return Math.clamp(percentageKeptExperience, 0, Math.max(0, maximumKeptExperience));
    }

    private static int expectedDeathExperienceDrop(ServerPlayer player) {
        int uncappedReward = player.experienceLevel * 7;
        int cappedReward = Math.min(uncappedReward, FairlandsGameRules.deathXpDropCap(player.level()));
        return Math.min(cappedReward, player.totalExperience);
    }

    private static void restoreKeptExperience(ServerPlayer player, int keptExperience) {
        if (keptExperience <= 0) {
            return;
        }

        player.giveExperiencePoints(keptExperience);
    }

    private static void restoreKeptItem(ServerPlayer player, Inventory inventory, KeptItem keptItem) {
        ItemStack stack = keptItem.stack().copy();
        if (canRestoreToOriginalSlot(inventory, keptItem.slot())) {
            inventory.setItem(keptItem.slot(), stack);
            return;
        }

        if (!inventory.add(stack) && !stack.isEmpty()) {
            player.drop(stack, false, true);
        }
    }

    private static boolean canRestoreToOriginalSlot(Inventory inventory, int slot) {
        return slot >= 0 && slot < inventory.getContainerSize() && inventory.getItem(slot).isEmpty();
    }

    private static void applyDeathPenalty(ServerPlayer player) {
        if (!FairlandsConfig.deathPenaltyEnabled || player.isCreative() || player.isSpectator()) {
            return;
        }

        int durationTicks = Math.max(1, FairlandsConfig.deathPenaltyDurationSeconds) * 20;
        int amplifier = Math.max(0, FairlandsConfig.deathPenaltyAmplifier);
        player.addEffect(new MobEffectInstance(resolveDeathPenaltyEffect(), durationTicks, amplifier));
    }

    private static Holder<MobEffect> resolveDeathPenaltyEffect() {
        Identifier effectId = Identifier.tryParse(FairlandsConfig.deathPenaltyEffect);
        if (effectId == null) {
            return MobEffects.WEAKNESS;
        }

        return BuiltInRegistries.MOB_EFFECT.get(effectId)
                .map(effect -> (Holder<MobEffect>) effect)
                .orElse(MobEffects.WEAKNESS);
    }

    private record KeptItem(int slot, ItemStack stack) {
    }

    private record PendingDeathRules(List<KeptItem> keptItems, int keptExperience) {
    }
}
