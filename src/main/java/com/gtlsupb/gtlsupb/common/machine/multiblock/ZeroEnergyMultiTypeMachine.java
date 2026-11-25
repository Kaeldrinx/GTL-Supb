package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.machine.trait.ILockRecipe;
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus;
import org.gtlcore.gtlcore.api.recipe.RecipeText;

import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.MachineModeFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import com.gtlsupb.gtlsupb.api.gui.BatchProcessingFancyConfigurator;
import com.gtlsupb.gtlsupb.api.gui.LimitedDurationConfigurator;
import com.gtlsupb.gtlsupb.api.machine.IBatchProcessingMachine;
import com.gtlsupb.gtlsupb.api.machine.ILimitedDurationMachine;
import com.gtlsupb.gtlsupb.api.machine.mutable.MultiTypeElectricMultiblockMachine;
import com.gtlsupb.gtlsupb.config.ConfigHolder;

import java.util.List;

public abstract class ZeroEnergyMultiTypeMachine extends MultiTypeElectricMultiblockMachine implements ILimitedDurationMachine, IBatchProcessingMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ZeroEnergyMultiTypeMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted(key = "drLimit")
    private int limitedDuration = ILimitedDurationMachine.DEFAULT_DURATION;

    @Persisted
    @DescSynced
    private boolean batchEnabled = true;

    public ZeroEnergyMultiTypeMachine(IMachineBlockEntity holder, GTRecipeType... recipeTypes) {
        super(holder, recipeTypes);
    }

    @Override
    public int getLimitedDuration() {
        return limitedDuration;
    }

    @Override
    public void setLimitedDuration(int duration) {
        if (duration != limitedDuration) {
            limitedDuration = Math.max(ILimitedDurationMachine.MIN_DURATION,
                    Math.min(ILimitedDurationMachine.MAX_DURATION, duration));
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
    public boolean keepSubscribing() {
        return false;
    }

    @Override
    public void onChanged() {
        super.onChanged();
        var logic = getRecipeLogic();
        if (logic != null) {
            logic.markLastRecipeDirty();
            logic.updateTickSubscription();
        }
    }

    public boolean shouldShowMachineModeSwitcher() {
        return false;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);
        if (shouldShowMachineModeSwitcher() && getRecipeTypes().length > 1) {

            sideTabs.attachSubTab(new MachineModeFancyConfigurator(this));
        }

        CombinedDirectionalFancyConfigurator directionalConfigurator = CombinedDirectionalFancyConfigurator.of(this, this);
        if (directionalConfigurator != null) {
            sideTabs.attachSubTab(directionalConfigurator);
        }
    }

    public boolean consumeEnergy() {
        return false;
    }

    @Override
    public int getTier() {
        return com.gregtechceu.gtceu.api.GTValues.MAX;
    }

    private static final long MAX_EU_BUFFER = 1_000_000_000_000L;

    @Persisted
    private java.util.UUID energyOwner;

    @Persisted
    private long generatedEuBuffer;

    private long lastGeneratedEu;

    private com.gregtechceu.gtceu.api.machine.TickableSubscription euSyncSubs;

    private boolean gtlsupb$flushingEu;

    private long gtlsupb$euRetryTick = Long.MIN_VALUE;

    public long getLastGeneratedEu() {
        return lastGeneratedEu;
    }

    public java.util.UUID gtlsupb$getEnergyOwner() {
        return energyOwner;
    }

    public void gtlsupb$bindEnergyOwner(java.util.UUID owner) {
        this.energyOwner = owner;
        markDirty();
        gtlsupb$tryFlushGeneratedEu();
    }

    public void gtlsupb$addGeneratedEu(long eu) {
        if (eu <= 0L) return;
        lastGeneratedEu = eu;
        if (energyOwner == null) gtlsupb$tryAutoBindOwner();
        if (energyOwner != null && gtlsupb$sendEu(eu)) return;
        generatedEuBuffer = Math.min(generatedEuBuffer + eu, MAX_EU_BUFFER);
        gtlsupb$scheduleEuRetry();
    }

    private boolean gtlsupb$sendEu(long eu) {
        if (eu <= 0L || energyOwner == null) return false;
        try {
            return com.hepdd.gtmthings.api.misc.WirelessEnergyManager.addEUToGlobalEnergyMap(
                    energyOwner, java.math.BigInteger.valueOf(eu), this);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void gtlsupb$tryFlushGeneratedEu() {
        if (isRemote() || gtlsupb$flushingEu || generatedEuBuffer <= 0L || energyOwner == null) return;
        gtlsupb$flushingEu = true;
        try {
            if (gtlsupb$sendEu(generatedEuBuffer)) {
                generatedEuBuffer = 0L;
                gtlsupb$unsubscribeEuRetry();
            } else {
                gtlsupb$scheduleEuRetry();
            }
        } finally {
            gtlsupb$flushingEu = false;
        }
    }

    private void gtlsupb$tryAutoBindOwner() {
        if (isRemote() || getLevel() == null) return;
        var nearest = com.gtlsupb.gtlsupb.common.data.PlayerBindingHelper.findNearestPlayer(getLevel(), getPos(), 16.0d);
        if (nearest != null) gtlsupb$bindEnergyOwner(nearest.getUUID());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && energyOwner == null) gtlsupb$tryAutoBindOwner();
        gtlsupb$tryFlushGeneratedEu();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        gtlsupb$unsubscribeEuRetry();
    }

    private static final int EU_TRANSFER_RETRY_INTERVAL = 20;

    private void gtlsupb$scheduleEuRetry() {
        if (isRemote() || generatedEuBuffer <= 0L || energyOwner == null || euSyncSubs != null) return;
        euSyncSubs = subscribeServerTick(euSyncSubs, this::gtlsupb$tickGeneratedEu);
    }

    private void gtlsupb$tickGeneratedEu() {
        if (isRemote()) return;
        long timer = getOffsetTimer();
        if (gtlsupb$euRetryTick != Long.MIN_VALUE && timer - gtlsupb$euRetryTick < EU_TRANSFER_RETRY_INTERVAL) return;
        gtlsupb$euRetryTick = timer;
        if (generatedEuBuffer > 0L && energyOwner != null) gtlsupb$tryFlushGeneratedEu();
        if (generatedEuBuffer <= 0L || energyOwner == null) gtlsupb$unsubscribeEuRetry();
    }

    private void gtlsupb$unsubscribeEuRetry() {
        if (euSyncSubs == null) return;
        euSyncSubs.unsubscribe();
        euSyncSubs = null;
    }

    public double outputMultiplier() {
        return 1.0;
    }

    public long maxParallel() {
        return Long.MAX_VALUE;
    }

    public double getHatchEfficiencyMultiplier() {
        double multiplier = 1.0;
        for (var part : getParts()) {
            if (part instanceof com.gtlsupb.gtlsupb.api.machine.part.IEfficiencyHatch hatch) {
                multiplier *= hatch.getEuEfficiency();
            }
        }
        return Math.max(0.1, multiplier);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            addMachineModeDisplay(textList);

            if (shouldShowEnergyMode()) {
                if (consumeEnergy()) {
                    textList.add(Component.translatable("gtlsupb.common.electric_powered"));
                } else {
                    textList.add(Component.translatable("gtlsupb.common.zero_energy"));
                }
            }

            if (lastGeneratedEu > 0L) {
                textList.add(
                        Component.translatable(
                                "gtlsupb.common.generating",
                                Component.literal(String.format("%,d", lastGeneratedEu)).withStyle(ChatFormatting.GOLD)));
            }

            if (outputMultiplier() != 1.0) {
                textList.add(
                        Component.translatable(
                                "gtlsupb.common.output_multiplier",
                                Component.literal(String.valueOf(outputMultiplier())).withStyle(ChatFormatting.GOLD)));
            }

            int ticks = getLimitedDuration();
            if (ticks != 1) {
                textList.add(
                        Component.translatable(
                                "gtlsupb.common.processing_time",
                                Component.literal(ticks + " ticks").withStyle(ChatFormatting.GOLD)));
            } else {
                textList.add(Component.translatable("gtlsupb.common.completion_time"));
            }

            addWorkingStatus(textList);

            RecipeLogic rl = getRecipeLogic();
            if (rl instanceof IRecipeStatus) {
                IRecipeStatus status = (IRecipeStatus) rl;
                if (status.getRecipeStatus() != null && status.getRecipeStatus().reason() != null) {
                    textList.add(status.getRecipeStatus().reason().copy().withStyle(ChatFormatting.RED));
                }
                if (status.getWorkingStatus() != null && status.getWorkingStatus().reason() != null) {
                    textList.add(status.getWorkingStatus().reason().copy().withStyle(ChatFormatting.RED));
                }
            }
        } else {
            textList.add(
                    Component.translatable("gtceu.multiblock.invalid_structure")
                            .withStyle(
                                    Style.EMPTY.withColor(ChatFormatting.RED)
                                            .withHoverEvent(
                                                    new HoverEvent(
                                                            HoverEvent.Action.SHOW_TEXT,
                                                            Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                                                                    .withStyle(ChatFormatting.GRAY)))));
        }

        RecipeLogic rl2 = getRecipeLogic();
        if (rl2 instanceof ILockRecipe) {
            addRecipeLockDisplay(textList, (ILockRecipe) rl2);
        }
        getDefinition().getAdditionalDisplay().accept(this, textList);
        getParts().forEach(it -> it.addMultiText(textList));
    }

    protected boolean shouldShowEnergyMode() {
        return true;
    }

    protected void addMachineModeDisplay(List<Component> textList) {
        textList.add(
                Component.translatable(
                        "gtceu.gui.machinemode",
                        Component.translatable(getRecipeType().registryName.toLanguageKey())).withStyle(ChatFormatting.AQUA));
    }

    protected void addWorkingStatus(List<Component> textList) {
        if (!isWorkingEnabled()) {
            textList.add(Component.translatable("gtceu.multiblock.work_paused").withStyle(ChatFormatting.GOLD));
        } else if (isActive()) {
            textList.add(Component.translatable("gtceu.multiblock.running").withStyle(ChatFormatting.GREEN));
        } else {
            textList.add(Component.translatable("gtceu.multiblock.idling"));
        }
    }

    protected void addRecipeLockDisplay(List<Component> textList, ILockRecipe iLockRecipe) {
        Component text;
        if (iLockRecipe.isLock() && iLockRecipe.getLockRecipe() != null) {
            text = Component.translatable("gui.gtlcore.recipe_lock.recipe").withStyle(style -> style.withHoverEvent(
                    new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            RecipeText.getRecipeInputText(iLockRecipe.getLockRecipe())
                                    .append(RecipeText.getRecipeOutputText(iLockRecipe.getLockRecipe())))));
        } else {
            text = Component.translatable("gui.gtlcore.recipe_lock.no_recipe");
        }
        textList.add(text);
    }
}
