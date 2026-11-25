package com.gtlsupb.gtlsupb.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import com.gtlsupb.gtlsupb.api.machine.part.IEfficiencyHatch;

public class EfficiencyHatchPartMachine extends MultiblockPartMachine implements IEfficiencyHatch {

    public static final double EFFICIENCY_PER_HATCH = 0.9;

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            EfficiencyHatchPartMachine.class,
            MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    private final int tier;

    public EfficiencyHatchPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder);
        this.tier = tier;
    }

    @Override
    public double getEuEfficiency() {
        return Math.pow(EFFICIENCY_PER_HATCH, this.tier + 1);
    }

    @Override
    public boolean canShared() {
        return true;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
