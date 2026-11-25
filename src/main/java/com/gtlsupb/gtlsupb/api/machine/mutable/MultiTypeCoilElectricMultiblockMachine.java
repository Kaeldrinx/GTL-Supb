package com.gtlsupb.gtlsupb.api.machine.mutable;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.gtlsupb.gtlsupb.api.machine.IMultiTypeMachine;
import com.gtlsupb.gtlsupb.api.machine.IWirelessOverclockParallelMachine;
import com.gtlsupb.gtlsupb.api.machine.logic.MultiTypeMutableRecipesLogic;

import java.util.Arrays;
import java.util.List;

public class MultiTypeCoilElectricMultiblockMachine extends CoilWorkableElectricMultiblockMachine
                                                    implements IWirelessOverclockParallelMachine, IMultiTypeMachine {

    private final List<GTRecipeType> recipeTypeList;
    private final GTRecipeType[] recipeTypeArray;

    public MultiTypeCoilElectricMultiblockMachine(IMachineBlockEntity holder, GTRecipeType... recipeTypes) {
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

    @SuppressWarnings("unchecked")
    @Override
    public MultiTypeMutableRecipesLogic<MultiTypeCoilElectricMultiblockMachine> getRecipeLogic() {
        return (MultiTypeMutableRecipesLogic<MultiTypeCoilElectricMultiblockMachine>) super.getRecipeLogic();
    }

    protected final double gtlsupb$coilTemperature() {
        var coil = getCoilType();
        return coil == null ? 0.0d : (double) coil.getCoilTemperature();
    }

    @Override
    public int getMaxParallel() {
        return (int) Math.min(
                Math.pow(2.0, gtlsupb$coilTemperature() / 900.0),
                (double) Integer.MAX_VALUE);
    }

    public long getMaxParallelLong() {
        return (long) Math.min(
                Math.pow(2.0, gtlsupb$coilTemperature() / 900.0),
                (double) Long.MAX_VALUE);
    }
}
