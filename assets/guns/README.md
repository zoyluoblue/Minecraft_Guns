# Guns Mod Art Asset Library / Guns Mod 素材库

This library preserves the approved recipe-poster source and the exact derived game textures for GitHub promotion and teaching.

本素材库保存已确认的配方海报源图，以及从源图提取并接入游戏的贴图，供 GitHub 宣传和教学使用。

## Approved source posters / 已确认源图

![Weapon recipes](recipes/gun-recipes-reference.png)

![Ammo recipes](recipes/ammo-recipes-reference.png)

The weapon textures are derived from the six icons in the first poster. The ammunition textures are derived from the five result icons in the second poster.

枪械贴图取自第一张海报中的六把枪图标；弹药贴图取自第二张海报中的五种弹药成品图标。

## In-game texture source / 游戏贴图源

![Reference-derived sprite sheet](sprites/reference-derived-sprite-sheet.png)

The 3-column by 4-row source sheet contains the items in this order: Sniper Rifle, Shotgun, Grenade Launcher; SMG, Flamethrower, Railgun; Rifle Round, Shotgun Shell, Grenade Round; Fuel Cell, Railgun Cell, empty. The game uses the corresponding cropped 16x16 PNG textures from src/main/resources/assets/guns/textures/item.

该 3 列 × 4 行源图依次包含：狙击枪、霰弹枪、榴弹枪；冲锋枪、火焰喷射器、轨道炮；步枪弹、霰弹、榴弹；燃料单元、轨道炮电池、空位。游戏使用 src/main/resources/assets/guns/textures/item 下对应裁切后的 16×16 PNG。

## Compatibility / 兼容性

This is a visual-only update. Registry IDs, recipes, server-side ammunition rules, 3D model geometry, network payloads, and save data remain unchanged.

本次只更新美术资源；registry ID、配方、服务端弹药规则、3D 模型几何、网络 Payload 和存档数据均保持不变。
