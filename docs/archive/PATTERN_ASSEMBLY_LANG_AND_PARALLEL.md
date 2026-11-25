# 维核样板总成：翻译键核查 + 最大并行数报告

> 生成时间：2026-09-25　范围：GTLSupb / GTLCore 1.2.3.2-fix1 / GTCEu（本仓库引用版本）

---

## 任务 1：维核样板总成翻译键核查

### 1.1 结论

| 判定 | 结果 |
|---|---|
| 代码引用但语言文件缺失的键 | **无**（代码引用的 7 个 `gtlsupb.pattern_assembly.*`、6 个 `gtlsupb.pattern_modifier.*`、`item.gtlsupb.pattern_modifier`、`block.gtlsupb.weihe_pattern_assembly`、`gtlsupb.machine.pattern_assembly.tooltip.0-2` 在 zh_cn / en_us 全部存在） |
| 冗余键 | `gtlsupb.machine.pattern_assembly.title` 全仓无引用 → **已删除** |
| 语义错位（已修） | `gtlsupb.pattern_assembly.stock_catalyst` 是整句操作说明，却被当分区标题 → 新增短标题键 `gtlsupb.pattern_assembly.stock_catalyst.title`＝`库存催化剂`，并把说明句移到 `...stock_catalyst.hint`；Mixin 改为 `Component.translatable(...)` 显式翻译 |
| 命名不一致（待拍板） | 同一物品三种叫法：GTLCore＝`供应器样板修改器`；本模组＝`AE样板供应器修改器`；GUI 内硬编码＝`AE样板倍乘器` |
| i18n 漏洞（待修） | `PatternModifier.java:77-80` 四行界面文字硬编码中文，en_us 下仍显示中文 |

### 1.2 本模组相关键与文案（zh_cn.json / en_us.json 同键）

```
block.gtlsupb.weihe_pattern_assembly            = 维核样板总成
gtlsupb.machine.pattern_assembly.tooltip.0      = 维核样板总成仓室——自动识别机器的配方模式
gtlsupb.machine.pattern_assembly.tooltip.1      = 放置于多方块结构中，自动读取控制器的配方类型
gtlsupb.machine.pattern_assembly.tooltip.2      = 在机器界面中可视化选择配方模式

gtlsupb.pattern_assembly.output_mode            = §e输出倍率模式
gtlsupb.pattern_assembly.input_mode             = §b输入倍率模式
gtlsupb.pattern_assembly.scale_config           = 样板倍率设置
gtlsupb.pattern_assembly.scale_multiply         = 乘法倍率
gtlsupb.pattern_assembly.scale_divide           = 除法倍率
gtlsupb.pattern_assembly.apply_scale            = §a应用样板倍率
gtlsupb.pattern_assembly.stock_catalyst         = §6库存催化剂§r：左键 / JEI 设定，右键取消；库存行显示 ME 网络可用数量
gtlsupb.pattern_assembly.stock_catalyst.title   = 库存催化剂                       （本次新增，用于分区标题）
gtlsupb.pattern_assembly.stock_catalyst.hint    = §7左键 / JEI 设定，右键取消；库存行显示 ME 网络可用数量（本次新增）

item.gtlsupb.pattern_modifier                   = AE样板供应器修改器
gtlsupb.item.pattern_modifier.tooltip.0         = §6AE 样板供应器修改器§r，批量调整样板的乘除倍率
gtlsupb.item.pattern_modifier.tooltip.1         = §b右键空气§r：打开 GUI 配置乘数 / 除数 / 应用次数
gtlsupb.item.pattern_modifier.tooltip.2         = §bShift+右键样板供应器§r：把配置的倍率应用到样板
gtlsupb.item.pattern_modifier.tooltip.3         = 应用次数范围 §61–16§r，一次最多修改 16 个样板
gtlsupb.pattern_modifier.scale                  = 乘数
gtlsupb.pattern_modifier.div_scale              = 除数
gtlsupb.pattern_modifier.applied_number         = 应用次数 §f(1–16)
gtlsupb.pattern_modifier.pattern_provider_only  = §c只能对 AE 样板供应器使用
gtlsupb.pattern_modifier.updated                = §a样板已更新
gtlsupb.pattern_modifier.right_click_air        = 右键空气打开 GUI，Shift+右键样板供应器修改
```

### 1.3 需"继承"的 GTLCore 原生键（**无需复制**，随 gtlcore 语言文件自动生效）

本模组的样板总成 GUI 直接复用 gtlcore 的 `MEPatternCatalystUIManager`，其文本取自 gtlcore 自带语言文件；我们**不应**复制这些键（会造成维护分裂），只需保证不复用错键、命名同风格：

