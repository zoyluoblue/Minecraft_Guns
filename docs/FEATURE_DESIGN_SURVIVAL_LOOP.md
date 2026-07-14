# Guns 生存循环设计：获取、弹药、维修与固定模块

状态：已确认，进入实现

## 1. 目标与非目标

### 目标

为现有六种枪械补齐一条不依赖其他 Mod 的原版生存循环：

```text
原版材料
  -> 枪械合成
  -> 对应弹药合成
  -> 服务端消耗弹药并使用枪械
  -> 铁砧使用铁锭维修
  -> Smithing Table 安装固定模块
  -> 继续使用、维修和升级
```

### 非目标

- 不增加品质、稀有度、时代或科技树分层。
- 不新增维度、机器、能源网络、世界生成或任务系统。
- 不做跨 Mod 联动和兼容层。
- 不新增 PvP 平衡、队伍、权限或竞技规则。
- v1 不做弹匣、手动换弹、装填动画或客户端预测弹药。

## 2. 兼容契约

现有武器 registry ID、实体 ID、Payload ID、`advanced_weapon_fire` 枚举顺序和无模块时的伤害/射程/冷却/耐久行为保持不变。新逻辑只在服务端开火成功前增加弹药检查，并在 ItemStack 上追加可选模块数据。

稳定 ID 不复用或重命名：

- 枪械：`sniper_rifle`、`shotgun`、`grenade_launcher`、`smg`、`flamethrower`、`railgun`
- 已发布弹药：`grenade_round`
- 新弹药：`rifle_round`、`shotgun_shell`、`fuel_cell`、`railgun_cell`
- 模块：`precision_barrel`、`cooling_system`、`reinforced_receiver`
- Smithing 模板：`upgrade_template`
- 自定义 recipe serializer：`guns:smithing_upgrade`

## 3. 生存获取配方

所有配方使用 `minecraft:crafting_shaped`，材料只来自原版；枪械仍保留在 `zyguns` 创造物品组中。

| 产物 | 主要原版材料 | 产量 |
| --- | --- | ---: |
| 狙击枪 | 铁锭、铜锭、望远镜、红石 | 1 |
| 霰弹枪 | 铁锭、弓、火药 | 1 |
| 榴弹枪 | 铁锭、铜锭、弓、火药 | 1 |
| 冲锋枪 | 铁锭、铜锭、红石 | 1 |
| 火焰喷射器 | 铁锭、煤炭、红石 | 1 |
| 电磁轨道炮 | 铁锭、钻石、铜锭、红石 | 1 |

具体图案以 `src/main/resources/data/guns/recipe/` 为唯一事实来源；文档只记录设计意图，不在 Java 中复制配方规则。

## 4. 弹药模型

服务端根据当前主手枪械决定弹药类型，客户端不能指定弹药、数量或扣除结果。v1 从玩家主背包和副手查找一枚弹药；创造模式不扣弹药，但仍沿用原有耐久豁免。

| 枪械 | 弹药 | 产量 |
| --- | --- | ---: |
| 狙击枪、冲锋枪 | `rifle_round` | 8 |
| 霰弹枪 | `shotgun_shell` | 8 |
| 榴弹枪 | `grenade_round` | 4 |
| 火焰喷射器 | `fuel_cell` | 8 |
| 电磁轨道炮 | `railgun_cell` | 4 |

扣除顺序固定为：验证世界与主手枪械 -> 验证开镜/冷却 -> 服务端扣除一枚弹药 -> 结算伤害、实体或视觉 -> 扣除枪械耐久并写入冷却。缺少弹药时不改变伤害、耐久、冷却或世界状态，并通过 actionbar 显示双语提示。

## 5. 铁砧维修

六种枪械统一使用铁锭作为原版 repairable ingredient。维修由原版 AnvilScreenHandler 负责，保留原版经验消耗、耐久合并和附魔处理；模组不新增维修界面、不在服务端重复扣除材料。

## 6. 固定模块

模块没有等级、品质和科技树；每个模块在同一把枪上最多安装一次，最多安装 3 个模块。模块效果只通过服务端 `GunUpgradeService` 读取，未安装模块时所有倍率为 1。

| 模块 | 效果 |
| --- | --- |
| 精密枪管 `precision_barrel` | 射程 ×1.20；散布/扇形角度 ×0.75；榴弹初速度 ×1.10 |
| 冷却系统 `cooling_system` | 武器冷却时间 ×0.80，最低 1 tick |
| 强化枪机 `reinforced_receiver` | 直接伤害和榴弹爆炸强度 ×1.15 |

Smithing Table 三个槽位固定为：`upgrade_template`、枪械、模块。自定义 recipe 只接受 `guns:upgradeable` 标签中的六种枪械，并在输出时复制基础 ItemStack，再追加新模块，因此已有耐久、附魔、自定义名称和旧模块不会丢失。

## 7. ItemStack Schema

模块数据存储在原版 `minecraft:custom_data`，不保存翻译后的文本：

```nbt
{
  "GunsUpgrades": {
    "schema": 1,
    "modules": ["guns:precision_barrel", "guns:cooling_system"]
  }
}
```

读入时未知模块只忽略其效果，写入时保留原始 ID；已知模块按稳定 ID 匹配。schema 高于当前版本时拒绝继续安装模块，避免旧版本破坏新数据。未来 schema 迁移必须新增显式迁移函数，不在业务逻辑中静默改写。

## 8. 服务端安全与生命周期

- 所有弹药、冷却、伤害、射程、模块效果和耐久修改均在服务端主线程执行。
- `advanced_weapon_fire` 仍只接受稳定枚举；服务端从主手 ItemStack 推导枪械和弹药。
- 缺弹反馈节流，按玩家清理；玩家断线时清除反馈状态。
- 不新增 tick 世界扫描；只维护有界的玩家反馈冷却 Map。
- 不引入新的第三方依赖、Gradle 子模块或 Mixin。

## 9. 验收矩阵

| 需求 | 自动验证 | 手工验证 |
| --- | --- | --- |
| 六种枪械可生存合成 | JSON/resource 校验、JAR 清单 | 新存档中按配方合成 |
| 弹药映射和扣除 | `runGameTest`、代码级检查 | 每种枪有弹/无弹、创造模式、自动武器连续射击 |
| 榴弹消耗 `grenade_round` | 服务器逻辑回归 | 发射前后背包数量和实体生成 |
| 铁砧维修 | registry/component 检查 | 六种枪损坏后用铁锭维修，附魔和模块保留 |
| Smithing 模块叠加 | `runGameTest`、recipe serializer/resource 校验 | 安装 1、2、3 个模块；重复模块拒绝；模块和耐久不丢失 |
| 双语 | `verifyTranslations` | 中文/英文切换后物品名、tooltip、缺弹提示和模块名变化 |
| 兼容性 | build、Dedicated Server smoke | 无模块枪械逐项回归现有射程、伤害、冷却和输入 |
