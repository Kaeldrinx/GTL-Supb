package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.recipe.IGTRecipe;
import org.gtlcore.gtlcore.api.recipe.IParallelLogic;
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import com.gtlsupb.gtlsupb.api.machine.SteamEnergyManager;
import com.gtlsupb.gtlsupb.api.recipe.ChanceParallelLogic;
import com.gtlsupb.gtlsupb.api.recipe.ModifyChance;
import com.gtlsupb.gtlsupb.common.data.ParallelData;
import com.gtlsupb.gtlsupb.utils.RecipeCalculationHelper;
import com.gtlsupb.gtlsupb.utils.RecipeSafety;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.math.BigInteger;
import java.util.UUID;

public class NoEnergyIntegratedProcessorLogic extends ZeroEnergyMultiTypeLogic<GTLSupbNoEnergyIntegratedProcessor> {

    private static final int INPUT_CHANCE_RATIO = 10;

    private BigInteger gtlsupb$pendingSteam = BigInteger.ZERO;
    private UUID gtlsupb$pendingSteamUuid;

    public NoEnergyIntegratedProcessorLogic(GTLSupbNoEnergyIntegratedProcessor machine) {
        super(machine);
    }

    @Override
    public boolean checkBeforeWorking() {
        if (!getMachine().hasProxies()) return false;
        if (getMachine().uuid == null) return false;
        return SteamEnergyManager.getUserSteam(getMachine().uuid).signum() > 0;
    }

    private void gtlsupb$stageSteam(UUID uuid, BigInteger cost) {
        if (uuid == null || cost == null || cost.signum() <= 0) {
            gtlsupb$discardStagedSteam();
            return;
        }
        gtlsupb$pendingSteam = cost;
        gtlsupb$pendingSteamUuid = uuid;
    }

    private void gtlsupb$commitStagedSteam() {
        if (gtlsupb$pendingSteamUuid == null || gtlsupb$pendingSteam.signum() <= 0) {
            gtlsupb$discardStagedSteam();
            return;
        }
        long cost = gtlsupb$pendingSteam.bitLength() <= 63 ? gtlsupb$pendingSteam.longValue() : Long.MAX_VALUE;
        SteamEnergyManager.addSteamToGlobalMap(gtlsupb$pendingSteamUuid, -cost);
        gtlsupb$discardStagedSteam();
    }

    private void gtlsupb$discardStagedSteam() {
        gtlsupb$pendingSteam = BigInteger.ZERO;
        gtlsupb$pendingSteamUuid = null;
    }

    private long gtlsupb$steamCost(GTRecipe recipe) {
        long eu = 0L;
        if (recipe.data != null && recipe.data.contains(GTLSupbNoEnergyIntegratedProcessor.STEAM_EU_KEY)) {
            eu = recipe.data.getLong(GTLSupbNoEnergyIntegratedProcessor.STEAM_EU_KEY);
        }
        long cost = eu / GTLSupbNoEnergyIntegratedProcessor.STEAM_CONVERSION_RATIO;
        if (cost <= 0L) return 1L;
        return Math.max(1L, (long) (cost * getMachine().getHatchEfficiencyMultiplier()));
    }

    @Override
    public ParallelData calculateParallels() {
        var recipes = lookupRecipeSet();
        if (recipes.isEmpty()) return null;

        GTLSupbNoEnergyIntegratedProcessor m = getMachine();
        UUID uuid = m.uuid;
        if (uuid == null) return null;

        long availableSteam = SteamEnergyManager.getUserSteamLong(uuid);
        if (availableSteam <= 0L) return null;

        ObjectArrayList<GTRecipe> recipeList = new ObjectArrayList<>();
        ObjectArrayList<GTRecipe> processedRecipeList = new ObjectArrayList<>();
        LongArrayList parallelsList = new LongArrayList();
        LongArrayList steamCosts = new LongArrayList();

        var candidates = gtlsupb$batchCandidates(recipes);
        int total = candidates.size();

        ObjectArrayList<Content> probeItems = new ObjectArrayList<>();
        ObjectArrayList<Content> probeFluids = new ObjectArrayList<>();
        for (int i = 0; i < total; i++) {
            GTRecipe recipe = candidates.get(i);
            GTRecipe modified = ModifyChance.modifyChance(recipe, INPUT_CHANCE_RATIO);
            long steamCost = gtlsupb$steamCost(recipe);

            long remainingCandidates = Math.max(1L, (long) total - i);
            long fairSteam = availableSteam / remainingCandidates;
            long steamParallel = steamCost <= 0L ? Long.MAX_VALUE : fairSteam / steamCost;
            if (steamParallel <= 0L) {

                if (availableSteam < steamCost) continue;
                steamParallel = 1L;
            }

            long p = ChanceParallelLogic.getMaxParallel(
                    m, modified, Math.min(m.maxParallel(), steamParallel),
                    getChanceCaches(),
                    recipe.getType().getChanceFunction(),
                    IGTRecipe.of(recipe).getEuTier(),
                    m.getTier());
            if (p <= 0) continue;

            long remaining = Math.max(0L, (long) total - 1L - i);
            long reserve = Math.min(remaining, p / 4L);
            if (reserve > 0L) {
                p = Math.max(1L, p - reserve);
            }

            final long scaledParallel = p;
            GTRecipe paralleledRecipe = IParallelLogic.getRecipeOutputChance(
                    m,
                    RecipeCalculationHelper.multipleRecipe(modified, scaledParallel, r -> RecipeCalculationHelper.copyFixRecipe(
                            r,
                            ContentModifier.multiplier((double) scaledParallel),
                            INPUT_CHANCE_RATIO)));

            if (RecipeCalculationHelper.accumulateInputsIfFit(m,
                    paralleledRecipe.inputs.get(ItemRecipeCapability.CAP),
                    paralleledRecipe.inputs.get(FluidRecipeCapability.CAP),
                    probeItems, probeFluids)) {
                recipeList.add(recipe);
                processedRecipeList.add(paralleledRecipe);
                parallelsList.add(p);
                steamCosts.add(steamCost);

                availableSteam -= RecipeSafety.saturatedMultiply(p, steamCost);
            }
        }

        if (recipeList.isEmpty()) return null;

        BigInteger totalSteamCost = BigInteger.ZERO;
        for (int i = 0; i < recipeList.size(); i++) {
            totalSteamCost = totalSteamCost.add(
                    BigInteger.valueOf(parallelsList.getLong(i)).multiply(BigInteger.valueOf(steamCosts.getLong(i))));
        }
        gtlsupb$stageSteam(uuid, totalSteamCost);

        return new ParallelData(recipeList, parallelsList.toLongArray(), false, processedRecipeList);
    }

    @Override
    public GTRecipe buildFinalNormalRecipe(ParallelData parallelData) {
        GTRecipe output = super.buildFinalNormalRecipe(parallelData);
        if (output == null || !RecipeRunnerHelper.matchRecipeOutput(getMachine(), output)) {
            gtlsupb$discardStagedSteam();
            return null;
        }
        return output;
    }

    @Override
    public void setupRecipe(GTRecipe recipe) {
        super.setupRecipe(recipe);
        if (getStatus() == Status.WORKING) {
            gtlsupb$commitStagedSteam();
        } else {
            gtlsupb$discardStagedSteam();
        }
    }

    @Override
    protected void gtlsupb$onBatchStarted(GTRecipe recipe) {
        gtlsupb$commitStagedSteam();
    }
}
