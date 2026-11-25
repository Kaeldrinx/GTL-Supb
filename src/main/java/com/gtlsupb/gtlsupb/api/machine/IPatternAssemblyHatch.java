package com.gtlsupb.gtlsupb.api.machine;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Collections;
import java.util.List;

public interface IPatternAssemblyHatch {

    default com.lowdragmc.lowdraglib.misc.ItemStackTransfer getPatternInventory() {
        return null;
    }

    default void onPatternInserted(int slot, net.minecraft.world.item.ItemStack stack) {}

    List<GTRecipeType> getAvailableRecipeTypes();

    int getActiveRecipeTypeIndex();

    void setActiveRecipeTypeIndex(int index);

    default GTRecipeType getActiveRecipeType() {
        List<GTRecipeType> types = getAvailableRecipeTypes();
        int idx = getActiveRecipeTypeIndex();
        if (idx < 0 || idx >= types.size()) {
            return types.get(0);
        }
        return types.get(idx);
    }

    default List<GTRecipe> getRepresentativeRecipes() {
        return getActiveRecipeType().getRepresentativeRecipes();
    }

    default List<GTRecipe> getRepresentativeRecipesFor(int typeIndex) {
        List<GTRecipeType> types = getAvailableRecipeTypes();
        GTRecipeType type = (typeIndex < 0 || typeIndex >= types.size()) ? types.get(0) : types.get(typeIndex);
        return type.getRepresentativeRecipes();
    }

    default List<GTRecipe> getBoundRecipes() {
        return Collections.emptyList();
    }

    default List<GTRecipeType> getBoundRecipeTypes() {
        return Collections.emptyList();
    }

    default java.util.Map<Integer, GTRecipe> getStockedBoundRecipes() {
        return Collections.emptyMap();
    }

    default boolean isStockedByFallback() {
        return false;
    }

    default Int2ObjectMap<GTRecipe> getSlotBoundRecipes() {
        return new Int2ObjectOpenHashMap<>();
    }

    default long getSlotBoundRecipeParallel(int slot, GTRecipe recipe, long limit) {
        return limit;
    }

    default boolean verifySlotBoundRecipes(Int2ObjectMap<GTRecipe> paralleledBySlot) {
        return true;
    }

    default boolean commitSlotBoundRecipes(Int2ObjectMap<GTRecipe> paralleledBySlot) {
        return true;
    }
}
