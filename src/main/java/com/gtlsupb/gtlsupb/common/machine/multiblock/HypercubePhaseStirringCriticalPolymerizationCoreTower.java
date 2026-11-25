package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;

import java.util.List;

public class HypercubePhaseStirringCriticalPolymerizationCoreTower extends ZeroEnergyMultiTypeMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HypercubePhaseStirringCriticalPolymerizationCoreTower.class,
            ZeroEnergyMultiTypeMachine.MANAGED_FIELD_HOLDER);

    public HypercubePhaseStirringCriticalPolymerizationCoreTower(IMachineBlockEntity holder) {
        super(holder,
                GTLSupbRecipeTypes.SUPERCRITICAL_SYNTHESIS,
                GTLSupbRecipeTypes.HYPERDIMENSIONAL_STIRRING);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void addMachineModeDisplay(List<Component> textList) {
        textList.add(
                Component.translatable(
                        "gtlsupb.gui.machine_mode.multitype",
                        Component.translatable("gtlsupb.machine_mode.hyperdimension_polymerization")).withStyle(ChatFormatting.AQUA));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(
                    Component.translatable(
                            "gtlsupb.common.output_multiplier",
                            Component.literal(String.valueOf(HypercubeCoreTowerLogic.OUTPUT_MULTIPLIER)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public int getMaxParallel() {
        return Integer.MAX_VALUE;
    }

    @Override
    public com.gregtechceu.gtceu.api.machine.trait.RecipeLogic createRecipeLogic(Object... args) {
        return new HypercubeCoreTowerLogic(this);
    }
}

class HypercubeCoreTowerLogic extends ZeroEnergyMultiTypeLogic<HypercubePhaseStirringCriticalPolymerizationCoreTower>
                              implements IRecipeStatus {

    public static final long OUTPUT_MULTIPLIER = 100L;

    public HypercubeCoreTowerLogic(HypercubePhaseStirringCriticalPolymerizationCoreTower machine) {
        super(machine);
    }

    @Override
    public double getDynamicOutputMultiplier() {
        return (double) OUTPUT_MULTIPLIER;
    }
}
