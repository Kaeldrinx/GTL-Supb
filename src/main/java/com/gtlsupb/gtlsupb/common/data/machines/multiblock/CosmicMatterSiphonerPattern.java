package com.gtlsupb.gtlsupb.common.data.machines.multiblock;

import org.gtlcore.gtlcore.utils.Registries;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;

import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Locale;
import java.util.function.Supplier;

public final class CosmicMatterSiphonerPattern {

    private CosmicMatterSiphonerPattern() {}

    public static final String TIER_KEY = "CasingTier";

    public static final Int2ObjectOpenHashMap<Supplier<Block>> TIER_CASING_MAP = new Int2ObjectOpenHashMap<>();

    static {
        for (int tier = 0; tier <= GTValues.MAX; tier++) {
            final String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
            TIER_CASING_MAP.put(tier, () -> Registries.getBlock("gtceu:" + tierName + "_machine_casing"));
        }
    }

    public static final FactoryBlockPattern PATTERN = FactoryBlockPattern.start()
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_001)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_002)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_003)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_004)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_005)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_006)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_007)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_008)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_009)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_010)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_011)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_012)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_013)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_014)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_015)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_016)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_017)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_018)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_019)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_020)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_021)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_022)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_023)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_024)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_025)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_026)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_027)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_028)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_029)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_030)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_031)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_032)
            .aisle(CosmicMatterSiphonerPatternLayers.LAYER_033);
}
