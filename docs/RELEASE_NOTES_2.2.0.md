# Guns v2.2.0 — Visual Arsenal Update / 视觉军械更新

## English

Guns v2.2.0 rebuilds the mod's complete visual identity while keeping its vanilla-survival progression and server-authoritative gameplay intact.

### Highlights

- Rebuilt all six weapons, five ammunition types, and four Gunsmithing items with custom cuboid models and owned `64x64` pixel-art textures matched to the approved reference style.
- Added 12 stable custom Particle registry IDs with 34 animated frames.
- Gave every weapon a distinct bounded trajectory: gray solid Sniper rounds, a gray Shotgun range fan, black SMG rounds, the existing orange Grenade arc, a Hydra-inspired continuous flame stream, and a thin white Railgun beam that fades over exactly one second.
- Removed oversized bright cross-shaped muzzle effects that obstructed the crosshair.
- Doubled the SMG base cadence from five to ten shots per second.
- Reduced the Railgun's worst-case visual composition to a verified `172` particle calls and added hard particle budgets for automated regression checks.
- Added deterministic model/texture generation, geometry and UV validation, a reusable bilingual art asset library, and both recipe posters as release assets.

### Compatibility

- Minecraft 1.21.3
- Fabric Loader 0.18.4 or newer
- Fabric API 0.114.1+1.21.3 or newer
- Java 21
- No new third-party runtime dependency, Payload change, registry ID removal, or save-data migration.
- Client and dedicated server should use the same release JAR.

Download the non-sources JAR for normal play. The release also contains a sources JAR, SHA-256 checksums, and the two bilingual recipe posters.

## 简体中文

Guns v2.2.0 在保留原版生存成长循环与服务端权威玩法的基础上，完整重制了模组的视觉体系。

### 主要更新

- 使用自定义方块模型和自有 `64x64` 像素材质，按照确认参考风格重做六把枪、五种弹药和四件枪械改装物品。
- 新增并稳定注册 12 个自定义 Particle ID，共包含 34 帧动画材质。
- 为每把武器设计独立且有硬预算的弹道：灰色实体狙击弹、灰色霰弹范围、黑色 SMG 弹粒、保留原有橙色轨迹的榴弹、Hydra 风格连续火焰流，以及精确在 1 秒内渐隐的细白色轨道炮光束。
- 移除遮挡准心的大型明亮十字枪口效果。
- 将 SMG 基础射速从每秒 5 发提高到每秒 10 发。
- 将轨道炮最坏视觉组合降至已验证的 `172` 次粒子调用，并加入自动化粒子预算回归检查。
- 新增确定性模型与材质生成、几何与 UV 校验、可复用双语素材库，并将两张配方宣传图加入 Release 资产。

### 兼容性

- Minecraft 1.21.3
- Fabric Loader 0.18.4 或更高版本
- Fabric API 0.114.1+1.21.3 或更高版本
- Java 21
- 不新增第三方运行时依赖，不修改 Payload，不删除 registry ID，也不需要迁移存档数据。
- 客户端与 Dedicated Server 应使用同一个 Release JAR。

正常游玩请下载非 sources 的 JAR。Release 同时提供 sources JAR、SHA-256 校验文件和两张双语配方宣传图。
