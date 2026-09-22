# SBW Extra Armory (sbwarmory)

[日本語 README](README.ja.md)

A [SuperbWarfare](https://www.curseforge.com/minecraft/mc-mods/superb-warfare) addon for Minecraft 1.20.1 / Forge 47.x. Adds extra weapons and equipment on top of the base mod - grenades with unusual behavior, a grenade-launching rifle, a throwing knife, a shoulder-fired guided missile launcher, a cruise-missile call-in system, night vision headgear, and some building blocks.

## Requirements

- Minecraft 1.20.1, Forge 47.x
- [SuperbWarfare](https://www.curseforge.com/minecraft/mc-mods/superb-warfare) 0.8.9.1+
- [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib) 4.4+ (SuperbWarfare already requires this)

## Items

All items are in their own **SBW Extra Armory** creative tab.

### Grenades & thrown weapons

| Item | Description |
|---|---|
| **Cluster Grenade** | A 40mm grenade that detects an imminent block hit ~8 ticks out and splits into 3 live sub-grenades before impact. Also comes as launcher-only ammo (non-throwable). |
| **Smoke Cluster Grenade** | Same split-before-impact behavior, but the sub-munitions are M18 smoke grenades instead of explosives. Also comes as launcher-only ammo. |
| **Spring Grenade** | A hand grenade (hold to cook, release to throw) that springs 1.5 blocks straight up right before it detonates, S-mine style. |
| **Throwing Knife** | A thrown blade using SuperbWarfare's generic bullet entity, so headshots get its native damage multiplier. Flies dead straight (zero gravity). |

### Guns

| Item | Description |
|---|---|
| **Grenade Launcher** | A rifle-shaped weapon carrying two ammo types: standard rifle rounds, and an underslung "Grenade" slot that fires the Cluster Grenade. |
| **SRAW** / **SRAW Missile** | A shoulder-fired launcher whose missile is *wire-guided*: every tick in flight it curves toward wherever the shooter is currently looking, not just its initial launch angle. |

### Call-in system

| Item | Description |
|---|---|
| **Cruise Missile Designator** | Right-click to aim down the sight (spyglass-style scope overlay, scroll to zoom), then hold left-click to call in a cruise missile at the marked point. The missile hovers out of sight for a few seconds (with an air-raid siren warning everyone nearby), then falls and hits a wide configurable area. Consumed on use. |
| **Beast Missile Designator** | Same call-in system, played for laughs - the "siren" is SuperbWarfare's own senpai mob growl, the missile takes twice as long to arrive, and impact scatters rock debris with a comedic voice line instead of a mushroom cloud. |

All cruise-missile balance (damage, footprint, timing, siren volume) is configurable server-side in `config/sbwarmory-server.toml`.

### Headgear

| Item | Description |
|---|---|
| **Night Vision Goggles** (blue/red) | A real helmet-slot item with a custom 3D model. Grants night vision while worn; press **N** to toggle the effect and its green first-person-only shader on/off without taking the helmet off. |
| **Helmet** (blue/red) | The same headgear model without the goggles or any effect - just armor. |

### Blocks

| Item | Description |
|---|---|
| **Concrete Barrier** | A Jersey-style barrier. Placed alone it's a wide tapered base; stack another on top and it continues as a thin straight wall. Follows the placement direction and reverts automatically if the block below is removed. |
| **Concrete Block** | A plain full cube using the same weathered concrete texture. |

## Building

```
./gradlew build
```

Requires JDK 21 available to the toolchain. Run configs: `./gradlew runClient` / `./gradlew runServer`.

**Resource-only changes** (JSON/textures, no Java) need `./gradlew processResources` explicitly - `compileJava` alone does not re-copy them into `build/resources`.

## License

GNU General Public License v3.0 (GPL-3.0-only).

## Known issues

- SRAW's launcher model occasionally crashes on render with a `NullPointerException` in GeckoLib's `GeoModel.getAnimation` (animation resource resolves to `null`) under conditions not yet isolated - root cause still open.
