package com.gtlsupb.gtlsupb.integration.ae2;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PatternOutputMultiplierRegistry {

    private static final Map<ResourceLocation, Integer> BY_RECIPE = new ConcurrentHashMap<>();

    private PatternOutputMultiplierRegistry() {}

    public static void clear() {
        BY_RECIPE.clear();
    }

    public static void register(GTRecipe recipe, int multiplier) {
        if (recipe == null || recipe.id == null) return;
        if (multiplier <= 1) {
            BY_RECIPE.remove(recipe.id);
        } else {
            BY_RECIPE.put(recipe.id, multiplier);
        }
    }

    public static void register(ResourceLocation recipeId, int multiplier) {
        if (recipeId == null) return;
        if (multiplier <= 1) {
            BY_RECIPE.remove(recipeId);
        } else {
            BY_RECIPE.put(recipeId, multiplier);
        }
    }

    public static int get(ResourceLocation recipeId) {
        if (recipeId == null) return 1;
        return BY_RECIPE.getOrDefault(recipeId, 1);
    }

    public static int multiplierOf(Object controller) {
        if (!(controller instanceof MetaMachine machine)) return 1;
        long value = 1L;
        if (controller instanceof com.gtlsupb.gtlsupb.common.machine.multiblock.UniversalOrderKeeper keeper) {
            value = Math.max(value, keeper.getOutputMultiplier());
        }
        if (controller instanceof com.gtlsupb.gtlsupb.common.machine.multiblock.module.UniversalOrderModuleBase module) {
            value = Math.max(value, module.getHostOutputMultiplier());
        }
        if (controller instanceof IRecipeLogicMachine logicMachine) {
            RecipeLogic logic = logicMachine.getRecipeLogic();
            if (logic instanceof com.gtlsupb.gtlsupb.common.machine.multiblock.ZeroEnergyMultiTypeLogic zero) {
                value = Math.max(value, (long) Math.floor(zero.getDynamicOutputMultiplier()));
            }
            try {
                var method = logic.getClass().getMethod("getDynamicOutputMultiplier");
                Object result = method.invoke(logic);
                if (result instanceof Number n) value = Math.max(value, (long) Math.floor(n.doubleValue()));
            } catch (Throwable ignored) {}
        }
        for (String methodName : new String[] {
                "getDynamicOutputMultiplier", "getOutputMultiplier", "getRecipeOutputMultiply",
                "getRecipeOutputMultiplier", "outputMultiplier" }) {
            try {
                var method = machine.getClass().getMethod(methodName);
                Object result = method.invoke(machine);
                if (result instanceof Number n) value = Math.max(value, (long) Math.floor(n.doubleValue()));
            } catch (Throwable ignored) {}
        }
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, value));
    }
}
