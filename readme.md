# Guns 2.3.0 — Minecraft Fabric Gun Mod

[简体中文](README.zh-CN.md) | English

[![Minecraft 1.21.3](https://img.shields.io/badge/Minecraft-1.21.3-62B47A?logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-DDBD6D)](https://fabricmc.net/)
[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-3DA639)](LICENSE)
[![Releases](https://img.shields.io/github/v/release/zoyluoblue/Minecraft_Guns?display_name=tag)](https://github.com/zoyluoblue/Minecraft_Guns/releases)

Guns is a survival-first, server-authoritative Minecraft Fabric gun mod for Minecraft 1.21.3. Craft six weapons from vanilla materials, manufacture ammunition, repair worn equipment in an Anvil, and install fixed upgrades in a Smithing Table. It works on dedicated servers and includes English plus Simplified Chinese through Minecraft's native language setting.

**Search keywords:** Minecraft Fabric gun mod, Minecraft 1.21.3 weapons, survival gun mod, Fabric ammunition mod, Minecraft weapon upgrades, custom gun particles, pixel art gun models, dedicated server safe mod, bilingual Minecraft mod.

## At a glance

| Topic | Details |
| --- | --- |
| Game and loader | Minecraft 1.21.3, Fabric Loader 0.18.4, Fabric API 0.114.1+1.21.3 |
| Java | Java 21 |
| Gameplay | Six weapons, five ammunition types, vanilla-material recipes, Anvil repair, Smithing Table upgrades |
| Multiplayer | Server authoritative; dedicated-server path is covered by GameTest |
| Languages | English and Simplified Chinese, switched through Options → Language |
| Downloads | [GitHub Releases](https://github.com/zoyluoblue/Minecraft_Guns/releases) |

## Install

1. Install Fabric Loader for Minecraft 1.21.3 and the matching Fabric API.
2. Download the non-sources JAR from [GitHub Releases](https://github.com/zoyluoblue/Minecraft_Guns/releases).
3. Put the JAR in the <code>mods</code> folder on every client and on the dedicated server.
4. Start Minecraft, then craft weapons and ammunition with vanilla materials or use the commands below for testing.

## Weapons

- `guns:sniper_rifle`: right-click cycles `1x/2x/4x/8x/16x`; left-click fires while scoped. Base damage `10`.
- `guns:shotgun`: an eight-block, 150-degree cone with distance-based damage and knockback falloff.
- `guns:grenade_launcher`: launches an arcing grenade that explodes at TNT power on impact.
- `guns:smg`: hold left-click for automatic fire at ten shots per second; base damage `2`.
- `guns:flamethrower`: a short-range continuous flame cone that damages and ignites targets.
- `guns:railgun`: a long-cooldown `35`-damage beam that pierces multiple entities until blocked.

## Ballistic visual system

Each weapon has a distinct server-synchronized visual language. Damage, range, spread, gravity, and explosion rules remain unchanged; the SMG base cadence is intentionally doubled from five to ten shots per second.

| Weapon | Muzzle, trajectory, and impact identity |
| --- | --- |
| Sniper Rifle | Compact opaque gray rounds moving at twice the previous visual velocity, with no bright cross-shaped muzzle flash |
| Shotgun | Thirteen moving gray pellets form a readable center/inner/outer spread fan with stronger range traces |
| Grenade Launcher | Visible projectile with orange embers, sparse smoke, and an impact shockwave |
| SMG | Compact opaque black rounds at ten shots per second with restrained impact feedback |
| Flamethrower | Six moving flame particles per emission overlap into a continuous Hydra-style fire stream |
| Railgun | A large white laser with a bright core, soft halo, Guardian/Beacon energy audio, and an exact 60-tick (three-second) fade |

Twelve owned custom Particle registry IDs and 34 animated pixel-art frames power these effects. The original seven IDs remain registered for compatibility, while five dedicated IDs provide gray rounds, a gray range fan, black rounds, moving flame jets, and the white beam. Water intersections switch to bubbles. Every effect has a hard sampling budget: the expanded Shotgun fan is bounded to `91` calls, while a maximum-range Railgun laser with four visible entity impacts is bounded to `204` calls and a hard limit of `208`.

## Survival loop

All six weapons have shaped recipes made from vanilla materials. Each weapon consumes ammunition from the server-side player inventory when a shot succeeds:

| Weapons | Ammunition |
| --- | --- |
| Sniper Rifle, SMG | `guns:rifle_round` |
| Shotgun | `guns:shotgun_shell` |
| Grenade Launcher | `guns:grenade_round` |
| Flamethrower | `guns:fuel_cell` |
| Railgun | `guns:railgun_cell` |

Weapons are repaired with iron ingots in a vanilla Anvil. The Gunsmithing Template and three fixed modules are crafted from vanilla materials and installed in a vanilla Smithing Table. A weapon can hold one of each module, up to three total:

- Precision Barrel: increases range and tightens spread.
- Cooling System: reduces firing cooldown.
- Reinforced Receiver: increases direct damage and grenade explosion power.

There are no rarity tiers, technology ages, external-Mod integrations, or PvP-specific systems in this version.

## Art assets

The approved weapon and ammunition recipe posters plus the exact gun/ammunition style sheet are archived in the [Guns art asset library](assets/guns/README.md) for promotion, teaching, and future QA. The game uses a fully owned visual rebuild: detailed custom cuboid models and matching `64x64` material textures for six weapons, five ammunition types, the Gunsmithing Template, and three upgrade modules. Sniper Rifle and SMG still share Rifle Round; visuals do not change gameplay rules.

[![Approved Guns weapon and ammunition pixel-art style](assets/guns/references/guns-ammo-style-reference.png)](assets/guns/references/guns-ammo-style-reference.png)

## Recipe posters

These two bilingual posters show the complete survival crafting loop: six weapons and five ammunition types, including the shared Rifle Round used by the Sniper Rifle and SMG.

[![Guns survival weapon recipes](assets/guns/recipes/gun-recipes-reference.png)](assets/guns/recipes/gun-recipes-reference.png)

[![Guns ammunition recipes](assets/guns/recipes/ammo-recipes-reference.png)](assets/guns/recipes/ammo-recipes-reference.png)

## Obtain items

~~~mcfunction
/give @p guns:sniper_rifle
/give @p guns:shotgun
/give @p guns:grenade_launcher
/give @p guns:smg
/give @p guns:flamethrower
/give @p guns:railgun
~~~

Weapons, ammunition, the template, and modules appear in the “New Weapons” creative tab. Weapons use a two-handed pose, suppress the vanilla main-hand attack swing while firing, and support sword-damage and durability enchantment tags where applicable. Missing ammunition never consumes durability or starts a cooldown.

## Language

English and Simplified Chinese are included. Switch through Minecraft's native **Options → Language** screen.

## Build

~~~bash
./gradlew clean build --no-daemon --stacktrace
./gradlew runGameTest --no-daemon --stacktrace
~~~

The build verifies bilingual key parity, gameplay resources, all 15 owned item visuals, 12 Particle definitions, and all 34 particle frames. `runGameTest` executes ammunition consumption, creative-mode ammunition, upgrade schema, Smithing recipe, and ballistic-budget regressions in an isolated dedicated server. Stable IDs, protocol contracts, survival rules, ItemStack schema, and the manual regression matrix are in [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md). Detailed designs are in [`docs/FEATURE_DESIGN_SURVIVAL_LOOP.md`](docs/FEATURE_DESIGN_SURVIVAL_LOOP.md), [`docs/FEATURE_DESIGN_VISUAL_REBUILD.md`](docs/FEATURE_DESIGN_VISUAL_REBUILD.md), and [`docs/FEATURE_DESIGN_BALLISTICS_VISUALS.md`](docs/FEATURE_DESIGN_BALLISTICS_VISUALS.md).

## Release automation

Push an annotated `vX.Y.Z` tag to publish a new GitHub Release automatically. The workflow requires the tag version to match `mod_version` in `gradle.properties` and a bilingual `docs/RELEASE_NOTES_X.Y.Z.md` file. It then runs a clean build and the dedicated-server GameTests, validates the release JAR, and uploads the production JAR, sources JAR, SHA256 checksum, and both recipe posters. A failed check creates no Release.

## Demo

[Watch the demo video](https://www.youtube.com/watch?v=7KhDonhsX98)

## Frequently asked questions

### Can Guns be used in Survival mode?

Yes. All six weapons have vanilla-material crafting recipes. Successful shots consume server-side ammunition, weapons are repaired with iron ingots in an Anvil, and fixed upgrade modules are installed through a Smithing Table.

### Which weapons and ammunition are included?

The mod includes Sniper Rifle, Shotgun, Grenade Launcher, SMG, Flamethrower, and Railgun. Sniper Rifle and SMG share Rifle Round; the other weapons use Shotgun Shell, Grenade Round, Fuel Cell, or Railgun Cell.

### Is Guns safe for multiplayer and dedicated servers?

Gameplay state is validated on the server. The release build is tested with a dedicated-server GameTest suite covering ammunition consumption, Creative mode, upgrade persistence, and Smithing recipes.

### Does Guns support Chinese and English?

Yes. Every player-facing feature ships in English and Simplified Chinese. Switch languages through Minecraft's built-in Options → Language screen without an additional dependency.

## For search, guides, and AI answers

Guns is a Minecraft 1.21.3 Fabric survival weapons mod with six craftable guns, five ammunition types, Anvil repairs, Smithing Table upgrades, custom bounded ballistic particles, detailed pixel-art-inspired models, bilingual Chinese and English localization, and server-authoritative multiplayer behavior. Guides may cite this README, the [architecture contract](docs/ARCHITECTURE.md), the [survival-loop design](docs/FEATURE_DESIGN_SURVIVAL_LOOP.md), the [ballistic visual design](docs/FEATURE_DESIGN_BALLISTICS_VISUALS.md), the [release notes](docs/RELEASE_NOTES_2.3.0.md), and the [art asset library](assets/guns/README.md).
