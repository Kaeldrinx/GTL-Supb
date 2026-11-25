package com.gtlsupb.gtlsupb.integration.jei;

import appeng.api.stacks.GenericStack;

import java.util.List;

public final class PendingEncoding {

    private static final long TTL_MS = 10_000L;

    private static PendingEncoding current;

    private final Object recipe;
    private final List<List<GenericStack>> genericIngredients;
    private final List<GenericStack> genericResults;
    private final boolean craftingRecipe;
    private final long createdAt;

    private PendingEncoding(Object recipe, List<List<GenericStack>> genericIngredients,
                            List<GenericStack> genericResults, boolean craftingRecipe) {
        this.recipe = recipe;
        this.genericIngredients = genericIngredients;
        this.genericResults = genericResults;
        this.craftingRecipe = craftingRecipe;
        this.createdAt = System.currentTimeMillis();
    }

    public static void set(Object recipe, List<List<GenericStack>> genericIngredients,
                           List<GenericStack> genericResults, boolean craftingRecipe) {
        current = new PendingEncoding(recipe, genericIngredients, genericResults, craftingRecipe);
    }

    public static PendingEncoding poll() {
        if (current == null) return null;
        if (System.currentTimeMillis() - current.createdAt > TTL_MS) {

            current = null;
            return null;
        }
        PendingEncoding snapshot = current;
        current = null;
        return snapshot;
    }

    public static void clear() {
        current = null;
    }

    public Object recipe() {
        return recipe;
    }

    public List<List<GenericStack>> genericIngredients() {
        return genericIngredients;
    }

    public List<GenericStack> genericResults() {
        return genericResults;
    }

    public boolean isCraftingRecipe() {
        return craftingRecipe;
    }
}
