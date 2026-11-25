package com.gtlsupb.gtlsupb.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import com.gtlsupb.gtlsupb.api.machine.part.IOverclockHatch;

public class OverclockHatchPartMachine extends MultiblockPartMachine implements IOverclockHatch {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            OverclockHatchPartMachine.class,
            MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    private final int tier;

    public OverclockHatchPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder);
        this.tier = tier;
    }

    @Override
    public int getOverclockTierBoost() {
        return this.tier + 1;
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
