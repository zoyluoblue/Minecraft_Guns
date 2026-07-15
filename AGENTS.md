# Guns AI 开发规则

## 开始前

依次阅读 `readme.md`/`README.zh-CN.md`、`fabric.mod.json`、`guns.mixins.json` 和待修改源码。位于 `mc_mods` 工作区时，同时遵循上级 `../AGENTS.md`。

## 不变量

- 基线：Minecraft `1.21.3`、Java `21`、Fabric Loader `0.18.4`、Fabric API `0.114.1+1.21.3`。
- 不得无迁移地修改 Mod ID、物品/实体/物品组 ID、Payload ID 或 `AdvancedWeaponFirePayload.Weapon` 的枚举顺序。
- 客户端只能发送开镜或射击意图；伤害、命中、冷却、耐久、爆炸和持枪校验均由服务端决定。
- 无平衡性需求时不得改变伤害、射程、散布、射速、冷却、耐久消耗、击退、爆炸或火焰参数。
- 保留 `Guns` 与 `GunsClient` 的现有公共静态入口，避免破坏 mixin 和潜在集成。
- Mixin 修改必须保持注入目标、客户端隔离和 `defaultRequire: 1` 的失败可见性。

## 双语

- 玩家文本只使用翻译键；`en_us.json` 与 `zh_cn.json` 同步更新。
- 使用 Minecraft 原生“设置 → 语言”切换，不保存已翻译字符串。
- 命令和 Registry ID 保持英文稳定标识；中英文说明放在文档和语言文件中。

## 完成门禁

~~~bash
./gradlew clean build --no-daemon --stacktrace
~~~

还需执行客户端开镜、六种武器、断线清理和 Dedicated Server 手工回归。
