# Fairlands

Fairlands is a server-focused Fabric mod for Minecraft 26.1.x. It is intended to make multiplayer survival worlds fairer without removing danger: death should still matter, spawn should be worth building in, and PVP should be less dependent on destructive edge cases.

## Status

This repository is initialized for Minecraft 26.1.2, Fabric Loader 0.19.2, Fabric API 0.149.1+26.1.2, Loom 1.16.1, Mojang mappings, and Java 25.

The first death rules module is implemented. Remaining gameplay systems are tracked in the roadmap below and should be implemented as small, configurable server-side modules.

## Implemented

- Partial KeepInventory keeps equipped armor, tools, weapons, configured allowlisted items, and items tagged `fairlands:keep_on_death`.
- Empty maps and filled maps are kept by default.
- Exploration Reloaded's Map Book is kept by default when Exploration Reloaded is installed.
- Normal inventory items still drop on death.
- Death applies a configurable negative effect after respawn.

## Planned Scope

- Spawn sanctuary: block PVP, TNT, crystal damage, destructive explosions, and grief interactions inside a configured spawn radius.
- Spawn setup rules: restrict beds and respawn anchors to an allowed area around world spawn.
- Creeper rules: prevent creepers from breaking blocks while keeping them dangerous with a slightly larger explosion radius.
- End crystal rules: cap player damage to remove crystal PVP one-shots.
- Combat balancing: revisit mace and lance behavior after the core server rules are stable.
- World progression: place rare biomes farther from spawn and make them more rewarding.
- Feedback command: add an in-game `/feedback` command for players.
- Invisibility privacy: hide invisible players from the player list name display.

## Side

Fairlands is designed as a server-side gameplay mod:

- Required on the server.
- Not required on vanilla clients for the core rules.
- Works in singleplayer when installed locally.
- A future optional client module may be added only for UI polish or visual feedback.

## Development

Use JDK 25 for Minecraft 26.1.x development.

```sh
./gradlew build
./gradlew runServer
```

Fairlands bundles MidnightLib for server config.

## Config

Fairlands writes its config through MidnightLib. The first configurable options are:

- `partialKeepInventoryEnabled`
- `keepEquippedArmor`
- `keepToolsAndWeapons`
- `keepOnDeathAllowlist`
- `deathPenaltyEnabled`
- `deathPenaltyEffect`
- `deathPenaltyDurationSeconds`
- `deathPenaltyAmplifier`

## Documentation References

- Fabric 26.1 porting guide: https://docs.fabricmc.net/develop/porting/
- Fabric Loom documentation: https://docs.fabricmc.net/develop/loom/
- Fabric project creation guide: https://docs.fabricmc.net/develop/getting-started/creating-a-project

## License

Fairlands is licensed under CC BY-NC-SA 4.0. See `LICENSE`.
