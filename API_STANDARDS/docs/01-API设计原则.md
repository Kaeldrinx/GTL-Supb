# 01 · API 设计原则

> 目标：让 API 具备**可操作性、可扩展性、可维护性**，并与行业通用设计（SOLID、契约优先、安全默认）兼容。
> 每条原则给出：定义、准则等级、项目正例、常见反例、评审要点。

---

## P1. 契约优先（Contract First）

**定义**：先明确 API 的对外契约（做什么、输入什么、输出什么、失败怎么办），再写实现。契约即文档，注释中的契约描述必须与行为一致。

- **MUST**：新 API 在编码前先写契约描述（类级 Javadoc 中的触发链路、校验规则、回发行为）。
- **MUST**：契约变更必须同步修改注释与调用方，禁止"注释与行为脱节"。

**项目正例**：[C2SWAPConnectionPacket.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/C2SWAPConnectionPacket.java) 的类注释完整描述了三类动作（查询/跳转/取消）、触发点、服务端回发与校验规则，读注释即可知道整个 API 行为。

**反例**：接口只有方法签名没有行为说明，实现细节与文档不一致。

---

## P2. 单一职责（SRP）

**定义**：一个类/接口只有一个改变的理由。

- **MUST**：一个数据包只承载一个业务目的。动作可通过动作常量细分（如查询/跳转/取消），但不可混合不相关业务。
- **SHOULD**：单文件超过 500 行应拆分（如逻辑抽到 trait / helper / 独立 service）。
- **MUST**：包按功能模块划分（network / integration / api / common / utils / data），跨职责内容不得混放。

**项目正例**：
- [C2SWAPConnectionPacket.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/C2SWAPConnectionPacket.java)：三个动作均属"WAP 连接列表"这一业务；
- [GTLSupbNetworkHandler.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/GTLSupbNetworkHandler.java)：只负责通道创建与数据包注册；
- [MutableRecipesLogic.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/api/machine/logic/MutableRecipesLogic.java)：并行分配、合并配方构建、批处理倍率读取从机器类中拆出为独立 trait。

**反例**：NetworkHandler 里既注册包又写业务逻辑；一个包同时处理任务提交与渲染同步。

---

## P3. 开闭原则（OCP）

**定义**：对扩展开放、对修改关闭。新增能力时通过新增接口/注册项扩展，而非修改既有核心类。

- **SHOULD**：新增机器能力时，优先新增 `PartAbility` 常量 + 能力接口（见 examples/api-layer），而不是改动上游核心类。
- **SHOULD**：需要改变上游类行为时，优先使用 Mixin 定向注入，且注入点按目标模组分目录管理。
- **MUST**：注入逻辑必须"按需生效"——只有满足条件（如装有对应 hatch）才改变行为，避免全局副作用。

**项目正例**：
- [GTLSupbMachines.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/common/machine/GTLSupbMachines.java) 通过标准 `PartAbility` 常量（IMPORT_ITEMS / EXPORT_FLUIDS 等）声明机器能力，不修改上游核心类；
- [IBatchProcessingMachine.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/api/machine/IBatchProcessingMachine.java) 以新增能力接口扩展机器（批处理开关与倍率），机器实现接口即获得新能力；
- [RecipeLogicProviderMixin.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/mixin/gtceu/recipe/RecipeLogicProviderMixin.java) 定向注入无线配方显示，仅在 `IWirelessGTRecipe` 时生效；
- mixin 目录按目标模组划分（mixin/gtceu、mixin/gtlcore、mixin/ae2、mixin/ftbquests 等），见 [GTLSUPBJAVA mixin 包](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/mixin)。

**反例**：直接修改第三方 jar 源码；注入的 mixin 无条件改变全局行为。

---

## P4. 依赖倒置（DIP）

**定义**：依赖抽象而不是具体实现。高层模块（机器逻辑）依赖接口/能力，不依赖具体部件类。

- **MUST**：机器逻辑通过能力接口与 `PartAbility` 查询部件，禁止 `instanceof` 具体实现类做功能判断。
- **SHOULD**：跨模组服务通过服务注册/事件解耦（如 AE2 的 GridServices 注册追踪服务），不直接 new 对方实现。

**项目正例**：
- [MutableRecipesLogic.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/api/machine/logic/MutableRecipesLogic.java) 通过 `IBatchProcessingMachine` / `ILimitedDurationMachine` 能力接口读取批处理倍率与运行时长，不感知具体机器实现类；
- [AEGridTrackerService.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/integration/ae2/AEGridTrackerService.java) 以服务方式接入 AE2 网格。

**反例**：`if (part instanceof 具体仓实现类)` 式硬编码；直接持有对方模组类实例。

---

## P5. 接口隔离（ISP）

**定义**：使用最小接口，不强迫实现方实现不需要的方法。

- **MUST**：能力接口保持最小——一个能力一个方法组，`IBatchProcessingMachine` 只含批处理开关/倍率一组方法。
- **SHOULD**：接口方法命名直接表达语义，Javadoc 说明返回值语义与边界（如"未开启批处理时返回 1"）。

**项目正例**：[ILimitedDurationMachine.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/api/machine/ILimitedDurationMachine.java) 默认方法 + 契约注释；[IBatchProcessingMachine.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/api/machine/IBatchProcessingMachine.java) 同理。

**反例**：一个大而全的 `IHatch` 接口包含十几个方法，新部件被迫实现无关方法。

---

## P6. 最少知识（LoD）

**定义**：降低耦合，对象只与直接相关的协作方通信，复杂计算收敛到专用工具/服务层。

