# ADR-0001：生存循环的 ItemStack 数据与 Smithing Table 实现

状态：Accepted

## 背景

Guns 需要在不增加工作站和 Mixin 的前提下支持同一把枪安装多个固定模块。原版 1.21.3 的 Smithing Transform recipe 会复制基础物品的组件，但会应用静态结果组件；用纯 JSON 静态结果无法安全地把第二个模块追加到已有 `CUSTOM_DATA`。

## 决策

1. 模块状态放在 `DataComponentTypes.CUSTOM_DATA`，根节点使用 `GunsUpgrades`，包含 `schema` 和稳定模块 ID 列表。
2. 注册 `guns:smithing_upgrade` recipe serializer，并实现一个属于原版 `RecipeType.SMITHING` 的 `SmithingRecipe`。
3. 自定义 recipe 的 `craft` 复制基础 ItemStack，再由集中式 `GunUpgradeService` 追加模块；因此耐久、附魔、名称和已安装模块都可保留。
4. 铁砧维修使用 `Item.Settings.repairable(Items.IRON_INGOT)`，交给原版流程处理。

## 备选方案

### 直接使用原版 `smithing_transform`

不采用。静态 result 会覆盖自定义组件，重复升级时可能丢失已有模块；为此再增加 Mixin 会扩大客户端/服务端风险。

### 增加自定义升级界面或 ScreenHandler

不采用。用户要求使用原版 Smithing Table；自定义界面会增加网络协议、客户端类加载和同步状态。

### 使用独立文件保存每把枪的模块

不采用。模块是 ItemStack 的固有状态，使用原版 component 能随箱子、玩家、掉落物和交易自然复制与保存。

## 后果

- 好处：无新增依赖、无 Mixin、原版工作站体验、模块可叠加、旧枪械没有模块时保持兼容。
- 代价：需要注册自定义 recipe serializer，并为 schema 维护读取和未来迁移边界。
- 兼容要求：`GunsUpgrades`、`schema`、`modules` 和模块 ID 一旦发布即视为存档契约。

