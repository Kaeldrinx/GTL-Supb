package com.gtlsupb.gtlsupb.common.data.machines.multiblock;

import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;

public final class UniversalOrderKeeperPattern {

    private UniversalOrderKeeperPattern() {}

    public static final FactoryBlockPattern PATTERN = FactoryBlockPattern.start()
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_001)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_002)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_003)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_004)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_005)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_006)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_007)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_008)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_009)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_010)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_011)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_012)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_013)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_014)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_015)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_016)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_017)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_018)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_019)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_020)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_021)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_022)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_023)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_024)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_025)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_026)
            .aisle(UniversalOrderKeeperPatternLayers.LAYER_027);

    public static final int[][] MODULE_SOCKET_OFFSETS = new int[][] {
            { -10, -11 }, { -4, -11 }, { 4, -11 }, { 10, -11 },
            { -10, -5 }, { -4, -5 }, { 4, -5 }, { 10, -5 },
            { -10, 3 }, { -4, 3 }, { 4, 3 }, { 10, 3 },
            { -10, 9 }, { -4, 9 }, { 4, 9 }, { 10, 9 },
    };
}
