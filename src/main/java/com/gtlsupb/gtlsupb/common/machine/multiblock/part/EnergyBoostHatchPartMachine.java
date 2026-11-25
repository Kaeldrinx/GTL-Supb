package com.gtlsupb.gtlsupb.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import com.gtlsupb.gtlsupb.api.machine.part.IEnergyBoostHatch;

public class EnergyBoostHatchPartMachine extends MultiblockPartMachine implements IEnergyBoostHatch {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            EnergyBoostHatchPartMachine.class,
            MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    private final int tier;

    public EnergyBoostHatchPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder);
        this.tier = tier;
    }

    @Override
    public int getEnergyBoostMultiplier() {
        return 1 << (this.tier + 1);
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
