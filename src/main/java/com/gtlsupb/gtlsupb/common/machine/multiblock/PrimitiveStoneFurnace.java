package com.gtlsupb.gtlsupb.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

public class PrimitiveStoneFurnace extends ZeroEnergyMultiTypeMachine {

    public PrimitiveStoneFurnace(IMachineBlockEntity holder) {
        super(holder, GTRecipeTypes.FURNACE_RECIPES);
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new PrimitiveStoneFurnaceLogic(this);
    }
}
