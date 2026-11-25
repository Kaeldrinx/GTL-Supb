package com.gtlsupb.gtlsupb.utils;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ThreadMultiplierStrategy {

    public static final ThreadMultiplierStrategy INSTANCE = new ThreadMultiplierStrategy();

    private static final Map<MultiblockMachineDefinition, Integer> BLOCK_MULTIPLIER_MAP = new HashMap<>();

    private ThreadMultiplierStrategy() {}

    public static void registerMultiplier(MultiblockMachineDefinition definition, int multiplier) {
        BLOCK_MULTIPLIER_MAP.put(definition, multiplier);
    }

    public static long getAdditionalMultiplier(@Nullable MultiblockMachineDefinition definition) {
        return Math.min(65536L, BLOCK_MULTIPLIER_MAP.getOrDefault(definition, 2).longValue());
    }
}
