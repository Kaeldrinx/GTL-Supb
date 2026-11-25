package com.gtlsupb.gtlsupb.api.recipe;

import org.gtlcore.gtlcore.api.machine.multiblock.ParallelMachine;
import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine;
import org.gtlcore.gtlcore.api.machine.trait.MEPatternRecipeHandlePart;
import org.gtlcore.gtlcore.api.machine.trait.RecipeHandlePart;
import org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient;
import org.gtlcore.gtlcore.utils.NumberUtils;
import org.gtlcore.gtlcore.utils.datastructure.Int128;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.utils.FluidStackHashStrategy;
import com.gregtechceu.gtceu.utils.IngredientEquality;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.hepdd.gtmthings.common.block.machine.trait.CatalystFluidStackHandler;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.Map;
import java.util.function.Predicate;

public final class ChanceParallelLogic {

    private ChanceParallelLogic() {}

    private static class ContentAmountPair {

        final int chance;
        final int maxChance;
        private long amount;

        ContentAmountPair(int chance, int maxChance) {
            this.chance = chance;
            this.maxChance = maxChance;
        }

        void addTo(long amount) {
            this.amount += amount;
        }
    }

    public static long getMaxParallel(
                                      ParallelMachine holder,
                                      GTRecipe recipe,
                                      long parallelAmount,
                                      Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches,
                                      ChanceBoostFunction boostFunction,
                                      int baseTier,
                                      int machineTier) {
        long parallel = parallelAmount;
        for (RecipeCapability<?> cap : recipe.inputs.keySet()) {
            if (cap == ItemRecipeCapability.CAP) {
                parallel = Math.min(
                        parallel,
                        getInputItemParallel(
                                holder,
                                recipe,
                                parallel,
                                chanceCaches.get(ItemRecipeCapability.CAP),
                                boostFunction,
                                baseTier,
                                machineTier));
                if (parallel == 0L) break;
            } else if (cap == FluidRecipeCapability.CAP) {
                parallel = Math.min(
                        parallel,
                        getInputFluidParallel(
                                holder,
                                recipe,
                                parallel,
                                chanceCaches.get(FluidRecipeCapability.CAP),
                                boostFunction,
                                baseTier,
                                machineTier));
                if (parallel == 0L) break;
            }
        }
        return parallel;
    }

    @SuppressWarnings("unchecked")
    public static long getInputItemParallel(
                                            ParallelMachine holder,
                                            GTRecipe recipe,
                                            long parallelAmount,
                                            Object2IntMap<?> cache,
                                            ChanceBoostFunction boostFunction,
                                            int baseTier,
                                            int machineTier) {
        if (parallelAmount <= 1) return parallelAmount;
        if (!(holder instanceof IRecipeCapabilityMachine)) return 1;
        if (((IRecipeCapabilityMachine) holder).emptyRecipeHandlePart()) return 0;

        Object2LongOpenCustomHashMap<Ingredient> guaranteedMap = new Object2LongOpenCustomHashMap<>(
                IngredientEquality.IngredientHashStrategy.INSTANCE);
        Object2ReferenceOpenCustomHashMap<Ingredient, ContentAmountPair> chanceMap = new Object2ReferenceOpenCustomHashMap<>(
                IngredientEquality.IngredientHashStrategy.INSTANCE);
        boolean confirmMEStock = holder.needConfirmMEStock();

        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            if (content.chance <= 0) continue;
            Ingredient ingredient = ItemRecipeCapability.CAP.of(content.getContent());
            long ingredientCount;
            if (ingredient instanceof LongIngredient) {
                ingredientCount = ((LongIngredient) ingredient).getActualAmount();
            } else if (ingredient instanceof SizedIngredient) {
                ingredientCount = ((SizedIngredient) ingredient).getAmount();
            } else {
                ingredientCount = 1L;
            }

            if (content.chance >= content.maxChance) {
                guaranteedMap.addTo(ingredient, ingredientCount);
            } else {
                int boostedChance = boostFunction.getBoostedChance(content, baseTier, machineTier);
                if (boostedChance >= content.maxChance) {
                    guaranteedMap.addTo(ingredient, ingredientCount);
                } else {
                    chanceMap.computeIfAbsent(
                            ingredient,
                            k -> new ContentAmountPair(boostedChance, content.maxChance)).addTo(ingredientCount);
                }
            }
        }

        if (guaranteedMap.isEmpty() && chanceMap.isEmpty()) return parallelAmount;

