# API 标准

本文件是 **API 参考的索引与查证规范**。具体到每个模组的 API 细节由本文件管理（本项目不设大量分册，核心结论直接落在「已确证结论」章节，按模组归类）。

**用途**：写 GTLSUPBJAVA（Java）代码前，先查对应模组的 API 结论。禁止凭记忆或网上教程写 API。

**与 `执行规则.md` 的关系**：规则手册规定「必须做什么、禁止做什么」；本文件规定「API 事实是什么、在哪查、可信到什么程度」。

---

## 一、查证规范（证据等级）

任何写入本项目代码的 API 结论，必须能追溯到下列证据之一。等级从高到低：

| 等级 | 证据 | 何时用 | 可靠性 |
|---|---|---|---|
| **A** | `参考\<模组>\` 下与该模组**运行期版本对应**的源码，标注 `路径:行号` | 有公开源码的模组（见 §三分册索引） | 最高 |
| **B** | `javap` 读 libs/ 或实例内 JAR 的字节码签名 | 无公开源码的模组，或需确认**运行期**实际名字 | 高（就是运行期事实） |
| **C** | 反编译实例内 JAR | 需要读逻辑而非签名 | 中（反编译可能失真） |
| **D** | 游戏日志实测行为 | 行为验证 | 高（针对该行为） |
| **X** | 记忆、网上教程、其他版本的文档 | —— | **禁止作为依据** |

**强制要求**：

1. 每条结论必须标注证据等级与来源。
2. 用等级 A 时，**源码版本必须与运行期版本对应**。默认分支通常指向更新的版本，用它得出的结论是错的。
3. 无法定位到任何证据的，写「未确认」并说明缺什么。**禁止推测**。
4. 结论中不得出现「可能」「也许」「大概」「应该是」（`执行规则.md` 规则 3）。

### 为什么等级 B 不可跳过

本模组已实测：仅靠「方法名看起来对」会得出错误结论。例如 gtlcore 运行期（1.2.3.2）的方法签名与编译期（1.2.2.8-fix6）不同，按编译期签名覆写会在运行期抛 `NoSuchMethodError`（`已知问题.md` GTLSUPB-001）。**签名必须按运行期核对**。Forge 1.20.1 生产环境下原版方法名是 SRG 名（如 `m_150399_`），mixin 按官方名注入会失败。

---

## 二、本项目的技术栈（API 兼容性基线）

**任何 API 结论都只对下列版本成立。** 换版本后必须重新实证。

| 组件 | 版本 |
|---|---|
| Minecraft / Forge | 1.20.1 / 47.3.7 |
| Java | 17 |
| GTCEu | `gtceu-1.20.1-1.4.4.jar`（libs 无源码，用 javap） |
| LDLib | `ldlib-forge-1.20.1-1.0.29.b.jar` |
| gtlcore | 编译期 `libs/gtlcore-1.2.3.2-fix3.jar`；**运行期 1.2.3.3** |
| AE2 | `appliedenergistics2-forge-15.2.1.jar` |
| ExtendedAE | `ExtendedAE-1.20-1.1.12-forge.jar` |
| gtmthings | `gtmthings-1.3.5.a.jar` |
| FTB Quests | `ftb-quests-forge-2001.4.22.jar`（libs） |
| FTB Chunks / Library / Teams | 2001.3.8 / 2001.2.13 / 2001.3.2 |
| Jade / JEI / EMI | `11.9.2` / `15.19.5.99` / compileOnly |

## 三、参考源码索引

源码根统一为 `参考\<目录>\`。

| 参考目录 | 对应运行期版本 | 用途 |
|---|---|---|
| `参考\GTLCore-version-1.2.3.2-fix1` | gtlcore **1.2.3.2**（运行期） | **gtlcore 唯一权威源码**（`BatchProcessing`、`RecipeRunnerHelper`、`IParallelLogic`、`MultipleRecipesLogic`、`MEPatternBufferPartMachine` 等） |
| `参考\gtladditions` | 设计参考（未安装） | 只读设计参考，禁止照搬（规则 9） |
| `参考\GTOCore-Main-main` | 设计参考（未安装） | 同上 |
| `参考\ae2` | AE2 15.2.x 线 | AE2 API（终端、网格、样板） |
| `参考\ae2wtlib` | 15.2.3 | AE2 无线终端参考 |
| `参考\extendedae` | ExtendedAE | 扩展 AE 参考 |
| `参考\ftb-quests` / `ftb-library` / `ftb-teams` / `ftb-chunks` | 2001.x 线 | FTB 集成 |
| `参考\jade` | Jade | Jade provider |
| `参考\jei` | JEI | JEI 集成 |
| `参考\kubejs` | KubeJS | KubeJS 集成 |

**注意**：

- `参考\` 下**没有 GTCEu 源码**。GTCEu 1.4.4 的 API 结论必须用 `javap` 读 `libs/gtceu-1.20.1-1.4.4.jar`（等级 B）实证。
- gtlcore 运行期版本（1.2.3.2）**不适用**编译期 jar 的文档/教程，一切以 `参考\GTLCore-version-1.2.3.2-fix1` 源码为准。

## 四、已确证的跨模组结论（可直接复用）

### 4.1 gtlcore 运行期（1.2.3.2）关键 API

来源：`参考\GTLCore-version-1.2.3.2-fix1\src\main\java\org\gtlcore\gtlcore\...`

| API | 签名 / 语义 | 来源 |
|---|---|---|
| `RecipeRunnerHelper.matchRecipeInput(holder, recipe)` | 不消耗的输入模拟校验；内部走 `RecipeRunner.handle(recipe, IO.IN, holder, recipe.inputs, emptyMap, false, recipe, true)` | `api/recipe/RecipeRunnerHelper.java` |
| `RecipeRunnerHelper.matchRecipeOutput(holder, recipe)` | 不消耗的输出校验 | 同上 |
| `RecipeRunnerHelper.handleRecipeInput(machine, recipe)` | **实际消耗**输入（旧行为；装配期禁止使用以免吞材料） | 同上 |
| `RecipeRunnerHelper.matchRecipe(machine, recipe)` | 输入 + 输出双向校验 | 同上 |
| `RecipeResult` | `SUCCESS` / `FAIL_INPUT` / `FAIL_OUTPUT` / `FAIL_NO_ENOUGH_EU_IN` 等；`RecipeResult.of(machine, result)` 写入机器 recipeStatus | `api/recipe/RecipeResult.java` |
| `IParallelLogic.getParallel(holder, recipe, limit)` | `getMaxParallel`（输入可取量）→ 非 0 时再 `getMinParallel`（输出空间）取最小 | `api/recipe/IParallelLogic.java` |
| `IParallelLogic.getRecipeOutputChance(holder, recipe)` | 机会产出结算 | 同上 |
| `BatchProcessing` | `apply(machine, recipe)`：按时间窗 + 输入/输出可取量算批处理倍率并 `scaleRecipe`（tickInputs 不放大、duration ×N） | `api/recipe/BatchProcessing.java` |
| `MultipleRecipesLogic` | gtlcore 多配方合并逻辑（装配期直接 `handleRecipeInput` 消耗——本项目改成了非消耗模拟，见 `MutableRecipesLogic`） | `common/machine/trait/MultipleRecipesLogic.java` |
| `MEPatternBufferPartMachine` | ME 合成样板缓冲区机器；1.2.3.2 自带 `getMERecipeHandlerTraits()` | `common/machine/multiblock/...`（参考源码） |
| `IMECraftIOPart.init(Set<BlockPos>)` | 分子操纵端口只有收到样板核心位置集合后，才会把样板核心库存并入自己的样板槽；gtlcore 在 `MolecularAssemblerMultiblockMachineBase.update()` 中调用 | `api/machine/trait/AECraft/IMECraftIOPart.java`；`api/machine/multiblock/MolecularAssemblerMultiblockMachineBase.java:146-183`（等级 A） |
| `WirelessAeNetworkRuntime.isWirelessMeTargetId(ResourceLocation)` | 无线 AE 网络的目标白名单（gtceu / gtmthings / gtlcore 的 ME 仓室 ID）；非白名单方块需 Mixin 放行才能被无线绑定工具识别 | `integration/ae2/wireless/WirelessAeNetworkRuntime.java:1531-1540`（等级 A，1.2.3.3） |
| `DraggableScrollableWidgetGroup` | LDLib 滚动容器；`setYScrollBarWidth(int)`、`setYBarStyle(IGuiTexture, IGuiTexture)`、`setScrollWheelDirection(VERTICAL)` 可构建向下滚动列表；子控件按内容坐标排布，容器负责裁剪与滚动；`addWidget` 与滚动偏移会调用子控件 `setVisible` 隐藏屏幕外控件 | `ldlib-forge-1.20.1-1.0.29.b-sources.jar`（等级 B，2026-11-22） |
| `SlotWidget` 可见性 | `canPutStack`/`canTakeStack` 经 `isEnabled()` 检查 `isVisible()`；滚动列表若用可见性裁剪，会让服务端把屏幕外槽位判为不可用并退回物品，需让槽位覆写 `isVisible()` 恒为 true，绘制裁剪交给滚动容器 scissor | `ldlib-forge-1.20.1-1.0.29.b-sources.jar`（等级 B，2026-11-22） |
| `MultiblockMachineDefinition.setPatternFactory(Supplier<BlockPattern>)` | 公开替换多方块结构；`getMatchingShapes()` 在 `shapes` 为空时使用 `patternFactory`，可在注册后覆盖第三方机器结构而无需 Mixin | `MultiblockMachineDefinition.java:32-69`、`MultiblockMachineBuilder.java:129-137`（等级 A，2026-11-22） |
| `gtceu:circuit_assembly_line` | GTL Core 通过 GTCEu Registrate 注册；定义字段为 `AdvancedMultiBlockMachineA.CIRCUIT_ASSEMBLY_LINE`，结构常量为 `AdvancedMultiBlockMachineStructureA.CIRCUIT_ASSEMBLY_LINE`；编译期 gtlcore jar 未暴露 `GTMachines.HUGE_FLUID_IMPORT_HATCH`，覆盖时使用 `PartAbility.IMPORT_FLUIDS_4X` / `EXPORT_FLUIDS_4X` 表达 | `GTLCore 1.2.3.3` 源码与 `libs/gtlcore-1.2.3.2-fix3.jar` javap（等级 A，2026-11-22） |
| `AEPatternViewExtendSlotWidget` | gtlcore 样板槽控件；`setOnPatternSlotChanged(Runnable)` 在槽位变化后回调，`setOnMiddleClick(Runnable)` 接收中键点击；继承 `setOccupiedTexture`、`setItemHook`、`setBackground`、`setOnAddedTooltips` | `javap gtlcore-1.2.3.2-fix3.jar`（等级 B，2026-11-21） |
| gtlcore 配方查询 | 普通 `RecipeLogic` 取首条可用候选；多配方 `MultipleRecipesLogic` 完整迭代，并受机器并行与 `totalEu / maxEUt > 20 × 500` 预算终止，无候选条数配置或 TTL 计划缓存 | `GTLCore-version-1.2.3.3`：`RecipeLogicMixin.java:180-345`、`MultipleRecipesLogic.java:95-136`（等级 A，2026-11-22） |
| 本项目多方块基类分层 | 显式继承 6 个 `api/machine/mutable` 基类；`MutableRecipesLogic` 负责 long 并行、跨配方合并、独立槽模式、无线 EU 与批处理；gtlcore 采用 GTCEu 原生基类 + 全局 Mixin + `MultipleRecipesLogic` + 静态 `RecipeModifier`；gtlcore 不存在 `MultiTypeRecipeLogic`，本项目用 `MultiTypeMutableRecipesLogic` 补全多类型搜索 | 两边源码盘点（等级 A，2026-11-23） |
| GTLAdditions 多方块基类分层 | Kotlin 自有 API 继承 gtlcore 基类，Java Mixin 改写 gtlcore/GTCEu；自有 `MutableRecipesLogic` 为单/多配方模式切换版，`GTLAddMultipleRecipesLogic` 用公平分配，`MultipleRecipesLogicMixin` 用 `@Overwrite` 注入无线与线程；无线 EU 使用 BigInteger，线程为 `128 + additionalThread`，多配方模式关闭 gtlcore 批处理配置 | `GTLAdditions-3.3.1Custom` 源码盘点（等级 A，2026-11-23） |
| GTLAdditions 单/多配方模式 | `MutableRecipesLogic` 用 `@DescSynced boolean useMultipleRecipes` 切换模式；单配方调用 `super.findAndHandleRecipe()`、`super.onRecipeFinish()`、`super.handleRecipeWorking()`；本项目已按同一入口语义补齐模式访问器、字段同步与完成阶段分支 | `GTLAdditions MutableRecipesLogic.kt:44-45,74-96,298-302`（等级 A，2026-11-23） |
| 本项目 4.8.6 事件驱动 | `NetMasterRegistry` 与 `ActiveAdaptiveNetStatistics` 提供监听器；`GTLSupbProviderManager` 提供 data/compute 监听器；电压调控器用库存变更事件；量子连接新增协议消息时协议版本 5→6 且消息追加末尾 | 4.8.6 源码与 `GTLSupbNetworkHandler.java`（等级 A，2026-11-23） |
| 量子纠缠最终连接语义 | 最终恢复为队伍强加载区块自动连接 + 工具黑名单排除；仓室 NBT 键为 `gtlsupb_cancelled`；传送按钮读取工具位置列表，传送包为协议 6 的尾追加消息 | `OverlimitQuantumEntanglementWirelessManager.java`、`OverlimitQuantumEntanglementHatchPartMachine.java`、`C2SQuantumTeleportPacket.java`（等级 A，2026-11-23） |
| gtlcore 成型后唤醒 | gtlcore 在 `WorkableMultiblockMachineMixin.onStructureFormed` 尾部用 `ServerLevel#getServer().execute(...)` 延迟一 tick 重建配方部件；模式切换与库存变化调用 `RecipeLogic#updateTickSubscription()`，`RecipeLogic` 的 `% 5` 是搜索节流，不是 5 次重试上限 | `GTLCore-version-1.2.3.3`：`WorkableMultiblockMachineMixin.java:66-88,363-366`（等级 A，2026-11-23） |
| FTB 任务检测事件化边界 | `AEQuestDetector` 当前按每仓 10~200 tick 间隔扫描，默认 40 tick；后续事件驱动只能置 dirty，必须保留最小间隔合并，并过滤未完成任务的关注 key；每次库存变化立即扫描会把默认 0.5 次/秒提高到 20 次/秒。本轮未修改 | `AEQuestDetector.java:26,49-51`、`AEQuestSettings.java:17-20`（等级 A，2026-11-23） |
| 五类对象事件驱动 | 电网发射仓用能量容器变化推送；零能耗基类生成 EU 时即时推送、失败 20 tick 重试；执序者用模式槽监听；模块用连接/成型回调；样板总成用一次性 TickTask。ME 驱动与量子管理器已回退为周期订阅/扫描 | 4.8.6 源码（等级 A，2026-11-23） |
| GTCEu 消声仓能力 | `PartAbility.MUFFLER` 对应 `MufflerPartMachine`；结构用 `Predicates.abilities(PartAbility.MUFFLER)` 识别；部件必须位于结构外缘且正前方无遮挡，否则 `modifyRecipe()` 返回 null；回收概率为 `max(1, tier × 10)%` | `GTCEu 1.4.4`：`PartAbility.java:39`、`MufflerPartMachine.java:40-82`、`IMufflerMachine.java:60-100`（等级 A，2026-11-24） |
| ME消声仓实现 | `gtlsupb:overlimit_me_muffler_hatch` 继承 `MufflerPartMachine` 并注册 `PartAbility.MUFFLER`，另有 `GridNodeHolder` ME 节点；回收物按原概率生成后优先插入 ME，剩余保留本地库存 | `MEMufflerPartMachine.java`、`GTLSupbMachines.java`（等级 A，2026-11-24） |
| 增广ME输出总成同款界面 | gtlcore `MEExtendedOutputPartMachineBase.createUIWidget()` 使用 `LabelWidget` 在线状态 + `MEOutListGridWidget(5,20,7,Object2LongMap<AEKey>)`；本项目 ME 消声仓使用 `FancyMachineUIWidget` 自动放置玩家背包，并绑定持久化 `buffer`，按 `AEKey` 聚合回收物；ME 在线后由 `AEUtils.reFunds` 批量插入 | `GTLCore-version-1.2.3.3`：`MEExtendedOutputPartMachineBase.java:88-97`、`MEExtendedOutputPartMachine.java:117-177,196-217`（等级 A，2026-11-24） |
| 超限综合总成 | `gtlsupb:overlimit_integrated_hatch` 注册四种 IO 能力，五类 handler 同时生效；UI 使用 UOK 式 `TabsWidget` 侧栏分页，无模式槽；主页面 158×146 放 ME 输出列表，库存页放 3 行 `AEDualConfigWidget`，电路页固定 158×146，标签过滤放左侧配置面板 | `OverlimitIntegratedHatchPartMachine.java`、`GTLSupbMachines.java`（等级 A，2026-11-24） |
| FancyMachineUI 尺寸 | `FancyMachineUIWidget` 按 `IFancyUIProvider.createMainPage()` 返回尺寸自动扩窗并在下方追加 `PlayerInventoryWidget`；主页面控件不得超出返回尺寸，否则 UI 挤到背包区域；子页覆写 `hasPlayerInventory()=false` 可避免背包叠加 | `GTCEu 1.4.4`：`FancyMachineUIWidget.java:53-75,191-230`（等级 A，2026-11-24） |
| AE节点输出唤醒 | AE2 `TickingRequest` 的 `canBeAlerted=true` 必须在 buffer 从空变非空后调用 `ITickManager.alertDevice/wakeDevice`，否则休眠设备不会执行 `reFunds`；成功插入后需 `markDirty()` 持久化剩余 buffer | `AE2 15.2.1`：`TickingRequest.java:31-37`、`ITickManager.java`（等级 A，2026-11-24） |
| BigInteger 输出与AE边界 | AE2 15 的 `GenericStack`、`MEStorage.insert/extract` 均为 `long`；本项目使用 `Object2ObjectMap<AEKey,BigInteger>` 做缓冲与显示，通过 `NumberFormatUtil.scientific(BigInteger)` 显示，按 `Long.MAX_VALUE` 分片调用 `StorageHelper.poweredInsert`；AE2 原生单 key 仍受 long 上限 | `MEBigIntegerTransfer.java`、`BigIntegerMEOutListGridWidget.java`、`NumberFormatUtil.java`（等级 A，2026-11-24） |
| 电路编码识别 | Enhancedcore 电路编码仓使用 `IntCircuitIngredient.circuitInput(0..32)` 作为配方输入匹配器，不能改成 ItemStack；选择变化需通知 `RecipeLogic.markLastRecipeDirty()` 与 `updateTickSubscription()`，客户端点击需经 C2S 包同步服务器并逐 tick 刷新按钮纹理；按钮按 9×4 布局可避免遮挡文字和全选/清空 | `GTL-Enhancedcore-master`：`CircuitEncoderHatchMachine.java`、`CircuitEncoderRecipeHandler.java`（等级 A 2026-11-25） |
| 量子黑名单自动连接 | `PartAbility.MUFFLER` 之外的量子仓自动扫描必须在候选目标入 `validTargets` 前和建连前调用 `isBlacklisted()`；仅 `blacklist()` 当次拆连会被下次 40 tick 扫描重建 | `OverlimitQuantumEntanglementWirelessManager.java`（等级 A，2026-11-25） |
| AE库存输入识别 | `ExportOnlyAEItemList` / `ExportOnlyAEFluidList` 已实现 `ICapabilityTrait`；配置槽写入 `GenericStack(key,1)` 后需同步 `setStock`，库存槽由标准 `handleRecipeInner` 消耗才能被控制器识别 | `GTCEu 1.4.4`：`ExportOnlyAEItemList.java`、`ExportOnlyAEItemSlot.java`（等级 A，2026-11-25） |
| Provider运行状态 | 用户要求回退事件驱动改造，当前恢复为 provider 计数读取；不再以 `isFormed()`/`isWorkingEnabled()` 作为 Jade 可用性前置 | `AbstractProviderMachine.java`、`HatchJadeProvider.java`（等级 A，2026-11-25） |
| 综合仓输入输出收集 | gtlcore `WorkableMultiblockMachineMixin#handleNormalPart` 在部件同时有 `IO.IN` / `IO.OUT` 时只收集输出；本项目必须保留 `IntegratedHatchRecipeHandlerMixin` 在 TAIL 补入 `IO.IN`，否则综合总成所有输入丢失 | `WorkableMultiblockMachineMixin.java:383-421`、`IntegratedHatchRecipeHandlerMixin.java`（等级 A，2026-11-25） |
| 标签页闪退 | `AEDualConfigWidget` 的 item/fluid 列表必须同容量，零容量列表会在初始化时越界；单类型页用另一类型的同容量空列表占位 | `AEDualConfigWidget.java`、综合仓 UI（等级 A，2026-11-25） |
| 状态刷新 | GUI 修改的持久化配置必须 `@Persisted @DescSynced`，客户端先本地更新再通过 C2S 包写服务器，服务器 `markDirty()` 并在必要时刷新配方缓存；字符串字段单独使用带 `writeUtf/readUtf` 的 C2S 包，协议版本同步递增 | `C2SIntegratedHatchPacket.java`、`C2SIntegratedTagFilterPacket.java`、`C2SPatternCustomNamePacket.java`（等级 A，2026-11-25） |
| 动态输出滚动 | `MEOutListGridWidget` 的滚动视口固定，按材料数量增加/移除 DisplaySlot 子控件；不要按材料数量改变视口高度，否则页面溢出 | `MEOutListGridWidget.java`、`BigIntegerMEOutListGridWidget.java`（等级 A，2026-11-25） |
| 普通输出消费协议 | GTLCore `RecipeHandlePart` 只有 handler 返回 `null` 才判定完全消费；输出 handler 返回空 `List` 会导致模拟匹配失败并阻止配方启动 | `RecipeHandlePart.java`、综合总成输出 handler（等级 A，2026-11-25） |
| 帧序列材质 | 机器/物品材质采用 32×256、竖向 8 帧透明 PNG；`.png.mcmeta` 固定 `width=32,height=32,frames=0..7`。GUI 图标采用 32×32 静态透明 PNG，禁止附加 `.png.mcmeta` | `textures/**/*.png`、`*.png.mcmeta`（等级 A，2026-11-25） |
| 部件几何 | `OverlayTieredMachineRenderer` 会把覆盖模型的所有 quads 加到基础机壳上；共用 `gtceu:block/overlay/front` 只能生成同一前脸。要改变外形必须为每种部件提供独立 `elements` 模型，模型 UV 使用 0–16 坐标 | `OverlayTieredMachineRenderer.java`、本项目 `models/block/machine/part/*.json`（等级 A，2026-11-25） |
| 多方块仓输入 trait | `MachineTrait` 构造自动 `attachTraits`；原生 `ItemBusPartMachine` / `FluidHatchPartMachine` 使用 `NotifiableItemStackHandler` / `NotifiableFluidTank`，依赖 `getRecipeHandlers()` 收集 `IO.IN`。独立缓存要稳定参与配方，应使用同型原生 handler，不使用自定义 `AEKey -> BigInteger` trait | `MachineTrait.java:34-38`、`ItemBusPartMachine.java:64-102`、`FluidHatchPartMachine.java:67-97`、`NotifiableItemStackHandler.java`、`NotifiableFluidTank.java`（等级 A，2026-11-25） |
| 服务端可见粒子 | `Level.addParticle` 在服务端不发包；服务端必须用 `ServerLevel.sendParticles(player, options, overrideLimiter, x, y, z, count, dx, dy, dz, speed)` | `UltimateTerminalBehavior.java`（等级 A，2026-11-25） |
| 请求器样板识别 | 请求器从自身持久化 `ItemStackTransfer` 的编码处理样板读取 `AEProcessingPattern.getSparseInputs()`；不扫描 ME 缓存，避免把网络上任意样板当作配置 | `OverlimitIntegratedHatchPartMachine.java`（等级 A，2026-11-25） |

