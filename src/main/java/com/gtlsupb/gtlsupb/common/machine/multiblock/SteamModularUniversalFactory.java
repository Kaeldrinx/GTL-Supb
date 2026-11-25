package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.machine.multiblock.NoEnergyMultiblockMachine;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTItems;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import com.gtlsupb.gtlsupb.api.machine.SteamEnergyManager;
import com.gtlsupb.gtlsupb.utils.NumberFormatUtil;
import com.gtlsupb.gtlsupb.utils.RainbowText;
import com.hepdd.gtmthings.utils.TeamUtil;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class SteamModularUniversalFactory extends NoEnergyMultiblockMachine implements IInteractedMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamModularUniversalFactory.class,
            NoEnergyMultiblockMachine.MANAGED_FIELD_HOLDER);

    public static final long STEAM_CONVERSION_RATE = 1L;

    public static final int DEFAULT_CONVERSION_TICK = 20;

    public static final int MAX_CONVERSION_TICK = 100;

    @Persisted
    public UUID uuid;

    @Persisted
    @DescSynced
    public long lastSteamOutput = 0;

    @Persisted
    @DescSynced
    public int conversionTick = DEFAULT_CONVERSION_TICK;

    @Persisted
    @DescSynced
    public boolean conversionEnabled = true;

    private final ConditionalSubscriptionHandler tickSubs;
    private int saveTickCounter = 0;
    private int conversionCounter = 0;

    public SteamModularUniversalFactory(IMachineBlockEntity holder) {
        super(holder);
        tickSubs = new ConditionalSubscriptionHandler(this, this::processSteamGenTick, this::isFormed);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public BigInteger getWirelessSteamEnergy() {
        if (uuid == null) return BigInteger.ZERO;
        return SteamEnergyManager.getUserSteam(uuid);
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
                            "gtlsupb.machine.steam_factory.auto_bind",
                            TeamUtil.GetName(nearest)));
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        tickSubs.initialize(getLevel());
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        tickSubs.unsubscribe();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        tickSubs.unsubscribe();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        tickSubs.unsubscribe();
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player,
                                   InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {
            uuid = player.getUUID();
            if (isRemote()) {
                player.sendSystemMessage(
                        Component.translatable(
                                "gtlsupb.machine.steam_factory.wireless_bind",
                                TeamUtil.GetName(player)));
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
            if (isRemote()) {
                player.sendSystemMessage(
                        Component.translatable("gtlsupb.machine.steam_factory.wireless_unbind"));
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

    private void processSteamGenTick() {
        if (uuid == null || !conversionEnabled) return;

        conversionCounter++;
        if (conversionCounter >= conversionTick) {
            conversionCounter = 0;

            long waterInput = getWaterInputDirect();
            if (waterInput > 0) {
                long consumed = extractWaterDirect(waterInput);
                if (consumed <= 0) return;

                long maxProduce = consumed * STEAM_CONVERSION_RATE;
                lastSteamOutput = maxProduce;
                SteamEnergyManager.addSteamToGlobalMap(uuid, maxProduce);
            } else {
                lastSteamOutput = 0;
            }
        }

        saveTickCounter++;
        if (saveTickCounter >= 20) {
            saveTickCounter = 0;
            if (SteamEnergyManager.isDirty) {
                SteamEnergyManager.isDirty = false;
                if (getLevel() instanceof ServerLevel serverLevel) {
                    SteamEnergyManager.getSavedData(serverLevel).setDirty();
                }
            }
        }
    }

    private long getWaterInputDirect() {
        var handlers = getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP);
        if (handlers == null) return 0L;
        long total = 0L;
        for (var handler : handlers) {
            if (handler instanceof NotifiableFluidTank tank) {
                for (int i = 0; i < tank.getTanks(); i++) {
                    FluidStack stack = tank.getFluidInTank(i);
                    if (!stack.isEmpty() && stack.getFluid() == Fluids.WATER) {
                        total += stack.getAmount();
                    }
                }
            }
        }
        return total;
    }

    private long extractWaterDirect(long amount) {
        var handlers = getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP);
        if (handlers == null) return 0L;
        long extracted = 0L;
        for (var handler : handlers) {
            if (extracted >= amount) break;
            if (handler instanceof NotifiableFluidTank tank) {
                for (int i = 0; i < tank.getTanks(); i++) {
                    FluidStack stack = tank.getFluidInTank(i);
                    if (!stack.isEmpty() && stack.getFluid() == Fluids.WATER) {
                        long need = amount - extracted;
                        FluidStack drained = tank.drainInternal(
                                FluidStack.create(stack.getFluid(), need, stack.getTag()),
                                true);
                        extracted += drained.getAmount();
                        if (extracted >= amount) break;
                    }
                }
            }
        }
        return extracted;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            textList.add(
                    Component.translatable("gtlsupb.machine.steam_factory.title")
                            .withStyle(ChatFormatting.AQUA));

            BigInteger wirelessSteam = getWirelessSteamEnergy();
            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.steam_factory.wireless_steam_pool",
                            RainbowText.createRainbowComponent(NumberFormatUtil.scientific(wirelessSteam)))
                            .withStyle(ChatFormatting.GRAY));

            if (lastSteamOutput > 0) {
                textList.add(
                        Component.translatable(
                                "gtlsupb.machine.steam_factory.steam_output",
                                Component.literal(String.format("%,d", lastSteamOutput)).withStyle(ChatFormatting.GREEN))
                                .withStyle(ChatFormatting.GRAY));
            }

            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.steam_factory.conversion_toggle",
                            ComponentPanelWidget.withButton(
                                    Component.translatable(conversionEnabled ? "gtlsupb.gui.toggle.on" : "gtlsupb.gui.toggle.off"),
                                    "conversion_toggle"),
                            conversionEnabled ?
                                    Component.translatable("gtlsupb.gui.toggle.on").withStyle(ChatFormatting.GREEN) :
                                    Component.translatable("gtlsupb.gui.toggle.off").withStyle(ChatFormatting.RED))
                            .withStyle(ChatFormatting.GRAY));

            Component tickLine = Component.translatable(
                    "gtlsupb.machine.steam_factory.conversion_tick",
                    Component.literal(String.valueOf(conversionTick)).withStyle(ChatFormatting.YELLOW))
                    .withStyle(ChatFormatting.GRAY);
            Component tickButton = Component.empty()
                    .append(ComponentPanelWidget.withButton(Component.literal("§c[-]"), "tick_sub"))
                    .append(Component.literal(" ").withStyle(ChatFormatting.RESET))
                    .append(tickLine)
                    .append(Component.literal(" ").withStyle(ChatFormatting.RESET))
                    .append(ComponentPanelWidget.withButton(Component.literal("§a[+]"), "tick_add"));
            textList.add(tickButton);

            if (uuid != null) {
                long connectionCount = SteamEnergyManager.getFactoryConnectionCount(uuid);
                textList.add(
                        Component.translatable(
                                "gtlsupb.machine.steam_factory.wireless_connections",
                                Component.literal(String.valueOf(connectionCount)).withStyle(ChatFormatting.GOLD))
                                .withStyle(ChatFormatting.GRAY));
            }

            if (uuid != null) {
                textList.add(
                        Component.translatable("gtlsupb.machine.steam_factory.wireless_bound")
                                .withStyle(ChatFormatting.GREEN));
            } else {
                textList.add(
                        Component.translatable("gtlsupb.machine.steam_factory.wireless_unbound")
                                .withStyle(ChatFormatting.RED));
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

        getDefinition().getAdditionalDisplay().accept(this, textList);
        getParts().forEach(it -> it.addMultiText(textList));
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote && isFormed()) {
            int multiplier = clickData.isCtrlClick ? 10 : clickData.isShiftClick ? 5 : 1;
            switch (componentData) {
                case "tick_sub" -> decConversionTick(multiplier);
                case "tick_add" -> incConversionTick(multiplier);
                case "conversion_toggle" -> {
                    conversionEnabled = !conversionEnabled;
                    lastSteamOutput = 0;
                    markDirty();
                }
            }
        }
    }

    private void incConversionTick(int increment) {
        int newVal = Math.min(conversionTick + increment, MAX_CONVERSION_TICK);
        if (newVal != conversionTick) {
            conversionTick = newVal;
            conversionCounter = 0;
        }
    }

    private void decConversionTick(int decrement) {
        int newVal = Math.max(conversionTick - decrement, 1);
        if (newVal != conversionTick) {
            conversionTick = newVal;
            conversionCounter = 0;
        }
    }
}
