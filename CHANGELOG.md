# Campfire Overhaul+ Changelog

## 2.0.1

This release focuses on how long campfires burn, how they handle fuel, and how reliably they keep that state through a burnout or a world reload. It replaces the 2.0.0 Beta.

### Added

- Making a campfire burn forever with the configured item (Dragon's Breath by default) now consumes one item per use and is accompanied by an ender dragon growl and purple portal particles, alongside the existing enchantment sound.

### Changed

- Campfires now start with the burn time specified by the lifetime settings, counted in ticks. Previously the configured value was multiplied by 10,000, so a campfire lasted far longer than its setting suggested.
- Normal and soul campfires now use the same lifetime and burnout rules, each still reading its own configuration values. World-generated campfires continue to follow the separate worldgen setting.
- Burned-out campfires now stop the vanilla campfire tick immediately after breaking or going out, so the resulting block and dropped items are left in a consistent state.

### Fixed

- Lighting an existing campfire with flint and steel no longer resets the fuel already stored in it.
- Throwing items onto a campfire no longer overflows the fuel calculation. Large stacks and high fuel multipliers are capped at the campfire's maximum lifetime, and an item is no longer consumed when the campfire has no room left for fuel.
- Remaining burn time is now saved reliably. Fuel added or removed is written to the save immediately, and the countdown is saved at least once per second, so finite campfires keep their remaining fuel after a save and reload or a server restart.
- Loading a saved campfire no longer marks it as newly changed on every load.

### Compatibility

- Tested with Minecraft 1.20.1, Forge 47.3.0, and Java 17.
- The build targets Minecraft 1.20.1 (declared range `[1.20.1,1.21)`). Minecraft 1.21.x has not been tested or ported.
- Compatibility with other mods remains unverified; no general compatibility guarantee is made. The included compatibility data alone does not confirm runtime compatibility.
- Vanilla smelting fuels were tested with coal and lava buckets. Modded fuel integration is not yet verified.

### Technical

- Configuration keys and default values remain unchanged. Campfire lifetime now follows the configured tick value directly.
