![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green)
![Forge](https://img.shields.io/badge/Forge-47.x-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

# 🔥 Campfire Overhaul+

> *"For comfy nightstands out in the wilderness."*

A community port of the original **Campfire Overhaul** by ZanderMeister to **Minecraft 1.20.x / 1.21.x**.  
This mod transforms the vanilla campfire from a slow, limited 4-slot smoker into a **useful survival tool** with enhanced mechanics, recipes, and immersion.

---

## ✨ Features

### 🪵 New Crafting Recipe
Both normal and soul campfires can now be crafted in a **2×2 grid** — no crafting table required.

- **Normal campfire:** 2 logs + 2 sticks  
- **Soul campfire:** 1 log + 1 soul soil/sand + 2 sticks  

> Perfect for early-game survival.

---

### 🍳 Expanded Campfire Recipes
Cook up to **4 items simultaneously** (slower than a furnace, but requires no fuel):

- 🪵 Logs → Charcoal  
- 🔥 Sticks → Torches  
- 🧽 Wet Sponge → Dry Sponge  
- 🧱 Clay → Bricks  

---

### 🌑 Campfire Starts Unlit *(Configurable)*
Campfires are now placed **unlit by default**.

Light them using:
- Flint & Steel  
- Fire Charges  
- Flaming Arrows  
- Or new mechanics below 👇

---

### 🪨 Two-Flint Ignition *(Configurable)*
A primitive way to start a fire:

1. Hold **Flint** in main hand  
2. Hold **Flint** in offhand (`F`)  
3. Right-click campfire  
4. ~33% success rate (default)  

> Fully configurable or can be disabled.

---

### ⏳ Campfire Fuel System *(Core Feature)*
Campfires now require **fuel to stay lit**.

#### 🔥 Refueling
- Drop (`Q`) any burnable item onto the campfire  
- Supports vanilla + most modded fuels  
- Lava bucket works (returns empty bucket)  
- Fuel value = furnace burn time  
- Adjustable via multiplier  

#### ⚙️ Special Mechanics
- 🐉 Dragon’s Breath → infinite fire *(configurable)*  
- 👋 Right-click (empty hand) → check remaining fuel  
- 🌑 Soul campfires → infinite by default *(configurable)*  
- 🏘️ Worldgen campfires → infinite *(configurable)*  

#### 💥 Burnout Behavior
- Extinguish *(default)*  
- OR break & drop items *(configurable)*  

---

## ⚙️ Configuration

📍 Location:
saves/[world]/serverconfig/campfire_overhaul-server.toml


### 🔥 Normal Campfire
| Option | Default | Description |
|--------|--------|------------|
| `campfireCreatedUnlit` | true | Starts unlit |
| `campfireDefaultLifeTime` | 2000 | Initial fuel |
| `campfireMaxLifeTime` | MAX | Max capacity |
| `campfireInfiniteLifeTime` | false | Infinite override |
| `campfireDestroyedOnBurnout` | false | Break on burnout |

---

### 🌑 Soul Campfire
| Option | Default | Description |
|--------|--------|------------|
| `soulCampfireCreatedUnlit` | false | Starts lit |
| `soulCampfireDefaultLifeTime` | 4000 | Initial fuel |
| `soulCampfireInfiniteLifeTime` | true | Infinite by default |
| `soulCampfireDestroyedOnBurnout` | false | Break on burnout |

---

### ⚙️ Misc
| Option | Default | Description |
|--------|--------|------------|
| `campfireFuelMultiplier` | 1 | Fuel scaling |
| `doubleFlintIgnition` | true | Enable feature |
| `flintIgniteChance` | 0.33 | Success rate |
| `worldgenCampfiresInfinite` | true | Village fires infinite |
| `makingEternalCampfires` | true | Enable infinite mechanic |
| `infinteCampfireItem` | minecraft:dragon_breath | Trigger item |

---

## 🔧 Compatibility

| Mod | Status |
|-----|--------|
| Healing Campfire | ✅ |
| No Hostiles Around Campfire | ✅ |
| Colored Campfire Smoke | ✅ |
| Campful | ✅ (recipe override) |
| Endergetic Expansion | ✅ |
| Infernal Expansion | ✅ |

> Most mods modifying campfires should be compatible.

---

## 📋 Requirements

- Minecraft: **1.20.1+**  
- Forge: **47.x**  
- Java: **17+**  
- Dependencies: None  

---

## 📜 Credits

Original mod: **Campfire Overhaul** by **ZanderMeister** (MIT License).  
This project is a community port preserving original mechanics while extending compatibility.

---

## 🐛 Bug Reports

Report issues via GitHub Issues.

Include:
- Forge version  
- Mod list  
- `latest.log` file  
