package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;

import java.util.List;

public class AbyssalChemicalEroderMachine extends ZeroEnergyMultiTypeMachine {

    public AbyssalChemicalEroderMachine(IMachineBlockEntity holder) {
        super(holder,
                GTLSupbRecipeTypes.DISTORT,
                GTRecipeTypes.LARGE_CHEMICAL_RECIPES,
                GTLRecipeTypes.DECAY_HASTENER_RECIPES,
                GTLRecipeTypes.MASS_FABRICATOR_RECIPES,
                GTLRecipeTypes.NEUTRON_ACTIVATOR_RECIPES);
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new AbyssalChemicalEroderLogic(this);
    }

    @Override
    protected void addMachineModeDisplay(List<Component> textList) {
        textList.add(
                Component.translatable(
                        "gtlsupb.gui.machine_mode.multitype",
                        Component.translatable("gtlsupb.machine_mode.abyssal_erosion")).withStyle(ChatFormatting.AQUA));
    }
}
