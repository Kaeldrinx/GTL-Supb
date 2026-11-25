package com.gtlsupb.gtlsupb.api.machine.mutable;

import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

public class MutableParallelHatchElectricMultiblockMachine extends MutableElectricMultiblockMachine {

    public MutableParallelHatchElectricMultiblockMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public long getMaxParallelLong() {
        if (!(this instanceof IRecipeCapabilityMachine)) return 1;
        IRecipeCapabilityMachine machine = (IRecipeCapabilityMachine) this;
        var parallelHatch = machine.getParallelHatch();
        int parallelHatchParallel = parallelHatch != null ? parallelHatch.getCurrentParallel() : 0;
        if (parallelHatchParallel > 0) {
            return Math.max((long) parallelHatchParallel, 1L);
        } else {
            return 1L;
        }
    }
}
