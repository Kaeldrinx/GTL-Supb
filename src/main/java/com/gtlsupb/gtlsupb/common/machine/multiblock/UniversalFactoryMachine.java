package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.machine.trait.ILockRecipe;
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus;
import org.gtlcore.gtlcore.api.recipe.IGTRecipe;
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper;
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
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

import com.gtlsupb.gtlsupb.api.machine.VoltageUnlockManager;
import com.hepdd.gtmthings.utils.TeamUtil;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class UniversalFactoryMachine extends ZeroEnergyMultiTypeMachine implements IInteractedMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            UniversalFactoryMachine.class,
            ZeroEnergyMultiTypeMachine.MANAGED_FIELD_HOLDER);

    private static final long[] VOLTAGE_THRESHOLDS = {
            8L, 32L, 128L, 512L, 2048L, 8192L, 32768L, 131072L,
            524288L, 2097152L, 8388608L, 33554432L, 134217728L, 536870912L, Long.MAX_VALUE
    };

    @Persisted
    public UUID uuid;

    @Persisted
    @DescSynced
    private int syncedVoltageTier = 0;

    private volatile int cachedVoltageTier = 0;
    private volatile long lastOverclockVoltage = -1L;

    private int cachedUnlockedTier = -1;
    private long cachedUnlockedTierTick = Long.MIN_VALUE;

    private static final int UNLOCKED_TIER_CACHE_TICKS = 10;

    public UniversalFactoryMachine(IMachineBlockEntity holder) {
        super(holder,
                GTRecipeTypes.BENDER_RECIPES,
                GTRecipeTypes.COMPRESSOR_RECIPES,
                GTRecipeTypes.FORGE_HAMMER_RECIPES,
                GTRecipeTypes.CUTTER_RECIPES,
                GTRecipeTypes.EXTRUDER_RECIPES,
                GTRecipeTypes.LATHE_RECIPES,
                GTRecipeTypes.WIREMILL_RECIPES,
                GTRecipeTypes.FORMING_PRESS_RECIPES,
                GTRecipeTypes.POLARIZER_RECIPES,
                GTRecipeTypes.LASER_ENGRAVER_RECIPES,
                GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES,
                GTRecipeTypes.ASSEMBLER_RECIPES,
                GTRecipeTypes.ARC_FURNACE_RECIPES,
                GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                GTRecipeTypes.CANNER_RECIPES,
                GTRecipeTypes.CENTRIFUGE_RECIPES,
                GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES,
                GTRecipeTypes.ELECTROLYZER_RECIPES,
                GTRecipeTypes.SIFTER_RECIPES,
                GTRecipeTypes.MACERATOR_RECIPES,
                GTRecipeTypes.EXTRACTOR_RECIPES,
                GTRecipeTypes.CHEMICAL_RECIPES,
                GTRecipeTypes.MIXER_RECIPES,
                GTRecipeTypes.CHEMICAL_BATH_RECIPES,
                GTRecipeTypes.ORE_WASHER_RECIPES,
                GTLRecipeTypes.DEHYDRATOR_RECIPES,
                GTRecipeTypes.LARGE_CHEMICAL_RECIPES,
                GTRecipeTypes.PACKER_RECIPES,
                GTRecipeTypes.DISTILLERY_RECIPES,
                GTRecipeTypes.AUTOCLAVE_RECIPES,
                GTRecipeTypes.FLUID_HEATER_RECIPES,
                GTRecipeTypes.BREWING_RECIPES,
                GTRecipeTypes.FERMENTING_RECIPES,
                GTLRecipeTypes.LIGHTNING_PROCESSOR_RECIPES);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new UniversalFactoryLogic(this);
    }

    @Override
    public boolean shouldShowMachineModeSwitcher() {
        return true;
    }

    public boolean isWirelessBound() {
        return uuid != null;
    }

    public int computeVoltageTier() {
        if (isRemote()) return Math.max(0, Math.min(GTValues.MAX, syncedVoltageTier));
        if (isWirelessBound()) {

            long now = getOffsetTimer();
            if (cachedUnlockedTier < 0 || now - cachedUnlockedTierTick >= UNLOCKED_TIER_CACHE_TICKS) {
                cachedUnlockedTier = VoltageUnlockManager.getTier(uuid);
                cachedUnlockedTierTick = now;
            }
            int unlockedTier = cachedUnlockedTier;
            int tier = unlockedTier >= 0 ? Math.max(0, Math.min(GTValues.MAX, unlockedTier)) : 0;
            cachedVoltageTier = tier;
            if (syncedVoltageTier != tier) syncedVoltageTier = tier;
            return tier;
        }

        long voltage = getOverclockVoltage();
        if (voltage == lastOverclockVoltage) return Math.max(0, Math.min(GTValues.MAX, cachedVoltageTier));
        lastOverclockVoltage = voltage;
        if (voltage <= 0) {

            if (cachedVoltageTier != GTValues.MAX) cachedVoltageTier = GTValues.MAX;
            if (syncedVoltageTier != GTValues.MAX) syncedVoltageTier = GTValues.MAX;
            return GTValues.MAX;
        }
        int tier = 0;
        long v = voltage;
        while (v > 8L) {
            v /= 4L;
            tier++;
        }
        int finalTier = Math.max(0, Math.min(GTValues.MAX, tier));
        cachedVoltageTier = finalTier;
        if (syncedVoltageTier != finalTier) syncedVoltageTier = finalTier;
        return finalTier;
    }

    @Override
    public int getMaxParallel() {
        int tier = computeVoltageTier();
        return Integer.MAX_VALUE >>> (2 * (14 - Math.max(0, Math.min(14, tier))));
    }

    @Override
    public long maxParallel() {
        return getMaxParallel();
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player,
                                   InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {
            uuid = player.getUUID();
            if (isRemote()) {
                player.sendSystemMessage(
                        Component.translatable(
                                "gtlsupb.machine.universal_factory.wireless_bind",
                                TeamUtil.GetName(player)));
            } else {
                cachedUnlockedTier = -1;
                computeVoltageTier();
            }
            return InteractionResult.sidedSuccess(isRemote());
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        var stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return false;
        if (stack.is(GTItems.TOOL_DATA_STICK.asItem())) {
            uuid = null;
            cachedUnlockedTier = -1;
            if (isRemote()) {
                player.sendSystemMessage(
                        Component.translatable("gtlsupb.machine.universal_factory.wireless_unbind"));
            }
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
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!isRemote()) {
            syncClientData();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            if (getLevel() instanceof ServerLevel serverLevel) {
                VoltageUnlockManager.getSavedData(serverLevel);
            }
            if (uuid == null) {
                tryAutoBind();
            }
            syncClientData();
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
                            "gtlsupb.machine.universal_factory.auto_bind",
                            TeamUtil.GetName(nearest)));
            computeVoltageTier();
        }
    }

    private void syncClientData() {
        computeVoltageTier();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            addMachineModeDisplay(textList);

            int displayTier = Math.max(0, Math.min(GTValues.MAX, computeVoltageTier()));
            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.max_voltage_tier",
                            Component.literal(GTValues.VN[displayTier]).withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.literal(String.valueOf(GTValues.V[displayTier])).withStyle(ChatFormatting.YELLOW))
                            .withStyle(ChatFormatting.GRAY));

            textList.add(Component.translatable("gtlsupb.common.zero_energy"));

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

        if (isFormed()) {

            int maxParallel = getMaxParallel();
            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.parallel_info",
                            Component.literal(String.valueOf(maxParallel)).withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.GRAY));
        }

        RecipeLogic rl2 = getRecipeLogic();
        if (rl2 instanceof ILockRecipe iLockRecipe) {
            addRecipeLockDisplay(textList, iLockRecipe);
        }
        getDefinition().getAdditionalDisplay().accept(this, textList);
        getParts().forEach(it -> it.addMultiText(textList));
    }
}

class UniversalFactoryLogic extends ZeroEnergyMultiTypeLogic<UniversalFactoryMachine> {

    public UniversalFactoryLogic(UniversalFactoryMachine machine) {
        super(machine);
    }

    @Override
    protected List<GTRecipeType> getSearchRecipeTypes() {
        return Collections.singletonList(getMachine().getRecipeType());
    }

    @Override
    public boolean checkRecipe(GTRecipe recipe) {
        if (getMachine().isWirelessBound()) {
            int unlockedTier = getMachine().computeVoltageTier();
            return RecipeRunnerHelper.matchRecipe(getMachine(), recipe) &&
                    IGTRecipe.of(recipe).getEuTier() <= unlockedTier;
        }
        return RecipeRunnerHelper.matchRecipe(getMachine(), recipe) &&
                IGTRecipe.of(recipe).getEuTier() <= getMachine().getTier() &&
                recipe.checkConditions(this).isSuccess();
    }
}
