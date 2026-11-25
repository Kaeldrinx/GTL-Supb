# GTL-Supb

GregTech Leisure Private goods expansion module —— 基于 Minecraft Forge 1.20.1 / GTCEu 1.4.4 的扩展模组，mod id 为 `gtlsupb`，当前版本 `4.8.6`。

## 主要内容

- 大量多方块机器与发电机/处理机器（虚空能量吸收器、通用工厂、炼金熔炉、蒸汽模块化工厂等）
- AE2 样板绑定、无限样板与量子纠缠相关逻辑
- 配方并行、概率改写与故障隔离计算
- FTB Quests、Jade、JEI 等第三方模组集成
- 客户端 UI、Mixin 补丁、C2S/S2C 网络包

源码位于 `src/main/java/com/gtlsupb/gtlsupb/`，资源位于 `src/main/resources/`，设计文档与变更记录见 `docs/`。

## 构建环境

| 项目 | 要求 |
|---|---|
| JDK | 17（例如 Temurin 17） |
| Gradle | 8.8，由 `gradlew.bat` / `gradle-wrapper.properties` 自动下载，无需单独安装 |
| 平台 | Minecraft 1.20.1 + Forge 47.3.7 |
| 映射 | Quilt（build.23）+ Parchment（2023.09.03）+ Mojang 官方映射 |
| 网络 | 首次构建或 Loom 缓存重建时需要联网下载 Minecraft/Forge/MCP 与 Maven 依赖 |
| 构建插件 | Architectury Loom 1.6-SNAPSHOT、Spotless 6.25.0 |

`gradle.properties` 集中管理版本号与 mod 元信息；`build.gradle` 定义依赖、资源占位符展开（写入 `META-INF/mods.toml`）、Mixin 配置与编译参数（UTF-8、Java 17）。

## 本地依赖

本仓库**不包含** `libs/` 目录中的第三方 jar（体积较大且部分为编译辅助包）。构建前需自行将以下 jar 放入项目根目录的 `libs/`：

- `gtlcore-1.2.3.2-fix3.jar`
- `gtmthings-1.3.5.a.jar`
- `ExtendedAE-1.20-1.1.12-forge.jar`
- `TravelAnchors-1.20.1-5.0.1.jar`
- `appliedenergistics2-forge-15.2.1.jar`
- `ftb-quests-forge-2001.4.22.jar`（仅编译期）
- `gtladditions-compile-only.jar`（仅编译期）
- `rubidium-extra-0.5.4.3+mc1.20.1-build.121.jar`（运行期）
- `jecharacters-1.20.1-forge-4.5.11-dev-shadow.jar`（运行期）

其余依赖（GTCEu、LDLib、Registrate、JEI、Jade、Architectury、FTB 系列等）由 `build.gradle` 中配置的远程 Maven 仓库解析。

## 构建命令

Windows PowerShell：

```powershell
$env:JAVA_HOME='C:\path\to\jdk-17'
.\gradlew.bat --stop
.\gradlew.bat spotlessApply build --console=plain
```

`spotlessApply` 会按 `spotless/` 下的配置统一格式并清理未使用的 import。构建产物位于 `build/libs/`：

- `GTL-Supb-4.8.6.jar`（版本号随 `gradle.properties` 的 `mod_version` 变化）
- `GTL-Supb-4.8.6-sources.jar`

## 验证

```powershell
.\gradlew.bat recipeSafetyTest
```

这是纯 JDK 测试（不启动游戏），覆盖 `safeDivide`、`clamp`、`at` 等边界与除零保护，已挂载到 `check` 任务；标准 `test` 任务被禁用。

构建后建议核对 jar 内存在 `META-INF/mods.toml`、`gtlsupb.mixin.json` 与 Loom 生成的 `GTL-Supb-refmap.json`，并确认新增机器/逻辑类已进入产物。

## 注意事项

- **不要默认加 `--offline`**：Loom 缓存锁残留或 Minecraft / Forge / MCP 版本变更时会触发联网重建，离线模式会直接失败。
- 首次构建或缓存重建可能耗时较长（MCP 处理 + 依赖重映射），CPU 持续运行时不要中途终止。
- 详细的构建踩坑记录与发布检查清单见 [`docs/构建与验证-20261102.md`](docs/构建与验证-20261102.md)，完整文档索引见 [`docs/README.md`](docs/README.md)。

## 许可

LGPLv3.0（见 `gradle.properties` 中的 `mod_license`）。