```
gui.gtlcore.pattern_buffer_gui_1 = 实际配方数量
gui.gtlcore.pattern_buffer_gui_2 = 物品催化剂槽
gui.gtlcore.pattern_buffer_gui_3 = 流体催化剂槽
gui.gtlcore.pattern_buffer_circuit_config       = 样板电路写入
gui.gtlcore.pattern_buffer_circuit_number       = 目标电路编号
gui.gtlcore.pattern_buffer_skip_existing_circuit= 跳过已有电路
gui.gtlcore.pattern_buffer_apply_circuit        = 写入
gui.gtlcore.pattern_buffer_clear_circuits       = 清空电路
gui.gtlcore.pattern_buffer_proxy_positions      = 镜像位置
gui.gtlcore.pattern_buffer_proxy_positions.count/entry/empty/highlight = 已绑定镜像：%s / #%s  X: %s, Y: %s, Z: %s / 尚未绑定任何镜像。 / [定位]
item.gtlcore.me_pattern_buffer_copy / _cut / _cut.has_content = 样板总成复制工具 / 样板总成剪切工具 / （已有剪切内容）
message.gtlcore.pattern_buffer_copied / _cut / _paste_failed / _not_copied / _not_cut … = 已复制/已剪切/粘贴失败/未复制/未剪切样板总成
tooltip.gtlcore.pattern_buffer_circuit_config    = 批量写入或清除样板总成内处理样板的编程电路
tooltip.gtlcore.pattern_buffer_circuit_number    = 允许范围：1-%s
tooltip.gtlcore.pattern_buffer_skip_existing_circuit = 开启后，写入时不会改动已经包含任意编程电路的样板
config.gtlcore.option.MEPatternOutputMax / Min   = ME样板总成输出最大/最小间隔
config.gtlcore.option.enableUltimateMEStocking   = 启用ME库存极限拉取模式
config.gtlcore.option.exPatternProvider          = 扩展样板供应器样板容量
gtlcore.machine.me_dual_hatch_stock.turns.0~3    = 自动拉取：关闭 / 全部 / 物品 / 流体
```

（完整 56 条相关键已由核查覆盖：`pattern_box*`、`pattern_quick_upload*`、`pattern_recipe_multiply/divide_*`、`item.gtlcore.pattern_modifier` 等，键名与文案均来自 `参考\GTLCore-.../assets/gtlcore/lang/zh_cn.json`。）

### 1.4 遗留建议（未改动，需你确认）

1. **工具名统一**：建议三处统一为 `AE 样板供应器修改器`（本模组物品名），或统一采用 GTLCore 的 `供应器样板修改器`——二者选一，避免玩家看到三个名字。
2. **`PatternModifier.java:77-80` 四行硬编码中文**（`AE样板倍乘器`、`设置参数后，Shift+右键样板供应器方块使用`、`先做乘法，后做除法`、`若应用次数为3…`）→ 建议改用 lang 键（现有 `gtlsupb.pattern_modifier.*` 可直接复用/扩容），否则 en_us 环境仍是中文。
3. **标题宽度**：`MEPatternCatalystUIManagerMixin` 追加分区的宽高按子控件计算，未计入标题文本；标题已改为短名词（`库存催化剂`），但若后续再加长标题需同步把文本宽度计入 `section.setSize(...)`。

---

## 任务 2：GTCEu / GTLCore 支持的最大机器并行数

### 2.1 分层结论

| 层 | 上限如何决定 | 上限数值 | 是否有配置项 |
|---|---|---|---|
| **GTCEu** | 无全局上限；`ParallelLogic.getParallelAmount(machine, recipe, parallelLimit)` 的上限**由调用方传入**，仅在输入/输出容量处收敛 | 并行仓 `4^(tier-EV)` → MAX 级 **1,048,576**；蒸汽多方块默认 **8**；冶炼炉 `32 × 线圈等级` | ✅ 仅 `machines.steamMultiParallelAmount`（默认 8） |
| **GTLCore 1.2.3.2-fix1** | mixin 覆写并行仓与 `ParallelLogic`（long 化）；多方块并行池 = `getMaxParallel() × MAX_THREADS` | 并行仓 `4^(tier-2)` → MAX 级 **16,777,216**（原生 16 倍）；"并行机" `Integer.MAX_VALUE` × 64 → 池 **≈1.37×10¹¹** | ❌ 无任何并行配置项（全为机器级常量） |
| **本模组 GTLSupb** | ①零能耗机器 `maxParallel()=Long.MAX_VALUE` 走"无上限"分支；②电多方块 `getMaxParallel()` 多为 `Integer.MAX_VALUE`；③电压等级型按公式 | 逻辑边界 **Long.MAX_VALUE**（实际由输入物品数决定）；电压等级型 MAX=2147483647 → ULV=7 | ❌ 本模组 config 无并行项（仅 batchDuration / adaptiveBatchMax / concurrentThreads） |

### 2.2 关键代码位置（含具体数值）

