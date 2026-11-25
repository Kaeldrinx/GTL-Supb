package com.gtlsupb.gtlsupb.common.machine.multiblock;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import com.gtlsupb.gtlsupb.api.recipe.ModifyChance;
import com.gtlsupb.gtlsupb.utils.RecipeCalculationHelper;

public class FragmentWorldCollectorLogic extends ZeroEnergyMultiTypeLogic<FragmentWorldCollector> {

    private static final int INPUT_CHANCE_RATIO = 10;

    public FragmentWorldCollectorLogic(FragmentWorldCollector machine) {
        super(machine);
    }

    @Override
    protected GTRecipe gtlsupb$transformCandidate(GTRecipe recipe) {
        return ModifyChance.modifyChance(recipe, INPUT_CHANCE_RATIO);
    }

    @Override
    protected GTRecipe gtlsupb$scaleCandidate(GTRecipe candidate, long parallel) {
        return RecipeCalculationHelper.multipleRecipe(candidate, parallel,
                r -> RecipeCalculationHelper.copyFixRecipe(r, ContentModifier.multiplier((double) parallel),
                        INPUT_CHANCE_RATIO));
    }
}
