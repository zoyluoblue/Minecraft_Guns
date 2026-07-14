# Guns Visual Rebuild

## Goal

Replace every Guns item visual with an internally owned, Minecraft-native low-poly model and matching texture. The six weapon silhouettes and their main color language follow the approved bilingual weapon and ammunition recipe posters. No previous Guns item texture, model, or vanilla item texture reference remains in the shipped item resources.

## Scope

| Group | Resources |
| --- | --- |
| Weapons | `sniper_rifle`, `shotgun`, `grenade_launcher`, `smg`, `flamethrower`, `railgun` |
| Ammunition | `rifle_round`, `shotgun_shell`, `grenade_round`, `fuel_cell`, `railgun_cell` |
| Gunsmithing | `upgrade_template`, `precision_barrel`, `cooling_system`, `reinforced_receiver` |
| Projectile | `grenade_round` model and texture are also used by `GrenadeProjectileEntity` through `ThrownItemEntity` rendering. |

## Visual Contract

- Each item has one detailed `64x64` RGBA texture owned by this repository. Weapon and ammunition palettes, silhouettes, dark pixel outlines, highlights, and shadow ramps follow the approved [`assets/guns/references/guns-ammo-style-reference.png`](../assets/guns/references/guns-ammo-style-reference.png) source, archived with SHA-256 `1bda78d81641ddaf7637a7227210d39bb019274b462441afdffb2f9aee9b5419`.
- Each item has a custom cuboid JSON model; no item model uses `minecraft:item/*` textures or the `minecraft:item/generated` parent.
- Weapon models use distinct barrels, receivers, grips, stocks, scopes, tanks, coils, or rails appropriate to their gameplay role.
- Ammunition and gunsmithing parts are physical low-poly objects rather than flat icons.
- Every model maps its visible faces to its own texture palette. Texture palettes use a dark outline, material shading, and item-specific primary/accent colors.
- `gui`, ground, fixed, third-person, and first-person transforms are explicit. First-person transforms prioritize readable silhouettes without changing gameplay input or weapon behavior.

## Non-goals

- No registry, recipe, language key, balance, network, save-data, mixin, or entity behavior changes.
- No third-party model loader, runtime dependency, or resource-pack dependency.
- Item models remain static; ballistic particles are designed separately in `FEATURE_DESIGN_BALLISTICS_VISUALS.md`.

## Asset Direction

| Item | Silhouette and palette |
| --- | --- |
| Sniper Rifle | Long blue receiver, cyan/blue/silver scope, silver muzzle, gold stock, brown grip. |
| Shotgun | Compact blue body, orange muzzle and grip, gold top sight. |
| Grenade Launcher | Purple tube body, lime muzzle and rear cap, orange top sight. |
| SMG | Short blue receiver, gold muzzle and rear frame, orange grip. |
| Flamethrower | Orange housing, white nozzle, blue fuel tank. |
| Railgun | Indigo front/body, cyan twin coils, orange top switch, cyan/green side core and green rear cap. |
| Ammunition | Blue/gold rifle round, red/brass shell, green grenade round, cyan fuel cell, purple railgun cell. |
| Gunsmithing | Brass template, blue precision barrel, cyan cooling unit, reinforced orange receiver. |

## Verification

1. Resource build validates all model JSON and packages every `64x64` RGBA texture.
2. Production JAR contains the fifteen model files and fifteen item textures, with no `minecraft:item/` texture reference in Guns item models.
3. GameTest retains gameplay coverage; resource changes do not alter server-side rules.
4. Manual client verification checks the inventory, dropped-item, third-person, first-person, and grenade-flight presentation for clipping and silhouette readability.
