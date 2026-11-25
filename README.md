![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green)
![Forge](https://img.shields.io/badge/Forge-47.3.0-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

# CampfireOverhaul+

A port of ZanderMeister's Campfire Overhaul to Minecraft 1.20.1.

Vanilla campfires are expensive to make, cook slowly, and have no real use beyond that. This mod gives them a cheaper recipe, a fuel lifetime, a few extra cooking recipes, and optional unlit placement. Most of it is configurable.

## Crafting

Both campfires can be crafted in the 2x2 inventory grid.

- **Campfire:** 2 logs + 2 sticks
- **Soul campfire:** 1 log + 1 soul soil or soul sand + 2 sticks

## Cooking recipes

A campfire cooks up to 4 items at once. It is slower than a furnace.

- Logs → charcoal
- Sticks → torches
- Wet sponge → sponge
- Clay ball → brick

## Placement and ignition

Normal campfires are placed unlit by default; soul campfires are placed lit. Both are configurable. You can light them with flint and steel, fire charges, flaming arrows, or two flints.

**Two-flint ignition:** hold flint in both hands (F swaps to offhand) and right-click an unlit campfire. Each attempt has a 33% chance to work by default. The chance can be changed, or the mechanic disabled. It only affects campfires.

## Fuel lifetime

Campfires burn out after a set time unless they are made infinite.

- **Add fuel** by throwing (Q) a burnable item onto the campfire. Vanilla smelting fuels are tested, including coal and lava buckets; modded fuel integration is not verified. Burn times match the furnace and can be scaled with the fuel multiplier. A lava bucket returns the empty bucket. Fuel is capped at the configured maximum, and an item is not consumed if the campfire has no room for it.
- **Check remaining time** by right-clicking with an empty hand. It is shown in seconds.
- **Make it permanent** by right-clicking with Dragon's Breath (the item is configurable). One item is consumed per conversion, with a growl and portal particles.
- **Soul campfires** have infinite lifetime by default.
- **Campfires generated in the world**, such as in villages, are infinite by default.
- **On burnout**, the campfire either extinguishes (default) or breaks and drops its items.

A global fuel multiplier scales all burn times.

## Configuration

The config is currently a per-world server config:

```
saves/<world_name>/serverconfig/campfire_overhaul-server.toml
```

### Campfire

| Option | Default | Description |
| --- | --- | --- |
| `campfireCreatedUnlit` | `true` | Placed campfires start unlit |
| `campfireDefaultLifeTime` | `2000` | Fuel when placed, in ticks (20 ticks = 1 second) |
| `campfireMaxLifeTime` | max | Maximum fuel it can hold |
| `campfireInfiniteLifeTime` | `false` | Never burns out |
| `campfireDestroyedOnBurnout` | `false` | Break instead of extinguish |

### Soul campfire

| Option | Default | Description |
| --- | --- | --- |
| `soulCampfireCreatedUnlit` | `false` | Placed soul campfires start lit |
| `soulCampfireDefaultLifeTime` | `4000` | Fuel when placed, in ticks |
| `soulCampfireMaxLifeTime` | max | Maximum fuel it can hold |
| `soulCampfireInfiniteLifeTime` | `true` | Never burns out |
| `soulCampfireDestroyedOnBurnout` | `false` | Break instead of extinguish |

### Other

| Option | Default | Description |
| --- | --- | --- |
| `campfireFuelMultiplier` | `1` | Multiplier for all fuel burn times |
| `doubleFlintIgnition` | `true` | Enable two-flint ignition |
| `flintIgniteChance` | `0.33` | Success chance, 0.0 to 1.0 |
| `worldgenCampfiresInfinite` | `true` | Generated campfires never burn out |
| `makingEternalCampfires` | `true` | Allow making campfires infinite |
| `infinteCampfireItem` | `minecraft:dragon_breath` | Item used to make a campfire infinite |

## Compatibility

| Mod | Status |
| --- | --- |
| Healing Campfire | Not Yet Verified |
| No Hostiles Around Campfire | Not Yet Verified |
| Colored Campfire Smoke | Not Yet Verified |
| Campful | Not Yet Verified |
| Endergetic Expansion | Not Yet Verified |
| Infernal Expansion | Not Yet Verified |

These integrations are not verified because compatible mod files were not available. The mod ships data files for Campful, Endergetic Expansion and Infernal Expansion, but those files alone do not prove runtime compatibility. There is no general compatibility guarantee for other mods that change campfires.

Vanilla smelting fuels have been tested. Modded fuel integration has not been verified.

## Requirements

- Minecraft 1.20.1 (tested and supported). Minecraft 1.21.x has not been tested or ported.
- Forge 47.3.0 (tested)
- Java 17
- No dependencies
- Mod version: 2.0.1

## Credits

Based on [Campfire Overhaul](https://www.curseforge.com/minecraft/mc-mods/campfire-overhaul) by ZanderMeister, licensed under MIT. This port keeps the original mechanics. Credit for the original concept and design goes to ZanderMeister. The 1.20.1 fork is maintained by AriAja17.

See [CHANGELOG.md](CHANGELOG.md) for what changed in 2.0.1.

## Bug reports

Open an issue on the GitHub repository and include your Forge version, your mod list, and `latest.log` from `.minecraft/logs/`.

