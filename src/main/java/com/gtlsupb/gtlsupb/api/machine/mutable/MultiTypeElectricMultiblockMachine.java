package com.gtlsupb.gtlsupb.api.machine.mutable;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.gtlsupb.gtlsupb.api.machine.IMultiTypeMachine;
import com.gtlsupb.gtlsupb.api.machine.IWirelessOverclockParallelMachine;
import com.gtlsupb.gtlsupb.api.machine.logic.MultiTypeMutableRecipesLogic;

import java.util.Arrays;
import java.util.List;

public class MultiTypeElectricMultiblockMachine extends WorkableElectricMultiblockMachine
                                                implements IWirelessOverclockParallelMachine, IMultiTypeMachine {

    private final List<GTRecipeType> recipeTypeList;
    private final GTRecipeType[] recipeTypeArray;

    public MultiTypeElectricMultiblockMachine(IMachineBlockEntity holder, GTRecipeType... recipeTypes) {
        super(holder);
        this.recipeTypeList = Arrays.asList(recipeTypes);
        this.recipeTypeArray = recipeTypes;
    }

    @Override
    public List<GTRecipeType> getRecipeTypeList() {
        return recipeTypeList;
    }

    @Override
    public GTRecipeType[] getRecipeTypes() {
        return recipeTypeArray;
    }

    @Override
    public GTRecipeType getRecipeType() {
        int index = getActiveRecipeType();
        if (index >= 0 && index < recipeTypeArray.length) {
            return recipeTypeArray[index];
        }
        return recipeTypeArray.length > 0 ? recipeTypeArray[0] : super.getRecipeType();
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new MultiTypeMutableRecipesLogic<>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MultiTypeMutableRecipesLogic<MultiTypeElectricMultiblockMachine> getRecipeLogic() {
        return (MultiTypeMutableRecipesLogic<MultiTypeElectricMultiblockMachine>) super.getRecipeLogic();
    }

    @Override
    public int getMaxParallel() {
        return Integer.MAX_VALUE;
    }

    public long getMaxParallelLong() {
        return Long.MAX_VALUE;
    }
}
