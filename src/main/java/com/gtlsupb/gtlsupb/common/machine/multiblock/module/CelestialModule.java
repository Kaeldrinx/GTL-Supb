package com.gtlsupb.gtlsupb.common.machine.multiblock.module;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CelestialModule extends ForgingShipModuleBase {

    public CelestialModule(IMachineBlockEntity holder) {
        super(holder, GTRecipeTypes.ALLOY_SMELTER_RECIPES);
    }

    @Override
    public GTRecipeType[] getModuleRecipeTypes() {
        return new GTRecipeType[] { GTRecipeTypes.ALLOY_SMELTER_RECIPES };
    }

    @Override
    protected void addMachineModeDisplay(List<Component> textList) {
        textList.add(
                Component.translatable(
                        "gtlsupb.gui.machine_mode.multitype",
                        Component.translatable("gtlsupb.machine_mode.celestial")).withStyle(ChatFormatting.AQUA));
    }
}