- **SHOULD**：复杂配方计算、分配算法收敛到 utils（如 [RecipeCalculationHelper.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/utils/RecipeCalculationHelper.java)、[FairAllocationUtils.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/utils/FairAllocationUtils.java)），调用方只暴露简单入口。
- **SHOULD**：数据包 handler 只做"校验 + 分发"，业务执行委托给对应 manager/helper。

**项目正例**：C2SWAPConnectionPacket 的 handler 校验后委托 `WirelessAccessPointWirelessManager` 执行连接查询/销毁。

**反例**：数据包 handler 里写 300 行业务代码；机器类直接遍历世界方块实现广播。

---

## P7. 服务端权威（Server Authority）

**定义**：所有状态校验、修改与权威数据源在服务端；客户端数据与请求一律视为不可信输入。

- **MUST**：数据包逻辑在 `ctx.get().enqueueWork()` 中于服务端线程执行。
- **MUST**：客户端发来的每个字段（containerId、坐标、动作）都必须经服务端重新校验。
- **MUST**：客户端缓存（如 AE 任务设置、WAP 状态）仅用于展示，最终状态以服务端回发为准。

**项目正例**：[C2SWAPConnectionPacket.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/C2SWAPConnectionPacket.java) 的 `handle()` 全部在 `enqueueWork` 内校验后执行；状态类数据包（[S2CWAPStatePacket.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/S2CWAPStatePacket.java)）由服务端下发。

---

## P8. 幂等与可重试

**定义**：重复执行同一请求不应产生重复副作用；临时失败可安全重试。

- **SHOULD**：查询类操作天然幂等（重发无副作用）。
- **SHOULD**：变更类操作带归属/幂等标识（如 containerId 关联），服务端校验通过才执行，重复包被校验拦截。
- **MAY**：对外 HTTP API 支持幂等键（Idempotency-Key）。

**项目正例**：WAP 连接"取消"在服务端校验目标仍存在于连接列表后才执行，重复包自然失效。

---

## P9. 安全默认（Secure by Default）

**定义**：默认拒绝，显式放行；任何未通过校验的请求静默终止。

- **MUST**：身份（sender）、会话（containerId）、菜单类型、目标归属四类校验缺一不可（详见 [04-认证授权流程](04-认证授权流程.md)）。
- **MUST**：未知动作/未知枚举值走 `default` 分支静默忽略，不抛异常不误执行。
- **MUST**：涉及他人资产的操作（传送、销毁、修改）必须校验目标归属。

**项目正例**：[C2SWAPConnectionPacket.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/C2SWAPConnectionPacket.java) `default -> {}` 忽略未知动作；`resolveAndVerifyTarget` 校验目标确在当前 WAP 连接列表，防止伪造坐标操作他人方块。

---

## P10. 命名即语义（Naming as Contract）

**定义**：命名反映意图，读名知义。

- **MUST**：网络包类名 `C2S<S名>/S2C<S名>`（方向 + 业务语义 + 动作/状态后缀，如 `Toggle`/`State`/`List`/`Submit`/`Apply`/`Settings`/`Connection`）。
- **MUST**：动作使用具名常量（`ACTION_QUERY_LIST = 0`）而非裸魔法数字。
- **SHOULD**：Java 标识符 camelCase，常量 UPPER_SNAKE，JSON 字段 snake_case；类名名词、方法名动词。
- **SHOULD**：模组 ID、注册名统一小写下划线（gtlsupb、gtpcore）。

**项目正例**：[GTLSupbNetworkHandler.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/GTLSupbNetworkHandler.java) 中 14 个数据包的命名模式；[C2SWAPConnectionPacket.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/C2SWAPConnectionPacket.java) 的动作常量。

---

## P11. 演进兼容（Forward Compatibility）

**定义**：API 演进时优先保持向后兼容，破坏性变更显式管理。

- **MUST**：协议版本字符串（PROTOCOL_VERSION）与实现绑定，变更协议必须递增。
- **SHOULD**：序列化新增可选字段时用布尔 has 标记 + 长度前缀，旧端可安全忽略。
- **SHOULD**：废弃接口用 `@Deprecated` 标注并保留一个迁移期后再移除（详见 [05-版本控制策略](05-版本控制策略.md)）。

**项目正例**：[GTLSupbNetworkHandler.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/GTLSupbNetworkHandler.java) 的 `PROTOCOL_VERSION = "1"` 双向严格匹配；[C2SWAPConnectionPacket.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/C2SWAPConnectionPacket.java) 的可空目标字段用 `writeBoolean(hasTarget)` 标记。

---

## P12. 显式优于隐式（Explicit over Implicit）

**定义**：可空性、副作用、异常路径都要显式表达。

- **SHOULD**：可空返回值用 `Optional` 或明确 `@Nullable`，禁止裸返回 null 却不说明。
- **MUST**：可能失败的外部操作（读世界、跨维度）显式 try-catch 并给出降级值。
- **SHOULD**：副作用（传送、关闭容器、销毁连接）在代码中就近注释说明。

**项目正例**：[C2SWAPConnectionPacket.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/C2SWAPConnectionPacket.java) 的 `resolveBlockDisplayName` 对未加载区块/异常显式返回 `"??"` 降级。

---

## 评审要点（API 评审 5 问）

1. 契约是否完整且注释与行为一致？（P1）
2. 是否面向接口、最小接口、无硬编码依赖？（P4/P5/P6）
3. 服务端是否校验了身份、会话、类型、归属四件事？（P7/P9）
4. 命名是否读名知义、无魔法数字？（P10）
5. 演进是否兼容、废弃是否显式？（P11）