### 4.2 GTCEu 1.4.4 关键 API（javap 实证）

来源：`javap` 读 `libs/gtceu-1.20.1-1.4.4.jar`（等级 B）。

| API | 签名 / 语义 |
|---|---|
| `GTRecipe.copy(ContentModifier, boolean)` | 复制配方并应用内容修饰器（不修改原配方）；`ContentModifier.multiplier(double)` 放大内容 |
| `GTRecipeBuilder.ofRaw().buildRawRecipe()` | 构建原始配方（无类型约束） |
| `RecipeLogic` | 机器配方逻辑基类：`findAndHandleRecipe` / `onRecipeFinish` / `handleRecipeWorking` / `setupRecipe` / `handleTickRecipe` |
| `IRecipeLogicMachine` | 机器配方能力接口（`getRecipeLogic()`、`getOverclockVoltage()`、`hasProxies()` 等） |
| `Content` | 配方内容项 `(content, chance, maxChance, tierChanceBoost, slotName, uiName)` |
| `ItemRecipeCapability.CAP` / `FluidRecipeCapability.CAP` / `EURecipeCapability.CAP` | 配方能力键 |
| `WorkableElectricMultiblockMachine` | 多方块电力机器基类（`getOverclockVoltage` 等） |

### 4.3 运行期名字与源码名字不同（mixin 关键）

