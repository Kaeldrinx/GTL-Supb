package com.gtlsupb.gtlsupb.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import java.util.Collections;
import java.util.List;

public class InfiniteCWUContainer extends NotifiableRecipeHandlerTrait<Integer> {

    public InfiniteCWUContainer(MetaMachine machine) {
        super(machine);
    }

    @Override
    public IO getHandlerIO() {
        return IO.IN;
    }

    @Override
    public RecipeCapability<Integer> getCapability() {
        return CWURecipeCapability.CAP;
    }

    @Override
    public List<Object> getContents() {
        return Collections.emptyList();
    }

    @Override
    public double getTotalContentAmount() {
        return (double) Integer.MAX_VALUE;
    }

    @Override
    public List<Integer> handleRecipeInner(
                                           IO io,
                                           GTRecipe recipe,
                                           List<Integer> left,
                                           String slotName,
                                           boolean simulate) {
        return null;
    }
}
