package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.machine.multiblock.MolecularAssemblerMultiblockMachine;
import org.gtlcore.gtlcore.mixin.gtm.api.recipe.RecipeLogicAccessor;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import com.gtlsupb.gtlsupb.api.gui.LimitedDurationConfigurator;
import com.gtlsupb.gtlsupb.api.machine.ILimitedDurationMachine;

import java.util.ArrayList;
import java.util.List;

public class GTLSupbMolecularAssembler extends MolecularAssemblerMultiblockMachine implements ILimitedDurationMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GTLSupbMolecularAssembler.class,
            MolecularAssemblerMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted(key = "drLimit")
    private int limitedDuration = ILimitedDurationMachine.DEFAULT_DURATION;

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

    public GTLSupbMolecularAssembler(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new LimitedDurationConfigurator(this));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new GTLSupbMolecularAssemblerLogic(this);
    }

    @Override
    public int getMaxParallel() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            textList.add(
                    Component.translatable(
                            "gtceu.gui.machinemode",
                            Component.translatable(getRecipeType().registryName.toLanguageKey())).withStyle(ChatFormatting.AQUA));
            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused").withStyle(ChatFormatting.GOLD));
            } else if (isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running").withStyle(ChatFormatting.GREEN));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
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
        getDefinition().getAdditionalDisplay().accept(this, textList);
        getParts().forEach(it -> it.addMultiText(textList));
    }
}

class GTLSupbMolecularAssemblerLogic extends RecipeLogic implements com.gtlsupb.gtlsupb.api.machine.logic.IMergedRecipeCount {

    private static final int MAX_RECIPES_PER_TICK = 256;

    private int gtlsupb$mergedRecipeCount;

    public GTLSupbMolecularAssemblerLogic(GTLSupbMolecularAssembler machine) {
        super(machine);
    }

    @Override
    public int gtlsupb$getMergedRecipeCount() {
        return gtlsupb$mergedRecipeCount;
    }

    @Override
    public GTLSupbMolecularAssembler getMachine() {
        return (GTLSupbMolecularAssembler) super.getMachine();
    }

    private GTRecipe getMultipleRecipes() {
        gtlsupb$mergedRecipeCount = 0;
        var handler = getMachine().getMAHandler();
        if (handler == null) return null;
        List<Content> itemOutputs = new ArrayList<>();
        List<Content> fluidOutputs = new ArrayList<>();

        int emptyStreak = 0;
        int mergedCount = 0;
        for (int i = 0; i < MAX_RECIPES_PER_TICK; i++) {
            GTRecipe recipe = handler.extractGTRecipe(Long.MAX_VALUE, 1);
            if (recipe == null) {
                if (++emptyStreak >= 2) break;
                continue;
            }
            emptyStreak = 0;
            List<Content> items = recipe.outputs.get(ItemRecipeCapability.CAP);
            if (items != null) itemOutputs.addAll(items);
            List<Content> fluids = recipe.outputs.get(FluidRecipeCapability.CAP);
            if (fluids != null) fluidOutputs.addAll(fluids);
            handler.handleRecipeOutput(recipe);
            mergedCount++;
        }
        gtlsupb$mergedRecipeCount = mergedCount;

        if (itemOutputs.isEmpty() && fluidOutputs.isEmpty()) {
            gtlsupb$mergedRecipeCount = 0;
            return null;
        }

        GTRecipe combined = GTRecipeBuilder.ofRaw().buildRawRecipe();
        if (!itemOutputs.isEmpty()) combined.outputs.put(ItemRecipeCapability.CAP, itemOutputs);
        if (!fluidOutputs.isEmpty()) combined.outputs.put(FluidRecipeCapability.CAP, fluidOutputs);
        combined.duration = this.machine instanceof ILimitedDurationMachine ? ((ILimitedDurationMachine) this.machine).getLimitedDuration() : 1;
        return combined;
    }

    @Override
    public void updateTickSubscription() {
        if (!isSuspend() && getMachine().isRecipeLogicAvailable()) {
            subscription = getMachine().subscribeServerTick(subscription, this::serverTick);
        } else if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void serverTick() {
        if (!isSuspend()) {
            if (!isIdle() && getLastRecipe() != null) {
                if (getProgress() < getDuration()) handleRecipeWorking();
                if (getProgress() >= getDuration()) onRecipeFinish();
            } else if (getLastRecipe() != null || !getMachine().keepSubscribing() || getMachine().getOffsetTimer() % 5L == 0L) {
                findAndHandleRecipe();
            }
        }
        boolean unsubscribe = false;
        if (isSuspend()) {
            unsubscribe = true;
        } else if (getLastRecipe() == null && isIdle() && !getMachine().keepSubscribing()) {
            unsubscribe = true;
        }
        if (unsubscribe && subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void handleRecipeWorking() {
        this.progress++;
        this.totalContinuousRunningTime++;
    }

    @Override
    public void findAndHandleRecipe() {
        this.lastRecipe = null;
        GTRecipe match = getMultipleRecipes();
        if (match != null) {
            setupRecipe(match);
        }
    }

    @Override
    public void setupRecipe(GTRecipe recipe) {
        this.lastRecipe = recipe;
        setStatus(Status.WORKING);
        this.progress = 0;
        this.duration = this.machine instanceof ILimitedDurationMachine ? ((ILimitedDurationMachine) this.machine).getLimitedDuration() : 1;
        ((RecipeLogicAccessor) this).setIsActive(true);
    }

    @Override
    public void onRecipeFinish() {
        GTRecipe match = getMultipleRecipes();
        if (match != null) {
            setupRecipe(match);
        } else {
            this.lastRecipe = null;
            setStatus(Status.IDLE);
            this.progress = 0;
            this.duration = 0;
            ((RecipeLogicAccessor) this).setIsActive(false);
        }
    }
}
