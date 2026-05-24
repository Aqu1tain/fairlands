package com.akitain.fairlands.death;

import com.akitain.fairlands.Fairlands;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class FairlandsItemTags {
    public static final TagKey<Item> KEEP_ON_DEATH = TagKey.create(Registries.ITEM, Fairlands.id("keep_on_death"));

    private FairlandsItemTags() {
    }
}
