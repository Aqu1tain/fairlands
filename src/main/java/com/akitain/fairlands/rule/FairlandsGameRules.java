package com.akitain.fairlands.rule;

import com.akitain.fairlands.Fairlands;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;

public final class FairlandsGameRules {
    public static final GameRule<Boolean> PARTIAL_KEEP_INVENTORY = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("partial_keep_inventory"));

    public static final GameRule<Integer> DEATH_XP_DROP_CAP = GameRuleBuilder
            .forInteger(1000)
            .range(0, 100000)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("death_xp_drop_cap"));

    public static final GameRule<Integer> DEATH_XP_KEEP_PERCENT = GameRuleBuilder
            .forInteger(50)
            .range(0, 100)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("death_xp_keep_percent"));

    public static final GameRule<Boolean> RESTRICT_RESPAWN_SETUP = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("restrict_respawn_setup"));

    public static final GameRule<Integer> RESPAWN_SETUP_RADIUS = GameRuleBuilder
            .forInteger(2000)
            .range(0, 100000)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("respawn_setup_radius"));

    public static final GameRule<Boolean> CREEPER_BLOCK_DAMAGE = GameRuleBuilder
            .forBoolean(false)
            .category(GameRuleCategory.MOBS)
            .buildAndRegister(Fairlands.id("creeper_block_damage"));

    public static final GameRule<Integer> CREEPER_EXPLOSION_RADIUS_BONUS = GameRuleBuilder
            .forInteger(1)
            .range(0, 10)
            .category(GameRuleCategory.MOBS)
            .buildAndRegister(Fairlands.id("creeper_explosion_radius_bonus"));

    public static final GameRule<Boolean> SPAWN_PROTECTION = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("spawn_protection"));

    public static final GameRule<Integer> SPAWN_PROTECTION_RADIUS = GameRuleBuilder
            .forInteger(2000)
            .range(0, 100000)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("spawn_protection_radius"));

    public static final GameRule<Boolean> SPAWN_PROTECTION_PVP = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("spawn_protection_pvp"));

    public static final GameRule<Boolean> SPAWN_PROTECTION_EXPLOSIONS = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("spawn_protection_grief"));

    public static final GameRule<Boolean> SPAWN_PROTECTION_EXPLOSIONS_ALIAS = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("spawn_protection_explosions"));

    public static final GameRule<Integer> END_CRYSTAL_PLAYER_DAMAGE_CAP = GameRuleBuilder
            .forInteger(8)
            .range(0, 100)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("end_crystal_player_damage_cap"));

    public static final GameRule<Integer> RESPAWN_ANCHOR_PLAYER_DAMAGE_CAP = GameRuleBuilder
            .forInteger(8)
            .range(0, 100)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("respawn_anchor_player_damage_cap"));

    public static final GameRule<Integer> TNT_MINECART_PLAYER_DAMAGE_CAP = GameRuleBuilder
            .forInteger(8)
            .range(0, 100)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("tnt_minecart_player_damage_cap"));

    public static final GameRule<Integer> MACE_SMASH_PLAYER_DAMAGE_CAP = GameRuleBuilder
            .forInteger(16)
            .range(0, 100)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("mace_smash_player_damage_cap"));

    public static final GameRule<Integer> SPEAR_PLAYER_DAMAGE_CAP = GameRuleBuilder
            .forInteger(14)
            .range(0, 100)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("spear_player_damage_cap"));

    public static final GameRule<Boolean> HIDE_INVISIBLE_PLAYERS_FROM_TAB = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Fairlands.id("hide_invisible_players_from_tab"));

    public static final GameRule<Boolean> WORLD_PROGRESSION = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.UPDATES)
            .buildAndRegister(Fairlands.id("world_progression"));

    public static final GameRule<Integer> ORE_PROGRESSION_INNER_RADIUS = GameRuleBuilder
            .forInteger(2000)
            .range(0, 100000)
            .category(GameRuleCategory.UPDATES)
            .buildAndRegister(Fairlands.id("ore_progression_inner_radius"));

    public static final GameRule<Integer> ORE_PROGRESSION_NORMAL_RADIUS = GameRuleBuilder
            .forInteger(6000)
            .range(0, 100000)
            .category(GameRuleCategory.UPDATES)
            .buildAndRegister(Fairlands.id("ore_progression_normal_radius"));

    public static final GameRule<Integer> ORE_PROGRESSION_INNER_VEIN_PERCENT = GameRuleBuilder
            .forInteger(60)
            .range(0, 100)
            .category(GameRuleCategory.UPDATES)
            .buildAndRegister(Fairlands.id("ore_progression_inner_vein_percent"));

    public static final GameRule<Integer> ORE_PROGRESSION_FAR_BONUS_PERCENT = GameRuleBuilder
            .forInteger(10)
            .range(0, 100)
            .category(GameRuleCategory.UPDATES)
            .buildAndRegister(Fairlands.id("ore_progression_far_bonus_percent"));

    private FairlandsGameRules() {
    }

    public static void register() {
    }

    public static boolean partialKeepInventoryEnabled(ServerLevel level) {
        GameRules gameRules = level.getGameRules();
        return !gameRules.get(GameRules.KEEP_INVENTORY) && gameRules.get(PARTIAL_KEEP_INVENTORY);
    }

    public static int deathXpDropCap(ServerLevel level) {
        return Math.max(0, level.getGameRules().get(DEATH_XP_DROP_CAP));
    }

    public static int deathXpKeepPercent(ServerLevel level) {
        return Math.clamp(level.getGameRules().get(DEATH_XP_KEEP_PERCENT), 0, 100);
    }

    public static boolean respawnSetupRestrictionEnabled(ServerLevel level) {
        return level.getGameRules().get(RESTRICT_RESPAWN_SETUP);
    }

    public static int respawnSetupRadius(ServerLevel level) {
        return Math.max(0, level.getGameRules().get(RESPAWN_SETUP_RADIUS));
    }

    public static boolean creeperBlockDamageEnabled(ServerLevel level) {
        return level.getGameRules().get(CREEPER_BLOCK_DAMAGE);
    }

    public static int creeperExplosionRadiusBonus(ServerLevel level) {
        return Math.max(0, level.getGameRules().get(CREEPER_EXPLOSION_RADIUS_BONUS));
    }

    public static boolean spawnProtectionEnabled(ServerLevel level) {
        return level.getGameRules().get(SPAWN_PROTECTION);
    }

    public static int spawnProtectionRadius(ServerLevel level) {
        return Math.max(0, level.getGameRules().get(SPAWN_PROTECTION_RADIUS));
    }

    public static boolean spawnProtectionPvpEnabled(ServerLevel level) {
        return level.getGameRules().get(SPAWN_PROTECTION_PVP);
    }

    public static boolean spawnProtectionExplosionsEnabled(ServerLevel level) {
        GameRules gameRules = level.getGameRules();
        return gameRules.get(SPAWN_PROTECTION_EXPLOSIONS) && gameRules.get(SPAWN_PROTECTION_EXPLOSIONS_ALIAS);
    }

    public static int endCrystalPlayerDamageCap(ServerLevel level) {
        return Math.max(0, level.getGameRules().get(END_CRYSTAL_PLAYER_DAMAGE_CAP));
    }

    public static int respawnAnchorPlayerDamageCap(ServerLevel level) {
        return Math.max(0, level.getGameRules().get(RESPAWN_ANCHOR_PLAYER_DAMAGE_CAP));
    }

    public static int tntMinecartPlayerDamageCap(ServerLevel level) {
        return Math.max(0, level.getGameRules().get(TNT_MINECART_PLAYER_DAMAGE_CAP));
    }

    public static int maceSmashPlayerDamageCap(ServerLevel level) {
        return Math.max(0, level.getGameRules().get(MACE_SMASH_PLAYER_DAMAGE_CAP));
    }

    public static int spearPlayerDamageCap(ServerLevel level) {
        return Math.max(0, level.getGameRules().get(SPEAR_PLAYER_DAMAGE_CAP));
    }

    public static boolean hideInvisiblePlayersFromTabEnabled(ServerLevel level) {
        return level.getGameRules().get(HIDE_INVISIBLE_PLAYERS_FROM_TAB);
    }

    public static boolean worldProgressionEnabled(ServerLevel level) {
        return level.getGameRules().get(WORLD_PROGRESSION);
    }

    public static int oreProgressionInnerRadius(ServerLevel level) {
        return Math.max(0, level.getGameRules().get(ORE_PROGRESSION_INNER_RADIUS));
    }

    public static int oreProgressionNormalRadius(ServerLevel level) {
        return Math.max(0, level.getGameRules().get(ORE_PROGRESSION_NORMAL_RADIUS));
    }

    public static int oreProgressionInnerVeinPercent(ServerLevel level) {
        return Math.clamp(level.getGameRules().get(ORE_PROGRESSION_INNER_VEIN_PERCENT), 0, 100);
    }

    public static int oreProgressionFarBonusPercent(ServerLevel level) {
        return Math.clamp(level.getGameRules().get(ORE_PROGRESSION_FAR_BONUS_PERCENT), 0, 100);
    }
}
