![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green)
![Forge](https://img.shields.io/badge/Forge-47.x-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

# 🔥 Campfire Overhaul+

> *"For comfy nightstands out in the wilderness."*

A community port of the original **Campfire Overhaul** by ZanderMeister to Minecraft 1.20.x and 1.21.x.  
Transforms the vanilla campfire from an expensive, slow, and pretty much useless 4-slot smoker into an actual utility with new mechanics, recipes, and overall increased immersion.

---

## ✨ Features

### 🪵 New Crafting Recipe
Both normal and soul campfires can now be crafted in a **2x2 grid** - no crafting table needed!
- **Normal campfire:** 2 logs + 2 sticks
- **Soul campfire:** 1 log + 1 soul soil/sand + 2 sticks

Perfect for survival situations when you don't have a crafting bench nearby.

---

### 🍳 New Campfire Recipes
Cook up to **4 items at once** directly on the campfire (slower than a furnace, but no fuel needed):
- 🪵 Burn **logs** → **Charcoal**
- 🔥 Craft **torches** from sticks
- 🧽 Dry **wet sponges** back to dry
- 🧱 Bake **clay** into **bricks**

---

### 🌑 Campfire Placed Unlit *(configurable)*
Both campfire types are now created **unlit** by default.  
Light them up vanilla-style: flint & steel, flaming arrows, fire charges - or use the new methods below.

---

### 🪨 Two-Flint Ignition *(configurable)*
A new way to light campfires without flint & steel:
1. Hold **Flint** in your **main hand**
2. Hold **Flint** in your **offhand** (press **F** to swap)
3. **Right-click** the unlit campfire
4. Has a **1/3 (33%) chance** of success by default

> You can configure the success chance or disable this mechanic entirely. Note: only works on campfires, not regular blocks.

---

### ⏳ Campfire Lifetime System *(configurable)*
Campfires now have a **fuel lifetime** - when they run out of fuel, they die out.

**Refueling:**
- **Throw (Q) any burnable item** onto the campfire to add fuel
- All vanilla fuels are supported, most modded fuels too
- Throwing a **Lava Bucket** works too - the empty bucket is returned
- Fuel items give the same amount of time as they do in a furnace
- Set a **fuel multiplier** to make fuel last longer or shorter

**Special mechanics:**
- **Right-click with Dragon's Breath** *(item configurable)* to make the campfire burn **forever**
- **Right-click with empty hands** to check remaining fuel in seconds
- Soul campfires have **infinite lifetime** by default *(configurable)* to make them feel more unique
- World-generated campfires (e.g. villages) are **infinite by default**

**Burnout behavior** *(configurable)*:
- Campfire simply **extinguishes** (default)
- Or campfire completely **breaks** and drops its items

---

## ⚙️ Configuration (temporary)

Config file location:
```
saves/[world_name]/serverconfig/campfire_overhaul-server.toml
```

### Normal Campfire
| Option | Default | Description |
|--------|---------|-------------|
| `campfireCreatedUnlit` | `true` | Placed campfire starts unlit |
| `campfireDefaultLifeTime` | `2000` ticks | Starting fuel when placed (20 ticks = 1 second) |
| `campfireMaxLifeTime` | `MAX` | Maximum fuel limit |
| `campfireInfiniteLifeTime` | `false` | Override — never burns out |
| `campfireDestroyedOnBurnout` | `false` | Break instead of extinguish |

### Soul Campfire
| Option | Default | Description |
|--------|---------|-------------|
| `soulCampfireCreatedUnlit` | `false` | Soul campfire starts lit when placed |
| `soulCampfireDefaultLifeTime` | `4000` ticks | Starting fuel when placed |
| `soulCampfireInfiniteLifeTime` | `true` | Burns forever by default |
| `soulCampfireDestroyedOnBurnout` | `false` | Break instead of extinguish |

### Miscellaneous
| Option | Default | Description |
|--------|---------|-------------|
| `campfireFuelMultiplier` | `1` | Multiplier for all fuel burn times |
| `doubleFlintIgnition` | `true` | Enable/disable two-flint ignition |
| `flintIgniteChance` | `0.33` | Success chance (0.0 – 1.0) |
| `worldgenCampfiresInfinite` | `true` | Village campfires burn forever |
| `makingEternalCampfires` | `true` | Enable/disable infinite campfire mechanic |
| `infinteCampfireItem` | `minecraft:dragon_breath` | Item used to make campfires infinite |

---

## 🔧 Compatibility

| Mod | Status |
|-----|--------|
| Healing Campfire | ✅ Compatible |
| No Hostiles Around Campfire | ✅ Compatible |
| Colored Campfire Smoke | ✅ Compatible |
| Campful | ✅ Replaces recipes and non-silk touch drops |
| Endergetic Expansion | ✅ Replaces recipe of ender campfire |
| Infernal Expansion | ✅ Compatible |
| Any mod modifying campfires | ✅ Should be compatible |

> **Found an incompatibility?** Please report it on the GitHub repository!

---

## 📋 Requirements

- **Minecraft:** 1.20.x (for now 1.20.1 already test and work, another  version still in test)
- **Mod Loader:** Forge 47.x
- **Java:** 17 or higher
- **Dependencies:** None

---

## 📜 Credits

Original mod **[Campfire Overhaul](https://www.curseforge.com/minecraft/mc-mods/campfire-overhaul)** created by **ZanderMeister**, licensed under **MIT License**.  
This port to 1.20.x and 1.21.x preserves all original mechanics. All credits for the original concept and design go to ZanderMeister.

---

## 🐛 Bug Reports

If you find any bugs, please open an issue on the GitHub repository.  
Include your **Forge version**, **list of other mods**, and the **`latest.log`** from `.minecraft/logs/`.
