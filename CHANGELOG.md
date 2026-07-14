# Changelog / 更新日志

All notable changes are documented here. Versions follow Semantic Versioning.

所有重要变更均记录于此。版本号遵循 Semantic Versioning。

## 2.3.0 — 2026-07-14

### Added / 新增

- Added a Railgun laser-audio composition using Minecraft's Guardian attack and Beacon activation sounds.
- 使用 Minecraft 原版 Guardian 攻击音效与 Beacon 激活音效，为电磁轨道炮加入激光音效组合。

### Changed / 变更

- Rebuilt the Railgun trajectory as a large white laser with a bright core, soft halo, and an exact three-second fade.
- Doubled the Sniper Rifle's gray-round visual velocity from `0.055` to `0.110` blocks per tick without changing hitscan gameplay.
- Expanded the Shotgun from seven faint visual traces to 13 moving pellets in a center/inner/outer spread pattern, with stronger gray range textures and a bounded worst case of `91` particle calls.

- 将电磁轨道炮弹道重做为带明亮核心和柔和外晕的巨大白色激光，并精确在 3 秒内渐隐。
- 将狙击枪灰色弹粒视觉速度从每 tick `0.055` 格提高到 `0.110` 格，不改变 hitscan 玩法判定。
- 将霰弹枪从 7 条较弱视觉轨迹扩展为中心、内环和外环组成的 13 枚运动弹粒，提高灰色范围材质可见度，最坏粒子调用仍限制为 `91` 次。

### Fixed / 修复

- Added a distance-aware Railgun muzzle offset and kept impact particles small, preventing the large beam from reversing at close range or turning hits into oversized flashes.
- 为轨道炮加入随命中距离调整的枪口偏移，并保持命中粒子为小尺寸，避免近距离光束反向或命中产生过大闪光。

### Performance / 性能

- Shotgun visual calls are bounded to `91/96` worst/hard maximum; Railgun calls are bounded to `204/208`. The Railgun's 60-tick lifetime matches its base cooldown to prevent unbounded accumulation.
- 霰弹视觉调用限制为最坏/硬上限 `91/96`，轨道炮限制为 `204/208`；轨道炮 60-tick 寿命与基础冷却一致，不会无界叠加。

### Compatibility / 兼容性

- No damage, range, cooldown, Payload, save schema, dependency, or registry ID changes.
- 不改变伤害、射程、冷却、Payload、存档 Schema、依赖或 registry ID。

### Migration / 迁移

- No migration is required. Clients and Dedicated Servers must use the same `2.3.0` JAR.
- 不需要迁移；客户端与 Dedicated Server 必须使用相同的 `2.3.0` JAR。

### Known Issues / 已知问题

- No known functional regression. Perceived laser size and volume still depend on FOV, camera distance, particle settings, and audio settings.
- 未发现功能回归；激光的视觉尺寸和音量仍会受到 FOV、观察距离、粒子设置与音量设置影响。

## 2.2.0 — 2026-07-14

### Changed / 变更

- Rebuilt all six weapon, five ammunition, and four Gunsmithing item visuals with detailed custom cuboid models and owned `64x64` reference-matched material textures.
- Replaced the six weapon trajectories with bounded, server-synchronized muzzle, flight, water, and impact compositions.
- Expanded the stable custom Particle set from seven IDs/21 frames to 12 IDs/34 frames, adding gray Sniper rounds, a gray Shotgun range fan, black SMG rounds, moving flame jets, and a white Railgun beam.
- Removed bright cross-shaped muzzle effects from the Sniper, Shotgun, SMG, Flamethrower, and Railgun; the Grenade Launcher keeps its existing orange blast language.
- Rebuilt the Flamethrower as a continuous Hydra-inspired moving flame stream and changed the Railgun to a thin white beam that fades over exactly one second.
- Doubled the SMG base cadence from five to ten shots per second (`4` ticks to `2` ticks).
- Removed the delayed Sniper fake projectile and reduced the maximum Railgun visual composition from thousands of particle calls to a verified worst case of `172`.
- Added deterministic asset generation, geometry/UV/resource validation, ballistic-budget GameTest coverage, and an archived SHA-256-pinned style reference.

