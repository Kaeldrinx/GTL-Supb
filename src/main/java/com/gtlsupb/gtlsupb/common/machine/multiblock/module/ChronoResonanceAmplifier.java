package com.gtlsupb.gtlsupb.common.machine.multiblock.module;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;

public class ChronoResonanceAmplifier extends UniversalOrderModuleBase {

    public ChronoResonanceAmplifier(IMachineBlockEntity holder) {
        super(holder, GTLSupbRecipeTypes.CHRONO_RESONANCE_RECIPES);
    }

    @Override
    public GTRecipeType[] getModuleRecipeTypes() {
        return new GTRecipeType[] { GTLSupbRecipeTypes.CHRONO_RESONANCE_RECIPES };
    }

    public boolean isAmplifying() {
        RecipeLogic logic = getRecipeLogic();
        return logic != null && logic.isWorking();
    }
}
