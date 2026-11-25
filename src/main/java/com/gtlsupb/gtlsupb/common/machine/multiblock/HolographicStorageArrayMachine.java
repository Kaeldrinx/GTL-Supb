package com.gtlsupb.gtlsupb.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.gtlsupb.gtlsupb.common.data.GTLSupbProviderManager;

public class HolographicStorageArrayMachine extends AbstractProviderMachine {

    public HolographicStorageArrayMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    protected void registerProvider() {
        GTLSupbProviderManager.registerDataProvider(getOwnerUuid());
    }

    @Override
    protected void unregisterProvider() {
        GTLSupbProviderManager.unregisterDataProvider(getOwnerUuid());
    }
}
