package com.gtlsupb.gtlsupb.common.data.machines.multiblock;

import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;

public final class ChronoModulePattern {

    private ChronoModulePattern() {}

    public static final FactoryBlockPattern PATTERN = FactoryBlockPattern.start()
            .aisle(ChronoModulePatternLayers.LAYER_001)
            .aisle(ChronoModulePatternLayers.LAYER_002)
            .aisle(ChronoModulePatternLayers.LAYER_003);
}
