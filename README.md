# SBW Extra Armory (sbwarmory)

A [SuperbWarfare](https://www.curseforge.com/minecraft/mc-mods/superb-warfare) addon for Minecraft 1.20.1 / Forge 47.x. Adds extra weapons on top of the base mod - grenades with unusual behavior, a custom grenade-launching carbine, a throwing knife, and a shoulder-fired guided missile launcher.

## Requirements

- Minecraft 1.20.1, Forge 47.x
- [SuperbWarfare](https://www.curseforge.com/minecraft/mc-mods/superb-warfare) 0.8.9+
- [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib) 4.4+ (SuperbWarfare already requires this)

## Items

All items are in their own **SBW Extra Armory** creative tab.

| Item | Description |
|---|---|
| **Cluster Grenade** | A 40mm grenade that detects an imminent block hit ~8 ticks out and splits into 3 live sub-grenades before impact. |
| **Smoke Cluster Grenade** | Same split-before-impact behavior, but the sub-munitions are M18 smoke grenades instead of explosives. |
| **Spring Grenade** | A hand grenade (hold to cook, release to throw) that springs 1.5 blocks straight up right before it detonates, S-mine style. |
| **Throwing Knife** | A thrown blade using SuperbWarfare's generic bullet entity, so headshots get its native damage multiplier. Flies dead straight (zero gravity). |
| **Grenade Carbine** | A rifle-shaped weapon carrying two ammo types: standard rifle rounds, and an underslung "Grenade" slot that fires the Cluster Grenade. |
| **SRAW** / **SRAW Missile** | A shoulder-fired launcher whose missile is *wire-guided*: every tick in flight it curves toward wherever the shooter is currently looking, not just its initial launch angle. |

## Building

```
./gradlew build
```

Requires JDK 21 available to the toolchain. Run configs: `./gradlew runClient` / `./gradlew runServer`.

## License

GNU General Public License v3.0 (GPL-3.0-only).

## Known issues

- SRAW's launcher model occasionally crashes on render with a `NullPointerException` in GeckoLib's `GeoModel.getAnimation` (animation resource resolves to `null`) under conditions not yet isolated - root cause still open.
