# World Progression

Fairlands' world progression system should make distance from spawn matter without making world generation fragile. The long-term fantasy is simple: common biomes and baseline resources stay near spawn, rare biomes and stronger rewards encourage travel farther out.

## Research Summary

Fabric's supported worldgen path is built around configured features, placed features, and biome modifications. That is a good fit for adding resources or biome-specific additions, but not enough by itself to reorder vanilla biome placement by distance from spawn.

Minecraft's biome placement is selected through `BiomeSource#getNoiseBiome`, which receives quart coordinates and the climate sampler. Changing where vanilla biomes appear means wrapping or replacing the biome source used by the dimension generator. That is possible, but it is a world-creation-level change with a higher compatibility cost than the rest of Fairlands.

Fabric's event API includes `LootTableEvents.MODIFY`, which is useful for compatible loot additions. Loot/event-based progression is safer than replacing vanilla loot tables.

## Library Check

### TerraBlender

TerraBlender is actively available on Modrinth for Fabric and 26.1.x. It is meant for adding biomes and regions in a compatible way. It is client-and-server, so it does not match Fairlands' current server-side-only goal.

Use it only if Fairlands later adds custom biomes or wants deep compatibility with biome mods that already use TerraBlender.

### Lithostitched

Lithostitched has a Fabric 26.1 build for 26.1-26.1.2, is server-side, and is MIT licensed. It provides data-driven worldgen helpers such as biome modifiers, surface rule modifiers, template pool injectors, and newer region tools.

This is the strongest candidate if Fairlands needs a maintained worldgen helper dependency later. For the first progression pass, Fabric API is enough.

### Modified Biome Source

Modified Biome Source supports 26.1-26.1.2 and can filter biome sources through dimension definitions. It is server-side, but it is ARR licensed and oriented around custom dimension generator JSON.

Do not add it as a dependency for now. It is too narrow and too invasive for Fairlands' first progression pass.

## Decision

Do not add a new dependency yet.

Implement world progression in small server-side pieces using Fabric API, Mixin hooks, and direct server checks:

1. Distance bands from world spawn.
2. Ore density scaling by distance from world spawn.
3. Rare-biome classification using biome keys and tags.
4. Resource/loot bonuses that require both distance and biome context.
5. Gamerules/config for tuning.

True radial biome redistribution should remain a separate experimental module or branch. It should not block the first playable progression system.

## First Implementation Target

The first useful feature is ore density progression:

- New Overworld chunks close to spawn generate fewer ore veins.
- Ore density interpolates back to vanilla farther from spawn.
- New Overworld chunks beyond the normal radius can receive a small bonus ore vein chance.
- Existing generated chunks are not rewritten.

The next progression feature should be distant rare-biome rewards:

- Define distance bands, defaulting to near, frontier, and far.
- Detect when a player breaks ore blocks or opens generated loot far from spawn.
- Apply modest bonus drops only in configured rare biome groups and distance bands.
- Keep all bonuses deterministic enough to avoid dupes and easy farms.
- Add gamerules to disable or tune the system.

This gives the gameplay loop we want without destabilizing chunk generation.

## Later Options

- Add custom placed ore features with a distance-aware placement modifier.
- Use Lithostitched if data-driven biome/resource injection becomes worth the dependency.
- Prototype a custom `BiomeSource` only after the loot/resource progression has been playtested.

## Sources

- Fabric feature generation docs: https://docs.fabricmc.net/develop/data-generation/features
- Fabric events and loot table docs: https://docs.fabricmc.net/develop/events
- TerraBlender on Modrinth: https://modrinth.com/mod/terrablender
- Lithostitched Fabric 26.1 on Modrinth: https://modrinth.com/mod/lithostitched/version/1.7.0-fabric-26.1
- Modified Biome Source on Modrinth: https://modrinth.com/mod/modifiedbiomesource
