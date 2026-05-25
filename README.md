# Fairlands

Fairlands is a server-focused Fabric mod for Minecraft 26.1.x. It is intended to make multiplayer survival worlds fairer without removing danger: death should still matter, spawn should be worth building in, and PVP should be less dependent on destructive edge cases.

## Status

This repository is initialized for Minecraft 26.1.2, Fabric Loader 0.19.2, Fabric API 0.149.1+26.1.2, Loom 1.16.1, Mojang mappings, and Java 25.

Core server rules and the first world progression pass are implemented and ready for local multiplayer testing.

## Implemented

### Death Rules

- Partial KeepInventory keeps equipped armor, tools, weapons, configured allowlisted items, and items tagged `fairlands:keep_on_death`.
- Empty maps and filled maps are kept by default.
- Exploration Reloaded's Map Book is kept by default when Exploration Reloaded is installed.
- Normal inventory items still drop on death.
- Death applies a configurable negative effect after respawn.
- `fairlands:partial_keep_inventory` enables the death rules by default per world.
- Death XP drops are capped by `fairlands:death_xp_drop_cap`.
- A percentage of total XP is restored after respawn with `fairlands:death_xp_keep_percent`.

### Spawn Rules

- Respawn setup is restricted to a configurable radius around world spawn by default.
- Beds can still be used outside the allowed area, but they do not set the player's respawn point.
- PVP is blocked inside the configured spawn safe zone.
- Destructive explosions inside the configured spawn safe zone do not break blocks.
- Withers cannot be spawned inside the configured spawn safe zone.

### Explosion And Combat Rules

- Creepers do not break blocks by default and their explosion radius is slightly larger.
- End crystal player damage is capped.
- Respawn anchor player damage is capped.
- TNT minecart player damage is capped.
- Mace smash player damage is capped.
- Spear player damage is capped.

### Player Tools

- Players can send server feedback with `/feedback <message>`.
- Invisible players are hidden from other players' tab list by default.
- Players enter a timed PVP combat state after damaging or being damaged by another player.
- Disconnecting during PVP combat halves max health for 10 minutes on reconnect by default.

### World Progression

- New Overworld chunks near spawn generate fewer ore veins by default.
- Ore density scales back toward vanilla levels with distance from world spawn.
- Distant chunks beyond the normal radius can receive a small bonus ore vein chance.
- Operator debug commands can count real generated ore blocks for testing.

## Planned Scope

- World progression: add rare-biome rewards and consider biome placement experiments after ore progression is playtested.
- Balance pass after playtesting: tune default damage caps, spawn radius, death penalty, and XP retention.

The world progression design notes are tracked in `docs/world-progression.md`.

## Side

Fairlands is designed as a server-side gameplay mod:

- Required on the server.
- Not required on vanilla clients.
- Works in singleplayer when installed locally.
- Optional on the client only for local singleplayer or future UI polish.

## Development

Use JDK 25 for Minecraft 26.1.x development.

```sh
./gradlew build
./gradlew runServer
./gradlew runClient
```

Fairlands bundles MidnightLib for server config.

For local dev clients launched by Loom, the dedicated dev server may need `online-mode=false` in `run/server.properties`. For spawn-zone building tests, set vanilla `spawn-protection=0`; Fairlands' spawn safe zone does not block normal building.

## Config

Fairlands writes its config through MidnightLib. Current config options cover death behavior:

- `partialKeepInventoryEnabled`
- `keepEquippedArmor`
- `keepToolsAndWeapons`
- `keepOnDeathAllowlist`
- `deathPenaltyEnabled`
- `deathPenaltyEffect`
- `deathPenaltyDurationSeconds`
- `deathPenaltyAmplifier`

## Gamerules

```mcfunction
/gamerule fairlands:partial_keep_inventory true
/gamerule fairlands:death_xp_drop_cap 1000
/gamerule fairlands:death_xp_keep_percent 50
/gamerule fairlands:restrict_respawn_setup true
/gamerule fairlands:respawn_setup_radius 2000
/gamerule fairlands:creeper_block_damage false
/gamerule fairlands:creeper_explosion_radius_bonus 1
/gamerule fairlands:spawn_protection true
/gamerule fairlands:spawn_protection_radius 2000
/gamerule fairlands:spawn_protection_pvp true
/gamerule fairlands:spawn_protection_grief true
/gamerule fairlands:spawn_protection_explosions true
/gamerule fairlands:end_crystal_player_damage_cap 8
/gamerule fairlands:respawn_anchor_player_damage_cap 8
/gamerule fairlands:tnt_minecart_player_damage_cap 8
/gamerule fairlands:mace_smash_player_damage_cap 16
/gamerule fairlands:spear_player_damage_cap 14
/gamerule fairlands:hide_invisible_players_from_tab true
/gamerule fairlands:world_progression true
/gamerule fairlands:ore_progression_inner_radius 2000
/gamerule fairlands:ore_progression_normal_radius 6000
/gamerule fairlands:ore_progression_inner_vein_percent 45
/gamerule fairlands:ore_progression_far_bonus_percent 10
/gamerule fairlands:pvp_combat_logging true
/gamerule fairlands:pvp_combat_tag_seconds 15
/gamerule fairlands:pvp_combat_log_penalty_seconds 600
/gamerule fairlands:pvp_combat_log_health_percent 50
```

`fairlands:spawn_protection_grief` is kept for existing worlds and currently mirrors the explosion protection setting.

## Testing

- Death: verify armor, tools, weapons, maps, filled maps, and tagged items are kept while normal inventory drops.
- XP: verify dropped XP is capped and part of total XP is restored after respawn.
- Respawn setup: sleep outside the allowed area and confirm the bed does not set spawn.
- Spawn safe zone: confirm players can still build and break blocks, while PVP, destructive explosions, and wither spawning are blocked.
- Combat caps: test end crystals, respawn anchors, TNT minecarts, mace smash attacks, and spears against players.
- Feedback: run `/feedback <message>` and confirm `run/fairlands-feedback.log` records the entry.
- Invisibility: with two players online, apply invisibility to one player and confirm they disappear from the other player's tab list.
- Ore progression: generate new chunks near spawn and far from spawn, then compare ore density. Near-spawn chunks should have fewer ore veins; chunks beyond `fairlands:ore_progression_normal_radius` should be vanilla density with a small bonus chance.
- Ore progression debug: run `/fairlands_debug world_progression count here <chunkRadius>`, `/fairlands_debug world_progression count at <x> <z> <chunkRadius>`, or `/fairlands_debug world_progression count samples <chunkRadius>` as an operator to count real generated ore blocks.
- Combat logging: hit another player and confirm both players get a bossbar timer. Disconnect during the timer, reconnect, and confirm max health is halved for 10 minutes with a chat message.

## Documentation References

- GitHub repository: https://github.com/Aqu1tain/fairlands
- Fabric 26.1 porting guide: https://docs.fabricmc.net/develop/porting/
- Fabric Loom documentation: https://docs.fabricmc.net/develop/loom/
- Fabric project creation guide: https://docs.fabricmc.net/develop/getting-started/creating-a-project

## License

Fairlands is licensed under CC BY-NC-SA 4.0. See `LICENSE`.
