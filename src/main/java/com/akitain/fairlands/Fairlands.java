package com.akitain.fairlands;

import com.akitain.fairlands.config.FairlandsConfig;
import com.akitain.fairlands.death.DeathRules;
import com.akitain.fairlands.rule.FairlandsGameRules;
import com.akitain.fairlands.spawn.SpawnProtectionRules;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public final class Fairlands implements ModInitializer {
    public static final String MOD_ID = "fairlands";

    @Override
    public void onInitialize() {
        MidnightConfig.init(MOD_ID, FairlandsConfig.class);
        FairlandsGameRules.register();
        DeathRules.register();
        SpawnProtectionRules.register();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