        Object2LongOpenCustomHashMap<ItemStack> ingredientStacks = new Object2LongOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());

        Object handle = ((IRecipeCapabilityMachine) holder).getActiveRecipeHandle(recipe);
        if (handle instanceof MEPatternRecipeHandlePart mep) {
            for (Object2LongMap.Entry<ItemStack> entry : Object2LongMaps.fastIterable(
                    mep.getMEContent(ItemRecipeCapability.CAP, recipe))) {
                ingredientStacks.mergeLong(
                        entry.getKey(),
                        entry.getLongValue(),
                        NumberUtils::saturatedAdd);
            }
        } else if (handle instanceof RecipeHandlePart rhp) {
            for (Object2LongMap.Entry<ItemStack> entry : Object2LongMaps.fastIterable(
                    rhp.getSelfContent(ItemRecipeCapability.CAP, confirmMEStock))) {
                ingredientStacks.mergeLong(
                        entry.getKey(),
                        entry.getLongValue(),
                        NumberUtils::saturatedAdd);
            }
        } else {
            RecipeHandlePart sharedRecipeHandlePart = ((IRecipeCapabilityMachine) holder).getSharedRecipeHandlePart();
            if (sharedRecipeHandlePart != null) {
                for (Object2LongMap.Entry<ItemStack> entry : Object2LongMaps.fastIterable(
                        sharedRecipeHandlePart.getSelfContent(ItemRecipeCapability.CAP, confirmMEStock))) {
                    ingredientStacks.mergeLong(
                            entry.getKey(),
                            entry.getLongValue(),
                            NumberUtils::saturatedAdd);
                }
            }
        }

        Object2IntMap<Ingredient> typedCache = cache != null ? (Object2IntMap<Ingredient>) cache : null;
        return calculateWithChance(
                parallelAmount,
                guaranteedMap,
                chanceMap,
                ingredientStacks,
                typedCache);
    }

    @SuppressWarnings("unchecked")
    public static long getInputFluidParallel(
                                             ParallelMachine holder,
                                             GTRecipe recipe,
                                             long parallelAmount,
                                             Object2IntMap<?> cache,
                                             ChanceBoostFunction boostFunction,
                                             int baseTier,
                                             int machineTier) {
        if (parallelAmount <= 1) return parallelAmount;
        if (!(holder instanceof IRecipeCapabilityMachine)) return 1;
        if (((IRecipeCapabilityMachine) holder).emptyRecipeHandlePart()) return 0;

        Object2LongOpenHashMap<FluidIngredient> guaranteedFluidMap = new Object2LongOpenHashMap<>();
        Object2ReferenceOpenHashMap<FluidIngredient, ContentAmountPair> chanceFluidMap = new Object2ReferenceOpenHashMap<>();
        boolean confirmMEStock = holder.needConfirmMEStock();

        for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            if (content.chance <= 0) continue;
            FluidIngredient fluidInput = FluidRecipeCapability.CAP.of(content.getContent());

            if (content.chance >= content.maxChance) {
                guaranteedFluidMap.addTo(fluidInput, fluidInput.getAmount());
            } else {
                int boostedChance = boostFunction.getBoostedChance(content, baseTier, machineTier);
                if (boostedChance >= content.maxChance) {
                    guaranteedFluidMap.addTo(fluidInput, fluidInput.getAmount());
                } else {
                    chanceFluidMap.computeIfAbsent(
                            fluidInput,
                            k -> new ContentAmountPair(boostedChance, content.maxChance)).addTo(fluidInput.getAmount());
                }
            }
        }

        if (guaranteedFluidMap.isEmpty() && chanceFluidMap.isEmpty()) {
            return parallelAmount;
        }

        Object2LongOpenCustomHashMap<FluidStack> ingredientStacks = new Object2LongOpenCustomHashMap<>(FluidStackHashStrategy.comparingAllButAmount());

        Object handle = ((IRecipeCapabilityMachine) holder).getActiveRecipeHandle(recipe);
        if (handle instanceof MEPatternRecipeHandlePart mep) {
            for (Object2LongMap.Entry<FluidStack> entry : Object2LongMaps.fastIterable(
                    mep.getMEContent(FluidRecipeCapability.CAP, recipe))) {
                ingredientStacks.mergeLong(
                        entry.getKey(),
                        entry.getLongValue(),
                        NumberUtils::saturatedAdd);
            }
        } else if (handle instanceof RecipeHandlePart rhp) {
            Object2LongMap<FluidStack> content;
            if (((IRecipeCapabilityMachine) holder).isDistinct()) {
                content = rhp.getSelfContent(FluidRecipeCapability.CAP, confirmMEStock);
            } else {
                content = rhp.getContentWithShared(FluidRecipeCapability.CAP, confirmMEStock);
            }
            for (Object2LongMap.Entry<FluidStack> entry : Object2LongMaps.fastIterable(content)) {
                ingredientStacks.mergeLong(
                        entry.getKey(),
                        entry.getLongValue(),
                        NumberUtils::saturatedAdd);
            }
        } else {
            RecipeHandlePart sharedRecipeHandlePart = ((IRecipeCapabilityMachine) holder).getSharedRecipeHandlePart();
            if (sharedRecipeHandlePart != null) {
                for (var handler : sharedRecipeHandlePart.getCapability(FluidRecipeCapability.CAP)) {
                    if (handler instanceof CatalystFluidStackHandler) continue;
                    for (var obj : handler.getContents()) {
                        if (obj instanceof FluidStack fluidStack) {
                            ingredientStacks.mergeLong(
                                    fluidStack,
                                    fluidStack.getAmount(),
                                    NumberUtils::saturatedAdd);
                        }
                    }
                }
            }
        }

        Object2IntMap<FluidIngredient> typedCache = cache != null ? (Object2IntMap<FluidIngredient>) cache : null;
        return calculateWithChance(
                parallelAmount,
                guaranteedFluidMap,
                chanceFluidMap,
                ingredientStacks,
                typedCache);
    }

    private static <I extends Predicate<S>, S> long calculateWithChance(
                                                                        long parallelLimit,
                                                                        Object2LongMap<I> guaranteedMap,
                                                                        Object2ReferenceMap<I, ContentAmountPair> chanceMap,
                                                                        Object2LongMap<S> ingredientStacks,
                                                                        Object2IntMap<I> cache) {
        if (ingredientStacks.isEmpty()) return 0;

        long parallel = parallelLimit;

        for (Object2LongMap.Entry<I> entry : Object2LongMaps.fastIterable(guaranteedMap)) {
            long needed = entry.getLongValue();
            long available = findAvailable(entry.getKey(), ingredientStacks);
            if (available < needed) return 0;
            parallel = Math.min(parallel, available / needed);
        }

        for (Object2ReferenceMap.Entry<I, ContentAmountPair> entry : Object2ReferenceMaps.fastIterable(chanceMap)) {
            ContentAmountPair chanceNeeded = entry.getValue();
            I ingredient = entry.getKey();

            if (chanceNeeded.chance <= 0) return 0;

            long available = findAvailable(ingredient, ingredientStacks);
            if (available < chanceNeeded.amount) return 0;

            int cached = getGuaranteedCachedChanceValue(
                    ingredient, chanceNeeded.maxChance, cache);

            long maxOutputs = available / chanceNeeded.amount;
            long maxParallelForThis = ((Number) new Int128(maxOutputs).add(1L)
                    .multiply(new Int128((long) chanceNeeded.maxChance))
                    .subtract(new Int128((long) (1 + cached)))
                    .divide((long) chanceNeeded.chance)).longValue();

            if (maxParallelForThis < 0) maxParallelForThis = 0;
            parallel = Math.min(parallel, maxParallelForThis);
        }

        return parallel;
    }

    private static <I extends Predicate<S>, S> long findAvailable(
                                                                  I ingredient,
                                                                  Object2LongMap<S> stacks) {
        ObjectIterator<Object2LongMap.Entry<S>> it = Object2LongMaps.fastIterator(stacks);
        while (it.hasNext()) {
            Object2LongMap.Entry<S> input = it.next();
            if (ingredient.test(input.getKey())) {
                long value = input.getLongValue();
                it.remove();
                return value;
            }
        }
        return 0;
    }

    private static <I> int getGuaranteedCachedChanceValue(
                                                          I content,
                                                          int maxChance,
                                                          Object2IntMap<I> cache) {
        if (cache == null) {
            return GTValues.RNG.nextInt(maxChance);
        }
        if (cache.containsKey(content)) {
            return cache.getInt(content);
        }
        int randomValue = GTValues.RNG.nextInt(maxChance);
        cache.put(content, randomValue);
        return randomValue;
    }
}
