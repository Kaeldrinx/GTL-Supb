package com.gtlsupb.gtlsupb.common.machine.multiblock.module;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

public class ChronoDeductionArray extends UniversalOrderModuleBase {

    public ChronoDeductionArray(IMachineBlockEntity holder) {
        super(holder, GTRecipeTypes.DUMMY_RECIPES);
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new ChronoDeductionArrayLogic(this);
    }

    @Override
    public GTRecipeType[] getModuleRecipeTypes() {
        return new GTRecipeType[0];
    }

    @Override
    public boolean isModeSelectable() {
        return true;
    }
}

class ChronoDeductionArrayLogic extends UniversalOrderModuleLogic {

    ChronoDeductionArrayLogic(ChronoDeductionArray machine) {
        super(machine);
    }

    @Override
    public ChronoDeductionArray getMachine() {
        return (ChronoDeductionArray) super.getMachine();
    }
}
