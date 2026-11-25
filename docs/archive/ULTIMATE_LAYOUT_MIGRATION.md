# 按 GTLUltimate 复刻：注册方式与配方路径迁移方案

> 目标：把 GTLSupb 的机器/物品注册方式、配方路径与目录组织，对齐 `e:\GTL\GTLUltimate` 的写法。
> 约束：**注册名（registry name）、语言键、mixin target 一律不改**（改了会连锁改动资源与兼容性）；只对齐"目录 + 写法"。

---

## 一、GTLUltimate 的约定（已核实）

基础包：`org.gtlultimate.gtlultimate`（mod id = `gtlultimate`）

| 位置 | 内容 |
|---|---|
| `GTLUltimate.java` / `GTLUltimateAddon.java` | mod 入口 + GT addon 入口 |
| `config/ConfigHolder.java` | 配置 |
| `data/recipe/GTLUltimateRecipes.java` | **配方总入口（单一 final 类，静态方法注册）** |
| `data/recipe/RemoveRecipe.java` | 配方移除类 |
| `common/machine/multiblock/*.java` | 多方块机器（如 `UltimateUniversalFactoryMachine`、`LargeFurnaceMachine`） |
| `common/machine/multiblock/part/*.java` | **仓室/部件**（`ParallelAmplifyHatchPartMachine`、`OverlimitGridHatchPartMachine` …） |
| `common/machine/generator/*.java` | 发电机（`OverlimitEnergyAbsorberMachine`） |
| `common/machine/trait/*.java` | **逻辑/trait**（`UniversalFactoryLogic`、`GeneratorLogic`、`ConcurrentBatch` …） |
| `common/machine/terminal/*.java` | 终端类机器 |
| `common/item/*.java` | 物品（`OverlimitQuantumEntanglementTool`） |
| `integration/ae2/*.java`、`integration/jade/*.java` | 集成（含 `IPacket` 式网络包、Jade 插件与样式） |
| `utils/*.java` | 工具（`EnergyBroadcastHelper`） |

命名约定：
- 机器类：`<名词>Machine`（放在 `multiblock` / `generator`）；
- 仓室类：`<名词>HatchPartMachine`（放在 `multiblock/part`）；
- 逻辑类：`<名词>Logic`（放在 `machine/trait`，**与机器类分家**）；
- 配方：**只有 `data/recipe/<Mod>Recipes` 一个入口**，内部静态方法分组（不散落多个 `*Recipes` 类）。

---

## 二、GTLSupb 现状对照

| 内容 | 我们现状 | 目标（对齐 ultimate） |
|---|---|---|
| 机器定义注册 | `common/machine/GTLSupbMachines.java` | 保留（ultimate 的定义类同样集中在 data 侧；**改名收益低，暂不动**） |
| 机器实现 | `common/machine/multiblock/*`、`common/machine/generator/*` | ✅ 已一致 |
| 仓室/部件 | `common/machine/hatch/*` | → `common/machine/multiblock/part/*`（**搬迁 + 改包名**） |
| 配方逻辑 | `api/machine/logic/*`、`common/machine/multiblock/*Logic.java` | → `common/machine/trait/*`（逻辑与机器分家） |
| 物品实现 | `common/item/*` | ✅ 已一致 |
| 配方入口 | `data/recipe/GTLSupbRecipes.java`（+ 各自注册方法） | ✅ 基本一致；**若已有多份 `*Recipes`，合并进一个入口** |
| 网络包 | `network/*` | → `integration/<平台>/*`（可选，收益低） |

---

## 三、执行顺序（每批必须可编译）

| 批次 | 动作 | 验证 |
|---|---|---|
| **B1 逻辑分家** | 把 `*Logic.java`（`common/machine/multiblock/*Logic`、`api/machine/logic/*`）迁到 `common/machine/trait/`，同步 `import` 与 `package` | `read_lints` 0 error → 构建通过 |
| **B2 仓室搬迁** | `common/machine/hatch/*` → `common/machine/multiblock/part/*`（含 `IPatternAssemblyHatch` 等接口） | 同上（注意 mixin 若按类名硬编码需同步 ✗ 只改包名不改类名可避免） |
| **B3 配方入口收敛** | 确保所有配方注册走 `data/recipe/GTLSupbRecipes` 单一入口；删除散落的注册类（若有） | 同上 |
| **B4 集成归位** | `network/*` 迁到 `integration/gtlsupb_net/*`（可选）；Jade/AE 相关保持 `integration/*` | 同上 |
| **B5 收尾** | 全量构建；核对语言键引用（迁移不改注册名 → 语言键零改动） | `BUILD SUCCESSFUL` + 键集 `sets-equal` |

---

## 四、风险与红线

1. **不改注册名**（`gtlsupb:xxx`）、**不改语言键**、**不改 mixin target 的类名**——只改包名/文件位置，这些零影响；
2. 若必须改类名（例如统一 `...Logic` 后缀），要同步检查：`GTLSupbMachines` 里的构造器引用、Jade/集成里的 `instanceof`、mixin 的 `@Mixin(targets=...)`；
3. 每批搬迁后先 `read_lints`（IDE 诊断）再构建，避免出现"半迁移 + 语法崩"的中间态（本会话已经踩过一次：批量删行留下悬空逗号与空 `.tooltips(`，已修复）；
4. 搬迁批次之间不要夹杂功能改动，便于回滚。

---

## 五、当前状态

- 本文件为**阶段 a（盘点与映射）**的产出；
- 阶段 b–d（B1–B5）尚未执行；
- 待执行时的第一条指令建议：
  > 按 `ULTIMATE_LAYOUT_MIGRATION.md` 执行 B1（逻辑分家），完成后跑 `read_lints` 与构建。
