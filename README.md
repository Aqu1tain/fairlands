# Fairlands

Fairlands is a server-focused Fabric mod for Minecraft 26.1.x. It is intended to make multiplayer survival worlds fairer without removing danger: death should still matter, spawn should be worth building in, and PVP should be less dependent on destructive edge cases.

## Status

This repository is initialized for Minecraft 26.1.2, Fabric Loader 0.19.2, Fabric API 0.149.1+26.1.2, Loom 1.16.1, Mojang mappings, and Java 25.

Core server rules are implemented and ready for local multiplayer testing. World progression is the main remaining gameplay module.

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

## Planned Scope

- World progression: place rare biomes farther from spawn and make distant regions more rewarding.
- Balance pass after playtesting: tune default damage caps, spawn radius, death penalty, and XP retention.

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

## Documentation References

- GitHub repository: https://github.com/Aqu1tain/fairlands
- Fabric 26.1 porting guide: https://docs.fabricmc.net/develop/porting/
- Fabric Loom documentation: https://docs.fabricmc.net/develop/loom/
- Fabric project creation guide: https://docs.fabricmc.net/develop/getting-started/creating-a-project

## License

Fairlands is licensed under CC BY-NC-SA 4.0. See `LICENSE`.
