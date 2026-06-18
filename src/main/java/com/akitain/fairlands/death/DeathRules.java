package com.akitain.fairlands.death;

import com.akitain.fairlands.config.FairlandsConfig;
import com.akitain.fairlands.rule.FairlandsGameRules;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class DeathRules {
    private DeathRules() {
    }

    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (alive) {
                return;
            }

            if (FairlandsGameRules.partialKeepInventoryEnabled(newPlayer.level())) {
                transferKeptItems(oldPlayer, newPlayer);
                restoreKeptExperience(newPlayer, calculateKeptExperience(oldPlayer));
            }

            applyDeathPenalty(newPlayer);
        });
    }

    // Replaces the vanilla on-death inventory drop: kept items stay in the inventory so they
    // survive a crash or disconnect on the death screen, then move to the respawn player intact.
    public static void dropNonKeptItems(Player player, Inventory inventory) {
        if (!(player.level() instanceof ServerLevel level) || !FairlandsGameRules.partialKeepInventoryEnabled(level)) {
            inventory.dropAll();
            return;
        }

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || shouldKeepItem(slot, stack)) {
                continue;
            }

            player.drop(stack, true, false);
            inventory.setItem(slot, ItemStack.EMPTY);
        }

        inventory.setChanged();
    }

    private static void transferKeptItems(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        Inventory from = oldPlayer.getInventory();
        Inventory to = newPlayer.getInventory();

        for (int slot = 0; slot < from.getContainerSize(); slot++) {
            ItemStack stack = from.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            restoreToInventory(newPlayer, to, slot, stack.copy());
            from.setItem(slot, ItemStack.EMPTY);
        }

        to.setChanged();
    }

    private static void restoreToInventory(ServerPlayer player, Inventory inventory, int slot, ItemStack stack) {
        if (slot >= 0 && slot < inventory.getContainerSize() && inventory.getItem(slot).isEmpty()) {
            inventory.setItem(slot, stack);
            return;
        }

        if (!inventory.add(stack) && !stack.isEmpty()) {
            player.drop(stack, false, true);
        }
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
                || stack.is(ConventionalItemTags.BUCKETS)
                || stack.has(DataComponents.GLIDER)
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
}
