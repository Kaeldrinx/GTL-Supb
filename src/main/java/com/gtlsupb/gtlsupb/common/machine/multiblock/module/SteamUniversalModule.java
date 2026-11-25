package com.gtlsupb.gtlsupb.common.machine.multiblock.module;

import org.gtlcore.gtlcore.api.machine.trait.ILockRecipe;
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus;
import org.gtlcore.gtlcore.api.recipe.IGTRecipe;
import org.gtlcore.gtlcore.api.recipe.RecipeResult;
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.gtlsupb.gtlsupb.api.machine.ILimitedDurationMachine;
import com.gtlsupb.gtlsupb.api.machine.IPatternAssemblyHatch;
import com.gtlsupb.gtlsupb.api.machine.SteamEnergyManager;
import com.gtlsupb.gtlsupb.common.data.ParallelData;
import com.gtlsupb.gtlsupb.common.machine.multiblock.ZeroEnergyMultiTypeLogic;
import com.gtlsupb.gtlsupb.common.machine.multiblock.ZeroEnergyMultiTypeMachine;
import com.gtlsupb.gtlsupb.utils.NumberFormatUtil;
import com.gtlsupb.gtlsupb.utils.RainbowText;
import com.gtlsupb.gtlsupb.utils.RecipeCalculationHelper;
import com.hepdd.gtmthings.utils.TeamUtil;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SteamUniversalModule extends ZeroEnergyMultiTypeMachine
                                  implements IInteractedMachine, ILimitedDurationMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamUniversalModule.class,
            ZeroEnergyMultiTypeMachine.MANAGED_FIELD_HOLDER);

    public static final long STEAM_CONVERSION_RATIO = 1L;

    public static final long PARALLEL_PER_CONNECTION = 16384L;

    @Persisted
    public UUID uuid;

    @Persisted
    public int gtlsupb$limitedDuration = ILimitedDurationMachine.DEFAULT_DURATION;

    @Override
    public int getLimitedDuration() {
        return Math.max(ILimitedDurationMachine.MIN_DURATION,
                Math.min(ILimitedDurationMachine.MAX_DURATION, gtlsupb$limitedDuration));
    }

    @Override
    public void setLimitedDuration(int duration) {
        gtlsupb$limitedDuration = Math.max(ILimitedDurationMachine.MIN_DURATION,
                Math.min(ILimitedDurationMachine.MAX_DURATION, duration));
        markDirty();
    }

    public SteamUniversalModule(IMachineBlockEntity holder) {
        super(holder,
                GTRecipeTypes.ORE_WASHER_RECIPES,
                GTRecipeTypes.CHEMICAL_BATH_RECIPES,
                GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES,
                GTRecipeTypes.CENTRIFUGE_RECIPES,
                GTRecipeTypes.MIXER_RECIPES,
                GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                GTRecipeTypes.MACERATOR_RECIPES,
                GTRecipeTypes.ALLOY_SMELTER_RECIPES,
                GTRecipeTypes.COMPRESSOR_RECIPES,
                GTRecipeTypes.FORGE_HAMMER_RECIPES,
                GTRecipeTypes.EXTRACTOR_RECIPES);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new SteamModuleLogic(this);
    }

    @Override
    public boolean shouldShowMachineModeSwitcher() {
        return true;
    }

    public BigInteger getWirelessSteamEnergy() {
        if (uuid == null) return BigInteger.ZERO;
        return SteamEnergyManager.getUserSteam(uuid);
    }

    @Override
    public int getMaxParallel() {
        long parallel = maxParallel();
        return parallel > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) parallel;
    }

    @Override
    public long maxParallel() {
        long base = PARALLEL_PER_CONNECTION;
        if (uuid != null) {
            long connectionCount = SteamEnergyManager.getFactoryConnectionCount(uuid);
            if (connectionCount > 0) {
                base = gtlsupb$saturatedMultiply(connectionCount, PARALLEL_PER_CONNECTION);
            }
        }
        return base;
    }

    static long gtlsupb$saturatedMultiply(long a, long b) {
        return com.gtlsupb.gtlsupb.utils.RecipeSafety.saturatedMultiply(a, b);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (uuid == null && !isRemote()) {
            tryAutoBind();
        }

        if (!isRemote()) {
            if (getLevel() instanceof ServerLevel serverLevel) {
                SteamEnergyManager.getSavedData(serverLevel);
            }

            if (uuid != null) {
                SteamEnergyManager.registerFactoryConnection(uuid, getPos());
            }
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();

        if (!isRemote() && uuid != null) {
            SteamEnergyManager.unregisterFactoryConnection(uuid, getPos());
        }
    }

    private void tryAutoBind() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Player player : serverLevel.players()) {
            double dist = player.distanceToSqr(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
            if (dist < minDist) {
                minDist = dist;
                nearest = player;
            }
        }
        if (nearest != null && minDist <= 64.0) {
            uuid = nearest.getUUID();
            nearest.sendSystemMessage(
                    Component.translatable(
                            "gtlsupb.machine.steam_module.auto_bind",
                            TeamUtil.GetName(nearest)));
            getRecipeLogic().updateTickSubscription();
        }
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player,
                                   InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {

            if (!isRemote() && uuid != null) {
                SteamEnergyManager.unregisterFactoryConnection(uuid, getPos());
            }
            uuid = player.getUUID();
            if (isRemote()) {
                player.sendSystemMessage(
                        Component.translatable(
                                "gtlsupb.machine.steam_module.wireless_bind",
                                TeamUtil.GetName(player)));
            } else {

                SteamEnergyManager.registerFactoryConnection(player.getUUID(), getPos());
            }

            getRecipeLogic().updateTickSubscription();
            return InteractionResult.sidedSuccess(isRemote());
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        var stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return false;
        if (stack.is(GTItems.TOOL_DATA_STICK.asItem())) {

            if (!isRemote() && uuid != null) {
                SteamEnergyManager.unregisterFactoryConnection(uuid, getPos());
            }
            uuid = null;
            if (isRemote()) {
                player.sendSystemMessage(
                        Component.translatable("gtlsupb.machine.steam_module.wireless_unbind"));
            }
            getRecipeLogic().updateTickSubscription();
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (player != null && hand != null && player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {
            return false;
        }
        return true;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {

            addMachineModeDisplay(textList);

            BigInteger wirelessSteam = getWirelessSteamEnergy();
            if (uuid != null) {
                textList.add(
                        Component.translatable("gtlsupb.module.wireless_bound")
                                .withStyle(ChatFormatting.GREEN));
                textList.add(
                        Component.translatable(
                                "gtlsupb.machine.steam_factory.wireless_steam_pool",
                                RainbowText.createRainbowComponent(NumberFormatUtil.scientific(wirelessSteam)))
                                .withStyle(ChatFormatting.GRAY));

                long connectionCount = SteamEnergyManager.getFactoryConnectionCount(uuid);
                long dynamicParallel = gtlsupb$saturatedMultiply(connectionCount, PARALLEL_PER_CONNECTION);
                textList.add(
                        Component.translatable(
                                "gtlsupb.machine.steam_factory.wireless_connections",
                                Component.literal(String.valueOf(connectionCount)).withStyle(ChatFormatting.GOLD))
                                .withStyle(ChatFormatting.GRAY));
                textList.add(
                        Component.translatable(
                                "gtlsupb.machine.steam_factory.current_parallel",
                                Component.literal(String.valueOf(dynamicParallel)).withStyle(ChatFormatting.LIGHT_PURPLE))
                                .withStyle(ChatFormatting.GRAY));
            } else {
                textList.add(
                        Component.translatable("gtlsupb.module.wireless_unbound")
                                .withStyle(ChatFormatting.RED));
            }

            addWorkingStatus(textList);

            RecipeLogic rl = getRecipeLogic();
            if (rl instanceof IRecipeStatus status) {
                if (status.getRecipeStatus() != null && status.getRecipeStatus().reason() != null) {
                    textList.add(status.getRecipeStatus().reason().copy().withStyle(ChatFormatting.RED));
                }
                if (status.getWorkingStatus() != null && status.getWorkingStatus().reason() != null) {
                    textList.add(status.getWorkingStatus().reason().copy().withStyle(ChatFormatting.RED));
                }
            }
        } else {
            textList.add(
                    Component.translatable("gtceu.multiblock.invalid_structure")
                            .withStyle(
                                    Style.EMPTY.withColor(ChatFormatting.RED)
                                            .withHoverEvent(
                                                    new HoverEvent(
                                                            HoverEvent.Action.SHOW_TEXT,
                                                            Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                                                                    .withStyle(ChatFormatting.GRAY)))));
        }

        RecipeLogic rl2 = getRecipeLogic();
        if (rl2 instanceof ILockRecipe iLockRecipe) {
            addRecipeLockDisplay(textList, iLockRecipe);
        }
        getDefinition().getAdditionalDisplay().accept(this, textList);
        getParts().forEach(it -> it.addMultiText(textList));
    }
}

class SteamModuleLogic extends ZeroEnergyMultiTypeLogic<SteamUniversalModule> {

    private BigInteger gtlsupb$pendingSteam = BigInteger.ZERO;
    private UUID gtlsupb$pendingSteamUuid;

    public SteamModuleLogic(SteamUniversalModule machine) {
        super(machine);
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

    private long getDynamicMaxParallel() {
        if (getMachine().uuid == null) return SteamUniversalModule.PARALLEL_PER_CONNECTION;
        long connectionCount = SteamEnergyManager.getFactoryConnectionCount(getMachine().uuid);
        if (connectionCount <= 0) return SteamUniversalModule.PARALLEL_PER_CONNECTION;
        return SteamUniversalModule.gtlsupb$saturatedMultiply(connectionCount, SteamUniversalModule.PARALLEL_PER_CONNECTION);
    }

    @Override
    protected boolean isSlotIndependentSupported() {
        return true;
    }

    @Override
    public boolean checkBeforeWorking() {
        if (!getMachine().hasProxies()) return false;
        if (getMachine().uuid == null) return false;
        return SteamEnergyManager.getUserSteam(getMachine().uuid).signum() > 0;
    }

    @Override
    public boolean checkRecipe(GTRecipe recipe) {
        return RecipeRunnerHelper.matchRecipe(getMachine(), recipe);
    }

    @Override
    protected List<GTRecipeType> getSearchRecipeTypes() {
        return Collections.singletonList(getMachine().getRecipeType());
    }

    private Set<GTRecipe> lookupSelectedRecipes() {
        boundCandidateCount = 1;
        Set<GTRecipe> bound = collectPatternBoundRecipes();
        if (!bound.isEmpty()) {
            Set<GTRecipe> result = new LinkedHashSet<>();
            for (GTRecipe recipe : bound) {

                if (isSupportedBoundRecipeType(recipe.recipeType) && checkRecipe(recipe)) {
                    result.add(recipe);
                }
            }

            boundCandidateCount = result.size();
            return result;
        }

        Set<GTRecipe> recipes = new LinkedHashSet<>();
        for (GTRecipeType type : getSearchRecipeTypes()) {
            var iterator = RecipeCalculationHelper.enableDiveMode(
                    type.getLookup().getRecipeIterator(getMachine(), this::checkRecipe));
            while (iterator.hasNext()) {
                recipes.add(iterator.next());
            }
        }
        return recipes;
    }

    protected Set<GTRecipe> collectPatternBoundRecipes() {
        Set<GTRecipe> bound = new LinkedHashSet<>();
        if (getMachine().isFormed()) {
            for (var part : getMachine().getParts()) {
                if (part instanceof IPatternAssemblyHatch hatch) {
                    bound.addAll(hatch.getBoundRecipes());
                }
            }
        }
        return bound;
    }

    private ParallelData gtlsupb$calculateIndependentWithSteam() {
        return gtlsupb$stageSteamFor(calculateIndependentBoundParallels());
    }

    private ParallelData gtlsupb$stageSteamFor(ParallelData data) {
        if (data == null || data.parallels == null || data.parallels.length == 0) return null;
        UUID uuid = getMachine().uuid;
        if (uuid == null) return null;
        BigInteger totalSteamCost = BigInteger.ZERO;
        for (int i = 0; i < data.parallels.length; i++) {

            GTRecipe recipe = data.originRecipeList.get(i);
            long recipeEu = getRecipeEut(recipe);
            long steamCost = recipeEu / SteamUniversalModule.STEAM_CONVERSION_RATIO;
            if (steamCost <= 0L) steamCost = 1L;
            steamCost = Math.max(1L, (long) (steamCost * getMachine().getHatchEfficiencyMultiplier()));
            totalSteamCost = totalSteamCost.add(
                    BigInteger.valueOf(data.parallels[i]).multiply(BigInteger.valueOf(steamCost)));
        }

        gtlsupb$stageSteam(uuid, totalSteamCost);
        return data;
    }

    @Override
    public ParallelData calculateParallels() {
        if (isIndependentSlotMode()) {

            return gtlsupb$calculateIndependentWithSteam();
        }
        Set<GTRecipe> recipes = lookupSelectedRecipes();
        if (recipes.isEmpty()) return null;

        UUID uuid = getMachine().uuid;
        if (uuid == null) return null;
        long globalMax = getDynamicMaxParallel();

        long availableSteam = SteamEnergyManager.getUserSteamLong(uuid);

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

            long recipeEu = getRecipeEut(recipe);
            long rawSteamCost = recipeEu / SteamUniversalModule.STEAM_CONVERSION_RATIO;
            long steamCost = rawSteamCost <= 0 ? 1L : rawSteamCost;
            steamCost = Math.max(1L, (long) (steamCost * getMachine().getHatchEfficiencyMultiplier()));

            long remainingCandidates = Math.max(0, total - 1 - i);
            long reserveSteam = Math.min(availableSteam,
                    SteamUniversalModule.gtlsupb$saturatedMultiply(remainingCandidates, steamCost));
            long usableSteam = Math.max(0L, availableSteam - reserveSteam);
            long steamParallel = Math.min(steamCost <= 0L ? Long.MAX_VALUE : usableSteam / steamCost, globalMax);
            if (steamParallel <= 0) {
                if (availableSteam < steamCost) continue;
                steamParallel = 1L;
            }

            long itemParallel = calculateParallel(getMachine(), recipe, Math.min(globalMax, steamParallel)).firstLong();

            long finalParallel = Math.min(Math.min(steamParallel, itemParallel), globalMax);
            if (finalParallel <= 0) continue;

            long remaining = Math.max(0, total - 1 - i);
            long reserve = Math.min(remaining, finalParallel / 4);
            if (reserve > 0L) {
                finalParallel = Math.max(1L, finalParallel - reserve);
            }

            GTRecipe paralleledRecipe = RecipeCalculationHelper.multipleRecipe(recipe, finalParallel);

            if (RecipeCalculationHelper.accumulateInputsIfFit(getMachine(),
                    paralleledRecipe.inputs.get(ItemRecipeCapability.CAP),
                    paralleledRecipe.inputs.get(FluidRecipeCapability.CAP),
                    probeItems, probeFluids)) {
                recipeList.add(recipe);
                processedRecipeList.add(paralleledRecipe);
                parallelsList.add(finalParallel);

                steamCosts.add(steamCost);

                availableSteam -= SteamUniversalModule.gtlsupb$saturatedMultiply(finalParallel, steamCost);
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
        ObjectArrayList<Content> itemOutputs = new ObjectArrayList<>();
        ObjectArrayList<Content> fluidOutputs = new ObjectArrayList<>();
        ObjectArrayList<Content> itemInputs = new ObjectArrayList<>();
        ObjectArrayList<Content> fluidInputs = new ObjectArrayList<>();

        List<GTRecipe> processedRecipes = parallelData.processedRecipeList;
        if (processedRecipes == null) {

            gtlsupb$discardStagedSteam();
            return null;
        }

        boolean independent = isIndependentSlotMode();
        for (GTRecipe processedRecipe : processedRecipes) {
            List<Content> inItems = processedRecipe.inputs.get(ItemRecipeCapability.CAP);
            List<Content> inFluids = processedRecipe.inputs.get(FluidRecipeCapability.CAP);
            boolean noInput = (inItems == null || inItems.isEmpty()) && (inFluids == null || inFluids.isEmpty());
            if (independent || noInput || RecipeCalculationHelper.accumulateInputsIfFit(
                    getMachine(), inItems, inFluids, itemInputs, fluidInputs)) {
                List<Content> items = processedRecipe.outputs.get(ItemRecipeCapability.CAP);
                if (items != null) itemOutputs.addAll(items);
                List<Content> fluids = processedRecipe.outputs.get(FluidRecipeCapability.CAP);
                if (fluids != null) fluidOutputs.addAll(fluids);
            }
        }

        if (itemOutputs.isEmpty() && fluidOutputs.isEmpty()) {
            RecipeResult.of(getMachine(), RecipeResult.FAIL_FIND);

            gtlsupb$discardStagedSteam();
            return null;
        }

        GTRecipe output = GTRecipeBuilder.ofRaw().buildRawRecipe();
        output.outputs.put(ItemRecipeCapability.CAP, itemOutputs);
        output.outputs.put(FluidRecipeCapability.CAP, fluidOutputs);
        if (!independent) {

            output.inputs.put(ItemRecipeCapability.CAP, itemInputs);
            output.inputs.put(FluidRecipeCapability.CAP, fluidInputs);
        }

        int baseDuration = Math.max(ILimitedDurationMachine.MIN_DURATION,
                Math.min(ILimitedDurationMachine.MAX_DURATION, getMachine().getLimitedDuration()));
        int batchMultiplier = independent ? 1 : getBatchMultiplier();
        if (batchMultiplier > 1) {

            output.duration = baseDuration;
            batchMultiplier = RecipeCalculationHelper.findFeasibleBatch(getMachine(), output, batchMultiplier);
            if (batchMultiplier > 1) {
                output = output.copy(ContentModifier.multiplier(batchMultiplier), false);
            }
        }
        output.duration = batchMultiplier > 1 ? baseDuration * batchMultiplier : baseDuration;
        IGTRecipe.of(output).setHasTick(false);

        if (!RecipeRunnerHelper.matchRecipeOutput(getMachine(), output)) {
            gtlsupb$discardStagedSteam();
            return null;
        }

        gtlsupb$setMergedRecipeCount(parallelData);
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
