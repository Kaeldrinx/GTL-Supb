package com.gtlsupb.gtlsupb.common.machine.multiblock.module;

import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CreationModule extends MicrocosmFoundryModuleBase {

    public CreationModule(IMachineBlockEntity holder) {
        super(holder,
                GTLRecipeTypes.QFT_RECIPES,
                GTLRecipeTypes.NANO_FORGE_RECIPES,
                GTLRecipeTypes.DOOR_OF_CREATE_RECIPES,
                GTLRecipeTypes.CREATE_AGGREGATION_RECIPES,
                GTLRecipeTypes.PCB_FACTORY_RECIPES,
                GTLRecipeTypes.AGGREGATION_DEVICE_RECIPES,
                GTLRecipeTypes.NEUTRON_COMPRESSOR_RECIPES);
    }

    @Override
    public GTRecipeType[] getModuleRecipeTypes() {
        return new GTRecipeType[] {
                GTLRecipeTypes.QFT_RECIPES,
                GTLRecipeTypes.NANO_FORGE_RECIPES,
                GTLRecipeTypes.DOOR_OF_CREATE_RECIPES,
                GTLRecipeTypes.CREATE_AGGREGATION_RECIPES,
                GTLRecipeTypes.PCB_FACTORY_RECIPES,
                GTLRecipeTypes.AGGREGATION_DEVICE_RECIPES,
                GTLRecipeTypes.NEUTRON_COMPRESSOR_RECIPES
        };
    }

    @Override
    protected void addMachineModeDisplay(List<Component> textList) {
        textList.add(
                Component.translatable(
                        "gtlsupb.gui.machine_mode.multitype",
                        Component.translatable("gtlsupb.machine_mode.miracle_creation")).withStyle(ChatFormatting.AQUA));
    }
}
