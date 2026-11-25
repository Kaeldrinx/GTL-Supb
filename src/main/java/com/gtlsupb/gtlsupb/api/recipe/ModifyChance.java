package com.gtlsupb.gtlsupb.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import java.util.List;
import java.util.Map;

public final class ModifyChance {

    private ModifyChance() {}

    public static GTRecipe modifyChance(GTRecipe recipe, int inputChanceRatio) {
        GTRecipe copy = new GTRecipe(
                recipe.recipeType,
                recipe.id,
                modifyContents(recipe.inputs, true, inputChanceRatio),
                modifyContents(recipe.outputs, false, inputChanceRatio),
                recipe.tickInputs,
                recipe.tickOutputs,
                recipe.inputChanceLogics,
                recipe.outputChanceLogics,
                recipe.tickInputChanceLogics,
                recipe.tickOutputChanceLogics,
                recipe.conditions,
                recipe.ingredientActions,
                recipe.data,
                recipe.duration,
                recipe.isFuel);
        org.gtlcore.gtlcore.api.recipe.IGTRecipe.of(copy).setRealParallels(
                org.gtlcore.gtlcore.api.recipe.IGTRecipe.of(recipe).getRealParallels());
        copy.ocTier = recipe.ocTier;
        return copy;
    }

    @SuppressWarnings("unchecked")
    private static Reference2ReferenceOpenHashMap<RecipeCapability<?>, List<Content>> modifyContents(
                                                                                                     Map<RecipeCapability<?>, List<Content>> before,
                                                                                                     boolean isInput,
                                                                                                     int inputChanceRatio) {
        Reference2ReferenceOpenHashMap<RecipeCapability<?>, List<Content>> after = new Reference2ReferenceOpenHashMap<>();
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : before.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            List<Content> contentList = after.computeIfAbsent(cap, k -> new ObjectArrayList<>());
            for (Content cont : entry.getValue()) {
                if (cont.chance >= cont.maxChance) {
                    contentList.add(cont);
                } else if (cont.chance != 0) {
                    Content copy = cont.copy(cap, null);
                    if (isInput) {
                        copy.maxChance = cont.maxChance * inputChanceRatio;
                    } else {
                        copy.chance = cont.maxChance;
                    }
                    contentList.add(copy);
                }
            }
            if (contentList.isEmpty()) after.remove(cap);
        }
        return after;
    }
}
