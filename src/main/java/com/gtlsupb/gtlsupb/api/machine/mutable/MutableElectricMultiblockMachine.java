package com.gtlsupb.gtlsupb.api.machine.mutable;

import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import com.gtlsupb.gtlsupb.api.gui.BatchProcessingFancyConfigurator;
import com.gtlsupb.gtlsupb.api.machine.IBatchProcessingMachine;
import com.gtlsupb.gtlsupb.api.machine.IWirelessOverclockParallelMachine;
import com.gtlsupb.gtlsupb.api.machine.logic.MutableRecipesLogic;
import com.gtlsupb.gtlsupb.config.ConfigHolder;

public class MutableElectricMultiblockMachine extends WorkableElectricMultiblockMachine
                                              implements IWirelessOverclockParallelMachine, IBatchProcessingMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MutableElectricMultiblockMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private boolean batchEnabled = true;

    public MutableElectricMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new MutableRecipesLogic<>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MutableRecipesLogic<MutableElectricMultiblockMachine> getRecipeLogic() {
        return (MutableRecipesLogic<MutableElectricMultiblockMachine>) super.getRecipeLogic();
    }

    @Override
    public int getMaxParallel() {
        return Integer.MAX_VALUE;
    }

    public long getMaxParallelLong() {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean isBatchEnabled() {
        return this.batchEnabled;
    }

    @Override
    public void setBatchEnabled(boolean enabled) {
        this.batchEnabled = enabled;
        onChanged();
    }

    @Override
    public int getBatchSize() {
        return ConfigHolder.INSTANCE.adaptiveBatchMax;
    }

    @Override
    public void setBatchSize(int size) {}

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(BatchProcessingFancyConfigurator.create(this));
    }
}
