# Guns 2.0.0

简体中文 | [English](readme.md)

Guns 是适用于 Minecraft `1.21.3`、Fabric Loader `0.18.4` 的 Fabric 枪械模组。命中、伤害、冷却、耐久与爆炸均由服务端权威计算。

## 创造模式与获取

模组新增“新型武器”创造物品组。当前枪械暂不支持合成，可使用以下英文命令获取：

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

## 语言切换

内置简体中文和英文，通过 Minecraft 原生“设置 → 语言”切换，无需额外配置界面。

## 构建与验证

~~~bash
./gradlew clean build --no-daemon --stacktrace
~~~

构建会校验中英文语言键一致。稳定 ID、协议、数值基线和手工回归矩阵见 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)。

## 演示

[点击查看演示视频](https://www.youtube.com/watch?v=7KhDonhsX98)

![演示 1](./demo/gun1.png)

![演示 2](./demo/gun2.png)
