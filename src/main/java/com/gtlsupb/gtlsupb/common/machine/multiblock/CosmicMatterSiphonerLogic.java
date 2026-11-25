package com.gtlsupb.gtlsupb.common.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import com.gtlsupb.gtlsupb.common.data.ParallelData;

public class CosmicMatterSiphonerLogic extends ZeroEnergyMultiTypeLogic<CosmicMatterSiphonerMachine> {

    public CosmicMatterSiphonerLogic(CosmicMatterSiphonerMachine machine) {
        super(machine);
        setLowVoltageInfiniteParallel(false);
    }

    @Override
    protected boolean gtlsupb$acceptsRecipe(GTRecipe recipe) {
        CosmicMatterSiphonerMachine machine = getMachine();
        int tier = Math.max(0, Math.min(GTValues.MAX, machine.getCasingTier()));
        return RecipeHelper.getInputEUt(recipe) <= GTValues.V[tier];
    }

    @Override
    public GTRecipe buildFinalNormalRecipe(ParallelData parallelData) {
        return super.buildFinalNormalRecipe(parallelData);
    }
}
