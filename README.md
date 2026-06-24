# Fairlands

Fairlands is a server-focused Fabric mod for Minecraft 26.1.x. It makes multiplayer survival worlds fairer without removing danger: death still matters, spawn stays worth building in, and PVP is less dependent on destructive edge cases.

## Status

This repository targets Minecraft 26.1.2 with Fabric Loader 0.19.2, Fabric API 0.149.1+26.1.2, Loom 1.16.1, Mojang mappings, and Java 25.

Core server rules and the first world progression pass are implemented and ready for local multiplayer testing.

## Side

Fairlands is designed as a server-side gameplay mod:

- Required on the server.
- Not required on vanilla clients.
- Works in singleplayer when installed locally.
- Optional on the client only for local singleplayer or future UI polish.

## Dependencies

Runtime dependencies:

- Fabric Loader 0.19.2 or newer.
- Fabric API for Minecraft 26.1.2.
- MidnightLib 1.9.3+26.1-fabric, bundled into the Fairlands jar.
- Java 25.

Optional compatibility:

- Exploration Reloaded's `exploration-reloaded:map_book` is kept on death when that mod is present.

## Features

### Partial KeepInventory

- Enabled by default through `fairlands:partial_keep_inventory`.
- Disabled automatically when vanilla `keepInventory` is enabled.
- Keeps equipped armor when `keepEquippedArmor` is enabled.
- Keeps tools and weapons when `keepToolsAndWeapons` is enabled.
- Keeps empty maps, filled maps, recovery compasses, and items tagged `fairlands:keep_on_death`.
- Keeps configured allowlist items from MidnightLib config.
- Drops normal inventory items as usual.
- Restores kept items after respawn, preserving original slots when possible.
- Drops kept items safely if the original slot and inventory are full.

Default kept tool/weapon groups include swords, axes, pickaxes, shovels, hoes, spears, bows, crossbows, tridents, maces, shields, shears, flint and steel, fishing rods, brushes, buckets, elytra, carrot-on-a-stick, and warped-fungus-on-a-stick.

### Death Penalty And XP

- Applies a configurable negative effect after respawn.
- Default effect is Weakness for 180 seconds.
- Creative and spectator players do not receive the death penalty.
- Caps vanilla-style XP drops with `fairlands:death_xp_drop_cap`.
- Restores a percentage of total XP after respawn with `fairlands:death_xp_keep_percent`.
- Default XP behavior drops up to 1000 XP and keeps 50% of eligible total XP.

### Respawn Setup

- Restricts respawn point setup to a configurable radius around world spawn.
- Default radius is 2000 blocks, configurable with `fairlands:respawn_setup_radius`.
- Applies in all dimensions.
- Beds can still be slept in outside the allowed area.
- Beds and respawn anchors outside the allowed area do not set the player's respawn point.
- Players receive a chat message when a respawn point is blocked.

### Spawn Safe Zone

- Only applies in the Overworld.
- Default radius is 700 blocks.
- Blocks PVP inside the protected zone.
- Prevents destructive explosions from breaking blocks inside the protected zone.
- Blocks wither spawning inside the protected zone.
- Does not block normal player building or mining.
- The respawn setup restriction uses its own larger radius, so only bed and respawn anchor spawn points are limited beyond the safe zone.

For local dedicated-server testing, vanilla `spawn-protection` in `run/server.properties` should be `0`; Fairlands' spawn safe zone handles gameplay protection separately.

### Creepers And Explosions

- Creepers do not break blocks by default.
- Creeper explosion radius is increased by 1 by default.
- Creeper block damage and radius bonus are gamerule-controlled.
- Spawn-zone explosion protection also applies to destructive explosions such as TNT, End crystals, respawn anchors, and TNT minecarts.

### Combat Damage Caps

Fairlands caps specific player damage sources to reduce one-shot or destructive PVP edge cases:

- End crystal player damage: 8 by default.
- Respawn anchor player damage: 8 by default.
- TNT minecart player damage: 8 by default.
- Mace smash player damage: 16 by default.
- Spear player damage: 14 by default.

These caps only apply when the damaged entity is a player.

### PVP Combat Logging

- Enabled by default with `fairlands:pvp_combat_logging`.
- When one player damages another, both players enter PVP combat.
- Combat state lasts 15 seconds by default.
- A red bossbar shows the remaining combat timer.
- Disconnecting during the timer stores a penalty for reconnect.
- On reconnect, max health is reduced to 50% for 10 minutes by default.
- If a player disconnects again during the penalty, the remaining time is preserved for the next reconnect.
- The penalty is removed automatically when it expires.

### Feedback Command

- Adds `/feedback <message>` for players.
- Feedback is written to `run/fairlands-feedback.log`.
- Console command sources cannot submit feedback.
- Players receive a success or failure message in chat.

### Recipes

- Rotten flesh can be smelted into leather.

### Invisibility Tab Hiding

- Enabled by default with `fairlands:hide_invisible_players_from_tab`.
- Players with the Invisibility effect are hidden from other players' tab list.
- Visibility is resynced when the effect changes and when players join.

### Visible Player Count

