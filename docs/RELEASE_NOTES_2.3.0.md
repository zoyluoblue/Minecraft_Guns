# Guns v2.3.0 — Heavy Laser Update / 重型激光更新

## English

Guns v2.3.0 makes the Railgun feel like a true heavy laser, doubles the Sniper Rifle's visual projectile speed, and gives the Shotgun a much clearer expanding pellet pattern. Damage, survival progression, ammunition, and server-authoritative hit detection remain unchanged.

### Added

- Added a Railgun laser-audio composition using Minecraft's Guardian attack and Beacon activation sounds.

### Changed

- Rebuilt the Railgun trajectory as a large white laser with a bright core, soft halo, and an exact three-second fade.
- Doubled the Sniper Rifle's gray-round visual velocity from `0.055` to `0.110` blocks per tick.
- Expanded the Shotgun from seven faint traces to 13 moving visual pellets: one center pellet, six inner-ring pellets, and six outer-ring pellets.
- Increased Shotgun range-particle size, opacity, contrast, and movement so the spread is readable in first and third person.

### Fixed

- Kept the large Railgun beam away from the camera with a distance-aware muzzle offset.
- Separated small white Railgun impact particles from the large beam texture so entity and block hits do not create oversized flashes.

### Performance

- Shotgun visual composition is bounded to a verified worst case of `91` particle calls with a hard limit of `96`.
- Railgun visual composition is bounded to a verified worst case of `204` particle calls with a hard limit of `208`.
- Railgun beam lifetime and base cooldown are both 60 ticks, preventing unbounded beam accumulation.

### Compatibility

- Minecraft 1.21.3
- Fabric Loader 0.18.4 or newer
- Fabric API 0.114.1+1.21.3 or newer
- Java 21
- No damage, range, cooldown, Payload, save-schema, dependency, item/entity/recipe ID, or Particle registry ID changes.

### Migration

No migration is required. Clients and Dedicated Servers must use the same `2.3.0` JAR. Existing worlds and upgraded weapons remain compatible.

### Known Issues

No functional regression is currently known. Perceived laser size and volume vary with FOV, camera distance, particle settings, and audio settings.

Download the non-sources JAR for normal play. The Release also includes a sources JAR, SHA-256 checksums, and both bilingual recipe posters.

## 简体中文

Guns v2.3.0 将电磁轨道炮升级为真正的重型激光，提高狙击枪弹粒视觉速度，并为霰弹枪加入更清晰的扩散弹丸表现。伤害、生存循环、弹药和服务端权威命中判定均保持不变。

### 新增

- 使用 Minecraft 原版 Guardian 攻击音效与 Beacon 激活音效，为电磁轨道炮加入激光音效组合。

### 变更

- 将电磁轨道炮弹道重做为带明亮核心和柔和外晕的巨大白色激光，并精确在 3 秒内渐隐。
- 将狙击枪灰色弹粒视觉速度从每 tick `0.055` 格提高到 `0.110` 格。
- 将霰弹枪从 7 条较弱轨迹扩展为 13 枚运动视觉弹粒：中心 1 枚、内环 6 枚、外环 6 枚。
- 提高霰弹范围粒子的尺寸、透明度、对比度和移动速度，使第一人称与第三人称都能清楚看到扩散。

### 修复

- 使用随命中距离调整的枪口偏移，让大型轨道炮光束与摄像机保持安全距离。
- 将小型白色命中粒子与大型光束材质分离，避免实体或方块命中产生过大的闪光。

### 性能

- 霰弹视觉组合最坏为已验证的 `91` 次粒子调用，硬上限 `96` 次。
- 轨道炮视觉组合最坏为已验证的 `204` 次粒子调用，硬上限 `208` 次。
- 轨道炮光束寿命和基础冷却均为 60 ticks，不会产生无界光束叠加。

### 兼容性

- Minecraft 1.21.3
- Fabric Loader 0.18.4 或更高版本
- Fabric API 0.114.1+1.21.3 或更高版本
- Java 21
- 不改变伤害、射程、冷却、Payload、存档 Schema、依赖、物品/实体/配方 ID 或 Particle registry ID。

### 迁移

不需要迁移。客户端与 Dedicated Server 必须使用相同的 `2.3.0` JAR；已有世界和已改装枪械保持兼容。

### 已知问题

当前未发现功能回归。激光的视觉尺寸和音量会受到 FOV、观察距离、粒子设置与音量设置影响。

正常游玩请下载非 sources 的 JAR。Release 同时提供 sources JAR、SHA-256 校验文件和两张双语配方宣传图。
