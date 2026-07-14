# Guns Architecture

## 1. 项目定位

Guns 是 Level 2 客户端/服务端武器 Mod。当前保持单 Gradle 模块，并按 `registry`、`item`、`entity`、`network`、客户端控制器和客户端 mixin 分层。

## 2. 依赖与权威边界

| 区域 | 职责 |
| --- | --- |
| `Guns` | 公共入口、兼容字段、初始化顺序 |
| `registry` | 物品、实体、创造物品组注册 |
| `item` | 物品交互、稳定武器参数、Tooltip |
| `network/GunsNetworking` | C2S Payload codec 注册 |
| `network/SniperRifleServer` | 服务端接收、校验、冷却、弹道、伤害和视觉事件 |
| `client/SniperScopeController` | 本地倍镜状态、FOV 和 HUD |
| `GunsClient` | 客户端事件装配、输入路由、兼容 API |
| `client/mixin` | 原版攻击/使用、FOV、准星和持枪姿势接入 |

客户端 Payload 不是事实来源。服务端每次射击都必须重新验证当前主手武器、世界、开镜状态和冷却，且不接受客户端传入伤害、射程或目标。

## 3. 稳定标识符

- Mod：`guns`
- 物品：`sniper_rifle`、`shotgun`、`grenade_launcher`、`smg`、`flamethrower`、`railgun`、`grenade_round`
- 实体：`grenade_projectile`
- 物品组：`zyguns`
- Payload：`sniper_zoom`、`sniper_fire`、`shotgun_fire`、`grenade_launcher_fire`、`advanced_weapon_fire`
- `advanced_weapon_fire` 枚举序列：`SMG`、`FLAMETHROWER`、`RAILGUN`

以上全部位于 `guns` namespace。修改 ID 或枚举 wire order 需要协议迁移；单纯重构不得改变。

## 4. 行为基线

| 武器 | 核心参数 | 服务端冷却/耐久 |
| --- | --- | --- |
| Sniper Rifle | 伤害 `10`，射程 `128`，倍镜 `1/2/4/8/16x`，单目标 | `16` ticks，耐久 `1` |
| Shotgun | 射程 `8`，扇形 `150°`，伤害 `15 → 3`，击退 `5 → 0` | `22` ticks，耐久 `1` |
| Grenade Launcher | 速度 `1.8`，重力 `0.035`，爆炸强度 `4` | `30` ticks，耐久 `1` |
| SMG | 射程 `32`，伤害 `2`，散布 `4°`，单目标 | `4` ticks，耐久 `1` |
| Flamethrower | 射程 `8`，伤害 `2`，点燃 `3s` | `2` ticks，耐久 `1` |
| Railgun | 射程 `160`，伤害 `35`，多目标穿透至方块 | `60` ticks，耐久 `2` |

霰弹枪的 `12` 个 pellet 仅用于弹道表现；每个扇形内有效目标按距离结算一次。榴弹当前不消耗 `grenade_round`，所有枪械当前也没有生存合成配方；这些都是现有玩法契约，不在架构重构中暗改。

## 5. 状态生命周期

- 客户端：倍镜等级、use/attack 按键边沿；打开界面、换下武器或断线时清理。
- 服务端：玩家倍镜、四类冷却、advanced cooldown、短期 bullet trace；玩家断线时清理对应状态。
- 服务端 Tick 只递减有界 Map 和推进短生命周期 trace，不允许增加无界世界扫描。

## 6. 本地化与客户端表现

物品名、物品组、Tooltip、倍镜 HUD 和服务端提示均使用翻译键。Minecraft 原生语言设置选择 `en_us` 或 `zh_cn`。`verifyTranslations` 在 `check`/`build` 中验证两种语言键完全一致且值非空。

Mixin 只存在于 `client` 配置；Common 初始化和 Dedicated Server 路径不得加载任何 `net.minecraft.client.*` 类。

## 7. 验证矩阵

自动化：

~~~bash
./gradlew clean build --no-daemon --stacktrace
~~~

手工回归：

1. 单人和 Dedicated Server 均能启动，注册表无重复或缺失。
2. 六种武器的输入、冷却、伤害、耐久和粒子/声音与行为基线一致。
3. 狙击枪未开镜提示可翻译；倍镜循环、FOV、HUD、隐藏原版准星和换枪清理正常。
4. 自动武器按住连续、半自动武器按键边沿、原版挥手/挖掘拦截行为不变。
5. 断线重连后没有残留倍镜或冷却；中英文切换后全部玩家文本随语言变化。
