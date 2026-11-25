# AGENTS.md — GTLSUPBJAVA（GTLsupb 模组）

本文件是本项目的**入口文档**。任何智能体（含子智能体）在动手前必须读完本文件与 `执行规则.md`。

---

## 一、项目是什么

**GTLsupb** 是一个 Minecraft 1.20.1 Forge 的**格雷科技（GTCEu）附属模组**，为 GTCEu / gtlcore / AE2 生态提供一批高端多方块机器与跨模组集成功能。

| 项 | 值 |
|---|---|
| 模组名 | GTL-Supb（简称 GTLsupb） |
| MOD ID | `gtlsupb` |
| Java 包 | `com.gtlsupb.gtlsupb` |
| 版本 | `4.8.6`（`gradle.properties#mod_version`） |
| 作者 / 协议 | XiaoChuan / LGPLv3.0 |
| 工程根 | `E:\GTL\GTLSUPBJAVA` |
| 构建体系 | Architectury Loom 1.6 + spotless |

## 二、技术栈（实测值，非推测）

| 项 | 值 | 依据 |
|---|---|---|
| Minecraft | 1.20.1 | `gradle.properties` |
| Forge | 47.3.7 | 同上 |
| Java（构建/运行） | 17（本机 `C:\Users\Administrator.DESKTOP-N0QCVKS\.jdks\temurin-17`） | 本机实测 |
| GTCEu | `gtceu-1.20.1-1.4.4` | `gradle.properties#gtceu_version` |
| LDLib | `ldlib-forge-1.20.1-1.0.29.b` | 同上 |
| gtlcore | **编译期 `libs/gtlcore-1.2.3.2-fix3.jar`；运行期 1.2.3.3**（见 `已知问题.md`） | libs/ + 运行日志 |
| AE2 | 编译期 `15.2.1`；实例实测运行期 `15.4.12`（存在版本偏斜，见 §四 硬规则 3） | `build.gradle` + 运行日志 |
| ExtendedAE | `ExtendedAE-1.20-1.1.12-forge` | 同上 |
| gtmthings | `gtmthings-1.3.5.a` | 同上 |
| FTB Quests | `ftb-quests-forge-2001.4.22.jar`（libs 编译期） | 同上 |
| Jade / JEI / EMI | `11.9.2` / `15.19.5.99` / compileOnly | 同上 |
| 配置库 | Toma's `configuration`（YAML，`config/gtlsupb.yaml`） | 源码实测 |

完整依赖清单见 [build.gradle](../GTLSUPBJAVA/build.gradle) 与 [gradle.properties](../GTLSUPBJAVA/gradle.properties)。

## 三、文档地图与读写权限

| 文件 | 作用 | 读取策略 | 写入策略 |
|---|---|---|---|
| `AGENTS.md` | 入口：项目定位、技术栈、文档地图、硬规则 | 每次任务必读 | 仅根智能体 |
| `执行规则.md` | 规则手册（唯一约束来源） | **每次任务必读全篇** | 仅根智能体，只追加 |
| `启动上下文.md` | 当前有效状态快照 | **每次任务必读全篇** | 仅根智能体，任务后同步 |
| `功能与需求.md` | 用户确认的需求与规格（按日期追加） | 按需读相关段落 | 仅根智能体 |
| `已知问题.md` | 问题与根因；修复后移入「已解决问题」 | 按需读相关段落 | 仅根智能体 |
| `API标准.md` | API 参考的索引与查证规范 | 写 API 前先读 | 仅根智能体 |
| `docs/` | 准则分章（API 设计 / 格式 / 错误处理 / 文档标准 / 最佳实践） | 按需 | 仅根智能体 |
| `examples/` | 可复用模板（网络包 / 能力接口 / trait） | 按需 | 仅根智能体 |
| `工作日志.md` | 任务日志 | **只追加，禁止从头读** | 仅根智能体 |
| `变更历史.md` | 变更流水（倒序，只追加） | 平时不读 | 仅根智能体 |
| `打包.md` | 构建与产物验证流程 | 仅在构建/打包任务时读 | 仅根智能体 |
| `岗位提示词.md` | 各岗位提示词（含根智能体自身） | 派发任务前读相关岗位段落 | 仅根智能体 |

## 四、硬规则（完整版见 `执行规则.md`）

以下 6 条违反即任务失败：

