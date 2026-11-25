package com.gtlsupb.gtlsupb.integration.ae2;

import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;

public final class PatternOutputScaleRewriter {

    private PatternOutputScaleRewriter() {}

    public static IPatternDetails rewrite(IPatternDetails pattern, Level level, int multiplier) {
        if (!(pattern instanceof AEProcessingPattern processing) || multiplier <= 1) return pattern;
        try {
            GenericStack[] outputs = processing.getSparseOutputs();
            if (outputs == null || outputs.length == 0) return pattern;
            GenericStack[] scaled = new GenericStack[outputs.length];
            boolean changed = false;
            for (int i = 0; i < outputs.length; i++) {
                GenericStack out = outputs[i];
                if (out == null) {
                    scaled[i] = null;
                    continue;
                }
                long amount = saturatedMultiply(out.amount(), multiplier);
                scaled[i] = new GenericStack(out.what(), amount);
                if (amount != out.amount()) changed = true;
            }
            if (!changed) return pattern;

            IPatternDetails decoded = PatternDetailsHelper.decodePattern(
                    PatternDetailsHelper.encodeProcessingPattern(processing.getSparseInputs(), scaled), level);
            return decoded == null ? pattern : decoded;
        } catch (Throwable ignored) {
            return pattern;
        }
    }

    private static long saturatedMultiply(long value, int multiplier) {
        if (value <= 0L) return value;
        if (multiplier <= 1) return value;
        if (value > Long.MAX_VALUE / multiplier) return Long.MAX_VALUE;
        return value * multiplier;
    }
}
