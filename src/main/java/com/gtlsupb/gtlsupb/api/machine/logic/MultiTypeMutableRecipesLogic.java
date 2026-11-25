package com.gtlsupb.gtlsupb.api.machine.logic;

import org.gtlcore.gtlcore.api.machine.multiblock.ParallelMachine;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.gtlsupb.gtlsupb.api.machine.IMultiTypeMachine;
import com.gtlsupb.gtlsupb.api.machine.IWirelessElectricMultiblockMachine;

import java.util.List;

public class MultiTypeMutableRecipesLogic<T extends WorkableElectricMultiblockMachine & IRecipeLogicMachine & IWirelessElectricMultiblockMachine & IMultiTypeMachine & ParallelMachine>
                                         extends MutableRecipesLogic<T> {

    public MultiTypeMutableRecipesLogic(T machine) {
        super(machine);
    }

    public MultiTypeMutableRecipesLogic(T machine, double reductionRatio) {
        super(machine, reductionRatio);
    }

    @Override
    protected List<GTRecipeType> getSearchRecipeTypes() {
        return getMachine().getRecipeTypeList();
    }
}
