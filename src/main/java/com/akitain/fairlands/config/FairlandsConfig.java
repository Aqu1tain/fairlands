package com.akitain.fairlands.config;

import eu.midnightdust.lib.config.MidnightConfig;

import java.util.ArrayList;
import java.util.List;

public final class FairlandsConfig extends MidnightConfig {
    private static final String DEATH = "death";

    @Entry(category = DEATH)
    public static boolean keepEquippedArmor = true;

    @Entry(category = DEATH)
    public static boolean keepToolsAndWeapons = true;

    @Entry(category = DEATH)
    public static List<String> keepOnDeathAllowlist = new ArrayList<>(List.of("exploration-reloaded:map_book"));

    @Entry(category = DEATH)
    public static boolean deathPenaltyEnabled = true;

    @Entry(category = DEATH)
    public static String deathPenaltyEffect = "minecraft:weakness";

    @Entry(category = DEATH, min = 1, max = 1800)
    public static int deathPenaltyDurationSeconds = 180;

    @Entry(category = DEATH, min = 0, max = 4)
    public static int deathPenaltyAmplifier = 0;
}
