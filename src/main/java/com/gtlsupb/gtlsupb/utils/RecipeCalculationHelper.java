package com.gtlsupb.gtlsupb.utils;

import org.gtlcore.gtlcore.api.machine.multiblock.ParallelMachine;
import org.gtlcore.gtlcore.api.recipe.IAdditionalRecipeIterator;
import org.gtlcore.gtlcore.api.recipe.IGTRecipe;
import org.gtlcore.gtlcore.api.recipe.IParallelLogic;
import org.gtlcore.gtlcore.api.recipe.RecipeResult;
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.gtlsupb.gtlsupb.api.recipe.ChanceParallelLogic;
import com.gtlsupb.gtlsupb.api.recipe.ModifyChance;
import com.gtlsupb.gtlsupb.api.recipe.WirelessGTRecipe;
import com.gtlsupb.gtlsupb.api.recipe.WirelessGTRecipeBuilder;
import com.gtlsupb.gtlsupb.common.data.ParallelData;
import com.gtlsupb.gtlsupb.config.ConfigHolder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RecipeCalculationHelper {

    public static final RecipeCalculationHelper INSTANCE = new RecipeCalculationHelper();

    public static final long EU_MULT_SCALE = 1_000_000L;
    public static final BigInteger BIG_EU_MULT_SCALE = BigInteger.valueOf(EU_MULT_SCALE);

    private RecipeCalculationHelper() {}

    public static Iterator<GTRecipe> enableDiveMode(Iterator<GTRecipe> iterator) {
        if (iterator instanceof IAdditionalRecipeIterator) {
            ((IAdditionalRecipeIterator) iterator).setUseDiveIngredientTreeFind(true);
        }
        return iterator;
    }

    @FunctionalInterface
    public interface CopyRecipeFunction {

        GTRecipe apply(GTRecipe recipe);
    }

    public static GTRecipe multipleRecipe(GTRecipe recipe, long parallel, CopyRecipeFunction copyRecipe) {
        GTRecipe processed = parallel > 1 ? copyRecipe.apply(recipe) : recipe;
        IGTRecipe.of(processed).setRealParallels(parallel);
        return processed;
    }

    public static GTRecipe multipleRecipe(GTRecipe recipe, long parallel) {
        return multipleRecipe(recipe, parallel, r -> r.copy(ContentModifier.multiplier((double) parallel), false));
    }

    public static <M extends IRecipeLogicMachine & ParallelMachine> ParallelData calculateChanceParallels(
                                                                                                          Set<GTRecipe> recipes,
                                                                                                          M machine,
                                                                                                          long parallelLimit,
                                                                                                          int chanceRatio,
                                                                                                          int machineTier) {
        if (recipes == null || recipes.isEmpty()) return null;

        ObjectArrayList<GTRecipe> recipeList = new ObjectArrayList<>();
        ObjectArrayList<GTRecipe> processedRecipeList = new ObjectArrayList<>();
        LongArrayList parallelsList = new LongArrayList();

        for (GTRecipe recipe : recipes) {
            GTRecipe modified = ModifyChance.modifyChance(recipe, chanceRatio);
            long parallel = ChanceParallelLogic.getMaxParallel(
                    machine,
                    modified,
                    parallelLimit,
                    machine.getRecipeLogic().getChanceCaches(),
                    recipe.getType().getChanceFunction(),
                    IGTRecipe.of(recipe).getEuTier(),
                    machineTier);
            if (parallel <= 0) continue;
            final long p = parallel;

            GTRecipe paralleledRecipe = IParallelLogic.getRecipeOutputChance(
                    machine,
                    multipleRecipe(modified, p, r -> copyFixRecipe(
                            r,
                            ContentModifier.multiplier((double) p),
                            chanceRatio)));

            if (!RecipeRunnerHelper.matchRecipeInput(machine, paralleledRecipe)) continue;

            recipeList.add(recipe);
            processedRecipeList.add(paralleledRecipe);
            parallelsList.add(p);
        }

        if (recipeList.isEmpty()) return null;
        return new ParallelData(recipeList, parallelsList.toLongArray(), false, processedRecipeList);
    }

    @FunctionalInterface
    public interface GetTotalRecipeEuFunction {

        BigInteger apply(GTRecipe recipe);
    }

    @FunctionalInterface
    public interface ShouldBreakFunction {

        boolean apply(BigInteger totalEu);
    }

    private static BigInteger scaleByDouble(BigInteger value, double multiplier) {
        if (multiplier == 1.0d) return value;
        double raw = multiplier * EU_MULT_SCALE;
        long scaled = raw >= (double) Long.MAX_VALUE ? Long.MAX_VALUE : raw <= 0.0d ? 0L : (long) raw;
        return value.multiply(BigInteger.valueOf(scaled)).divide(BIG_EU_MULT_SCALE);
    }

    public static class ProcessParallelDataNormalResult {

        public final ObjectArrayList<Content> itemInputs;
        public final ObjectArrayList<Content> fluidInputs;
        public final ObjectArrayList<Content> itemOutputs;
        public final ObjectArrayList<Content> fluidOutputs;
        public final BigInteger totalEu;

        public final int[] acceptedIndices;

        public ProcessParallelDataNormalResult(ObjectArrayList<Content> itemInputs, ObjectArrayList<Content> fluidInputs, ObjectArrayList<Content> itemOutputs, ObjectArrayList<Content> fluidOutputs, BigInteger totalEu) {
            this(itemInputs, fluidInputs, itemOutputs, fluidOutputs, totalEu, null);
        }

        public ProcessParallelDataNormalResult(ObjectArrayList<Content> itemInputs, ObjectArrayList<Content> fluidInputs, ObjectArrayList<Content> itemOutputs, ObjectArrayList<Content> fluidOutputs, BigInteger totalEu, int[] acceptedIndices) {
            this.itemInputs = itemInputs;
            this.fluidInputs = fluidInputs;
            this.itemOutputs = itemOutputs;
            this.fluidOutputs = fluidOutputs;
            this.totalEu = totalEu;
            this.acceptedIndices = acceptedIndices;
        }
    }

    public static ProcessParallelDataNormalResult processParallelDataNormal(
                                                                            ParallelData parallelData,
                                                                            IRecipeLogicMachine machine,
                                                                            long maxEUt,
                                                                            double euMultiplier,
                                                                            GetTotalRecipeEuFunction getTotalRecipeEu,
                                                                            ShouldBreakFunction shouldBreak,
                                                                            boolean skipInputProbe) {
        ObjectArrayList<Content> itemInputs = new ObjectArrayList<>();
        ObjectArrayList<Content> fluidInputs = new ObjectArrayList<>();
        ObjectArrayList<Content> itemOutputs = new ObjectArrayList<>();
        ObjectArrayList<Content> fluidOutputs = new ObjectArrayList<>();
        BigInteger totalEu = BigInteger.ZERO;

        GTRecipe probe = GTRecipeBuilder.ofRaw().buildRawRecipe();

        IntArrayList acceptedIndices = new IntArrayList();

        int recipeCount = Math.min(parallelData.originRecipeList.size(), parallelData.parallels.length);
        for (int i = 0; i < recipeCount; i++) {
            GTRecipe r = parallelData.originRecipeList.get(i);
            long p = parallelData.parallels[i];
            boolean accepted = true;
            if (parallelData.shouldProcess) {
                GTRecipe processedRecipe = IParallelLogic.getRecipeOutputChance(machine, multipleRecipe(r, p));

                if (!skipInputProbe) {
                    accepted = accumulateInputsIfFit(probe, machine, processedRecipe, itemInputs, fluidInputs);
                } else {
                    collectInputs(processedRecipe, itemInputs, fluidInputs);
                }
                if (accepted) {
                    totalEu = totalEu.add(scaleByDouble(
                            getTotalRecipeEu.apply(r).multiply(BigInteger.valueOf(p)), euMultiplier));
                    collectOutputs(processedRecipe, itemOutputs, fluidOutputs);
                    acceptedIndices.add(i);
                }
            } else {

                GTRecipe processedRecipe = parallelData.processedRecipeList != null &&
                        i < parallelData.processedRecipeList.size() ? parallelData.processedRecipeList.get(i) : multipleRecipe(r, p);
                if (!skipInputProbe) {
                    accepted = accumulateInputsIfFit(probe, machine, processedRecipe, itemInputs, fluidInputs);
                } else {
                    collectInputs(processedRecipe, itemInputs, fluidInputs);
                }
                if (accepted) {
                    BigInteger recipeEu = getTotalRecipeEu.apply(r).multiply(BigInteger.valueOf(p));
                    if (euMultiplier != 1.0d) {
                        double rawMult = euMultiplier * EU_MULT_SCALE;
                        long scaledMult = rawMult >= (double) Long.MAX_VALUE ? Long.MAX_VALUE : rawMult <= 0.0d ? 0L : (long) rawMult;
                        recipeEu = recipeEu.multiply(BigInteger.valueOf(scaledMult)).divide(BIG_EU_MULT_SCALE);
                    }
                    totalEu = totalEu.add(recipeEu);
                    collectOutputs(processedRecipe, itemOutputs, fluidOutputs);
                    acceptedIndices.add(i);
                }
            }

            if (shouldBreak.apply(totalEu)) break;
        }

        return new ProcessParallelDataNormalResult(itemInputs, fluidInputs, itemOutputs, fluidOutputs, totalEu,
                acceptedIndices.toIntArray());
    }

    public static ProcessParallelDataNormalResult processParallelDataNormal(
                                                                            ParallelData parallelData,
                                                                            IRecipeLogicMachine machine,
                                                                            long maxEUt,
                                                                            double euMultiplier,
                                                                            GetTotalRecipeEuFunction getTotalRecipeEu,
                                                                            ShouldBreakFunction shouldBreak) {
        return processParallelDataNormal(parallelData, machine, maxEUt, euMultiplier, getTotalRecipeEu, shouldBreak, false);
    }

    private static boolean accumulateInputsIfFit(
                                                 GTRecipe probe,
                                                 IRecipeLogicMachine machine,
                                                 GTRecipe recipe,
                                                 ObjectArrayList<Content> itemInputs,
                                                 ObjectArrayList<Content> fluidInputs) {
        return accumulateInputsIfFit(
                probe, machine,
                recipe.inputs.get(ItemRecipeCapability.CAP),
                recipe.inputs.get(FluidRecipeCapability.CAP),
                itemInputs, fluidInputs);
    }

    public static boolean accumulateInputsIfFit(
                                                IRecipeLogicMachine machine,
                                                List<Content> items,
                                                List<Content> fluids,
                                                ObjectArrayList<Content> itemInputs,
                                                ObjectArrayList<Content> fluidInputs) {
        return accumulateInputsIfFit(
                GTRecipeBuilder.ofRaw().buildRawRecipe(), machine, items, fluids, itemInputs, fluidInputs);
    }

    private static boolean isCircuitContent(Content content) {
        if (content == null || content.content == null) return false;
        try {
            Ingredient ingredient = ItemRecipeCapability.CAP.of(content.content);
            ItemStack[] stacks = ingredient.getItems();
            return stacks.length > 0 && IntCircuitBehaviour.isIntegratedCircuit(stacks[0]);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean accumulateInputsIfFit(
                                                 GTRecipe probe,
                                                 IRecipeLogicMachine machine,
                                                 List<Content> items,
                                                 List<Content> fluids,
                                                 ObjectArrayList<Content> itemInputs,
                                                 ObjectArrayList<Content> fluidInputs) {
        if ((items == null || items.isEmpty()) && (fluids == null || fluids.isEmpty())) {
            return true;
        }

        int itemBase = itemInputs.size();
        int fluidBase = fluidInputs.size();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Content content = items.get(i);

                if (isCircuitContent(content)) continue;
                itemInputs.add(content);
            }
        }
        if (fluids != null) fluidInputs.addAll(fluids);
        probe.inputs.put(ItemRecipeCapability.CAP, itemInputs);
        probe.inputs.put(FluidRecipeCapability.CAP, fluidInputs);
        if (!RecipeRunnerHelper.matchRecipeInput(machine, probe)) {
            if (itemInputs.size() > itemBase) itemInputs.removeElements(itemBase, itemInputs.size());
            if (fluidInputs.size() > fluidBase) fluidInputs.removeElements(fluidBase, fluidInputs.size());
            return false;
        }
        return true;
    }

    public static int findFeasibleBatch(IRecipeLogicMachine machine, GTRecipe merged, int maxBatch) {
        if (maxBatch <= 1 || machine == null || merged.inputs.isEmpty()) {
            return 1;
        }
        int batchDuration = ConfigHolder.INSTANCE != null ? ConfigHolder.INSTANCE.batchDuration : 100;

        if (batchDuration <= 0) {
            return 1;
        }
        int baseDuration = Math.max(1, merged.duration);
        if (baseDuration >= batchDuration) {
            return 1;
        }
        int limit = Math.min(maxBatch, batchDuration / baseDuration);
        if (limit <= 1) {
            return 1;
        }

        long feasible = RecipeSafety.maxFeasible(limit, n -> {
            GTRecipe scaled = merged.copy(ContentModifier.multiplier((double) n), false);
            return RecipeRunnerHelper.matchRecipeInput(machine, scaled) &&
                    RecipeRunnerHelper.matchRecipeOutput(machine, scaled);
        });
        return (int) Math.max(1L, feasible);
    }

    public static ProcessParallelDataNormalResult processParallelDataNormal(
                                                                            ParallelData parallelData,
                                                                            IRecipeLogicMachine machine,
                                                                            long maxEUt,
                                                                            double euMultiplier,
                                                                            GetTotalRecipeEuFunction getTotalRecipeEu) {
        return processParallelDataNormal(parallelData, machine, maxEUt, euMultiplier, getTotalRecipeEu,
                totalEu -> totalEu.divide(BigInteger.valueOf(Math.max(1L, maxEUt)))
                        .compareTo(BigInteger.valueOf(10_000L)) > 0);
    }

    @FunctionalInterface
    public interface GetRecipeEutFunction {

        long apply(GTRecipe recipe);
    }

    public static class ProcessParallelDataWirelessResult {

        public final ObjectArrayList<Content> itemInputs;
        public final ObjectArrayList<Content> fluidInputs;
        public final ObjectArrayList<Content> itemOutputs;
        public final ObjectArrayList<Content> fluidOutputs;
        public final BigInteger totalEu;

        public final int[] acceptedIndices;

        public ProcessParallelDataWirelessResult(ObjectArrayList<Content> itemInputs, ObjectArrayList<Content> fluidInputs, ObjectArrayList<Content> itemOutputs, ObjectArrayList<Content> fluidOutputs, BigInteger totalEu) {
            this(itemInputs, fluidInputs, itemOutputs, fluidOutputs, totalEu, null);
        }

        public ProcessParallelDataWirelessResult(ObjectArrayList<Content> itemInputs, ObjectArrayList<Content> fluidInputs, ObjectArrayList<Content> itemOutputs, ObjectArrayList<Content> fluidOutputs, BigInteger totalEu, int[] acceptedIndices) {
            this.itemInputs = itemInputs;
            this.fluidInputs = fluidInputs;
            this.itemOutputs = itemOutputs;
            this.fluidOutputs = fluidOutputs;
            this.totalEu = totalEu;
            this.acceptedIndices = acceptedIndices;
        }
    }

    public static ProcessParallelDataWirelessResult processParallelDataWireless(
                                                                                ParallelData parallelData,
                                                                                IRecipeLogicMachine machine,
                                                                                BigInteger maxTotalEu,
                                                                                double euMultiplier,
                                                                                GetRecipeEutFunction getRecipeEut,
                                                                                boolean isEnergyConsumer,
                                                                                boolean skipInputProbe) {
        ObjectArrayList<Content> itemInputs = new ObjectArrayList<>();
        ObjectArrayList<Content> fluidInputs = new ObjectArrayList<>();
        ObjectArrayList<Content> itemOutputs = new ObjectArrayList<>();
        ObjectArrayList<Content> fluidOutputs = new ObjectArrayList<>();
        BigInteger accumulatedEu = BigInteger.ZERO;

        double euMultRaw = euMultiplier * EU_MULT_SCALE;
        long euMultScaled = euMultRaw >= (double) Long.MAX_VALUE ? Long.MAX_VALUE : euMultRaw <= 0.0d ? 0L : (long) euMultRaw;
        BigInteger bigEuMultScaled = BigInteger.valueOf(euMultScaled);

        GTRecipe probe = GTRecipeBuilder.ofRaw().buildRawRecipe();
        IntArrayList acceptedIndices = new IntArrayList();

        int recipeCount = Math.min(parallelData.originRecipeList.size(), parallelData.parallels.length);
        for (int i = 0; i < recipeCount; i++) {
            GTRecipe r = parallelData.originRecipeList.get(i);
            long p = parallelData.parallels[i];

            BigInteger parallelEUt = BigInteger.valueOf(getRecipeEut.apply(r)).multiply(BigInteger.valueOf(p));
            BigInteger tempAccumulatedEu = accumulatedEu.add(
                    BigInteger.valueOf(r.duration).multiply(bigEuMultScaled).multiply(parallelEUt).divide(BIG_EU_MULT_SCALE));

            boolean accepted = true;
            if (parallelData.shouldProcess) {
                if (isEnergyConsumer && tempAccumulatedEu.compareTo(maxTotalEu) > 0) {
                    if (accumulatedEu.signum() == 0) RecipeResult.of(machine, RecipeResult.FAIL_NO_ENOUGH_EU_IN);
                    break;
                }

                GTRecipe paralleledRecipe = IParallelLogic.getRecipeOutputChance(machine, multipleRecipe(r, p));

                if (!skipInputProbe) {
                    accepted = accumulateInputsIfFit(probe, machine, paralleledRecipe, itemInputs, fluidInputs);
                } else {
                    collectInputs(paralleledRecipe, itemInputs, fluidInputs);
                }
                if (accepted) {
                    accumulatedEu = tempAccumulatedEu;
                    collectOutputs(paralleledRecipe, itemOutputs, fluidOutputs);
                    acceptedIndices.add(i);
                }
            } else {

                GTRecipe processedRecipe = parallelData.processedRecipeList != null &&
                        i < parallelData.processedRecipeList.size() ? parallelData.processedRecipeList.get(i) : multipleRecipe(r, p);
                if (!skipInputProbe) {
                    accepted = accumulateInputsIfFit(probe, machine, processedRecipe, itemInputs, fluidInputs);
                } else {
                    collectInputs(processedRecipe, itemInputs, fluidInputs);
                }
                if (accepted) {
                    accumulatedEu = tempAccumulatedEu;
                    collectOutputs(processedRecipe, itemOutputs, fluidOutputs);
                    acceptedIndices.add(i);
                }
            }
        }

        BigInteger totalEu = isEnergyConsumer ? accumulatedEu : accumulatedEu.negate();
        return new ProcessParallelDataWirelessResult(itemInputs, fluidInputs, itemOutputs, fluidOutputs, totalEu,
                acceptedIndices.toIntArray());
    }

    public static ProcessParallelDataWirelessResult processParallelDataWireless(
                                                                                ParallelData parallelData,
                                                                                IRecipeLogicMachine machine,
                                                                                BigInteger maxTotalEu,
                                                                                double euMultiplier,
                                                                                GetRecipeEutFunction getRecipeEut,
                                                                                boolean isEnergyConsumer) {
        return processParallelDataWireless(parallelData, machine, maxTotalEu, euMultiplier, getRecipeEut, isEnergyConsumer, false);
    }

    public static ProcessParallelDataWirelessResult processParallelDataWireless(
                                                                                ParallelData parallelData,
                                                                                IRecipeLogicMachine machine,
                                                                                BigInteger maxTotalEu,
                                                                                double euMultiplier,
                                                                                GetRecipeEutFunction getRecipeEut) {
        return processParallelDataWireless(parallelData, machine, maxTotalEu, euMultiplier, getRecipeEut, true);
    }

    public static GTRecipe buildNormalRecipe(
                                             List<Content> itemOutputs,
                                             List<Content> fluidOutputs,
                                             BigInteger totalEu,
                                             long maxEUt,
                                             int minDuration) {
        return buildNormalRecipe(null, new ObjectArrayList<>(), new ObjectArrayList<>(), itemOutputs, fluidOutputs, totalEu, maxEUt, minDuration, 1);
    }

    public static GTRecipe buildNormalRecipe(
                                             IRecipeLogicMachine machine,
                                             List<Content> itemInputs,
                                             List<Content> fluidInputs,
                                             List<Content> itemOutputs,
                                             List<Content> fluidOutputs,
                                             BigInteger totalEu,
                                             long maxEUt,
                                             int minDuration,
                                             int batchMultiplier) {
        GTRecipe recipe = GTRecipeBuilder.ofRaw().buildRawRecipe();

        recipe.inputs.put(ItemRecipeCapability.CAP, itemInputs);
        recipe.inputs.put(FluidRecipeCapability.CAP, fluidInputs);
        recipe.outputs.put(ItemRecipeCapability.CAP, itemOutputs);
        recipe.outputs.put(FluidRecipeCapability.CAP, fluidOutputs);

        double baseDuration = Math.max(1.0d, minDuration);
        int batch = Math.max(1, batchMultiplier);

        if (batch > 1 && machine != null) {

            recipe.duration = (int) baseDuration;
            batch = findFeasibleBatch(machine, recipe, batch);
            if (batch > 1) {
                recipe = recipe.copy(ContentModifier.multiplier(batch), false);
            }
        }

        int duration = (int) Math.min(baseDuration * batch, Integer.MAX_VALUE);

        BigInteger perTickEu = totalEu.divide(BigInteger.valueOf((long) baseDuration));
        long eut = Math.min(perTickEu.bitLength() <= 63 ? perTickEu.longValue() : Long.MAX_VALUE, maxEUt);
        recipe.tickInputs.put(EURecipeCapability.CAP, Arrays.asList(new Content(eut, 10000, 10000, 0, null, null)));
        recipe.duration = duration;
        IGTRecipe.of(recipe).setHasTick(true);
        return recipe;
    }

    public static WirelessGTRecipe buildWirelessRecipe(
                                                       List<Content> itemInputs,
                                                       List<Content> fluidInputs,
                                                       List<Content> itemOutputs,
                                                       List<Content> fluidOutputs,
                                                       int duration,
                                                       BigInteger totalEu,
                                                       GTRecipeType recipeType) {
        BigInteger euTick = RecipeSafety.safeDivide(totalEu, duration, BigInteger.ZERO);
        BigInteger eut = euTick.negate();
        WirelessGTRecipe recipe = WirelessGTRecipeBuilder
                .ofRaw(recipeType)
                .duration(duration)
                .setWirelessEut(eut)
                .output(ItemRecipeCapability.CAP, itemOutputs)
                .output(FluidRecipeCapability.CAP, fluidOutputs)
                .buildRawRecipe();

        recipe.inputs.put(ItemRecipeCapability.CAP, itemInputs);
        recipe.inputs.put(FluidRecipeCapability.CAP, fluidInputs);
        return recipe;
    }

    public static WirelessGTRecipe buildWirelessRecipe(
                                                       List<Content> itemInputs,
                                                       List<Content> fluidInputs,
                                                       List<Content> itemOutputs,
                                                       List<Content> fluidOutputs,
                                                       int duration,
                                                       BigInteger totalEu) {
        return buildWirelessRecipe(itemInputs, fluidInputs, itemOutputs, fluidOutputs, duration, totalEu, GTRecipeTypes.DUMMY_RECIPES);
    }

    public static void collectInputs(
                                     GTRecipe recipe,
                                     List<Content> itemInputs,
                                     List<Content> fluidInputs) {
        List<Content> items = recipe.inputs.get(ItemRecipeCapability.CAP);
        if (items != null) itemInputs.addAll(items);
        List<Content> fluids = recipe.inputs.get(FluidRecipeCapability.CAP);
        if (fluids != null) fluidInputs.addAll(fluids);
    }

    public static void collectOutputs(
                                      GTRecipe recipe,
                                      List<Content> itemOutputs,
                                      List<Content> fluidOutputs) {
        List<Content> items = recipe.outputs.get(ItemRecipeCapability.CAP);
        if (items != null) itemOutputs.addAll(items);
        List<Content> fluids = recipe.outputs.get(FluidRecipeCapability.CAP);
        if (fluids != null) fluidOutputs.addAll(fluids);
    }

    public static boolean hasOutputs(List<Content> itemOutputs, List<Content> fluidOutputs) {
        return !itemOutputs.isEmpty() || !fluidOutputs.isEmpty();
    }

    @FunctionalInterface
    public interface GetParallelAndConsumptionFunction {

        LongLongPair apply(GTRecipe recipe, long remaining);
    }

    @FunctionalInterface
    public interface ModifyRecipeFunction {

        GTRecipe apply(GTRecipe recipe);
    }

    @FunctionalInterface
    public interface CreateParalleledRecipeFunction {

        GTRecipe apply(GTRecipe recipe, long parallel);
    }

    public static ParallelData calculateParallelsWithGreedyAllocation(
                                                                      Collection<GTRecipe> recipes,
                                                                      long totalParallel,
                                                                      IRecipeLogicMachine machine,
                                                                      ModifyRecipeFunction modifyRecipe,
                                                                      CreateParalleledRecipeFunction createParalleledRecipe,
                                                                      GetParallelAndConsumptionFunction getParallelAndConsumption) {
        long remain = totalParallel;

        ObjectArrayList<GTRecipe> recipeList = new ObjectArrayList<>();
        ObjectArrayList<GTRecipe> processedRecipeList = new ObjectArrayList<>();
        LongArrayList parallelsList = new LongArrayList();

        for (GTRecipe match : recipes) {
            if (remain <= 0) break;
            GTRecipe modified = modifyRecipe.apply(match);
            LongLongPair pair = getParallelAndConsumption.apply(modified, remain);
            long p = pair.firstLong();
            if (p <= 0) continue;

            GTRecipe paralleledRecipe = IParallelLogic.getRecipeOutputChance(
                    machine,
                    createParalleledRecipe.apply(modified, p));
            if (RecipeRunnerHelper.matchRecipeInput(machine, paralleledRecipe)) {
                remain -= pair.secondLong();
                recipeList.add(match);
                processedRecipeList.add(paralleledRecipe);
                parallelsList.add(p);
            }
        }

        return recipeList.isEmpty() ? null : new ParallelData(recipeList, parallelsList.toLongArray(), false, processedRecipeList);
    }

    public static ParallelData calculateParallelsWithGreedyAllocation(
                                                                      Collection<GTRecipe> recipes,
                                                                      long totalParallel,
                                                                      IRecipeLogicMachine machine,
                                                                      GetParallelAndConsumptionFunction getParallelAndConsumption) {
        return calculateParallelsWithGreedyAllocation(
                recipes, totalParallel, machine,
                r -> r,
                RecipeCalculationHelper::multipleRecipe,
                getParallelAndConsumption);
    }

    public static ParallelData calculateParallelsWithFairAllocation(
                                                                    Collection<GTRecipe> recipes,
                                                                    long totalParallel,
                                                                    IRecipeLogicMachine machine,
                                                                    GetParallelAndConsumptionFunction getParallelAndConsumption) {
        if (recipes.isEmpty() || totalParallel <= 0L) return null;
        long share = totalParallel / recipes.size();
        long remain = totalParallel - share * recipes.size();
        ObjectArrayList<GTRecipe> recipeList = new ObjectArrayList<>();
        ObjectArrayList<GTRecipe> processedRecipeList = new ObjectArrayList<>();
        LongArrayList parallelsList = new LongArrayList();
        for (GTRecipe match : recipes) {
            long want = share + (remain > 0L ? 1L : 0L);
            if (remain > 0L) remain--;
            LongLongPair pair = getParallelAndConsumption.apply(match, want);
            long p = pair.firstLong();
            if (p <= 0L) continue;
            GTRecipe paralleled = IParallelLogic.getRecipeOutputChance(
                    machine, multipleRecipe(match, p));
            if (!RecipeRunnerHelper.matchRecipeInput(machine, paralleled)) continue;
            recipeList.add(match);
            processedRecipeList.add(paralleled);
            parallelsList.add(p);
        }
        return recipeList.isEmpty() ? null : new ParallelData(recipeList, parallelsList.toLongArray(), false, processedRecipeList);
    }

    @FunctionalInterface
    public interface GetParallelLimitForRecipeFunction {

        long apply(GTRecipe recipe);
    }

    @FunctionalInterface
    public interface GetMaxParallelForRecipeFunction {

        long apply(GTRecipe recipe, long limit);
    }

    public static ParallelData calculateParallelsWithProcessing(
                                                                Collection<GTRecipe> recipes,
                                                                IRecipeLogicMachine machine,
                                                                GetParallelLimitForRecipeFunction getParallelLimitForRecipe,
                                                                GetMaxParallelForRecipeFunction getMaxParallelForRecipe,
                                                                ModifyRecipeFunction modifyRecipe,
                                                                CreateParalleledRecipeFunction createParalleledRecipe,
                                                                boolean useModifiedRecipe,
                                                                boolean preProcessRecipes) {
        int length = recipes.size();
        if (length == 0) return null;

        ObjectArrayList<GTRecipe> recipeList = new ObjectArrayList<>(length);
        ObjectArrayList<GTRecipe> processedRecipeList = preProcessRecipes ? new ObjectArrayList<>(length) : null;
        LongArrayList parallelsList = new LongArrayList(length);

        for (GTRecipe recipe : recipes) {
            GTRecipe modified = modifyRecipe.apply(recipe);
            long limit = getParallelLimitForRecipe.apply(modified);
            long parallel = getMaxParallelForRecipe.apply(modified, limit);

            if (parallel > 0) {
                if (preProcessRecipes) {
                    GTRecipe paralleledRecipe = IParallelLogic.getRecipeOutputChance(
                            machine,
                            createParalleledRecipe.apply(modified, parallel));
                    if (RecipeRunnerHelper.matchRecipeInput(machine, paralleledRecipe)) {
                        recipeList.add(useModifiedRecipe ? modified : recipe);
                        processedRecipeList.add(paralleledRecipe);
                        parallelsList.add(parallel);
                    }
                } else {
                    recipeList.add(useModifiedRecipe ? modified : recipe);
                    parallelsList.add(parallel);
                }
            }
        }

        return recipeList.isEmpty() ? null : new ParallelData(recipeList, parallelsList.toLongArray(), !preProcessRecipes, processedRecipeList);
    }

    public static ParallelData calculateParallelsWithProcessing(
                                                                Collection<GTRecipe> recipes,
                                                                IRecipeLogicMachine machine,
                                                                GetParallelLimitForRecipeFunction getParallelLimitForRecipe,
                                                                GetMaxParallelForRecipeFunction getMaxParallelForRecipe) {
        return calculateParallelsWithProcessing(
                recipes, machine,
                getParallelLimitForRecipe, getMaxParallelForRecipe,
                r -> r,
                RecipeCalculationHelper::multipleRecipe,
                false, true);
    }

    public static GTRecipe copyFixRecipe(GTRecipe origin, ContentModifier modifier, int fixMultiplier) {
        return new GTRecipe(
                origin.recipeType,
                origin.id,
                copyFixContents(origin.inputs, modifier, fixMultiplier),
                copyFixContents(origin.outputs, modifier, fixMultiplier),
                copyFixContents(origin.tickInputs, modifier, fixMultiplier),
                copyFixContents(origin.tickOutputs, modifier, fixMultiplier),
                new Reference2ReferenceArrayMap<>(origin.inputChanceLogics),
                new Reference2ReferenceArrayMap<>(origin.outputChanceLogics),
                new Reference2ReferenceArrayMap<>(origin.tickInputChanceLogics),
                new Reference2ReferenceArrayMap<>(origin.tickOutputChanceLogics),
                new ObjectArrayList<>(origin.conditions),
                new ObjectArrayList<>(origin.ingredientActions),
                origin.data,
                origin.duration,
                origin.isFuel);
    }

    public static Map<RecipeCapability<?>, List<Content>> copyFixContents(
                                                                          Map<RecipeCapability<?>, List<Content>> contents,
                                                                          ContentModifier modifier,
                                                                          int fixMultiplier) {
        Reference2ReferenceArrayMap<RecipeCapability<?>, List<Content>> result = new Reference2ReferenceArrayMap<>();
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : contents.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            List<Content> contentList = entry.getValue();
            if (!contentList.isEmpty()) {

                ObjectArrayList<Content> newList = new ObjectArrayList<>(contentList.size());
                for (Content content : contentList) {
                    newList.add(copyFixBoost(content, cap, modifier, fixMultiplier));
                }
                result.put(cap, newList);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Content copyFixBoost(
                                       Content content,
                                       RecipeCapability<?> capability,
                                       ContentModifier modifier,
                                       int fixMultiplier) {
        Object newContent;
        if (content.chance != 0) {
            newContent = ((RecipeCapability<Object>) capability).copyContent(content.content, modifier);
        } else {
            newContent = ((RecipeCapability<Object>) capability).copyContent(content.content);
        }

        return new Content(
                newContent,
                content.chance,
                content.maxChance,
                content.tierChanceBoost / fixMultiplier,
                content.slotName,
                content.uiName);
    }
}
