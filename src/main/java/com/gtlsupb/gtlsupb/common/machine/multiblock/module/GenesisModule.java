package com.gtlsupb.gtlsupb.common.machine.multiblock.module;

import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class GenesisModule extends MicrocosmFoundryModuleBase {

    public GenesisModule(IMachineBlockEntity holder) {
        super(holder,
                GTLRecipeTypes.COSMOS_SIMULATION_RECIPES,
                GTLRecipeTypes.SPACE_COSMIC_PROBE_RECEIVERS_RECIPES,
                GTLRecipeTypes.MAGIC_MANUFACTURER_RECIPES,
                GTLRecipeTypes.DRILLING_MODULE_RECIPES,
                GTLRecipeTypes.MINER_MODULE_RECIPES);
    }

    @Override
    public GTRecipeType[] getModuleRecipeTypes() {
        return new GTRecipeType[] {
                GTLRecipeTypes.COSMOS_SIMULATION_RECIPES,
                GTLRecipeTypes.SPACE_COSMIC_PROBE_RECEIVERS_RECIPES,
                GTLRecipeTypes.MAGIC_MANUFACTURER_RECIPES,
                GTLRecipeTypes.DRILLING_MODULE_RECIPES,
                GTLRecipeTypes.MINER_MODULE_RECIPES
        };
    }

    @Override
    protected void addMachineModeDisplay(List<Component> textList) {
        textList.add(
                Component.translatable(
                        "gtlsupb.gui.machine_mode.multitype",
                        Component.translatable("gtlsupb.machine_mode.miracle_genesis")).withStyle(ChatFormatting.AQUA));
    }
}