- Enabled by default with `fairlands:hide_invisible_players_from_count`.
- The server-list ping reports only visible players: players with the Invisibility effect are excluded from the online count and the player sample.
- This is what Discord bots and server-list tools read when they ping the server. The count refreshes within about 5 seconds of an invisibility change (vanilla status cache interval).
- Other mods can read the same value directly: `com.akitain.fairlands.visibility.PlayerVisibility.visiblePlayerCount(server)` and `visiblePlayers(server)`.
- Note: this adjusts the status/ping count only. The Query protocol and RCON `list` still report the raw count.

### World Progression

- Enabled by default with `fairlands:world_progression`.
- Affects new Overworld ore generation only.
- Existing generated chunks are not rewritten.
- Near world spawn, ore vein attempts are reduced to 45% by default.
- Between the inner radius and normal radius, ore density interpolates back to vanilla.
- Beyond the normal radius, ore generation is vanilla density with a 10% bonus attempt chance by default.
- The system targets vanilla ore tags for coal, copper, iron, gold, redstone, lapis, emerald, and diamond ores.

Default distance bands:

- Inner radius: 2000 blocks.
- Normal radius: 6000 blocks.
- Far bonus starts beyond 6000 blocks.

Operator debug commands are included for playtesting actual generated ore counts:

```mcfunction
/fairlands_debug world_progression here
/fairlands_debug world_progression at <x> <z>
/fairlands_debug world_progression samples
/fairlands_debug world_progression count here <chunkRadius>
/fairlands_debug world_progression count at <x> <z> <chunkRadius>
/fairlands_debug world_progression count samples <chunkRadius>
```

The count commands force-load and scan generated chunks around the target position. `chunkRadius` is intentionally small: `0..2` for single-position scans and `0..1` for sample scans.

## Config

Fairlands writes its config through MidnightLib. Current config options cover death behavior:

- `keepEquippedArmor`: default `true`.
- `keepToolsAndWeapons`: default `true`.
- `keepOnDeathAllowlist`: default `["exploration-reloaded:map_book"]`.
- `deathPenaltyEnabled`: default `true`.
- `deathPenaltyEffect`: default `minecraft:weakness`.
- `deathPenaltyDurationSeconds`: default `180`.
- `deathPenaltyAmplifier`: default `0`.

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
/gamerule fairlands:spawn_protection_radius 700
/gamerule fairlands:spawn_protection_pvp true
/gamerule fairlands:spawn_protection_grief true
/gamerule fairlands:spawn_protection_explosions true
/gamerule fairlands:end_crystal_player_damage_cap 8
/gamerule fairlands:respawn_anchor_player_damage_cap 8
/gamerule fairlands:tnt_minecart_player_damage_cap 8
/gamerule fairlands:mace_smash_player_damage_cap 16
/gamerule fairlands:spear_player_damage_cap 14
/gamerule fairlands:hide_invisible_players_from_tab true
/gamerule fairlands:hide_invisible_players_from_count true
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

`fairlands:spawn_protection_grief` is kept for existing worlds and currently mirrors `fairlands:spawn_protection_explosions`.

## Development

Use JDK 25 for Minecraft 26.1.x development.

```sh
./gradlew build
./gradlew runServer
./gradlew runClient
```

For local dev clients launched by Loom, the dedicated dev server may need `online-mode=false` in `run/server.properties`.

## Testing

- Death: verify armor, tools, weapons, maps, filled maps, allowlisted items, and tagged items are kept while normal inventory drops.
- XP: verify dropped XP is capped and part of total XP is restored after respawn.
- Death penalty: die in survival and confirm Weakness is applied after respawn.
- Respawn setup: sleep outside the allowed area and confirm the bed does not set spawn.
- Spawn safe zone: confirm players can still build and break blocks, while PVP, destructive explosions, and wither spawning are blocked.
- Creepers: confirm creepers have a larger blast radius but do not break blocks by default.
- Combat caps: test End crystals, respawn anchors, TNT minecarts, mace smash attacks, and spears against players.
- Combat logging: hit another player and confirm both players get a bossbar timer. Disconnect during the timer, reconnect, and confirm max health is halved for 10 minutes with a chat message.
- Feedback: run `/feedback <message>` and confirm `run/fairlands-feedback.log` records the entry.
- Invisibility: with two players online, apply Invisibility to one player and confirm they disappear from the other player's tab list.
- Ore progression: generate new chunks near spawn and far from spawn, then compare ore density. Near-spawn chunks should have fewer ore veins; chunks beyond `fairlands:ore_progression_normal_radius` should be vanilla density with a small bonus chance.
- Ore progression debug: run the `/fairlands_debug world_progression ...` commands as an operator to count real generated ore blocks.

## Planned Scope

- Add rare-biome rewards after ore progression is playtested.
- Consider biome placement experiments only after the lower-risk progression systems are stable.
- Tune default damage caps, spawn radius, death penalty, XP retention, and world progression values after multiplayer testing.

The world progression design notes are tracked in `docs/world-progression.md`.

## Documentation References

- GitHub repository: https://github.com/Aqu1tain/fairlands
- Fabric 26.1 porting guide: https://docs.fabricmc.net/develop/porting/
- Fabric Loom documentation: https://docs.fabricmc.net/develop/loom/
- Fabric project creation guide: https://docs.fabricmc.net/develop/getting-started/creating-a-project

## License

Fairlands is licensed under CC BY-NC-SA 4.0. See `LICENSE`.