- 使用精细自定义方块模型和自有 `64x64` 参考图匹配材质，重做六把枪、五种弹药和四件枪械改装物品的全部视觉。
- 将六把武器的弹道替换为有硬预算、由服务端同步的枪口、飞行、水下与命中效果。
- 将稳定自定义 Particle 集从 7 个 ID/21 帧扩展为 12 个 ID/34 帧，新增灰色狙击弹粒、灰色霰弹范围、黑色 SMG 弹粒、运动火焰流和白色轨道炮光束。
- 移除狙击枪、霰弹枪、SMG、喷火器和轨道炮的明亮十字枪口效果；榴弹枪保持现有橙色爆炸视觉。
- 将喷火器重构为 Hydra 风格连续运动火焰流，并将轨道炮改成 1 秒渐隐的细白色光束。
- 将 SMG 基础射速从每秒 5 发提高到每秒 10 发（`4` ticks 调整为 `2` ticks）。
- 移除狙击枪延迟假弹丸，并将轨道炮从数千次粒子调用降至已验证最坏 `172` 次。
- 新增确定性资源生成、模型几何/UV/资源校验、弹道预算 GameTest，以及带 SHA-256 固定值的参考图归档。

### Compatibility / 兼容性

- No third-party runtime dependency, Payload change, save-data migration, or existing item/entity/recipe/Particle ID removal. The only gameplay tuning change is the requested SMG cadence increase.
- 不新增第三方运行时依赖，不改变 Payload 或存档 Schema，不删除现有物品、实体、配方或 Particle ID；唯一玩法数值变化是按需求提高 SMG 射速。

## 2.1.0 — 2026-07-14

### Added / 新增

- A complete vanilla-survival loop: shaped weapon recipes, five ammunition recipes, Anvil repair, and Smithing Table upgrades.
- Six server-authoritative weapons: Sniper Rifle, Shotgun, Grenade Launcher, SMG, Flamethrower, and Railgun.
- Five ammunition types; Sniper Rifle and SMG share Rifle Round.
- Three fixed upgrades: Precision Barrel, Cooling System, and Reinforced Receiver.
- English and Simplified Chinese localization, including exact translation-key parity verification.
- Dedicated-server GameTests for ammunition, Creative mode, upgrade persistence, schema compatibility, and Smithing recipes.
- A GitHub-ready bilingual art asset library with the approved recipe posters and derived item textures.

- 完整原版生存循环：有序枪械配方、五种弹药配方、铁砧维修与 Smithing Table 升级。
- 六把服务端权威枪械：狙击枪、霰弹枪、榴弹枪、冲锋枪、火焰喷射器和电磁轨道炮。
- 五种弹药；狙击枪与冲锋枪共用步枪弹。
- 三种固定升级：精密枪管、冷却系统和强化枪机。
- 简体中文与英文双语，并提供语言 key 完全一致校验。
- Dedicated Server GameTest，覆盖弹药、创造模式、升级持久化、Schema 兼容与 Smithing 配方。
- 可用于 GitHub 宣传与教学的双语素材库，包含确认版配方图和衍生物品贴图。

### Compatibility / 兼容性

- Minecraft 1.21.3, Fabric Loader 0.18.4, Fabric API 0.114.1+1.21.3, Java 21.
- No new third-party runtime dependency, no registry ID removal, and no save-data migration for this release.
- 使用 Minecraft 1.21.3、Fabric Loader 0.18.4、Fabric API 0.114.1+1.21.3 和 Java 21。
- 本版本未增加第三方运行时依赖，未删除 registry ID，也不需要存档迁移。