Forge 1.20.1 生产环境下，**原版方法的运行期名字是 SRG 名**（如 `m_150399_`），而源码里是官方名。模组自己的方法保留原名。**推论**：任何按方法名注入或在字节码层面按名字查找的代码，都必须先确认运行期名字（用 `javap` 读目标 jar）。

### 4.4 本项目关键约定（源码实证）

| 约定 | 位置 |
|---|---|
| `Button.Builder.tooltip()` 需要 `Tooltip` 对象；`Component` 需用 `Tooltip.create()` 包裹（1.20.1） | `common/machine/...`（机器 GUI） |
| 合并配方 EU 一律放 `tickInputs`（每 tick 扣电），绝不放进 `inputs`——否则 ME 配方处理器遇非物品/流体能力项崩溃 | `utils/RecipeCalculationHelper.buildNormalRecipe` |
| 批处理倍率读取：`getBatchMultiplier()` 走 `IBatchProcessingMachine.isBatchEnabled()/getBatchSize()` | `api/machine/logic/MutableRecipesLogic.java` |
| `RecipeRunnerHelper.matchRecipeInput/matchRecipeOutput` 只模拟不消耗；`handleRecipeInput` 才真实扣料 | `javap libs/gtlcore-1.2.3.2-fix3.jar`（2026-11-02 实证） |
| `RecipeLogic.setupRecipe()` 内部顺序：`machine.beforeWorking` → `handleRecipeIO(IN)`；`handleRecipeIO` 为 protected 可覆写 | `javap libs/gtceu-1.20.1-1.4.4.jar`（2026-11-02 实证） |
| `RecipeLogic.onRecipeFinish()` 默认在 `lastRecipe` 仍匹配时直接 `setupRecipe(lastRecipe)`；一次性合并配方必须覆写为重新搜索 | 同上（字节码实证） |
| `IItemUIFactory` 的默认 `use` 已在服务端调用 `HeldItemUIFactory.openUI`，实现该接口的物品无需再覆写 `use` | `参考\gtceu\src\main\java\com\gregtechceu\gtceu\api\item\component\IItemUIFactory.java:18-25`（等级 A） |
| `ComponentItem` 把 `useOn` / `onItemUseFirst` 分发给已附加的 `IInteractionItem` 组件 | `参考\gtceu\...\api\item\ComponentItem.java:133-142,222-230`（等级 A） |
| 客户端 `MultiPlayerGameMode.useItemOn` 先调用 `ItemStack.onItemUseFirst`；结果非 PASS 时立即返回，`ServerboundUseItemOnPacket` 不会发出。需要服务端执行的物品交互必须走 `useOn`，不能在客户端 `onItemUseFirst` 返回 SUCCESS | 合并 jar `net.minecraft.client.multiplayer.MultiPlayerGameMode`（javap 字节码，等级 B，2026-11-03） |
| 网络包 `PROTOCOL_VERSION="3"` 双向严格匹配 | `network/GTLSupbNetworkHandler.java`（2026-11-03） |
| GTCEu 配方 JSON 的 `"type":"gtceu:circuit"` 内容无法匹配机器配方；一号电路启动配方必须用 `GTRecipeBuilder.circuitMeta(1)` 代码注册 | GTLUltimate `GTLUltimateRecipes` 实证（2026-11-03）；本工程 `GTLSupbMachineRecipes` |

---

## 五、维护规则

1. 新发现的 API 事实写入本文件对应章节，标注证据等级与来源（`路径:行号` 或 `javap` 命令）。
2. 分册/章节中标注「未确认」的条目，一旦解决就地改写，不要追加矛盾段落。
3. 升级任何依赖（尤其 gtlcore / GTCEu）后，必须**复核本文件受影响条目**，未复核的标记为「待复核」。
4. 本索引中的版本表必须与 `启动上下文.md` 一致（`执行规则.md` 规则 20）。
