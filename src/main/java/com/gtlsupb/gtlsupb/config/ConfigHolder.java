package com.gtlsupb.gtlsupb.config;

import com.gtlsupb.gtlsupb.GTLSupb;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = GTLSupb.MOD_ID)
public class ConfigHolder {

    @Configurable
    @Configurable.Range(min = 1, max = 100)
    @Configurable.Comment("ME驱动器存储行数，每行固定10格，默认10行=100槽位，最大100行=1000槽位；界面固定显示5行并向下滚动，修改后需重新放置方块生效 / ME drive storage rows; each row has 10 slots, default 10 rows = 100 slots, max 100 rows = 1000 slots; GUI shows 5 rows and scrolls; re-place the block after changing")
    public int meDriveRows = 10;

    @Configurable
    @Configurable.Range(min = 1, max = 100)
    @Configurable.Comment("样板总成存储行数，每行固定10格，默认10行=100槽位，最大100行=1000槽位；界面固定显示5行并向下滚动，修改后需重新放置方块生效 / Pattern assembly storage rows; each row has 10 slots, default 10 rows = 100 slots, max 100 rows = 1000 slots; GUI shows 5 rows and scrolls; re-place the block after changing")
    public int patternAssemblyRows = 10;

    @Configurable
    @Configurable.Range(min = 1, max = 999999999)
    @Configurable.Comment("超稳态环形奇迹空间站永恒模块产出倍率(锁定值)，默认100 / Eternity module output multiplier on the Microcosm Foundry (locked value), default 100")
    public long mfEternityOutputMultiplier = 100L;

    @Configurable
    @Configurable.Range(min = 1, max = 999999999)
    @Configurable.Comment("超恒星苍穹煅天舰奇点模块产出倍率(锁定值)，默认100 / Singularity module output multiplier on the Forging Heaven Ship (locked value), default 100")
    public long fhsSingularityOutputMultiplier = 100L;

    @Configurable
    @Configurable.Range(min = 1, max = 100000000)
    @Configurable.Comment("自适应批处理最大可适应倍率，默认100。开启批处理后机器按输入/输出自动降档，实际倍率最高不超过该值，修改后重启生效 / Max adaptive batch multiplier, default 100; the machine downgrades automatically based on inputs/outputs and never exceeds this value; restart after changing")
    public int adaptiveBatchMax = 100;

    @Configurable
    @Configurable.Range(min = 1, max = 8640000)
    @Configurable.Comment("批处理资格时长阈值(时钟tick)，对齐GTCEu新版官方batchMode规则：仅当配方耗时小于该值时允许批处理，倍率上限=该值/耗时，默认100。慢于该阈值的配方不再批处理，修改后重启生效 / Batch eligibility duration threshold in ticks, aligned with GTCEu batchMode: batching is allowed only when the recipe duration is below this value; multiplier limit = threshold / duration; default 100; restart after changing")
    public int batchDuration = 100;

    public static ConfigHolder INSTANCE;

    public static void init() {
        INSTANCE = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
        GTLSupb.LOGGER.info("{} config initialized", GTLSupb.NAME);
    }
}
