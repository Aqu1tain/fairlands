package com.akitain.fairlands.alchemy;

import com.akitain.fairlands.Fairlands;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

public final class FairlandsAlchemy {
    // Matches the blue used for the bonus hearts on the HUD.
    private static final int TIDAL_COLOR = 0x6EAFFF;

    public static final Holder<MobEffect> TIDAL_BLESSING = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Fairlands.id("tidal_blessing"),
            new TidalBlessingEffect(MobEffectCategory.BENEFICIAL, TIDAL_COLOR));

    public static final Holder<Potion> TIDAL_DRAUGHT = Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Fairlands.id("tidal_draught"),
            new Potion("tidal_draught", new MobEffectInstance(TIDAL_BLESSING, 1)));

    private FairlandsAlchemy() {
    }

    public static void register() {
        // Thick potions are a dead end in vanilla brewing, so they finally get a use here.
        FabricPotionBrewingBuilder.BUILD.register(builder ->
                builder.registerPotionRecipe(Potions.THICK, Ingredient.of(Items.HEART_OF_THE_SEA), TIDAL_DRAUGHT));
    }
}
