# TIPS 规范

机器/部件在配方查看界面（GTCEu `GTLMachines` 注册的 `tooltips(...)`）与 Jade 中的提示，统一遵守本条规范。

## 键命名

统一使用**单一 `machine` 命名空间**，与 gtlcore / GTLAdditions 一致：

```
gtlsupb.machine.<id>.tooltip.<n>        # 多方块与部件通用 tip，n 从 0 递增
gtlsupb.machine.<id>.<name>             # GUI/Jade 读取的具体字段值
gtlsupb.machine.infinite.parallel       # 特殊：无限并行显示文案
```

- 历史键 `gtlsupb.multiblock.<id>.tooltip.<n>` 已废弃，不得再新增。
- 中英语言文件键必须一一对应（`zh_cn.json` / `en_us.json`），缺键会显示原始键。

## 代码写法

统一走 `GTLSupbTips.tip(id, n)`：

```java
GTLMachines.tooltips(...).add(GTLSupbTips.tip("machine_id", 0));
```

## 内容要求

- 每条 tip 只讲一个玩家能感知的机制点（输入/输出/限时/温度/绑定等），不写实现细节。
- 数值、单位与配方实际一致；涉及概率时写明"概率产物"。
- 不允许把"等待原因"文案（`recipe_stalled_*`）当作 tip 复用，那是状态提示，不是属性说明。

## 中英对照

- `zh_cn.json` 为源，`en_us.json` 同步翻译；更新功能时两处一起改。
- 语序：中文主语先行、英文祈使/名词短语均可，避免机器翻译腔。
