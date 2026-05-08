# Guns 2.0.0

适用于 Minecraft `1.21.3`、Fabric Loader `0.18.4` 的 Fabric 枪械模组。

## 创造模式物品栏

模组新增创造模式分类栏“新型武器”，用于放置当前设计的枪械。

## 获取命令

```mcfunction
/give @p guns:sniper_rifle
/give @p guns:shotgun
/give @p guns:grenade_launcher
/give @p guns:smg
/give @p guns:flamethrower
/give @p guns:railgun
```

## 武器说明

- 狙击枪 `guns:sniper_rifle`：右键切换 `1x/2x/4x/8x/16x` 倍镜，开镜后左键射击，基础伤害 10，不穿透目标。
- 霰弹枪 `guns:shotgun`：近距离 150 度扇形打击，射程 8 格，伤害和击退随距离衰减。
- 榴弹枪 `guns:grenade_launcher`：发射黑色圆形榴弹，榴弹按抛物线飞行，接触实体、方块或落地后产生 TNT 强度爆炸。
- 冲锋枪 `guns:smg`：按住左键连续射击，每秒 5 发，每发基础伤害 2。
- 火焰喷射器 `guns:flamethrower`：按住左键持续喷火，短距离范围伤害并点燃目标。
- 电磁轨道炮 `guns:railgun`：单发高伤害光柱，基础伤害 35，可穿透路径上的多个生物，直到命中方块障碍物。

## 通用设定

- 当前枪械暂不支持合成获得。
- 持枪时会使用双手持枪姿势。
- 枪械射击会拦截原版主手挥动攻击动作，减少视野遮挡。
- 命中类枪械会复用剑类伤害附魔逻辑；耐久类附魔也可用于枪械。
