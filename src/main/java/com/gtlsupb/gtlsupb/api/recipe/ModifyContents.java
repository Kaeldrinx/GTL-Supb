package com.gtlsupb.gtlsupb.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import net.minecraft.world.item.Item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import java.util.List;
import java.util.Map;

public final class ModifyContents {

    private ModifyContents() {}

    public static GTRecipe copyAndModifyRecipe(GTRecipe recipe, ContentModifier modifier) {
        GTRecipe copy = new GTRecipe(
                recipe.recipeType,
                recipe.id,
                modifyInputContents(recipe.inputs, modifier),
                modifyOutputContents(recipe.outputs, modifier),
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

    private static Map<RecipeCapability<?>, List<Content>> modifyOutputContents(
                                                                                Map<RecipeCapability<?>, List<Content>> before,
                                                                                ContentModifier modifier) {
        Reference2ReferenceOpenHashMap<RecipeCapability<?>, List<Content>> after = new Reference2ReferenceOpenHashMap<>();
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : before.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            List<Content> contentList = entry.getValue();
            ObjectArrayList<Content> copyList = new ObjectArrayList<>(contentList.size());
            if (cap == ItemRecipeCapability.CAP) {
                for (Content content : contentList) {
                    if (content.getContent() instanceof SizedIngredient &&
                            CycleItems.cycleItems.contains(
                                    ((SizedIngredient) content.getContent()).getItems()[0].getItem())) {
                        copyList.add(content);
                    } else {
                        copyList.add(content.copy(ItemRecipeCapability.CAP, modifier));
                    }
                }
            } else {
                for (Content content : contentList) {
                    copyList.add(content.copy(cap, modifier));
                }
            }
            after.put(cap, copyList);
        }
        return after;
    }

    private static Map<RecipeCapability<?>, List<Content>> modifyInputContents(
                                                                               Map<RecipeCapability<?>, List<Content>> before,
                                                                               ContentModifier modifier) {
        if (!before.containsKey(ItemRecipeCapability.CAP)) return before;
        Reference2ReferenceOpenHashMap<RecipeCapability<?>, List<Content>> after = new Reference2ReferenceOpenHashMap<>();
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : before.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            List<Content> contentList = entry.getValue();
            if (cap == ItemRecipeCapability.CAP) {
                ObjectArrayList<Content> copyList = new ObjectArrayList<>(contentList.size());
                for (Content content : contentList) {
                    if (content.getContent() instanceof SizedIngredient &&
                            CycleItems.cycleItems.contains(
                                    ((SizedIngredient) content.getContent()).getItems()[0].getItem())) {
                        copyList.add(content.copy(ItemRecipeCapability.CAP, modifier));
                    } else {
                        copyList.add(content);
                    }
                }
                after.put(cap, copyList);
            } else {
                after.put(cap, contentList);
            }
        }
        return after;
    }

    private static class CycleItems {

        static final ObjectOpenHashSet<Item> cycleItems = new ObjectOpenHashSet<>(
                new Item[] {
                        org.gtlcore.gtlcore.utils.Registries.getItem("kubejs:extremely_durable_plasma_cell"),
                        org.gtlcore.gtlcore.utils.Registries.getItem("kubejs:time_dilation_containment_unit"),
                        org.gtlcore.gtlcore.utils.Registries.getItem("kubejs:plasma_containment_cell"),
                });
    }
}
