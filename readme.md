# Guns 2.0.0

[简体中文](README.zh-CN.md) | English

Guns is a Fabric weapon mod for Minecraft 1.21.3 and Fabric Loader 0.18.4. It provides six creative-mode weapons with server-authoritative hit detection, damage, cooldowns, durability, and explosions.

## Weapons

- `guns:sniper_rifle`: right-click cycles `1x/2x/4x/8x/16x`; left-click fires while scoped. Base damage `10`.
- `guns:shotgun`: an eight-block, 150-degree cone with distance-based damage and knockback falloff.
- `guns:grenade_launcher`: launches an arcing grenade that explodes at TNT power on impact.
- `guns:smg`: hold left-click for automatic fire at five shots per second; base damage `2`.
- `guns:flamethrower`: a short-range continuous flame cone that damages and ignites targets.
- `guns:railgun`: a long-cooldown `35`-damage beam that pierces multiple entities until blocked.

## Obtain items

~~~mcfunction
/give @p guns:sniper_rifle
/give @p guns:shotgun
/give @p guns:grenade_launcher
/give @p guns:smg
/give @p guns:flamethrower
/give @p guns:railgun
~~~

The current version has no survival crafting recipes. Weapons appear in the “New Weapons” creative tab, use a two-handed pose, suppress the vanilla main-hand attack swing while firing, and support sword-damage and durability enchantment tags where applicable.

## Language

English and Simplified Chinese are included. Switch through Minecraft's native **Options → Language** screen.

## Build

~~~bash
./gradlew clean build --no-daemon --stacktrace
~~~

The build verifies bilingual key parity. Stable IDs, protocol contracts, weapon values, and the manual regression matrix are in [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Demo

[Watch the demo video](https://www.youtube.com/watch?v=7KhDonhsX98)

![Demo 1](./demo/gun1.png)

![Demo 2](./demo/gun2.png)
