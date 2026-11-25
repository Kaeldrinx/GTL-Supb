package com.gtlsupb.gtlsupb.common.machine.multiblock.module;

import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class VoidModule extends MicrocosmFoundryModuleBase {

    public VoidModule(IMachineBlockEntity holder) {
        super(holder,
                GTLRecipeTypes.ULTIMATE_MATERIAL_FORGE_RECIPES,
                GTLRecipeTypes.DIMENSIONALLY_TRANSCENDENT_PLASMA_FORGE_RECIPES,
                GTLRecipeTypes.STELLAR_FORGE_RECIPES,
                GTLRecipeTypes.GRAVITATION_SHOCKBURST_RECIPES);
    }

    @Override
    public GTRecipeType[] getModuleRecipeTypes() {
        return new GTRecipeType[] {
                GTLRecipeTypes.ULTIMATE_MATERIAL_FORGE_RECIPES,
                GTLRecipeTypes.DIMENSIONALLY_TRANSCENDENT_PLASMA_FORGE_RECIPES,
                GTLRecipeTypes.STELLAR_FORGE_RECIPES,
                GTLRecipeTypes.GRAVITATION_SHOCKBURST_RECIPES
        };
    }

    @Override
    protected void addMachineModeDisplay(List<Component> textList) {
        textList.add(
                Component.translatable(
                        "gtlsupb.gui.machine_mode.multitype",
                        Component.translatable("gtlsupb.machine_mode.miracle_void")).withStyle(ChatFormatting.AQUA));
    }
}
