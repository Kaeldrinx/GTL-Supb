package com.gtlsupb.gtlsupb.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.gtlsupb.gtlsupb.common.data.GTLSupbProviderManager;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitComputeReceiveHatchPartMachine;

import java.util.List;

public class BoundCWUContainer extends NotifiableRecipeHandlerTrait<Integer> {

    private final OverlimitComputeReceiveHatchPartMachine hatch;

    public BoundCWUContainer(OverlimitComputeReceiveHatchPartMachine machine) {
        super(machine);
        this.hatch = machine;
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
        return List.of(getAvailableCWU());
    }

    @Override
    public double getTotalContentAmount() {
        return getAvailableCWU();
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, String slotName, boolean simulate) {
        int sum = left.stream().reduce(0, Integer::sum);
        int available = getAvailableCWU();
        if (available >= sum) {
            return null;
        }
        return List.of(sum - available);
    }

    private int getAvailableCWU() {
        return GTLSupbProviderManager.getComputeProviderCount(this.hatch.getOwnerUuid()) > 0 ? Integer.MAX_VALUE : 0;
    }
}
