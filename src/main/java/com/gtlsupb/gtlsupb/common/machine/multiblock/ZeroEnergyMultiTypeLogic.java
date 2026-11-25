package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.gtlsupb.gtlsupb.api.machine.logic.MultiTypeMutableRecipesLogic;
import com.gtlsupb.gtlsupb.common.data.ParallelData;
import com.gtlsupb.gtlsupb.utils.RecipeCalculationHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

public abstract class ZeroEnergyMultiTypeLogic<T extends ZeroEnergyMultiTypeMachine> extends MultiTypeMutableRecipesLogic<T> {

    public ZeroEnergyMultiTypeLogic(T machine) {
        super(machine);

        if (!machine.consumeEnergy()) {
            setLowVoltageInfiniteParallel(true);
            setLowVoltageTier(Integer.MAX_VALUE);
        }
    }

    @Override
    public boolean checkBeforeWorking() {
        if (!getMachine().hasProxies()) return false;
        return (getMachine().consumeEnergy() && getMachine().getOverclockVoltage() > 0) || !getMachine().consumeEnergy();
    }

    public double getDynamicOutputMultiplier() {
        return getMachine().outputMultiplier();
    }

    protected boolean requiresOutputs() {
        return true;
    }

    @Override
    public boolean checkRecipe(GTRecipe recipe) {
        return RecipeRunnerHelper.matchRecipeInput(getMachine(), recipe) &&
                (getRecipeCheck() == null || getRecipeCheck().test(recipe, getMachine()));
    }

    @Override
    protected long getIndependentSlotParallelCap() {
        return Math.max(getMachine().maxParallel(), 1L);
    }

    @Override
    public GTRecipe buildFinalNormalRecipe(ParallelData parallelData) {
        long maxEUt = getMachine().consumeEnergy() ? getMachine().getOverclockVoltage() : Long.MAX_VALUE;
        boolean independent = isIndependentSlotMode();
        if (independent) {

            parallelData = calculateIndependentBoundParallels();
            if (parallelData == null) return null;
        }

        boolean skipInputProbe = independent || gtlsupb$hasPendingBatchRecipes();
        RecipeCalculationHelper.ProcessParallelDataNormalResult result = RecipeCalculationHelper.processParallelDataNormal(
                parallelData, getMachine(), maxEUt, getEuMultiplier(),
                recipe -> BigInteger.valueOf(getRecipeEut(recipe)).multiply(BigInteger.valueOf(recipe.duration)),
                recipe -> false,
                skipInputProbe);

        boolean hasEuOutput = false;
        if (parallelData.originRecipeList != null) {
            for (int i = 0; i < parallelData.originRecipeList.size(); i++) {
                GTRecipe origin = parallelData.originRecipeList.get(i);
                if (origin != null && RecipeHelper.getOutputEUt(origin) > 0L) {
                    hasEuOutput = true;
                    break;
                }
            }
        }
        if (requiresOutputs() && !hasEuOutput && !RecipeCalculationHelper.hasOutputs(result.itemOutputs, result.fluidOutputs)) {
            if (getRecipeStatus() == null || getRecipeStatus().isSuccess()) {
                RecipeResult.of(getMachine(), RecipeResult.FAIL_FIND);
            }
            return null;
        }
        GTRecipe recipe = GTRecipeBuilder.ofRaw().buildRawRecipe();

        recipe.inputs.put(ItemRecipeCapability.CAP, independent ? new ObjectArrayList<>() : result.itemInputs);
        recipe.inputs.put(FluidRecipeCapability.CAP, independent ? new ObjectArrayList<>() : result.fluidInputs);

        double outputMult = getDynamicOutputMultiplier();
        if (outputMult != 1.0) {
            ContentModifier modifier = ContentModifier.multiplier(outputMult);
            List<Content> modifiedItems = result.itemOutputs.stream()
                    .map(it -> it.copy(ItemRecipeCapability.CAP, modifier))
                    .toList();
            List<Content> modifiedFluids = result.fluidOutputs.stream()
                    .map(it -> it.copy(FluidRecipeCapability.CAP, modifier))
                    .toList();
            recipe.outputs.put(ItemRecipeCapability.CAP, modifiedItems);
            recipe.outputs.put(FluidRecipeCapability.CAP, modifiedFluids);
        } else {
            recipe.outputs.put(ItemRecipeCapability.CAP, result.itemOutputs);
            recipe.outputs.put(FluidRecipeCapability.CAP, result.fluidOutputs);
        }

        int requestedBatch = independent ? 1 : getBatchMultiplier();
        int batchMultiplier = requestedBatch;
        if (batchMultiplier > 1) {

            recipe.duration = Math.max(1, getMachine().getLimitedDuration());

            batchMultiplier = RecipeCalculationHelper.findFeasibleBatch(getMachine(), recipe, batchMultiplier);
            if (batchMultiplier > 1) {
                recipe = recipe.copy(ContentModifier.multiplier(batchMultiplier), false);
            }
        }
        recipe.duration = batchMultiplier > 1 ? safeMultiplyDuration(getMachine().getLimitedDuration(), batchMultiplier) : getMachine().getLimitedDuration();

        long euOut = 0L;
        if (parallelData.originRecipeList != null) {
            for (int i = 0; i < parallelData.originRecipeList.size(); i++) {
                GTRecipe origin = parallelData.originRecipeList.get(i);
                if (origin == null) continue;
                long perTick = RecipeHelper.getOutputEUt(origin);
                if (perTick <= 0L) continue;
                long p = parallelData.parallels != null && i < parallelData.parallels.length ?
                        Math.max(1L, parallelData.parallels[i]) : 1L;
                long scaled = perTick > Long.MAX_VALUE / 4L / p ? Long.MAX_VALUE / 4L : perTick * p;
                euOut = Math.min(Long.MAX_VALUE / 4L, euOut + scaled);
            }
        }
        if (euOut > 0L) {
            double totalMult = getDynamicOutputMultiplier() * (batchMultiplier > 1 ? batchMultiplier : 1);
            euOut = (long) Math.min(Long.MAX_VALUE / 4d, euOut * totalMult);
            if (recipe.data == null) {
                recipe.data = new net.minecraft.nbt.CompoundTag();
            }
            recipe.data.putLong(EU_OUT_KEY, euOut);
        }

        gtlsupb$setMergedRecipeCount(parallelData);
        return recipe;
    }

