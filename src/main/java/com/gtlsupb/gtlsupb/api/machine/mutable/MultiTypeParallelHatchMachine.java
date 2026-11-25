package com.gtlsupb.gtlsupb.api.machine.mutable;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.gtlsupb.gtlsupb.api.machine.IMultiTypeMachine;
import com.gtlsupb.gtlsupb.api.machine.logic.MultiTypeMutableRecipesLogic;

import java.util.Arrays;
import java.util.List;

public class MultiTypeParallelHatchMachine extends MutableParallelHatchElectricMultiblockMachine
                                           implements IMultiTypeMachine {

    private final List<GTRecipeType> recipeTypeList;
    private final GTRecipeType[] recipeTypeArray;

    public MultiTypeParallelHatchMachine(IMachineBlockEntity holder, GTRecipeType... recipeTypes) {
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
    public RecipeLogic createRecipeLogic(Object... args) {
        return new MultiTypeMutableRecipesLogic<>(this);
    }
}
