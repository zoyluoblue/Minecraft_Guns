# Guns Architecture

## 1. 项目定位

Guns 是 Level 2 客户端/服务端武器 Mod。当前保持单 Gradle 模块，并按 `registry`、`item`、`ammo`、`upgrade`、`recipe`、`entity`、`network`、`visual`、客户端控制器和客户端 mixin 分层。生存扩展优先复用原版 crafting、Anvil 和 Smithing Table，不引入机器或跨 Mod 依赖。

## 2. 依赖与权威边界

| 区域 | 职责 |
| --- | --- |
| `Guns` | 公共入口、兼容字段、初始化顺序 |
| `registry` | 物品、实体、创造物品组注册 |
| `item` | 物品交互、稳定武器参数、Tooltip |
| `ammo` | 服务端弹药类型映射、背包查找、扣除和缺弹反馈节流 |
| `upgrade` | 固定模块 ID、ItemStack schema、模块效果和 Tooltip |
| `recipe` | `guns:smithing_upgrade` SmithingRecipe 及 serializer |
| `network/GunsNetworking` | C2S Payload codec 注册 |
| `network/SniperRifleServer` | 服务端接收、校验、冷却、射线、伤害和视觉事件入口 |
| `visual/BallisticsVisuals` | 服务端权威的有界粒子采样、水下降级和六类武器视觉编排 |
| `registry/ModParticles` | 十二个稳定 Particle registry ID 的 Common 注册 |
| `client/GunParticle` | 自定义 Sprite 粒子的生命周期、缩放、淡出、旋转与动画 |
| `client/SniperScopeController` | 本地倍镜状态、FOV 和 HUD |
| `GunsClient` | 客户端事件装配、输入路由、兼容 API |
| `client/mixin` | 原版攻击/使用、FOV、准星和持枪姿势接入 |

客户端 Payload 不是事实来源。服务端每次射击都必须重新验证当前主手武器、世界、开镜状态和冷却，且不接受客户端传入伤害、射程或目标。

## 3. 稳定标识符

- Mod：`guns`
- 物品：`sniper_rifle`、`shotgun`、`grenade_launcher`、`smg`、`flamethrower`、`railgun`、`grenade_round`
- 弹药：`rifle_round`、`shotgun_shell`、`fuel_cell`、`railgun_cell`
- 改装：`upgrade_template`、`precision_barrel`、`cooling_system`、`reinforced_receiver`
- 实体：`grenade_projectile`
- 物品组：`zyguns`
- Recipe serializer：`smithing_upgrade`
- Payload：`sniper_zoom`、`sniper_fire`、`shotgun_fire`、`grenade_launcher_fire`、`advanced_weapon_fire`
- `advanced_weapon_fire` 枚举序列：`SMG`、`FLAMETHROWER`、`RAILGUN`
- Particle：`tracer`、`muzzle_flash`、`impact_spark`、`flame_core`、`energy_arc`、`blast_wave`、`shockwave`、`gray_round`、`gray_range`、`black_round`、`flame_jet`、`white_beam`

以上全部位于 `guns` namespace。修改 ID 或枚举 wire order 需要协议迁移；单纯重构不得改变。

## 4. 行为基线

| 武器 | 核心参数 | 服务端冷却/耐久 |
| --- | --- | --- |
| Sniper Rifle | 伤害 `10`，射程 `128`，倍镜 `1/2/4/8/16x`，单目标 | `16` ticks，耐久 `1` |
| Shotgun | 射程 `8`，扇形 `150°`，伤害 `15 → 3`，击退 `5 → 0` | `22` ticks，耐久 `1` |
| Grenade Launcher | 速度 `1.8`，重力 `0.035`，爆炸强度 `4` | `30` ticks，耐久 `1` |
| SMG | 射程 `32`，伤害 `2`，散布 `4°`，单目标，每秒 `10` 发 | `2` ticks，耐久 `1` |
| Flamethrower | 射程 `8`，伤害 `2`，点燃 `3s` | `2` ticks，耐久 `1` |
| Railgun | 射程 `160`，伤害 `35`，多目标穿透至方块 | `60` ticks，耐久 `2` |

霰弹枪的 `7` 条 tracer 仅用于弹道表现；每个扇形内有效目标仍按距离结算一次。无模块时，所有枪械继续使用上述原始行为参数。生存模式下服务端从主背包或副手扣除对应弹药；创造模式不扣弹药。铁砧使用铁锭维修，Smithing Table 使用 `upgrade_template` 和固定模块进行最多三次不同模块安装。

### 弹道视觉不变量