**GTCEu 层**
- `gtceu\...\api\recipe\modifier\ParallelLogic.java:34-35`　`getParallelAmount(machine, recipe, parallelLimit)`——上限取自入参；
- `gtceu\...\common\machine\multiblock\part\ParallelHatchPartMachine.java:37`　`maxParallel = (int) Math.pow(4, tier - GTValues.EV)`（EV=1 … MAX=1,048,576）；
- `gtceu\...\common\data\GTRecipeModifiers.java:91-95`　并行仓消费路径；`:249`　冶炼炉 `32 × coilLevel`；
- `gtceu\...\common\machine\multiblock\SteamParallelMultiblockMachine.java:52`　蒸汽多方块 `steamMultiParallelAmount`；
- `gtceu\...\config\ConfigHolder.java:577-582`　`steamMultiParallelAmount = 8`（**全模组唯一并行配置项**）；
- `LargeTurbineMachine.java:160` / `LargeCombustionEngineMachine.java:137`　`maxParallel = 最大电压 / EUt`。

**GTLCore 层**
- `gtlcore\...\mixins\gtm\machine\ParallelHatchPartMachineMixin.java:28`　`maxParallel = (int) Math.pow(4, tier - 2)`（MAX 级 = **16,777,216**）；
- `gtlcore\...\common\machine\trait\MultipleRecipesLogic.java:39 / :104`　`MAX_THREADS = 64`，并行池 `= getMaxParallel() × 64`；
- `gtlcore\...\api\recipe\IParallelLogic.java:72-92 / :220`　long 化并行与"按库存收敛"（实际并行 = 库存 ÷ 单份需求）；
- 逐机器常量：`WorkableElectricMultipleRecipesMachine.java:29-31`（`Integer.MAX_VALUE`）、`WorkableElectricParallelHatchMultipleRecipesMachine.java:13-16`、`CoilWorkableElectricMultipleRecipesMachine.java:53-55`、`CoilWorkableElectricMultipleRecipesMultiblockMachine.java:37-40`（`min(2147483647, 2^(温度/900))`）、`GTLRecipeModifiers.java:51/71/108`；
- `gtlcore\...\config\ConfigHolder.java`　**无并行字段**。

**本模组层**
- `common\machine\multiblock\ZeroEnergyMultiTypeLogic.java:28-31`　零能耗机器：`lowVoltageInfiniteParallel = true`、`lowVoltageTier = Integer.MAX_VALUE`；
- `api\machine\logic\MutableRecipesLogic.java:785-788`　无上限分支：`IParallelLogic.getParallel(machine, match, Long.MAX_VALUE)`（**逻辑边界 Long.MAX_VALUE**）；`:491`　`machineParallel = getMachine().getMaxParallel()`；`:750-752` 逐槽上限；
- `common\machine\multiblock\UniversalFactoryMachine.java:193-197`　`getMaxParallel() = Integer.MAX_VALUE >>> (2 * (14 - tier))`（MAX=2147483647 … ULV=7）；`:205-208` `maxParallel()` 同值；
- `common\machine\multiblock\CosmicMatterSiphonerMachine.java:61-65`　同公式；
- `MoWanMachine.java` / `LingZhuMachine.java:71-76`　`16 × 2^(线圈温度/900)`，联动 ×2，封顶 `Integer.MAX_VALUE`；`AlchemicalForgeMachine.java:60-65`　`4 × 2^(线圈温度/1800)`；
- `module\SteamUniversalModule.java:77 / :127-146`　`连接数 × 16384`；
- `mixin\gtceu\integration\ParallelProviderMixin.java:40`　Jade 以 `getMaxParallel() == Integer.MAX_VALUE` 判定"无限并行"并显示 `gtlsupb.multiblock.infinite.parallel/threads`。

### 2.3 想改"最大并行"时的写入位置

1. **蒸汽多方块** → GTCEu `ConfigHolder.java:582 machines.steamMultiParallelAmount`（配置文件层）；
2. **所有用并行仓的多方块** → GTLCore `ParallelHatchPartMachineMixin.java:28`（`4^(tier-2)`）与 GTCEu `ParallelHatchPartMachine.java:37`（电压等级层）；
3. **GTLCore "无限"并行机** → `WorkableElectricMultipleRecipesMachine.getMaxParallel()` + `MultipleRecipesLogic.MAX_THREADS`（机器层 + 全局乘数）；
4. **本模组电多方块** → 各自 `getMaxParallel()`（多为 `Integer.MAX_VALUE`）与 `maxParallel()`（零能耗 = `Long.MAX_VALUE`）——注意：**改这里往往不生效**，真正瓶颈是 `MutableRecipesLogic.calculateParallel` + `IParallelLogic` 的"库存 ÷ 单份需求"收敛；
5. **本模组要加并行配置项** → 在 `com\gtlsupb\gtlsupb\config\ConfigHolder.java` 新增字段并接入相应 `getMaxParallel()`。
