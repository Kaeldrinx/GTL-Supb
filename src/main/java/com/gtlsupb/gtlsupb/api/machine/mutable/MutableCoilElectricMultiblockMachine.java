package com.gtlsupb.gtlsupb.api.machine.mutable;

import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import com.gtlsupb.gtlsupb.api.gui.BatchProcessingFancyConfigurator;
import com.gtlsupb.gtlsupb.api.gui.LimitedDurationConfigurator;
import com.gtlsupb.gtlsupb.api.machine.IBatchProcessingMachine;
import com.gtlsupb.gtlsupb.api.machine.ILimitedDurationMachine;
import com.gtlsupb.gtlsupb.api.machine.IWirelessOverclockParallelMachine;
import com.gtlsupb.gtlsupb.api.machine.logic.MutableRecipesLogic;
import com.gtlsupb.gtlsupb.config.ConfigHolder;

public class MutableCoilElectricMultiblockMachine extends CoilWorkableElectricMultiblockMachine
                                                  implements IWirelessOverclockParallelMachine, ILimitedDurationMachine, IBatchProcessingMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MutableCoilElectricMultiblockMachine.class,
            CoilWorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted(key = "drLimit")
    private int limitedDuration = ILimitedDurationMachine.DEFAULT_DURATION;

    @Persisted
    @DescSynced
    private boolean batchEnabled = true;

    public MutableCoilElectricMultiblockMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public int getLimitedDuration() {
        return limitedDuration;
    }

    @Override
    public void setLimitedDuration(int duration) {
        if (duration != limitedDuration) {
            limitedDuration = Math.max(ILimitedDurationMachine.MIN_DURATION, Math.min(duration, ILimitedDurationMachine.MAX_DURATION));
        }
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
        configuratorPanel.attachConfigurators(new LimitedDurationConfigurator(this));
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new MutableRecipesLogic<>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MutableRecipesLogic<MutableCoilElectricMultiblockMachine> getRecipeLogic() {
        return (MutableRecipesLogic<MutableCoilElectricMultiblockMachine>) super.getRecipeLogic();
    }

    protected final double gtlsupb$coilTemperature() {
        var coil = getCoilType();
        return coil == null ? 0.0d : (double) coil.getCoilTemperature();
    }

    @Override
    public int getMaxParallel() {
        return (int) Math.min(
                Math.pow(2.0, gtlsupb$coilTemperature() / 900.0),
                (double) Integer.MAX_VALUE);
    }

    public long getMaxParallelLong() {
        return (long) Math.min(
                Math.pow(2.0, gtlsupb$coilTemperature() / 900.0),
                (double) Long.MAX_VALUE);
    }
}
