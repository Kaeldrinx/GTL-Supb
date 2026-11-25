package com.gtlsupb.gtlsupb.api.machine.logic;

import org.gtlcore.gtlcore.api.machine.multiblock.ParallelMachine;
import org.gtlcore.gtlcore.api.machine.trait.ILockRecipe;
import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine;
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus;
import org.gtlcore.gtlcore.api.recipe.IGTRecipe;
import org.gtlcore.gtlcore.api.recipe.IParallelLogic;
import org.gtlcore.gtlcore.api.recipe.RecipeResult;
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.gtlsupb.gtlsupb.api.machine.IBatchProcessingMachine;
import com.gtlsupb.gtlsupb.api.machine.ILimitedDurationMachine;
import com.gtlsupb.gtlsupb.api.machine.IPatternAssemblyHatch;
import com.gtlsupb.gtlsupb.api.machine.IWirelessElectricMultiblockMachine;
import com.gtlsupb.gtlsupb.api.machine.trait.IWirelessNetworkEnergyHandler;
import com.gtlsupb.gtlsupb.api.recipe.IWirelessGTRecipe;
import com.gtlsupb.gtlsupb.api.recipe.WirelessGTRecipe;
import com.gtlsupb.gtlsupb.common.data.ParallelData;
import com.gtlsupb.gtlsupb.config.ConfigHolder;
import com.gtlsupb.gtlsupb.utils.RecipeCalculationHelper;
import com.gtlsupb.gtlsupb.utils.RecipeSafety;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