    @Override
    public ParallelData calculateParallels() {
        if (isIndependentSlotMode()) {
            return calculateIndependentBoundParallels();
        }
        Set<GTRecipe> recipes = lookupRecipeSet();
        if (recipes.isEmpty()) return null;
        List<GTRecipe> candidates = gtlsupb$batchCandidates(recipes);
        if (candidates.isEmpty()) return null;

        return gtlsupb$collectBatchParallels(candidates);
    }

    @Override
    public GTRecipe getRecipe() {
        gtlsupb$beginSearch();
        if (!checkBeforeWorking()) return null;
        ParallelData parallelData = calculateParallels();
        if (parallelData == null) return null;
        GTRecipe recipe = buildFinalNormalRecipe(parallelData);

        if (recipe != null && !gtlsupb$outputsFit(recipe)) return null;
        return recipe;
    }

    private static final String EU_OUT_KEY = "gtlsupb:euOut";

    @Override
    public GTRecipe.ActionResult handleTickRecipe(GTRecipe recipe) {
        if (recipe != null && recipe.data != null && recipe.data.contains(EU_OUT_KEY)) {
            getMachine().gtlsupb$addGeneratedEu(recipe.data.getLong(EU_OUT_KEY));
        }
        return GTRecipe.ActionResult.SUCCESS;
    }

    @Override
    public void setupRecipe(GTRecipe recipe) {
        super.setupRecipe(recipe);

        progress = 0;
    }

    private static int safeMultiplyDuration(int duration, int batch) {
        long result = (long) duration * (long) batch;
        if (result > 2_000_000L) return 2_000_000;
        if (result < 1L) return 1;
        return (int) result;
    }
}
