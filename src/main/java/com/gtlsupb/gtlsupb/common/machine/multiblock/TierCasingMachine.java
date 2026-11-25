package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.network.chat.Component;

import java.util.List;

public class TierCasingMachine extends WorkableElectricMultiblockMachine {

    private final String tierType;
    private int casingTier = 0;

    public TierCasingMachine(IMachineBlockEntity holder, String tierType, Object... args) {
        super(holder, args);
        this.tierType = tierType;
    }

    public String getTierType() {
        return tierType;
    }

    public int getCasingTier() {
        return casingTier;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        casingTier = (int) getMultiblockState().getMatchContext().get(tierType);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        casingTier = 0;
    }

    @Override
    public boolean beforeWorking(GTRecipe recipe) {
        if (recipe != null && recipe.data.contains(tierType) && recipe.data.getInt(tierType) > casingTier) {
            RecipeResult.of(this, RecipeResult.FAIL_NO_ENOUGH_TIER);
            getRecipeLogic().interruptRecipe();
            return false;
        }
        return super.beforeWorking(recipe);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("gtceu.casings.tier", casingTier));
        }
    }
}