public class MutableRecipesLogic<T extends WorkableElectricMultiblockMachine & IRecipeLogicMachine & IWirelessElectricMultiblockMachine & ParallelMachine>
                                extends RecipeLogic implements ILockRecipe, IWirelessRecipeLogic, IRecipeStatus, IMergedRecipeCount {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MutableRecipesLogic.class, RecipeLogic.MANAGED_FIELD_HOLDER);

    @DescSynced
    private boolean useMultipleRecipes = true;

    private final double reductionRatio;
    protected final BiPredicate<GTRecipe, IRecipeLogicMachine> recipeCheck;
    protected final List<GTRecipeType> extraRecipeTypes = new java.util.ArrayList<>();

    private boolean enableModify = false;
    private double outputMultiplier = 1.0;
    private boolean enableChanceModify = false;
    private boolean lowVoltageInfiniteParallel = false;
    private int lowVoltageTier = -1;

    protected int boundCandidateCount = 1;

    private int gtlsupb$boundSlotCountCache = -1;

    private long gtlsupb$bindingUnresolvedSince = Long.MIN_VALUE;

    protected final java.util.List<BoundSlotEntry> lastBoundSlots = new java.util.ArrayList<>();

    private final ObjectArrayList<GTRecipe> gtlsupb$pendingBatchRecipes = new ObjectArrayList<>();

    private int gtlsupb$lastMergedRecipeCount;

    @Override
    public int gtlsupb$getMergedRecipeCount() {
        return gtlsupb$lastMergedRecipeCount;
    }

    protected final void gtlsupb$setMergedRecipeCount(ParallelData data) {
        gtlsupb$lastMergedRecipeCount = data == null || data.originRecipeList == null ? 0 : data.originRecipeList.size();
    }

    private boolean gtlsupb$inStatusChange;

    private static final class BoundSlotRef {

        final IPatternAssemblyHatch hatch;
        final int slot;

        BoundSlotRef(IPatternAssemblyHatch hatch, int slot) {
            this.hatch = hatch;
            this.slot = slot;
        }
    }

    private final java.util.IdentityHashMap<GTRecipe, java.util.List<BoundSlotRef>> gtlsupb$boundSlotIndex = new java.util.IdentityHashMap<>();

    private boolean gtlsupb$boundSlotIndexBuilt;

    protected static class BoundSlotEntry {

        protected final IPatternAssemblyHatch hatch;
        protected final int slot;
        protected final GTRecipe paralleled;

        protected BoundSlotEntry(IPatternAssemblyHatch hatch, int slot, GTRecipe paralleled) {
            this.hatch = hatch;
            this.slot = slot;
            this.paralleled = paralleled;
        }
    }

    public MutableRecipesLogic(T machine) {
        this(machine, null, 1.0);
    }

    public MutableRecipesLogic(T machine, double reductionRatio) {
        this(machine, null, reductionRatio);
    }

    public MutableRecipesLogic(T machine, BiPredicate<GTRecipe, IRecipeLogicMachine> recipeCheck) {
        this(machine, recipeCheck, 1.0);
    }

    public MutableRecipesLogic(T machine, BiPredicate<GTRecipe, IRecipeLogicMachine> recipeCheck, double reductionRatio) {
        super(machine);
        this.recipeCheck = recipeCheck;
        this.reductionRatio = reductionRatio;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getMachine() {
        return (T) super.getMachine();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public void setUseMultipleRecipes(boolean value) {
        if (this.useMultipleRecipes == value) return;
        this.useMultipleRecipes = value;
        this.supportedRecipeTypeCache = null;
        this.gtlsupb$boundSlotCountCache = -1;
        this.gtlsupb$boundSlotIndexBuilt = false;
        this.gtlsupb$boundSlotIndex.clear();
        this.lastBoundSlots.clear();
        this.gtlsupb$pendingBatchRecipes.clear();
        this.lastRecipe = null;
        this.lastOriginRecipe = null;
        this.recipeDirty = true;
        resetRecipeLogic();
    }

    public boolean isMultipleRecipeMode() {
        return useMultipleRecipes;
    }

    @Override
    public void findAndHandleRecipe() {
        if (!useMultipleRecipes) {
            super.findAndHandleRecipe();
            return;
        }

        lastRecipe = null;
        setRecipeStatus(null);
        GTRecipe match = getRecipe();
        if (match != null) {
            RecipeResult.of(getMachine(), RecipeResult.SUCCESS);
            gtlsupb$diagnosticReason = null;
            setupRecipe(match);
        } else {
            recipeDirty = false;
        }
    }

    private Set<GTRecipeType> supportedRecipeTypeCache;

    private static final int BINDING_UNRESOLVED_FALLBACK_TICKS = 1200;

    private String gtlsupb$lastSelectedId;

    private Component gtlsupb$diagnosticReason;

    public Component getDiagnosticReason() {
        return gtlsupb$diagnosticReason;
    }

    @Override
    public void updateTickSubscription() {
        super.updateTickSubscription();

        if (!gtlsupb$inStatusChange) {
            recipeDirty = true;
        }
        this.gtlsupb$boundSlotCountCache = -1;
        this.supportedRecipeTypeCache = null;
    }

    @Override
    public void setStatus(Status status) {
        boolean previous = gtlsupb$inStatusChange;
        gtlsupb$inStatusChange = true;
        try {
            super.setStatus(status);
        } finally {
            gtlsupb$inStatusChange = previous;
        }
    }

    protected void gtlsupb$onBatchStarted(GTRecipe recipe) {}

    protected String gtlsupb$ownerDesc() {
        var machine = getMachine();
        var level = machine.getLevel();
        return machine.getDefinition().getId() + "@" + machine.getPos().toShortString() + (level != null ? "/" + level.dimension().location() : "/?");
    }

    @Override
    public void onRecipeFinish() {
        if (!useMultipleRecipes) {
            super.onRecipeFinish();
            return;
        }
        if (lastRecipe != null && !RecipeRunnerHelper.matchRecipeOutput(getMachine(), lastRecipe)) {
            setWaiting(Component.translatable("gtlsupb.machine.recipe_stalled_output"));
            return;
        }
        getMachine().afterWorking();
        GTRecipe finished = lastRecipe;
        if (finished != null) {
            finished.postWorking(getMachine());

            RecipeRunnerHelper.handleRecipeOutput(getMachine(), finished);
        }
        lastRecipe = null;
        lastOriginRecipe = null;

        setStatus(Status.IDLE);
        progress = 0;
        duration = 0;
        findAndHandleRecipe();
    }

    @Override
    public void handleRecipeWorking() {
        if (!useMultipleRecipes) {
            super.handleRecipeWorking();
            return;
        }
        assert lastRecipe != null;
        boolean success;
        if (lastRecipe instanceof IWirelessGTRecipe wireless) {
            success = handleWirelessTickInput(wireless);
        } else {
            success = lastRecipe.checkConditions(this).isSuccess() && handleTickRecipe(lastRecipe).isSuccess();
        }
        if (success) {
            setStatus(Status.WORKING);
            if (!getMachine().onWorking()) {
                interruptRecipe();
                return;
            }
            progress++;
            totalContinuousRunningTime++;
        } else {
            RecipeResult.of(getMachine(), RecipeResult.FAIL_NO_ENOUGH_EU_IN);
            setWaiting(RecipeResult.FAIL_NO_ENOUGH_EU_IN.reason());
        }
        if (getStatus() == Status.WAITING) {
            doDamping();
        }
    }

    @Override
    public void interruptRecipe() {
        super.interruptRecipe();
        this.lastBoundSlots.clear();
    }

    @Override
    public void resetRecipeLogic() {
        super.resetRecipeLogic();

        this.gtlsupb$bindingUnresolvedSince = Long.MIN_VALUE;
        this.gtlsupb$boundSlotCountCache = -1;

        this.gtlsupb$boundSlotIndex.clear();
        this.gtlsupb$boundSlotIndexBuilt = false;
        this.lastBoundSlots.clear();
    }

    @Override
    public void markLastRecipeDirty() {
        super.markLastRecipeDirty();
        this.gtlsupb$boundSlotCountCache = -1;
    }

    protected final void gtlsupb$beginSearch() {
        gtlsupb$boundSlotCountCache = -1;

        gtlsupb$lastMergedRecipeCount = 0;

        gtlsupb$boundSlotIndex.clear();
        gtlsupb$boundSlotIndexBuilt = false;
        lastBoundSlots.clear();

        gtlsupb$pendingBatchRecipes.clear();
    }

    protected GTRecipe getRecipe() {
        gtlsupb$beginSearch();
        if (!checkBeforeWorking()) {
            gtlsupb$diagnosticReason = Component.translatable("gtlsupb.machine.diag_unavailable");
            return null;
        }

        IWirelessNetworkEnergyHandler wirelessTrait = getMachine().getWirelessNetworkEnergyHandler();
        if (wirelessTrait != null && !wirelessTrait.isOnline()) {
            gtlsupb$diagnosticReason = Component.translatable("gtlsupb.machine.diag_unavailable");
            return null;
        }

        ParallelData parallelData = calculateParallels();
        if (parallelData == null) {

            gtlsupb$diagnosticReason = Component.translatable("gtlsupb.machine.diag_no_match");
            return null;
        }

        GTRecipe result;
        if (wirelessTrait != null) {
            result = buildFinalWirelessRecipe(parallelData, wirelessTrait);
        } else {
            result = buildFinalNormalRecipe(parallelData);
        }

        if (result != null && !gtlsupb$outputsFit(result)) {
            gtlsupb$pendingBatchRecipes.clear();
            return null;
        }

        return result;
    }

    protected boolean gtlsupb$outputsFit(GTRecipe recipe) {
        if (recipe == null) return false;
        if (RecipeRunnerHelper.matchRecipeOutput(getMachine(), recipe)) return true;

        gtlsupb$lastMergedRecipeCount = 0;
        gtlsupb$diagnosticReason = Component.translatable("gtlsupb.machine.diag_output_full");
        if (getRecipeStatus() == null || getRecipeStatus().isSuccess()) {
            RecipeResult.of(getMachine(), RecipeResult.FAIL_OUTPUT);
        }
        return false;
    }

    @Override
    protected boolean handleRecipeIO(GTRecipe recipe, IO io) {
        if (io == IO.IN) {

            if (!lastBoundSlots.isEmpty()) {
                boolean consumed = consumeIndependentBoundSlotInputs();

                lastBoundSlots.clear();
                if (!consumed) {
                    gtlsupb$diagnosticReason = Component.translatable("gtlsupb.machine.diag_no_match");
                    return false;
                }
                return true;
            }

            if (!gtlsupb$pendingBatchRecipes.isEmpty()) {
                boolean ok = true;
                for (int i = 0; i < gtlsupb$pendingBatchRecipes.size(); i++) {
                    if (!RecipeRunnerHelper.handleRecipeInput(getMachine(), gtlsupb$pendingBatchRecipes.get(i))) {
                        com.gtlsupb.gtlsupb.GTLSupb.LOGGER.error(
                                "[配方合并] 扣料失败，已扣部分无法回滚：{}（机器 {}）",
                                gtlsupb$pendingBatchRecipes.get(i), gtlsupb$ownerDesc());
                        ok = false;
                        break;
                    }
                }
                gtlsupb$pendingBatchRecipes.clear();
                if (!ok) {
                    gtlsupb$diagnosticReason = Component.translatable("gtlsupb.machine.diag_no_match");
                    return false;
                }

                return true;
            }
        }
        return super.handleRecipeIO(recipe, io);
    }

    private static final java.util.Map<Class<?>, java.lang.reflect.Method> LONG_PARALLEL_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private static final java.lang.reflect.Method NO_LONG_PARALLEL = gtlsupb$noLongParallelSentinel();

    private static java.lang.reflect.Method gtlsupb$noLongParallelSentinel() {
        try {
            return Object.class.getMethod("toString");
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    protected final long gtlsupb$machineParallelLong() {
        IRecipeLogicMachine machine = getMachine();
        try {
            Class<?> type = machine.getClass();
            java.lang.reflect.Method method = LONG_PARALLEL_CACHE.get(type);
            if (method == null) {
                java.lang.reflect.Method found;
                try {
                    found = type.getMethod("getMaxParallelLong");
                } catch (Throwable ignored) {
                    found = NO_LONG_PARALLEL;
                }
                LONG_PARALLEL_CACHE.put(type, found);
                method = found;
            }
            if (method != NO_LONG_PARALLEL) {
                Object value = method.invoke(machine);
                if (value instanceof Long longValue) {
                    return Math.max(longValue, 1L);
                }
            }
        } catch (Throwable ignored) {}

        if (machine instanceof ParallelMachine parallelMachine) {
            return Math.max((long) parallelMachine.getMaxParallel(), 1L);
        }
        return 1L;
    }

    protected long gtlsupb$parallelBudget() {
        return gtlsupb$machineParallelLong();
    }

    private static String gtlsupb$idOf(GTRecipe recipe) {
        return recipe == null || recipe.getId() == null ? "" : recipe.getId().toString();
    }

    protected final List<GTRecipe> gtlsupb$orderedCandidates(java.util.Collection<GTRecipe> candidates) {
        List<GTRecipe> ordered = new java.util.ArrayList<>(candidates == null ? Collections.emptyList() : candidates);
        if (ordered.isEmpty()) return ordered;
        ordered.sort(java.util.Comparator.comparing(MutableRecipesLogic::gtlsupb$idOf));
        if (gtlsupb$lastSelectedId == null) return ordered;
        int start = 0;
        while (start < ordered.size()) {
            if (gtlsupb$idOf(ordered.get(start)).compareTo(gtlsupb$lastSelectedId) > 0) break;
            start++;
        }
        if (start >= ordered.size()) start = 0;
        if (start <= 0) return ordered;
        List<GTRecipe> rotated = new java.util.ArrayList<>(ordered.size());
        rotated.addAll(ordered.subList(start, ordered.size()));
        rotated.addAll(ordered.subList(0, start));
        return rotated;
    }

    protected final List<GTRecipe> gtlsupb$batchCandidates(java.util.Collection<GTRecipe> candidates) {
        return gtlsupb$orderedCandidates(candidates);
    }

    protected ParallelData calculateParallels() {
        Set<GTRecipe> recipes = lookupRecipeSet();
        if (recipes.isEmpty()) return null;
        List<GTRecipe> ordered = gtlsupb$batchCandidates(recipes);
        if (ordered.isEmpty()) return null;

        if (isIndependentSlotMode()) {
            return calculateIndependentBoundParallels();
        }

        if (!isIndependentSlotMode()) {
            ParallelData pooled = calculatePooledParallels(ordered);
            if (pooled != null) return pooled;
        }
        return gtlsupb$collectBatchParallels(ordered);
    }

    protected ParallelData calculatePooledParallels(List<GTRecipe> ordered) {
        long totalParallel = gtlsupb$parallelBudget();

        if (ordered.size() == 1) {
            return RecipeCalculationHelper.calculateParallelsWithGreedyAllocation(
                    Collections.singletonList(ordered.get(0)),
                    totalParallel,
                    getMachine(),
                    (recipe, remaining) -> calculateParallel(getMachine(), recipe, remaining));
        }

        RecipeCalculationHelper.GetParallelAndConsumptionFunction getParallelAndConsumption = (recipe, remaining) -> calculateParallel(getMachine(), recipe, remaining);

        ParallelData base = RecipeCalculationHelper.calculateParallelsWithFairAllocation(
                ordered,
                totalParallel,
                getMachine(),
                getParallelAndConsumption);
        if (base != null && !base.originRecipeList.isEmpty()) {
            gtlsupb$lastSelectedId = gtlsupb$idOf(base.originRecipeList.get(0));
        }

        return base;
    }

    protected GTRecipe gtlsupb$transformCandidate(GTRecipe recipe) {
        return recipe;
    }

    protected GTRecipe gtlsupb$scaleCandidate(GTRecipe candidate, long parallel) {
        return RecipeCalculationHelper.multipleRecipe(candidate, parallel);
    }

    protected final ParallelData gtlsupb$collectBatchParallels(List<GTRecipe> ordered) {
        long perRecipeCap = gtlsupb$machineParallelLong();
        ObjectArrayList<GTRecipe> recipeList = new ObjectArrayList<>();
        ObjectArrayList<GTRecipe> processedRecipeList = new ObjectArrayList<>();
        LongArrayList parallelsList = new LongArrayList();

        ObjectArrayList<Content> probeItems = new ObjectArrayList<>();
        ObjectArrayList<Content> probeFluids = new ObjectArrayList<>();
        int total = ordered.size();
        for (int i = 0; i < total; i++) {
            GTRecipe match = ordered.get(i);

            GTRecipe candidate = gtlsupb$transformCandidate(match);
            if (candidate == null) continue;
            if (!gtlsupb$acceptsRecipe(candidate)) continue;
            long pMax = calculateParallel(getMachine(), candidate, perRecipeCap).firstLong();
            if (pMax <= 0) pMax = 1L;

            long remaining = Math.max(0, total - 1 - i);
            long reserve = Math.min(remaining, pMax / 4);
            long p = Math.max(1L, pMax - reserve);
            GTRecipe paralleled = IParallelLogic.getRecipeOutputChance(
                    getMachine(), gtlsupb$scaleCandidate(candidate, p));

            if (requiresOutputs() && RecipeHelper.getOutputItems(paralleled).isEmpty() &&
                    RecipeHelper.getOutputFluids(paralleled).isEmpty() &&
                    RecipeHelper.getOutputEUt(paralleled) <= 0L) {
                continue;
            }
            if (!RecipeCalculationHelper.accumulateInputsIfFit(getMachine(),
                    paralleled.inputs.get(ItemRecipeCapability.CAP),
                    paralleled.inputs.get(FluidRecipeCapability.CAP),
                    probeItems, probeFluids)) {
                if (p <= 1L) continue;

                p = 1L;
                paralleled = IParallelLogic.getRecipeOutputChance(
                        getMachine(), gtlsupb$scaleCandidate(candidate, p));
                if (!RecipeCalculationHelper.accumulateInputsIfFit(getMachine(),
                        paralleled.inputs.get(ItemRecipeCapability.CAP),
                        paralleled.inputs.get(FluidRecipeCapability.CAP),
                        probeItems, probeFluids)) {
                    continue;
                }
            }

            recipeList.add(candidate);
            processedRecipeList.add(paralleled);
            parallelsList.add(p);
            gtlsupb$pendingBatchRecipes.add(paralleled);
        }
        if (recipeList.isEmpty()) return null;
        gtlsupb$lastSelectedId = gtlsupb$idOf(recipeList.get(0));
        return new ParallelData(recipeList, parallelsList.toLongArray(), false, processedRecipeList);
    }

    protected boolean requiresOutputs() {
        return true;
    }

    protected boolean gtlsupb$acceptsRecipe(GTRecipe recipe) {
        return true;
    }

    protected final boolean gtlsupb$hasPendingBatchRecipes() {
        return !gtlsupb$pendingBatchRecipes.isEmpty();
    }

    private void gtlsupb$prunePendingBatchRecipes(int[] acceptedIndices) {
        if (gtlsupb$pendingBatchRecipes.isEmpty() || acceptedIndices == null) return;
        if (acceptedIndices.length == gtlsupb$pendingBatchRecipes.size()) return;
        ObjectArrayList<GTRecipe> kept = new ObjectArrayList<>(Math.max(1, acceptedIndices.length));
        for (int idx : acceptedIndices) {
            if (idx >= 0 && idx < gtlsupb$pendingBatchRecipes.size()) {
                kept.add(gtlsupb$pendingBatchRecipes.get(idx));
            }
        }
        gtlsupb$pendingBatchRecipes.clear();
        gtlsupb$pendingBatchRecipes.addAll(kept);
    }

    protected static long safeMultiplyLong(long a, int b) {
        return RecipeSafety.saturatedMultiply(a, (long) b);
    }

    protected GTRecipe buildFinalNormalRecipe(ParallelData parallelData) {
        long maxEUt = getMachine().getOverclockVoltage();
        if (maxEUt <= 0) return null;
        boolean independent = isIndependentSlotMode();

        if (independent) {

            parallelData = calculateIndependentBoundParallels();
            if (parallelData == null) return null;
        }

        boolean skipInputs = independent || !gtlsupb$pendingBatchRecipes.isEmpty();

        double safeMaxDuration = 20.0 * 500.0;
        RecipeCalculationHelper.ProcessParallelDataNormalResult result = RecipeCalculationHelper.processParallelDataNormal(
                parallelData,
                getMachine(),
                maxEUt,
                getEuMultiplier(),
                new RecipeCalculationHelper.GetTotalRecipeEuFunction() {

                    @Override
                    public BigInteger apply(GTRecipe recipe) {
                        return BigInteger.valueOf(getRecipeEut(recipe)).multiply(BigInteger.valueOf(recipe.duration));
                    }
                },
                new RecipeCalculationHelper.ShouldBreakFunction() {

                    @Override
                    public boolean apply(BigInteger totalEu) {
                        return gtlsupb$limitTotalEu() &&
                                totalEu.divide(BigInteger.valueOf(Math.max(1L, maxEUt)))
                                        .compareTo(BigInteger.valueOf((long) safeMaxDuration)) > 0;
                    }
                },
                skipInputs);
        ObjectArrayList<Content> itemOutputs = result.itemOutputs;
        ObjectArrayList<Content> fluidOutputs = result.fluidOutputs;
        BigInteger totalEu = result.totalEu;

        gtlsupb$prunePendingBatchRecipes(result.acceptedIndices);

        if (!RecipeCalculationHelper.hasOutputs(itemOutputs, fluidOutputs)) {
            if (getRecipeStatus() == null || getRecipeStatus().isSuccess()) {
                RecipeResult.of(getMachine(), RecipeResult.FAIL_FIND);
            }
            gtlsupb$diagnosticReason = Component.translatable("gtlsupb.machine.diag_no_output");
            return null;
        }

        int minDuration = RecipeSafety.clamp(
                getMachine() instanceof ILimitedDurationMachine limited ? limited.getLimitedDuration() : ILimitedDurationMachine.MIN_DURATION,
                ILimitedDurationMachine.MIN_DURATION, ILimitedDurationMachine.MAX_DURATION);

        int batch = getBatchMultiplier();

        gtlsupb$setMergedRecipeCount(parallelData);

        return RecipeCalculationHelper.buildNormalRecipe(
                getMachine(),
                independent ? new ObjectArrayList<>() : result.itemInputs,
                independent ? new ObjectArrayList<>() : result.fluidInputs,
                itemOutputs, fluidOutputs, totalEu, maxEUt, minDuration, independent ? 1 : batch);
    }

    protected boolean gtlsupb$limitTotalEu() {
        return true;
    }

    protected WirelessGTRecipe buildFinalWirelessRecipe(
                                                        ParallelData parallelData,
                                                        IWirelessNetworkEnergyHandler wirelessTrait) {
        if (!wirelessTrait.isOnline()) return null;
        boolean independent = isIndependentSlotMode();

        if (independent) {

            parallelData = calculateIndependentBoundParallels();
            if (parallelData == null) return null;
        }

        boolean skipInputs = independent || !gtlsupb$pendingBatchRecipes.isEmpty();
        RecipeCalculationHelper.ProcessParallelDataWirelessResult result = RecipeCalculationHelper.processParallelDataWireless(
                parallelData,
                getMachine(),
                wirelessTrait.getMaxAvailableEnergy(),
                getEuMultiplier(),
                this::getRecipeEut,
                true,
                skipInputs);
        ObjectArrayList<Content> itemOutputs = result.itemOutputs;
        ObjectArrayList<Content> fluidOutputs = result.fluidOutputs;
        BigInteger totalEu = result.totalEu;

        gtlsupb$prunePendingBatchRecipes(result.acceptedIndices);

        if (!RecipeCalculationHelper.hasOutputs(itemOutputs, fluidOutputs)) {
            if (getRecipeStatus() == null || getRecipeStatus().isSuccess()) {
                RecipeResult.of(getMachine(), RecipeResult.FAIL_FIND);
            }
            gtlsupb$diagnosticReason = Component.translatable("gtlsupb.machine.diag_no_output");
            return null;
        }

        int minDuration = RecipeSafety.clamp(
                getMachine() instanceof ILimitedDurationMachine limited ? limited.getLimitedDuration() : ILimitedDurationMachine.MIN_DURATION,
                ILimitedDurationMachine.MIN_DURATION, ILimitedDurationMachine.MAX_DURATION);

        int batch = getBatchMultiplier();
        gtlsupb$setMergedRecipeCount(parallelData);
        if (skipInputs) {
            WirelessGTRecipe wireless = RecipeCalculationHelper.buildWirelessRecipe(
                    new ObjectArrayList<>(), new ObjectArrayList<>(), itemOutputs, fluidOutputs, minDuration, totalEu);
            if (independent && batch > 1) {
                wireless.duration = (int) Math.min((double) Math.max(1, minDuration) * batch, Integer.MAX_VALUE);
            }
            return wireless;
        }
        return RecipeCalculationHelper.buildWirelessRecipe(
                result.itemInputs, result.fluidInputs, itemOutputs, fluidOutputs, minDuration, totalEu);
    }

    protected boolean isSlotIndependentSupported() {
        return true;
    }

    protected final boolean isIndependentSlotMode() {
        if (!isSlotIndependentSupported()) {
            int slots = countBoundSlots();
            if (slots >= 2) {
                gtlsupb$logBindingIssue("noindependent:" + slots,
                        "[样板绑定] 该机器不支持逐槽独立供线：{} 个绑定槽位将共用输入池，可能出现样板间抢料（请反馈该机器类型）",
                        slots);
            }
            return false;
        }
        return countBoundSlots() >= 2;
    }

    protected final int countBoundSlots() {
        if (gtlsupb$boundSlotCountCache >= 0) return gtlsupb$boundSlotCountCache;

        int total = 0;
        try {
            gtlsupb$ensureBoundSlotIndex();
            for (var refs : gtlsupb$boundSlotIndex.values()) {
                total += refs.size();
            }
        } catch (Throwable ignored) {

        }
        gtlsupb$boundSlotCountCache = total;
        return total;
    }

    private final Set<String> gtlsupb$loggedBindingIssues = new ObjectOpenHashSet<>();

    private static String gtlsupb$recipeKey(GTRecipe recipe) {
        if (recipe == null) return "null";
        ResourceLocation id = recipe.getId();
        if (id != null) return id.toString();
        GTRecipeType type = recipe.recipeType;
        return "type:" + (type == null || type.registryName == null ? "?" : type.registryName);
    }

    private void gtlsupb$logBindingIssue(String key, String message, Object... args) {
        try {
            if (gtlsupb$loggedBindingIssues.size() > 64) gtlsupb$loggedBindingIssues.clear();
            if (key != null && !gtlsupb$loggedBindingIssues.add(key)) return;
            com.gtlsupb.gtlsupb.GTLSupb.LOGGER.warn(message, args);
        } catch (Throwable ignored) {}
    }

    protected ParallelData calculateIndependentBoundParallels() {
        lastBoundSlots.clear();
        ObjectArrayList<GTRecipe> recipeList = new ObjectArrayList<>();
        ObjectArrayList<GTRecipe> processedRecipeList = new ObjectArrayList<>();
        LongArrayList parallelsList = new LongArrayList();

        long machineMax = safeMultiplyLong(getIndependentSlotParallelCap(), Math.max(1, getBatchMultiplier()));
        for (var part : getMachine().getParts()) {
            if (!(part instanceof IPatternAssemblyHatch hatch)) continue;
            Int2ObjectMap<GTRecipe> slotMap = hatch.getSlotBoundRecipes();
            if (slotMap.isEmpty()) continue;
            for (var entry : Int2ObjectMaps.fastIterable(slotMap)) {
                int slot = entry.getIntKey();
                GTRecipe recipe = entry.getValue();
                if (recipe == null) continue;

                if (!isSupportedBoundRecipeType(recipe.recipeType) || !checkRecipe(recipe)) continue;
                long p = hatch.getSlotBoundRecipeParallel(slot, recipe, machineMax);
                if (p <= 0) continue;

                p = IParallelLogic.getMinParallel(getMachine(), recipe, p);
                if (p <= 0) continue;
                GTRecipe paralleled = IParallelLogic.getRecipeOutputChance(
                        getMachine(), RecipeCalculationHelper.multipleRecipe(recipe, p));

                Int2ObjectOpenHashMap<GTRecipe> single = new Int2ObjectOpenHashMap<>();
                single.put(slot, paralleled);
                if (!hatch.verifySlotBoundRecipes(single)) {
                    gtlsupb$logBindingIssue("slotverify:" + slot + ":" + gtlsupb$recipeKey(recipe),
                            "[样板绑定] 槽位 {} 的绑定配方 {} 预校验未通过（并行 {}），本批次跳过该槽位、其余槽位照常运行",
                            slot, recipe, p);
                    continue;
                }
                recipeList.add(recipe);
                processedRecipeList.add(paralleled);
                parallelsList.add(p);
                lastBoundSlots.add(new BoundSlotEntry(hatch, slot, paralleled));
            }
        }
        if (recipeList.isEmpty()) return null;
        return new ParallelData(recipeList, parallelsList.toLongArray(), false, processedRecipeList);
    }

    protected long getIndependentSlotParallelCap() {
        return gtlsupb$machineParallelLong();
    }

    protected boolean consumeIndependentBoundSlotInputs() {
        if (lastBoundSlots.isEmpty()) return true;
        Object2ObjectOpenHashMap<IPatternAssemblyHatch, Int2ObjectOpenHashMap<GTRecipe>> byPart = new Object2ObjectOpenHashMap<>();
        for (BoundSlotEntry b : lastBoundSlots) {
            Int2ObjectOpenHashMap<GTRecipe> slotMap = byPart.computeIfAbsent(b.hatch, k -> new Int2ObjectOpenHashMap<>());
            slotMap.put(b.slot, b.paralleled);
        }
        for (var entry : Object2ObjectMaps.fastIterable(byPart)) {
            if (!entry.getKey().verifySlotBoundRecipes(entry.getValue())) return false;
        }

        for (var entry : Object2ObjectMaps.fastIterable(byPart)) {
            if (!entry.getKey().commitSlotBoundRecipes(entry.getValue())) {
                com.gtlsupb.gtlsupb.GTLSupb.LOGGER.error(
                        "[样板绑定] 槽位提交失败，已提交的仓室无法回滚，可能出现部分消耗：{}（机器 {}）",
                        entry.getKey(), getMachine().getDefinition().getId());
                return false;
            }
        }
        return true;
    }

    protected boolean checkBeforeWorking() {
        if (!getMachine().hasProxies()) return false;
        if (getMachine().getOverclockVoltage() <= 0) return false;
        return true;
    }

    protected LongLongPair calculateParallel(
                                             IRecipeLogicMachine machine,
                                             GTRecipe match,
                                             long remain) {
        if (lowVoltageInfiniteParallel && lowVoltageTier >= 0 &&
                IGTRecipe.of(match).getEuTier() <= lowVoltageTier) {
            long full = IParallelLogic.getMaxParallel(machine, match, Long.MAX_VALUE);
            return LongLongPair.of(Math.max(gtlsupb$applyBoundSlotCap(match, full), 0L), 0L);
        }

        long p = IParallelLogic.getMaxParallel(machine, match, remain);
        p = gtlsupb$applyBoundSlotCap(match, p);
        return LongLongPair.of(p, p);
    }

    private long gtlsupb$applyBoundSlotCap(GTRecipe match, long p) {
        long slotCap = gtlsupb$boundSlotStockParallel(match, p);
        if (slotCap < 0L) return p;
        int boundSlots = countBoundSlots();
        if (boundSlots < 2) {
            gtlsupb$logBindingIssue("cap-skipped:" + gtlsupb$recipeKey(match),
                    "[样板绑定] 绑定配方 {} 未按各槽存量封顶：识别到的绑定槽位数={}（<2）。若机器确有多条绑定样板，请反馈此日志",
                    match, boundSlots);
            return p;
        }
        return Math.min(p, slotCap);
    }

    protected long gtlsupb$boundSlotStockParallel(GTRecipe recipe, long upperBound) {
        if (recipe == null) return -1L;
        gtlsupb$ensureBoundSlotIndex();
        java.util.List<BoundSlotRef> refs = gtlsupb$boundSlotIndex.get(recipe);
        if (refs == null || refs.isEmpty()) return -1L;
        long cap = Math.max(1L, Math.min(upperBound, Long.MAX_VALUE / 4L));
        long total = 0L;
        for (int i = 0; i < refs.size(); i++) {
            BoundSlotRef ref = refs.get(i);
            long p;
            try {
                p = ref.hatch.getSlotBoundRecipeParallel(ref.slot, recipe, cap);
            } catch (Throwable ignored) {
                return -1L;
            }
            if (p <= 0L) continue;
            try {
                total = Math.addExact(total, p);
            } catch (ArithmeticException overflow) {
                return Long.MAX_VALUE;
            }
        }
        return total;
    }

    private void gtlsupb$ensureBoundSlotIndex() {
        if (gtlsupb$boundSlotIndexBuilt) return;
        gtlsupb$boundSlotIndexBuilt = true;
        try {
            for (var part : getMachine().getParts()) {
                if (!(part instanceof IPatternAssemblyHatch hatch)) continue;
                Int2ObjectMap<GTRecipe> slotMap = hatch.getSlotBoundRecipes();
                if (slotMap.isEmpty()) continue;
                for (var entry : Int2ObjectMaps.fastIterable(slotMap)) {
                    GTRecipe recipe = entry.getValue();
                    if (recipe == null) continue;
                    gtlsupb$boundSlotIndex.computeIfAbsent(recipe, key -> new java.util.ArrayList<>(2))
                            .add(new BoundSlotRef(hatch, entry.getIntKey()));
                }
            }
        } catch (Throwable ignored) {

        }
    }

    protected int getBatchMultiplier() {
        if (getMachine() instanceof IBatchProcessingMachine batch && batch.isBatchEnabled()) {
            int n = Math.max(1, batch.getBatchSize());
            int baseDuration = Math.max(1, getMachine() instanceof ILimitedDurationMachine limited ? limited.getLimitedDuration() : 1);
            int batchDuration = ConfigHolder.INSTANCE != null ? ConfigHolder.INSTANCE.batchDuration : 100;
            if (baseDuration >= batchDuration) {
                return 1;
            }
            return Math.max(1, Math.min(n, batchDuration / baseDuration));
        }
        return 1;
    }

    protected double getEuMultiplier() {
        if (!(getMachine() instanceof IRecipeCapabilityMachine capabilityMachine)) {
            return this.reductionRatio;
        }
        IMaintenanceMachine maintenanceMachine = capabilityMachine.getMaintenanceMachine();
        if (maintenanceMachine != null) {
            return maintenanceMachine.getDurationMultiplier() * this.reductionRatio;
        }
        return this.reductionRatio;
    }

    protected long getRecipeEut(GTRecipe recipe) {
        return RecipeHelper.getInputEUt(recipe);
    }

    protected List<GTRecipeType> getSearchRecipeTypes() {
        return Collections.singletonList(getMachine().getRecipeType());
    }

    protected boolean isSupportedBoundRecipeType(GTRecipeType type) {
        if (type == null) return false;
        if (supportedRecipeTypeCache == null) {
            Set<GTRecipeType> set = new ObjectOpenHashSet<>();
            for (GTRecipeType supported : getMachine().getRecipeTypes()) {
                set.add(supported);
            }
            supportedRecipeTypeCache = set;
        }
        return supportedRecipeTypeCache.contains(type);
    }

    protected Set<GTRecipe> lookupRecipeSet() {
        boundCandidateCount = 1;
        List<GTRecipeType> searchTypes = getSearchRecipeTypes();
        if (isLock()) {
            GTRecipe locked = getLockRecipe();
            if (locked != null && checkRecipe(locked)) {
                return Collections.singleton(locked);
            }
            setLockRecipe(null);
            for (GTRecipeType type : searchTypes) {
                setLockRecipe(type.getLookup().find(getMachine(), this::checkRecipe));
                if (getLockRecipe() != null) break;
            }
            if (getLockRecipe() == null && !extraRecipeTypes.isEmpty()) {
                for (GTRecipeType extraType : extraRecipeTypes) {
                    setLockRecipe(extraType.getLookup().find(getMachine(), this::checkRecipe));
                    if (getLockRecipe() != null) break;
                }
            }
            locked = getLockRecipe();
            return locked != null ? Collections.singleton(locked) : Collections.emptySet();
        } else {

            Set<GTRecipe> bound = collectPatternBoundRecipes();
            if (bound.isEmpty()) {

                gtlsupb$bindingUnresolvedSince = Long.MIN_VALUE;

                List<GTRecipeType> declaredTypes = collectBoundRecipeTypes();
                if (!declaredTypes.isEmpty()) {
                    searchTypes = declaredTypes;
                    gtlsupb$logBindingIssue("narrowType:" + declaredTypes.size(),
                            "[样板绑定] 样板仅声明类型、无精确绑定，搜索范围已收窄到 {} 类（机器支持的模式内）",
                            declaredTypes.size());
                }
            }
            if (!bound.isEmpty()) {

                ObjectOpenHashSet<GTRecipe> boundResult = new ObjectOpenHashSet<>();
                boolean anyTypeValid = false;

                int boundSlotCount = countBoundSlots();
                boolean independentSlotMode = isIndependentSlotMode();
                for (GTRecipe recipe : bound) {

                    if (!isSupportedBoundRecipeType(recipe.recipeType)) {
                        gtlsupb$logBindingIssue("type:" + recipe.recipeType + ":" + gtlsupb$recipeKey(recipe),
                                "[样板绑定] 拒绝一条绑定：配方 {} 的配方类型 {} 不在机器支持的模式列表内（避免跨类型串跑）",
                                recipe, recipe.recipeType);
                        continue;
                    }
                    anyTypeValid = true;

                    if (!independentSlotMode && boundSlotCount >= 2 && gtlsupb$boundSlotStockParallel(recipe, 1L) <= 0L) {
                        gtlsupb$logBindingIssue("nostock:" + gtlsupb$recipeKey(recipe),
                                "[样板绑定] 暂不运行绑定配方 {}：其绑定槽位本槽尚无存料（各槽各料，等待该槽自己的料到位）",
                                recipe);
                        continue;
                    }

                    boolean inputReady = checkRecipe(recipe);
                    if (inputReady) {
                        boundResult.add(recipe);
                    } else {

                        gtlsupb$logBindingIssue("fail:" + gtlsupb$recipeKey(recipe),
                                "[样板绑定] 绑定配方 {} 暂不可执行：{}",
                                recipe, inputReady ? "输入已匹配、被机器附加校验拒绝（电压等级/条件类）" : "输入未匹配（等待该配方供料）");
                    }
                }
                boundCandidateCount = boundResult.size();
                if (!boundResult.isEmpty()) {
                    gtlsupb$bindingUnresolvedSince = Long.MIN_VALUE;
                    return boundResult;
                }
                if (anyTypeValid) {

                    Set<GTRecipe> fallback = gtlsupb$tryBoundTypeFallback();
                    if (fallback != null) return fallback;
                    gtlsupb$logBindingIssue("empty:" + bound.size(),
                            "[样板绑定] 本周期 {} 条绑定配方均不满足执行条件，暂不运行任何配方（不回退全池，避免样板间抢料）",
                            bound.size());
                    return boundResult;
                }

                gtlsupb$logBindingIssue("allRejected:" + bound.size(),
                        "[样板绑定] {} 条绑定全部被类型校验拒绝（疑似误绑），本次回退按当前模式输入匹配执行",
                        bound.size());
            }

            ObjectOpenHashSet<GTRecipe> recipes = new ObjectOpenHashSet<>();
            gtlsupb$collectRecipes(searchTypes, recipes);
            gtlsupb$collectRecipes(extraRecipeTypes, recipes);
            return recipes;
        }
    }

    private void gtlsupb$collectRecipes(List<GTRecipeType> types, Set<GTRecipe> out) {
        if (types == null || types.isEmpty()) return;
        for (GTRecipeType type : types) {
            if (type == null) continue;
            Iterator<GTRecipe> it = RecipeCalculationHelper.enableDiveMode(
                    type.getLookup().getRecipeIterator(getMachine(), this::checkRecipe));
            while (it.hasNext()) {
                out.add(it.next());
            }
        }
    }

    private List<GTRecipeType> collectBoundRecipeTypes() {
        List<GTRecipeType> types = new java.util.ArrayList<>();
        for (var part : getMachine().getParts()) {
            if (part instanceof IPatternAssemblyHatch hatch) {
                for (GTRecipeType type : hatch.getBoundRecipeTypes()) {
                    if (type != null && isSupportedBoundRecipeType(type) && !types.contains(type)) {
                        types.add(type);
                    }
                }
            }
        }
        return types;
    }

    private Set<GTRecipe> gtlsupb$tryBoundTypeFallback() {
        long now = getMachine().getOffsetTimer();
        if (gtlsupb$bindingUnresolvedSince == Long.MIN_VALUE) {
            gtlsupb$bindingUnresolvedSince = now;
            return null;
        }
        if (now - gtlsupb$bindingUnresolvedSince < BINDING_UNRESOLVED_FALLBACK_TICKS) return null;
        List<GTRecipeType> types = collectBoundRecipeTypes();
        if (types.isEmpty()) return null;
        ObjectOpenHashSet<GTRecipe> out = new ObjectOpenHashSet<>();
        gtlsupb$collectRecipes(types, out);
        if (out.isEmpty()) return null;
        boundCandidateCount = out.size();
        gtlsupb$bindingUnresolvedSince = Long.MIN_VALUE;
        gtlsupb$logBindingIssue("fallbackType:" + types.size(),
                "[样板绑定] 绑定配方持续 {} tick 不可执行，已降级为类型级匹配（{} 类），避免整机停摆",
                BINDING_UNRESOLVED_FALLBACK_TICKS, types.size());
        return out;
    }

    protected Set<GTRecipe> collectPatternBoundRecipes() {
        T machine = getMachine();
        if (machine == null || !machine.isFormed()) return new ObjectOpenHashSet<>();

        gtlsupb$ensureBoundSlotIndex();
        if (gtlsupb$boundSlotIndex.isEmpty()) return new ObjectOpenHashSet<>();
        return new ObjectOpenHashSet<>(gtlsupb$boundSlotIndex.keySet());
    }

    protected boolean checkRecipe(GTRecipe recipe) {
        return RecipeRunnerHelper.matchRecipeInput(getMachine(), recipe) &&
                (recipeCheck == null || recipeCheck.test(recipe, getMachine()));
    }

    @Override
    public IWirelessElectricMultiblockMachine getWirelessMachine() {
        return (IWirelessElectricMultiblockMachine) getMachine();
    }

    public boolean isEnableModify() {
        return enableModify;
    }

    public void setEnableModify(boolean enableModify) {
        this.enableModify = enableModify;
    }

    public double getOutputMultiplier() {
        return outputMultiplier;
    }

    public void setOutputMultiplier(double outputMultiplier) {
        this.outputMultiplier = outputMultiplier;
    }

    public boolean isEnableChanceModify() {
        return enableChanceModify;
    }

    public void setEnableChanceModify(boolean enableChanceModify) {
        this.enableChanceModify = enableChanceModify;
    }

    public void setLowVoltageInfiniteParallel(boolean lowVoltageInfiniteParallel) {
        this.lowVoltageInfiniteParallel = lowVoltageInfiniteParallel;
    }

    public int getLowVoltageTier() {
        return lowVoltageTier;
    }

    public void setLowVoltageTier(int lowVoltageTier) {
        this.lowVoltageTier = lowVoltageTier;
    }

    protected BiPredicate<GTRecipe, IRecipeLogicMachine> getRecipeCheck() {
        return recipeCheck;
    }

    protected List<GTRecipeType> getExtraRecipeTypes() {
        return extraRecipeTypes;
    }
}
