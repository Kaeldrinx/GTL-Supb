# 提示词API准则代码库

> 一套系统、规范、可操作的 API 设计、开发与维护准则，基于对
> [GTLSUPBJAVA](../GTLSUPBJAVA) 项目（本库主项目）的全面分析提炼而成，
> 并兼容 REST、语义化版本（SemVer）、OpenAPI 等行业通用规范。

## 1. 定位

本代码库回答三个问题：

| 问题 | 章节 |
| --- | --- |
| API 应该长什么样？ | [docs/01-API设计原则.md](docs/01-API设计原则.md) |
| 请求与响应如何组织、序列化？ | [docs/02-请求响应格式规范.md](docs/02-请求响应格式规范.md) |
| 出错、越权、升级、文档如何处理？ | [docs/03](docs/03-错误处理机制.md) ~ [docs/06](docs/06-文档生成标准.md) |

并附**可直接复用的模板代码**（[examples/](examples/)）。新 API 开发流程：

1. 阅读 `docs/01` 确认设计是否符合原则；
2. 到 `examples/` 找到对应场景模板，复制起步；
3. 完成后逐条走完对应章节末尾的**检查清单**；
4. 提交前过一遍 `docs/07` 的**评审清单**。

## 2. 准则等级

| 等级 | 含义 | 违反后果 |
| --- | --- | --- |
| **MUST** | 必须遵守 | 视为缺陷，评审必须拦截 |
| **SHOULD** | 应当遵守，可豁免但需在评审中说明理由 | 评审时审查 |
| **MAY** | 可以选用 | 无约束 |

## 3. 目录结构

```
API准则代码库/
├── README.md                        # 本文件：总览、使用方式、术语
├── docs/                            # 准则文档（中文，按主题分章）
│   ├── 01-API设计原则.md            # 12 条设计原则（SOLID + 契约 + 安全默认）
│   ├── 02-请求响应格式规范.md        # 网络包/内部 API/REST JSON 三层格式规范
│   ├── 03-错误处理机制.md            # 错误码、静默失败、日志、重试
│   ├── 04-认证授权流程.md            # 身份识别、归属校验、越权防护
│   ├── 05-版本控制策略.md            # 语义化版本、协议版本、依赖锁定
│   ├── 06-文档生成标准.md            # 注释、Javadoc、语言文件、接口文档
│   └── 07-最佳实践示例.md            # 完整链路、能力解耦、反模式、评审清单
└── examples/                        # 可复用参考代码（模板性质，非可编译产物）
    ├── network/                     # 请求-响应式网络 API 模板（仿 GTLSUPBJAVA）
    ├── api-layer/                   # 能力接口/PartAbility/trait 模板（仿 GTLSUPBJAVA）
    ├── error/                       # 错误码枚举与错误响应构建
    └── json/                        # REST 请求/响应/错误 JSON 示例
```

## 4. 项目来源与映射

准则并非凭空制定，每条均可在主项目 GTLSUPBJAVA 中找到对应实现，文档中以链接标注。核心对应关系：

| 准则主题 | GTLSUPBJAVA 对应实现 |
| --- | --- |
| 网络请求-响应 | [GTLSupbNetworkHandler.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/GTLSupbNetworkHandler.java)、[C2SWAPConnectionPacket.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/network/C2SWAPConnectionPacket.java) |
| 能力接口解耦 | [IBatchProcessingMachine.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/api/machine/IBatchProcessingMachine.java)、[ILimitedDurationMachine.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/api/machine/ILimitedDurationMachine.java) |
| 逻辑扩展（trait/mixin） | [MutableRecipesLogic.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/api/machine/logic/MutableRecipesLogic.java)、[RecipeLogicProviderMixin.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/mixin/gtceu/recipe/RecipeLogicProviderMixin.java)、[ParallelProviderMixin.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/mixin/gtceu/integration/ParallelProviderMixin.java) |
| 外部模组集成 | [AEGridTrackerService.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/integration/ae2/AEGridTrackerService.java)、[EnergyBroadcastHelper.java](../GTLSUPBJAVA/src/main/java/com/gtlsupb/gtlsupb/utils/EnergyBroadcastHelper.java) |

## 5. 适用范围

本准则覆盖三种 API 形态：

1. **模组内部网络 API**（C2S/S2C 数据包）—— 本代码库主场景；
2. **内部 Java API**（能力接口、trait、事件、服务注册）—— 本代码库主场景；
3. **对外 HTTP API**（REST/JSON）—— 以行业通用规范对齐，供未来扩展。

## 6. 术语表

| 术语 | 含义 |
| --- | --- |
| C2S / S2C | 客户端→服务端 / 服务端→客户端 的数据包 |
| 契约（Contract） | API 的对外承诺：入参、出参、行为、错误语义 |
| 服务端权威 | 所有状态校验与修改以服务端为准，客户端数据一律不可信 |
| 请求-响应 | 一次 C2S 触发、服务端校验处理后回发 S2C 的成对交互 |
| 能力（Ability） | 部件/机器对外暴露的能力抽象（如 PartAbility、能力接口） |
| trait | GTCEu 体系中可复用的机器逻辑扩展单元 |
