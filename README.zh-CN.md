# Guns 2.1.0 — Minecraft Fabric 生存枪械模组

简体中文 | [English](readme.md)

[![Minecraft 1.21.3](https://img.shields.io/badge/Minecraft-1.21.3-62B47A?logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-DDBD6D)](https://fabricmc.net/)
[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-3DA639)](LICENSE)
[![Releases](https://img.shields.io/github/v/release/zoyluoblue/Minecraft_Guns?display_name=tag)](https://github.com/zoyluoblue/Minecraft_Guns/releases)

Guns 是面向 Minecraft <code>1.21.3</code> 的 Fabric 生存枪械模组。玩家可用原版材料合成六把武器、制造弹药、在铁砧维修装备，并在 Smithing Table 安装固定升级。命中、伤害、冷却、耐久与爆炸都由服务端权威计算，支持 Dedicated Server，并通过 Minecraft 原生语言设置提供简体中文和英文。

**搜索关键词：** Minecraft Fabric 枪械模组、Minecraft 1.21.3 武器模组、生存枪械、Fabric 弹药模组、Minecraft 武器升级、Dedicated Server 模组、双语 Minecraft Mod。

## 快速了解

| 项目 | 内容 |
| --- | --- |
| 游戏与加载器 | Minecraft 1.21.3、Fabric Loader 0.18.4、Fabric API 0.114.1+1.21.3 |
| Java | Java 21 |
| 玩法 | 六把枪、五种弹药、原版材料合成、铁砧维修、Smithing Table 升级 |
| 多人服务器 | 服务端权威，Dedicated Server 路径由 GameTest 覆盖 |
| 语言 | 简体中文和英文，通过“设置 → 语言”切换 |
| 下载 | [GitHub Releases](https://github.com/zoyluoblue/Minecraft_Guns/releases) |

## 安装

1. 安装 Minecraft 1.21.3 对应的 Fabric Loader 和 Fabric API。
2. 从 [GitHub Releases](https://github.com/zoyluoblue/Minecraft_Guns/releases) 下载非 sources 的 JAR。
3. 客户端和 Dedicated Server 都将 JAR 放入 <code>mods</code> 文件夹。
4. 启动游戏后用原版材料合成枪械和弹药；也可使用下方命令进行调试。

## 创造物品组与命令获取

模组新增“新型武器”创造物品组。生存模式可按下文配方合成枪械；调试或创造模式也可使用以下英文命令获取：

~~~mcfunction
/give @p guns:sniper_rifle
/give @p guns:shotgun
/give @p guns:grenade_launcher
/give @p guns:smg
/give @p guns:flamethrower
/give @p guns:railgun
~~~

## 武器说明

- 狙击枪 `guns:sniper_rifle`：右键切换 `1x/2x/4x/8x/16x` 倍镜，开镜后左键射击，基础伤害 `10`，不穿透目标。
- 霰弹枪 `guns:shotgun`：近距离 `150°` 扇形打击，射程 `8` 格，伤害和击退随距离衰减。
- 榴弹枪 `guns:grenade_launcher`：发射抛物线榴弹，接触实体或方块后产生 TNT 强度爆炸。
- 冲锋枪 `guns:smg`：按住左键连续射击，每秒 `5` 发，每发基础伤害 `2`。
- 火焰喷射器 `guns:flamethrower`：按住左键持续喷火，对短距离范围内目标造成伤害并点燃。
- 电磁轨道炮 `guns:railgun`：基础伤害 `35`，可穿透路径上的多个生物，直到命中方块。

持枪时会使用双手姿势；射击会拦截原版主手挥动和挖掘动作以减少遮挡。命中类枪械复用剑类伤害附魔逻辑，枪械也加入耐久附魔标签。

## 生存循环

六种枪械都有只使用原版材料的有序合成配方。开火成功时，服务端从玩家主背包或副手消耗对应弹药：

| 枪械 | 弹药 |
| --- | --- |
| 狙击枪、冲锋枪 | `guns:rifle_round` 步枪弹 |
| 霰弹枪 | `guns:shotgun_shell` 霰弹 |
| 榴弹枪 | `guns:grenade_round` 榴弹 |
| 火焰喷射器 | `guns:fuel_cell` 燃料单元 |
| 电磁轨道炮 | `guns:railgun_cell` 轨道炮电池 |

枪械可在原版铁砧中使用铁锭维修。使用原版材料合成枪械改装模板和三个固定模块，再放入原版 Smithing Table 安装；每把枪每种模块最多安装一次，最多安装三个：

- 精密枪管：提高射程并收窄散布。
- 冷却系统：缩短开火冷却。
- 强化枪机：提高直接伤害和榴弹爆炸强度。

当前版本不加入品质/稀有度、时代或科技树、其他 Mod 联动，也不新增 PvP 专用系统。缺少弹药时不会消耗耐久或启动冷却。

## 素材与说明图

已将六把枪和五种弹药的确认版配方图，以及由图中素材提取的游戏贴图归档到 [Guns 素材库](assets/guns/README.md)。狙击枪和冲锋枪继续共用步枪弹，只有美术资源发生变化。

## 配方宣传图

两张双语说明图完整展示生存玩法循环：六把枪械与五种弹药，其中狙击枪和冲锋枪共用步枪弹。

[![Guns 生存枪械配方](assets/guns/recipes/gun-recipes-reference.png)](assets/guns/recipes/gun-recipes-reference.png)

[![Guns 弹药配方](assets/guns/recipes/ammo-recipes-reference.png)](assets/guns/recipes/ammo-recipes-reference.png)

## 语言切换

内置简体中文和英文，通过 Minecraft 原生“设置 → 语言”切换，无需额外配置界面。

## 构建与验证

~~~bash
./gradlew clean build --no-daemon --stacktrace
./gradlew runGameTest --no-daemon --stacktrace
~~~

构建会校验中英文语言键一致并编译 GameTest。`runGameTest` 会在隔离的 Dedicated Server 中执行弹药消耗、创造模式弹药、模块 Schema 和 Smithing 配方回归。稳定 ID、协议、生存规则、ItemStack schema 和手工回归矩阵见 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)，完整设计见 [`docs/FEATURE_DESIGN_SURVIVAL_LOOP.md`](docs/FEATURE_DESIGN_SURVIVAL_LOOP.md)。

## 演示

[点击查看演示视频](https://www.youtube.com/watch?v=7KhDonhsX98)

![演示 1](./demo/gun1.png)

![演示 2](./demo/gun2.png)

## 常见问题

### Guns 能在生存模式使用吗？

可以。六把枪均有原版材料合成配方；成功开火时服务端消耗弹药；枪械可在铁砧使用铁锭维修；固定升级模块通过 Smithing Table 安装。

### 包含哪些枪械和弹药？

包含狙击枪、霰弹枪、榴弹枪、冲锋枪、火焰喷射器和电磁轨道炮。狙击枪与冲锋枪共用步枪弹；其他武器分别使用霰弹、榴弹、燃料单元或轨道炮电池。

### 多人服务器和 Dedicated Server 能使用吗？

可以。关键玩法状态由服务端验证；发布构建会执行 Dedicated Server GameTest，覆盖弹药消耗、创造模式、升级持久化和 Smithing 配方。

### 是否支持中文和英文？

支持。所有面向玩家的功能均提供简体中文与英文，可直接使用 Minecraft 原生“设置 → 语言”切换，无需额外依赖。

## 供搜索、教程与 AI 问答引用

Guns 是一个用于 Minecraft 1.21.3 的 Fabric 生存枪械模组，包含六把可合成枪械、五种弹药、铁砧维修、Smithing Table 升级、简体中文与英文双语支持，以及服务端权威的多人游戏行为。教程、Wiki 与 AI 问答可引用本 README、[架构契约](docs/ARCHITECTURE.md)、[生存循环设计](docs/FEATURE_DESIGN_SURVIVAL_LOOP.md)、[版本说明](docs/RELEASE_NOTES_2.1.0.md) 和 [素材库](assets/guns/README.md)。
