package com.gtlsupb.gtlsupb.integration.ae2;

import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;

public final class CatalystPatternWrapper implements IPatternDetails {

    private final IPatternDetails delegate;
    private final IPatternDetails.IInput[] inputs;

    @Nullable
    private final int[] keptIndices;

    public CatalystPatternWrapper(IPatternDetails delegate, Set<AEKey> catalystKeys, @Nullable Level level,
                                  Predicate<AEKey> selfSupplied, Predicate<AEKey> autoCraftable) {
        this.delegate = delegate;
        IPatternDetails.IInput[] originals = delegate.getInputs();
        if (originals == null || originals.length == 0) {
            this.inputs = originals == null ? new IPatternDetails.IInput[0] : originals;
            this.keptIndices = null;
            return;
        }

        java.util.List<Integer> kept = new java.util.ArrayList<>(originals.length);
        java.util.List<IPatternDetails.IInput> filtered = new java.util.ArrayList<>(originals.length);
        for (int i = 0; i < originals.length; i++) {
            IPatternDetails.IInput input = originals[i];
            boolean catalyst = false;
            if (input != null) {
                try {
                    GenericStack[] possible = input.getPossibleInputs();
                    if (possible != null && possible.length > 0 && possible[0] != null) {
                        catalyst = resolveCatalystKey(input, catalystKeys, possible[0].what(), level) != null;
                    }
                } catch (Throwable ignored) {

                }
            }
            if (catalyst) continue;
            kept.add(i);
            filtered.add(input);
        }
        if (kept.size() == originals.length) {

            this.inputs = originals;
            this.keptIndices = null;
        } else {
            this.inputs = filtered.toArray(IPatternDetails.IInput[]::new);
            int[] indices = new int[kept.size()];
            for (int i = 0; i < indices.length; i++) indices[i] = kept.get(i);
            this.keptIndices = indices;
        }
    }

    @Nullable
    private static AEKey resolveCatalystKey(IPatternDetails.IInput input, Set<AEKey> catalystKeys, AEKey primary,
                                            @Nullable Level level) {
        if (catalystKeys.contains(primary)) return primary;
        for (AEKey key : catalystKeys) {
            if (input.isValid(key, level)) return key;
        }
        return null;
    }

    public IPatternDetails gtlsupb$unwrap() {
        return this.delegate;
    }

    @Override
    public AEItemKey getDefinition() {
        return this.delegate.getDefinition();
    }

    @Override
    public IPatternDetails.IInput[] getInputs() {
        return this.inputs;
    }

    @Override
    public GenericStack[] getOutputs() {
        return this.delegate.getOutputs();
    }

    @Override
    public boolean supportsPushInputsToExternalInventory() {
        return this.delegate.supportsPushInputsToExternalInventory();
    }

    @Override
    public void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink inputSink) {
        int[] indices = this.keptIndices;
        if (indices == null) {
            this.delegate.pushInputsToExternalInventory(inputHolder, inputSink);
            return;
        }
        KeyCounter[] full = new KeyCounter[this.delegate.getInputs().length];
        for (int i = 0; i < full.length; i++) full[i] = new KeyCounter();
        for (int i = 0; i < indices.length && i < inputHolder.length; i++) {
            KeyCounter source = inputHolder[i];
            if (source == null) continue;
            KeyCounter target = full[indices[i]];
            for (AEKey key : source.keySet()) {
                long amount = source.get(key);
                if (amount > 0L) target.add(key, amount);
            }
        }
        this.delegate.pushInputsToExternalInventory(full, inputSink);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IPatternDetails other)) return false;
        IPatternDetails target = obj instanceof CatalystPatternWrapper wrapper ? wrapper.delegate : other;
        return this.delegate.equals(target);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    @Override
    public String toString() {
        return "CatalystPatternWrapper[" + this.delegate + "]";
    }
}
