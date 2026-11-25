package com.gtlsupb.gtlsupb.common.machine.multiblock.module;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.gtlsupb.gtlsupb.config.ConfigHolder;

import java.util.List;

public class SingularityModule extends ForgingShipModuleBase {

    public SingularityModule(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public GTRecipeType[] getModuleRecipeTypes() {
        return new GTRecipeType[] {};
    }

    public double getDecayReductionMultiplier() {
        return 0.8;
    }

    @Override
    protected void addMachineModeDisplay(List<Component> textList) {
        long multiplier = ConfigHolder.INSTANCE.fhsSingularityOutputMultiplier;
        textList.add(
                Component.translatable(
                        "gtlsupb.gui.machine_mode.multitype",
                        Component.translatable("gtlsupb.machine_mode.singularity")).withStyle(ChatFormatting.GOLD));
        textList.add(
                Component.translatable(
                        "gtlsupb.machine.singularity.output_multiplier",
                        Component.literal("×" + multiplier).withStyle(ChatFormatting.LIGHT_PURPLE)).withStyle(ChatFormatting.GRAY));
    }
}