- 命中、伤害、穿透、爆炸和终点由服务端计算；粒子只表现结果，不参与玩法判定。
- 狙击枪使用紧凑灰色实体弹粒，不保存或推进命中后的假弹丸。
- 霰弹枪使用 7 条、每条最多 4 点的灰色扇形范围表现；SMG 使用黑色实体弹粒且不生成枪口大闪光。
- 火焰喷射器每次只从枪口发射 6 个有速度和寿命的粒子，由连续射击叠加成稳定火流，不预铺整条静态锥体。
- 轨道炮只生成细白光束，客户端生命周期固定 `20` ticks 并逐 tick 淡出，不生成青色枪口环或螺旋电弧。
- 空气中使用 Guns 自定义粒子；采样点落入水体时改用气泡或弱化能量表现。
- 不新增 Guns Payload，复用 Minecraft 原生服务器粒子同步。
- 单次硬上限：Sniper `80` 个采样，Shotgun `7 × 4`，SMG `12`，Flamethrower `6` 个移动粒子，Grenade trail 每 tick `3` 个调用，Railgun 在 4 个可见实体命中点下最坏 `172` 个调用且绝不超过 `192`。SMG 在每秒 10 发时仍限制为最多 `120` 次粒子调用/秒。
- 所有视觉计算只处理当前射击的常量规模数据，不创建跨 tick 队列、不强制加载区块。

### 弹药映射

| 枪械 | 服务端弹药 |
| --- | --- |
| Sniper Rifle、SMG | `rifle_round` |
| Shotgun | `shotgun_shell` |
| Grenade Launcher | `grenade_round` |
| Flamethrower | `fuel_cell` |
| Railgun | `railgun_cell` |

缺弹时只发送翻译消息，不消耗枪械耐久、不设置冷却、不创建实体或伤害。

### 模块数据

模块使用 `minecraft:custom_data` 的 `GunsUpgrades` 根节点，当前 schema 为 `1`：

~~~nbt
{
  "GunsUpgrades": {
    "schema": 1,
    "modules": ["guns:precision_barrel", "guns:cooling_system"]
  }
}
~~~

模块 ID 是持久化契约。`GunUpgradeService` 集中负责读取、重复模块拒绝、最多三模块限制和效果倍率；schema 高于当前版本时不继续写入，未知模块 ID 不参与效果计算。

## 5. 状态生命周期

- 客户端：倍镜等级、use/attack 按键边沿；打开界面、换下武器或断线时清理。
- 服务端：玩家倍镜、四类冷却、advanced cooldown 和缺弹反馈节流；玩家断线/停服时清理对应状态。
- 服务端 Tick 只递减有界 Map；弹道视觉不保留跨 tick 状态，也不增加无界世界扫描。

## 6. 本地化与客户端表现

物品名、物品组、Tooltip、倍镜 HUD 和服务端提示均使用翻译键。Minecraft 原生语言设置选择 `en_us` 或 `zh_cn`。`verifyTranslations` 在 `check`/`build` 中验证两种语言键完全一致且值非空。

Mixin 只存在于 `client` 配置；Common 初始化和 Dedicated Server 路径不得加载任何 `net.minecraft.client.*` 类。

## 7. 验证矩阵

自动化：

~~~bash
./gradlew clean build --no-daemon --stacktrace
./gradlew runGameTest --no-daemon --stacktrace
~~~

`src/gametest` 是独立测试源集，不会进入生产 JAR；GameTest 在隔离 Dedicated Server 中覆盖弹药扣除、创造模式免扣、模块数量/Schema 边界、Smithing 配方保留已有模块，以及弹道采样硬上限和轨道炮 20 tick 淡出契约。`verifyVisualAssets` 验证十五件物品均使用自有 `64x64` RGBA 贴图、精细方块模型和完整手持展示变换，且不允许回退到原版物品贴图或 `generated` 扁平模型。`verifyBallisticsResources` 验证十二个 Particle ID、定义 JSON、34 帧 PNG、资源清单和 Dedicated Server 类隔离。

手工回归：

1. 单人和 Dedicated Server 均能启动，注册表无重复或缺失。
2. 六种武器的输入、冷却、伤害、耐久和粒子/声音与行为基线一致。
3. 狙击枪未开镜提示可翻译；倍镜循环、FOV、HUD、隐藏原版准星和换枪清理正常。
4. 自动武器按住连续、半自动武器按键边沿、原版挥手/挖掘拦截行为不变。
5. 断线重连后没有残留倍镜或冷却；中英文切换后全部玩家文本随语言变化。
6. 生存模式中六种枪械、五类弹药、改装模板和三个模块的原版配方可加载；铁砧可用铁锭修复六种枪械。
7. Smithing Table 安装模块后保留耐久、附魔、自定义名称和已有模块；重复模块、第四个模块或高版本 schema 不产生输出。
8. 六把枪在第一/第三人称、近距离、最大射程、水下和多人旁观视角中的枪口、弹道与命中特效可读，且不会遮满屏幕。
