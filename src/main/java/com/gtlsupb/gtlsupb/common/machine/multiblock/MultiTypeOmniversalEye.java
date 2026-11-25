package com.gtlsupb.gtlsupb.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

public class MultiTypeOmniversalEye extends ZeroEnergyMultiTypeMachine {

    public MultiTypeOmniversalEye(IMachineBlockEntity holder, GTRecipeType... recipeTypes) {
        super(holder, recipeTypes);
    }
}
