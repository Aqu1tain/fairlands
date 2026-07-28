package com.akitain.fairlands.alchemy;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.entries.LootItem;

// Hearts of the sea only came from buried treasure, which made the tidal draught a one-shot reward.
// Fishing them up keeps them an exploration prize while letting players recover lost bonus hearts.
public final class TidalLoot {
    private static final Identifier FISHING_TREASURE = Identifier.withDefaultNamespace("gameplay/fishing/treasure");
    private static final int HEART_WEIGHT = 1;

    private TidalLoot() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!key.identifier().equals(FISHING_TREASURE)) {
                return;
            }

            tableBuilder.withPool(net.minecraft.world.level.storage.loot.LootPool.lootPool()
                    .setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1))
                    .add(net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem().setWeight(24))
                    .add(LootItem.lootTableItem(Items.HEART_OF_THE_SEA).setWeight(HEART_WEIGHT)));
        });
    }
}
