# ADR-0002：使用 Fabric 自定义粒子实现枪械弹道视觉

- Status：accepted
- Date：2026-07-14
- Owners：zoyluo
- Related feature：`FEATURE_DESIGN_BALLISTICS_VISUALS.md`
- Supersedes：无

## 1. Context

当前弹道大量使用沿直线重复生成的原版 `SMOKE`、`CLOUD` 和 `END_ROD`。狙击枪的命中在服务端立即结算，但视觉弹丸随后用六 tick 缓慢移动；冲锋枪是一条烟点虚线；霰弹枪生成大量随机烟点；轨道炮在最大射程可生成数千粒子。第一轮自定义效果仍使用大尺寸发光十字和静态整段铺设，实机截图证明近距离会遮挡准心，喷火器也呈现离散机关枪脉冲。表现与新枪械美术不一致，也缺少足够明确的武器差异。

用户允许新增第三方依赖和 Particle registry ID，但要求选择视觉质量更高且适配 Minecraft 1.21.3 的方案。

## 2. Decision Drivers

- 六把枪必须拥有清晰且互不混淆的弹道语言。
- Dedicated Server 和多人客户端必须看到一致的权威终点。
- 粒子数量必须有硬上限，不允许随射程无界增长。
- 客户端类不能进入 Dedicated Server 类加载路径。
- Minecraft 1.21.3、Fabric API 0.114.1+1.21.3 和现有 Payload 必须保持兼容。
- 新增依赖必须提供明显且无法以现有 Fabric API 合理实现的收益。

## 3. Considered Options

### Option A：继续组合原版粒子

优点是零注册成本。缺点是颜色、生命周期、缩放和动画不可控，无法稳定匹配枪械配色，现有视觉问题难以根治。

### Option B：使用 Fabric API 注册 Guns 自定义粒子

注册专用粒子类型和纹理，由服务端使用原版粒子同步协议广播位置，客户端工厂控制颜色、透明度、缩放、速度和生命周期。无需新增 Guns Payload，也无需第三方依赖。

### Option C：引入 Veil 等高级渲染库

可以使用 Shader、Framebuffer 和更复杂的后处理，但 Minecraft 1.21.3 缺少明确的稳定兼容发布；依赖体积、Mixin 冲突面和客户端安装要求显著增加。当前需求不需要屏幕后处理或独立 Framebuffer，收益不足以抵消风险。

## 4. Decision

选择 Option B。新增以下稳定 Particle registry ID：

- `guns:tracer`
- `guns:muzzle_flash`
- `guns:impact_spark`
- `guns:flame_core`
- `guns:energy_arc`
- `guns:blast_wave`
- `guns:shockwave`
- `guns:gray_round`
- `guns:gray_range`
- `guns:black_round`
- `guns:flame_jet`
- `guns:white_beam`

所有类型使用 Fabric API 的 `FabricParticleTypes.simple()` 注册。原有七个 ID 全部保留；新增五个 ID 分离灰色实体弹粒、灰色范围、黑色实体弹粒、运动火焰流和白色渐隐光束。客户端通过 `ParticleFactoryRegistry` 提供专用工厂和自有纹理，并允许实体弹粒/火焰使用 opaque sheet、范围/光束使用 translucent sheet。服务端继续计算射线、碰撞、伤害、穿透和爆炸，并使用 Minecraft 原生粒子同步；不新增或修改 Payload。

## 5. Consequences

### Positive

- 粒子可以用灰、黑、橙和白明确区分武器，同时消除近枪口大尺寸发光十字。
- 每种粒子的生命周期、缩放、淡出和运动独立可控。
- 喷火器采用本地 Twilight Forest Hydra 源码所示的连续运动粒子原则；轨道炮白束固定 20 tick 淡出。
- 服务器和客户端保持同一 JAR，Dedicated Server 不加载客户端粒子实现。
- 无额外运行时依赖，发布和安装要求不变。

### Negative

- 新 Particle ID 发布后成为稳定 registry 契约。
- 需要维护粒子纹理、JSON、客户端工厂和性能预算。

### Risks

- 高频武器可能产生过多网络粒子包。
- 第一人称近距离粒子可能遮挡视野。
- 客户端资源缺失会产生粒子纹理错误。

## 6. Migration

- 数据迁移：无。
- API 迁移：新增 Particle ID，不修改已有公共 API。
- 网络迁移：不新增 Guns Payload；使用 Minecraft 原生粒子同步。
- 旧世界：不保存粒子状态，完全兼容。
- 测试：注册表、客户端资源加载、粒子预算纯逻辑测试、Dedicated Server GameTest。

## 7. Rollback

可将视觉调用回退到原版粒子，并保留已发布 Particle ID 的空兼容注册。不得在发布后直接删除 ID。模型、配方和存档不受影响；SMG 的 2-tick 基础冷却是独立、可单独回滚的平衡参数。

## 8. Verification

- `verifyBallisticsResources` 校验十二个粒子 JSON 和 34 帧纹理。
- 纯逻辑测试校验采样数量与硬预算。
- `clean build` 验证客户端/服务端类隔离。
- `runGameTest` 验证弹药、升级、配方和服务端行为，并锁定新视觉预算与 20-tick 光束寿命。
- 开发客户端资源加载已验证；六把枪在空气、水下、近距离和最大射程的实际世界观感仍列入手工回归。

## 9. References

- [Fabric Documentation: Creating Custom Particles](https://docs.fabricmc.net/develop/rendering/particles/creating-particles)
- [Veil rendering library](https://github.com/FoundryMC/Veil)