1. **禁止不确定字眼**。不使用「可能 / 也许 / 大概 / 应该是」。信息不足就说「不知道」，做不到就说「不会」，并给出确定判断与依据。
2. **改动必须可验证，禁止假成功**。构建退出码 0 不等于产物正确；必须解包产物核对实际内容（新增/修改的类、资源、语言键），并确认部署的是新产物。
3. **不猜 API**。第三方 API（gtlcore / GTCEu / AE2）语义必须先查 `参考/` 官方源码（gtlcore 运行时源码在 `参考/GTLCore-version-1.2.3.2-fix1`）或 `javap` 读 libs/ 与实例 JAR 实证，再下结论。
4. **任务前先审查**。任务是否合理、提示词是否无歧义、是否存在多种实现方向、边界与预期结果是什么。任一不明确，暂停提问。
5. **先备份后覆盖**。替换任何当前能正常运行的文件前，先保留原件（另存 / 改名 / 新目录）。禁止用未经验证的代码覆盖唯一可用副本。
6. **文本一律中文**。所有 GUI 文本、Tooltip、提示消息必须走语言文件（`zh_cn.json` + `en_us.json` 双语），禁止硬编码英文显示文案。

## 五、开发面（本项目的实际改动载体）

本项目是纯 Java 模组工程，改动全部落在 `GTLSUPBJAVA\src\main\java`（代码）与 `src\main\resources`（资源/语言/数据）：

| 目录/文件 | 内容 |
|---|---|
| `api/` | 能力接口（`IBatchProcessingMachine`、`ILimitedDurationMachine`、`IWirelessElectricMultiblockMachine` 等）、机器逻辑 trait（`MutableRecipesLogic`、`MultiTypeMutableRecipesLogic`） |
| `common/machine/` | 机器实现：无限并行电力机器、线圈机器、零能耗多类型机器、微宇宙铸造厂、蒸汽通用模块、魔丸/灵珠联动、碎界采集器等 |
| `common/machine/multiblock/module/` | 模块化主机子模块（微宇宙 / 锻造舰 / 蒸汽通用） |
| `common/machine/multiblock/part/` | 仓部件（ME 驱动、样板总成、蒸汽仓、数据/算力维护仓等） |
| `network/` | C2S/S2C 网络包（AE 任务提交/设置、编码终端、虚空能源开关、量子位置过滤等，8 个包，协议版本 3） |
| `mixin/` | 按目标模组分组：`gtceu/`、`gtlcore`、`ae2`、`ftbquests/`、`minecraft/`、`jei/` |
| `utils/` | 配方计算（`RecipeCalculationHelper`、`FairAllocationUtils`、`ChanceParallelLogic`）、能量广播（`EnergyBroadcastHelper`）、彩虹文本 |
| `integration/` | AE2 集成（`AEGridTrackerService` 等） |
| `common/modify/` | 运行时配方复制/修改（`RecipesModify`） |

核心机制：

- **跨配方并行 + 合并配方**：`MutableRecipesLogic` 把同批匹配的多个配方聚合为一条合并配方，并行分配走 FAIR / GREEDY（`FairAllocationUtils`），批处理作为独立维度自适应放大内容与耗时。
- **批处理**：侧边栏开关（`BatchProcessingFancyConfigurator`）+ 配置项 `adaptiveBatchMax`（默认 100）；倍率按输入/输出可取量自适应降档（`RecipeCalculationHelper.findFeasibleBatch`）。
- **零能耗机器**：`ZeroEnergyMultiTypeMachine` 系无 EU 输入，处理时间由运行速率调控器设定。
- **AE2 集成**：ME 无线访问点（WAP）无线连接、AE 合成样板驱动的样板总成（基于 gtlcore `MEPatternBufferPartMachine`）、AE2-FTB Quests 任务提交（快捷键开 GUI、开关在 GUI 内）。
- **FTB 联动**：无线能量广播只面向 ftb-chunks 强制加载区块；任务提交与物品/任务/奖励 mixin。

## 六、命令速查

```powershell
# 工程根
cd E:\GTL\GTLSUPBJAVA

# 构建（必须先终止 Gradle 守护进程 + Java 进程，避免 Loom 缓存损坏；JAVA_HOME 指向 temurin-17；
# 沙箱会拦截 java 写 ProgramData，构建需在沙箱外执行）
$env:JAVA_HOME='C:\Users\Administrator.DESKTOP-N0QCVKS\.jdks\temurin-17'
.\gradlew.bat --stop
.\gradlew.bat spotlessApply build

# 产物
E:\GTL\GTLSUPBJAVA\build\libs\GTLsupb-<版本>.jar
```

## 七、技能与工具

- 无专用技能；API 实证优先用本机 JDK 的 `javap`（`C:\Users\Administrator.DESKTOP-N0QCVKS\.jdks\temurin-17\bin\javap.exe`）与 `参考/` 源码。
- 本机可复用资产：`~/.gradle/caches/`（含 Loom 反混淆缓存）、`libs/` 本地化依赖 jar。

## 八、任务收尾（每次必做）

1. 把需求 / 改动 / 验证结果 / 遗留风险 / 踩坑追加到 `工作日志.md`。
2. 把当前有效状态同步到 `启动上下文.md`。
3. 新发现的 API 事实写入 `API标准.md`；新问题写入 `已知问题.md`；新确认的需求写入 `功能与需求.md`。
4. 状态型文档之间的数字与清单必须一致（`执行规则.md` 规则 25）。
