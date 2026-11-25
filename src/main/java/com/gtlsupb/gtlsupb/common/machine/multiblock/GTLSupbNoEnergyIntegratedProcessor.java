package com.gtlsupb.gtlsupb.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.data.GTItems;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.gtlsupb.gtlsupb.api.machine.SteamEnergyManager;
import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;
import com.gtlsupb.gtlsupb.utils.NumberFormatUtil;
import com.gtlsupb.gtlsupb.utils.RainbowText;
import com.hepdd.gtmthings.utils.TeamUtil;

import java.math.BigInteger;
import java.util.UUID;

public class GTLSupbNoEnergyIntegratedProcessor extends ZeroEnergyMultiTypeMachine implements IInteractedMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GTLSupbNoEnergyIntegratedProcessor.class,
            ZeroEnergyMultiTypeMachine.MANAGED_FIELD_HOLDER);

    public static final String STEAM_EU_KEY = "gtlsupb_steam_eu";

    public static final long STEAM_CONVERSION_RATIO = 1L;

    @Persisted
    public UUID uuid;

    public GTLSupbNoEnergyIntegratedProcessor(IMachineBlockEntity holder) {
        super(holder, GTLSupbRecipeTypes.STEAM_SPACE_ORE_PROCESSOR);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new NoEnergyIntegratedProcessorLogic(this);
    }

    public BigInteger getWirelessSteamEnergy() {
        if (uuid == null) return BigInteger.ZERO;
        return SteamEnergyManager.getUserSteam(uuid);
    }

    @Override
    protected boolean shouldShowEnergyMode() {
        return false;
    }

    @Override
    public void addDisplayText(java.util.List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) return;
        if (uuid == null) {
            textList.add(Component.translatable("gtlsupb.machine.steam_ore_hub.not_bound"));
            return;
        }

        textList.add(Component.translatable("gtlsupb.machine.steam_ore_hub.wireless_steam_pool",
                RainbowText.createRainbowComponent(NumberFormatUtil.scientific(getWirelessSteamEnergy())))
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (uuid == null && !isRemote()) {
            tryAutoBind();
        }
        if (!isRemote() && getLevel() instanceof ServerLevel serverLevel) {
            SteamEnergyManager.getSavedData(serverLevel);
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
                            "gtlsupb.machine.steam_ore_hub.auto_bind",
                            TeamUtil.GetName(nearest)));
            getRecipeLogic().updateTickSubscription();
        }
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player,
                                   InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {
            uuid = player.getUUID();
            if (isRemote()) {
                player.sendSystemMessage(
                        Component.translatable(
                                "gtlsupb.machine.steam_ore_hub.wireless_bind",
                                TeamUtil.GetName(player)));
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
            uuid = null;
            if (isRemote()) {
                player.sendSystemMessage(
                        Component.translatable("gtlsupb.machine.steam_ore_hub.wireless_unbind"));
            }
            getRecipeLogic().updateTickSubscription();
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return player == null || hand == null || !player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem());
    }
}
