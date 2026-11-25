package com.gtlsupb.gtlsupb.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;

public class OmniversalEye extends ZeroEnergyMultiTypeMachine {

    public OmniversalEye(IMachineBlockEntity holder) {
        super(holder, GTLSupbRecipeTypes.COSMOS_SIMULATION);
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new OmniversalEyeLogic(this);
    }
}
