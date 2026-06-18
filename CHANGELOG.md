# Changelog

## 0.1.5-beta

- Fixed Partial KeepInventory losing kept items (armor, tools, and other keepables) on a server crash or a disconnect on the death screen. Kept items now stay in the inventory through death instead of being held in memory, so they survive crashes and relogs.

## 0.1.4-beta

- Spawn safe zone now applies only in the Overworld.
- Spawn safe zone radius (PVP, explosions, and wither spawning) is now 700 blocks by default, down from 2000. The respawn setup restriction keeps its own 2000-block radius and still applies in all dimensions.
- The spawn protection message now shows above the hotbar and fades out instead of appearing in chat.

## 0.1.3-beta

- Elytra and other glider items are now treated as tools and kept on death.

## 0.1.2-beta

- Fixed totems of undying voiding kept inventory items.
- Fixed the death penalty effect applying when returning from the End.
- Respawn setup restriction now only applies when beds and respawn anchors set a spawn point, no longer to `/spawnpoint` or internal respawn restores.
- Added recovery compasses to the default kept items.
- Added a smelting recipe turning rotten flesh into leather.
- All buckets (`c:buckets`) are now kept on death, not only water buckets.

## 0.1.1-beta

- Water buckets are now treated as tools and kept on death.

## 0.1.0-beta

- Initialized Fairlands for Minecraft 26.1.2.
- Added Fabric Loader, Fabric API, Loom, Gradle, and Java 25 project setup.
- Added server-focused mod metadata and roadmap documentation.
- Added MidnightLib as the bundled config dependency.
- Added Partial KeepInventory for equipped armor, tools, weapons, allowlisted items, and the `fairlands:keep_on_death` item tag.
- Added empty maps and filled maps to the default kept items.
- Added Exploration Reloaded Map Book compatibility for Partial KeepInventory.
- Added configurable death penalty effect after respawn.
- Added `fairlands:partial_keep_inventory`, enabled by default.
- Added death XP drop cap and XP keep percentage gamerules.
- Added respawn setup restriction gamerules for beds and respawn anchors.
- Added creeper block damage and explosion radius gamerules.
- Added spawn safe zone rules for PVP, destructive explosions, and wither spawning.
- Added player damage caps for End crystals, respawn anchors, TNT minecarts, mace smash attacks, and spears.
- Added `/feedback <message>` logging to `run/fairlands-feedback.log`.
- Added tab-list hiding for invisible players.
- Added Overworld ore density progression by distance from world spawn.
- Added PVP combat logging punishment with a bossbar timer and temporary max-health penalty.
