package com.gtlsupb.gtlsupb.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.gtlsupb.gtlsupb.common.data.GTLSupbProviderManager;

public class FloatingPointEngineMachine extends AbstractProviderMachine {

    public FloatingPointEngineMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    protected void registerProvider() {
        GTLSupbProviderManager.registerComputeProvider(getOwnerUuid());
    }

    @Override
    protected void unregisterProvider() {
        GTLSupbProviderManager.unregisterComputeProvider(getOwnerUuid());
    }
}
