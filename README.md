# EpicExoticGarden

一个独立的 Minecraft 农场插件：种植异域果树与灌木、制作果汁料理、培育魔法作物——无需 Slimefun4 前置。

**适用平台**：Spigot / Paper 1.12+（含 1.20+/1.21+）
**SpigotMC 资源页**：https://www.spigotmc.org/resources/139067
**源码 / Source**：https://github.com/quantifyingepic/EpicExoticGarden

---

## 致谢与来源 / Attribution

本项目是基于 **[ExoticGarden](https://github.com/Slimefun-Addon-Community/ExoticGarden)** 的衍生作品（重构版）。
有少部分代码是基于ExoticGarden的,大多数代码都是EpicExoticGarden独立编辑。已和ExoticGarden有很大的不同（见下文"与原版的差异"）。

- 原作者：TheBusyBiscuit 及 Slimefun Addon Community 贡献者
- 原项目许可证：GNU General Public License v3.0
- 原项目版权：Copyright (c) The Slimefun Addon Community

ExoticGarden 的物品内容、作物数值与果树结构数据来源于原项目。EpicExoticGarden 在其基础上进行了大量修改与重写（见下文"与原版的差异"）。

This project is a derivative work of [ExoticGarden](https://github.com/Slimefun-Addon-Community/ExoticGarden) by TheBusyBiscuit and the Slimefun Addon Community, licensed under GPLv3. Significant modifications have been made — see below.

## 与原版的差异 / Modifications

依据 GPLv3 §5(a)，本插件对原 ExoticGarden 的主要修改包括：

- **移除 Slimefun4 依赖**：物品注册从 SlimefunItem 体系改为自研 PDC（PersistentDataContainer）标签体系，可独立运行于 Spigot/Paper
- **方块存储重写**：从 flat-file 改为 SQLite（`blocksdata/blocks.db`），含 WAL、定时 checkpoint、方块 PDC 自识别与活塞迁移
- **多语言系统**：内置 10 种语言（de/en/es/fr/it/nl/pl/pt-BR/ru/zh-CN），磁盘优先合并，`/eeg reload` 即时生效
- **图鉴 GUI**：自研分类菜单 + 合成详情页 + 图鉴手册物品（`/eeg guidebook`）
- **配方外置**：`recipe/*.yml` 可读可改，支持 SHAPELESS/SHAPED、hunger/amount 参数
- **CustomModelData 支持**：`models.yml` 为资源包提供 overrides 入口
- **新增物品**：石采集钩（3x3 范围收割）、面粉/盐/黄油/奶酪等 pantry 物品（替代原 Slimefun 原料）
- **饮品体系**：药水颜色 + SATURATION 效果
- **SpigotMC 更新检查**、独立 bStats 统计（插件 ID 34267）

## 功能

- 8 种浆果灌木、10 种果树、16 种蔬菜香料、14 种魔法作物
- 80+ 种料理与饮品（果汁、冰沙、冰茶、派、三明治……）
- 采集钩：破坏树叶 25% 概率额外掉落树苗；石采集钩：右键 3x3 范围收割
- 作物自然生成于新区块（可在 `config.yml` 调整概率与世界黑名单）
- 破坏高草丛掉落种子与作物（`grass-drops` 可逐项开关）

## 命令与权限

| 命令 | 说明 | 权限 |
|------|------|------|
| `/eeg guide` | 打开图鉴主菜单 | `epicexoticgarden.guide`（默认所有人） |
| `/eeg guidebook` | 获取图鉴手册（右键使用） | `epicexoticgarden.guide` |
| `/eeg help` | 命令帮助 | - |
| `/eeg give <物品> [数量] [玩家]` | 获取物品 | `epicexoticgarden.give`（默认 OP） |
| `/eeg reload` | 重载配置与语言 | `epicexoticgarden.reload`（默认 OP） |
| `/eeg debug` | 输出调试信息 | `epicexoticgarden.debug`（默认 OP） |

## 构建

```bash
mvn clean package
```

产物位于 `target/`。需要 JDK 与 Maven。

## 配置

- `config.yml`：语言、生成概率、世界黑名单、grass-drops、进服更新提示
- `languages/<语言>/items.yml`：物品名、GUI 文案、命令提示（可自定义）
- `recipe/*.yml`：合成配方（修改后需重启）
- `models.yml`：CustomModelData（1.14+）

## 许可证 / License

```
EpicExoticGarden — a derivative work of ExoticGarden
Copyright (C) The Slimefun Addon Community (original ExoticGarden)
Modifications Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
```

完整许可证文本见 [LICENSE](LICENSE) 文件。

---

**免责声明**：本项目为技术学习与交流目的发布。Minecraft 是 Mojang AB 的商标，本项目与 Mojang 无关。
