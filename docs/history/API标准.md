# API 标准（归档）

> 本文件为早期核心规范归档，仅供备查。现行约定见 `docs/TIPS规范.md` 与 `API_STANDARDS/AGENTS.md`。

## 命名

- 类名大驼峰；方法/字段小驼峰；常量全大写下划线。
- 本模组内部私有辅助统一前缀 `gtlsupb$`，避免与 GTCEu/gtlcore 继承成员冲突。
- 语言键统一 `gtlsupb.*` 单命名空间，中英一一对应。

## 注释与异常

- 说明"为什么"而非"做了什么"；涉及历史缺陷处标注根因与修复。
- 不吞异常：需要容错处使用 `RecipeSafety` 纯 JDK 原语，而非包大段 `try/catch`。

## 测试

- 纯 JDK 逻辑抽到 `utils/`，用 `recipeSafetyTest`（`main` + 断言）覆盖，挂 `check`。
- 需要启动游戏的逻辑不写单测，以实机验证为准并在变更记录留痕。

## 构建

- 必须通过 `spotlessApply` 格式检查；离线构建；产物用 `javap` 抽查符号。
