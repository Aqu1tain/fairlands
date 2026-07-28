package com.akitain.fairlands.item;

import com.akitain.fairlands.Fairlands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class FairlandsItems {
    public static final Item VITALITY_SHARD = register("vitality_shard");

    private FairlandsItems() {
    }

    private static Item register(String name) {
        Identifier id = Fairlands.id(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(BuiltInRegistries.ITEM, key,
                new VitalityShardItem(new Item.Properties().setId(key).rarity(Rarity.UNCOMMON).stacksTo(16)));
    }

    public static void register() {
    }
}
