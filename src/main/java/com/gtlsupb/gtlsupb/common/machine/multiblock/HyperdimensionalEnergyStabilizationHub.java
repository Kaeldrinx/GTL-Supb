package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.machine.multiblock.NoEnergyMultiblockMachine;

import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import com.gtlsupb.gtlsupb.utils.EnergyBroadcastHelper;
import com.gtlsupb.gtlsupb.utils.RainbowText;
import com.hepdd.gtmthings.api.misc.WirelessEnergyManager;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

public class HyperdimensionalEnergyStabilizationHub extends NoEnergyMultiblockMachine implements IMachineLife {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HyperdimensionalEnergyStabilizationHub.class,
            NoEnergyMultiblockMachine.MANAGED_FIELD_HOLDER);

    public static final long MAX_STABILITY = 10_000_000L;
    public static final int MAX_OC_LEVEL = 12;
    public static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    public static final BigInteger ASTRONOMICAL_MAX = BigInteger.ONE.shiftLeft(65536);

    public static final BigInteger BIG_1M = BigInteger.valueOf(1_000_000L);
    public static final BigInteger BIG_100K = BigInteger.valueOf(100_000L);

    private final ConditionalSubscriptionHandler tickSubs;

    {
        tickSubs = new ConditionalSubscriptionHandler(this, this::onTickUpdate, () -> isFormed());
    }

    @Persisted
    private long dimensionalStability = 0;
    @Persisted
    private long dimensionalStrain = 0;
    @Persisted
    private boolean ocEnabled = false;
    @Persisted
    private int ocLevel = 0;
    @Persisted
    private boolean wirelessTransferEnabled = false;
    @Persisted
    @DescSynced
    private boolean generationEnabled = true;
    @Persisted
    @DescSynced
    private UUID userId;

    public UUID gtlsupb$getOwnerUuid() {
        return userId;
    }

    @Persisted
    private int generationTickRate = 20;

    private long accumulatedEU = 0L;
    private int broadcastCounter = 0;
    private int generationCounter = 0;
    private BigInteger lastOutputEU = BigInteger.ZERO;

    private int lastOCLevel = -1;
    private BigInteger cachedOCPower = BigInteger.ZERO;

    private long lastEffectiveStability = -1L;
    private BigInteger cachedStabilityPower = BigInteger.ZERO;

    public HyperdimensionalEnergyStabilizationHub(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public int getOcLevel() {
        return ocLevel;
    }

    public boolean isOcEnabled() {
        return ocEnabled;
    }

    public long getDimensionalStability() {
        return dimensionalStability;
    }

    public long getAccumulatedEU() {
        return accumulatedEU;
    }

    public boolean isWirelessTransferEnabled() {
        return wirelessTransferEnabled;
    }

    public int getGenerationTickRate() {
        return generationTickRate;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && userId == null) {
            var nearest = com.gtlsupb.gtlsupb.common.data.PlayerBindingHelper.findNearestPlayer(getLevel(), getPos(), 16.0d);
            if (nearest != null) {
                userId = nearest.getUUID();
                markDirty();
            }
        }
    }

    @Override
    public void onMachinePlaced(LivingEntity player, ItemStack stack) {
        if (player != null) {
            userId = player.getUUID();
            markDirty();
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
    public void onPartUnload() {
        super.onPartUnload();
        tickSubs.unsubscribe();
    }

    private void onTickUpdate() {
        if (!generationEnabled) {
            lastOutputEU = BigInteger.ZERO;
            return;
        }
        if (dimensionalStability < MAX_STABILITY) {
            dimensionalStability += 10000;
            if (dimensionalStability > MAX_STABILITY) dimensionalStability = MAX_STABILITY;
        }

        if (dimensionalStability > MAX_STABILITY / 10 && Math.random() < 0.005 * (1.0 + (double) dimensionalStability / MAX_STABILITY)) {
            if (dimensionalStrain > 99) {
                dimensionalStability -= 1000;
                dimensionalStrain = 0;
            } else {
                dimensionalStrain++;
            }
        }

        if (dimensionalStrain > 70 && dimensionalStability > 0) {
            dimensionalStability -= 50;
            dimensionalStrain -= 10;
        }

        long strainPenalty = dimensionalStrain > 60 ? (dimensionalStrain - 60) * 100_000L : 0L;
        long effectiveStability = Math.max(0L, dimensionalStability - strainPenalty);
        if (effectiveStability <= 0) {
            lastOutputEU = BigInteger.ZERO;
            accumulatedEU = 0L;
            return;
        }

        if (dimensionalStability >= MAX_STABILITY && !ocEnabled) {
            ocEnabled = true;
            ocLevel = 1;
        }

        BigInteger stability = BigInteger.valueOf(effectiveStability);
        if (ocEnabled && ocLevel > 0) {
            BigInteger ocPower = computeOCExponentialPower(ocLevel);
            BigInteger stabilityMult = stability.multiply(stability).multiply(BIG_1M);
            lastOutputEU = ocPower.multiply(stabilityMult).min(ASTRONOMICAL_MAX);
        } else {
            lastOutputEU = stability.multiply(stability).multiply(BIG_100K).min(ASTRONOMICAL_MAX);
        }

        if (lastOutputEU.compareTo(BigInteger.ZERO) > 0 && userId != null) {

            generationCounter++;
            if (generationCounter >= generationTickRate) {
                generationCounter = 0;
                BigInteger batchEU = lastOutputEU.multiply(BigInteger.valueOf(generationTickRate));
                WirelessEnergyManager.addEUToGlobalEnergyMap(userId, batchEU, this);
            }
        }

        accumulatedEU += lastOutputEU.min(LONG_MAX).longValue();
        if (accumulatedEU < 0) accumulatedEU = Long.MAX_VALUE;

        if (wirelessTransferEnabled && userId != null) {
            broadcastCounter++;
            if (broadcastCounter >= 20 && accumulatedEU > 0) {
                broadcastCounter = 0;
                accumulatedEU = EnergyBroadcastHelper.broadcastEnergy(this, accumulatedEU, userId);
            }
        }
    }

    private BigInteger getDisplayPower() {
        long strainPenalty = dimensionalStrain > 60 ? (dimensionalStrain - 60) * 100_000L : 0L;
        long effectiveStability = Math.max(0L, dimensionalStability - strainPenalty);
        if (effectiveStability <= 0) return BigInteger.ZERO;

        if (effectiveStability == lastEffectiveStability && ocLevel == lastOCLevel) {
            return cachedStabilityPower;
        }

        lastEffectiveStability = effectiveStability;

        BigInteger s = BigInteger.valueOf(effectiveStability);
        if (ocEnabled && ocLevel > 0) {
            BigInteger ocPower = computeOCExponentialPower(ocLevel);
            BigInteger stabilityMult = s.multiply(s).multiply(BIG_1M);
            cachedStabilityPower = ocPower.multiply(stabilityMult).min(ASTRONOMICAL_MAX);
        } else {
            cachedStabilityPower = s.multiply(s).multiply(BIG_100K).min(ASTRONOMICAL_MAX);
        }
        return cachedStabilityPower;
    }

    private BigInteger computeOCExponentialPower(int ocLevel) {
        if (ocLevel <= 0) return BigInteger.ZERO;
        if (ocLevel == lastOCLevel) return cachedOCPower;
        BigInteger parallel = BigInteger.valueOf(ocLevel).pow(4);
        int ocTimes = 100 + (ocLevel - 1) * 100;
        cachedOCPower = LONG_MAX.multiply(parallel.pow(ocTimes)).min(ASTRONOMICAL_MAX);
        lastOCLevel = ocLevel;
        return cachedOCPower;
    }

    private String formatBigIntegerReadable(BigInteger num) {
        if (num.compareTo(BigInteger.ZERO) <= 0) return "0";
        if (num.compareTo(BigInteger.valueOf(1000L)) < 0) return num.toString();
        String[] units = { "", "K", "M", "G", "T", "P", "E", "Z", "Y", "B", "N", "D" };
        BigDecimal temp = new BigDecimal(num);
        int idx = 0;
        BigDecimal thousand = BigDecimal.valueOf(1000);
        while (temp.compareTo(thousand) >= 0 && idx < units.length - 1) {
            temp = temp.divide(thousand, 2, RoundingMode.HALF_UP);
            idx++;
        }
        return String.format("%,.2f %s", temp, units[idx]);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            addMachineModeDisplay(textList);
            addWorkingStatus(textList);

            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.dimensional_stabilizer.stability_rainbow",
                            RainbowText.createRainbowComponent(String.format("%,d", dimensionalStability))).withStyle(ChatFormatting.GRAY));

            if (ocEnabled && ocLevel > 0) {
                int ocTimes = 100 + (ocLevel - 1) * 100;
                Component ocLine = Component.translatable(
                        "gtlsupb.machine.dimensional_stabilizer.oc_rainbow",
                        RainbowText.createRainbowComponent(String.valueOf(ocLevel)),
                        RainbowText.createRainbowComponent(String.format("%,d", ocTimes))).withStyle(ChatFormatting.GRAY);
                Component buttonText = Component.empty()
                        .append(ComponentPanelWidget.withButton(Component.literal("§c[-]"), "oc_sub"))
                        .append(Component.literal(" ").withStyle(ChatFormatting.RESET))
                        .append(ocLine)
                        .append(Component.literal(" ").withStyle(ChatFormatting.RESET))
                        .append(ComponentPanelWidget.withButton(Component.literal("§a[+]"), "oc_add"));
                textList.add(buttonText);
            } else if (dimensionalStability >= MAX_STABILITY) {
                textList.add(
                        Component.translatable("gtlsupb.machine.dimensional_stabilizer.oc_unlock")
                                .withStyle(ChatFormatting.GREEN));
            } else {
                textList.add(
                        Component.translatable("gtlsupb.machine.dimensional_stabilizer.oc_locked")
                                .withStyle(ChatFormatting.DARK_GRAY));
            }

            if (ocEnabled && ocLevel > 0) {
                textList.add(
                        Component.translatable(
                                "gtlsupb.machine.dimensional_stabilizer.power_rainbow",
                                RainbowText.createLanguageRainbowComponentOnServer(
                                        Component.translatable("gtlsupb.machine.infinite.parallel")))
                                .withStyle(ChatFormatting.GRAY));
            } else {
                BigInteger power = getDisplayPower();
                String formatted = formatBigIntegerReadable(power);
                textList.add(
                        Component.translatable(
                                "gtlsupb.machine.dimensional_stabilizer.power_rainbow",
                                RainbowText.createRainbowComponent(formatted + " EU/t")).withStyle(ChatFormatting.GRAY));
            }

            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.dimensional_stabilizer.wireless_rainbow",
                            RainbowText.createRainbowComponent(String.format("%,d EU", accumulatedEU))).withStyle(ChatFormatting.GRAY));

            Component generationToggleText = generationEnabled ?
                    Component.translatable("gtlsupb.gui.toggle.on").withStyle(ChatFormatting.GREEN) :
                    Component.translatable("gtlsupb.gui.toggle.off").withStyle(ChatFormatting.RED);
            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.dimensional_stabilizer.generation_toggle",
                            ComponentPanelWidget.withButton(
                                    Component.translatable(generationEnabled ? "gtlsupb.gui.toggle.on" : "gtlsupb.gui.toggle.off"),
                                    "generation_toggle"),
                            generationToggleText).withStyle(ChatFormatting.GRAY));

            Component transferToggleText = wirelessTransferEnabled ?
                    Component.translatable("gtlsupb.machine.dimensional_stabilizer.wireless_toggle.on").withStyle(ChatFormatting.GREEN) :
                    Component.translatable("gtlsupb.machine.dimensional_stabilizer.wireless_toggle.off").withStyle(ChatFormatting.RED);
            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.dimensional_stabilizer.wireless_toggle",
                            ComponentPanelWidget.withButton(
                                    Component.translatable(wirelessTransferEnabled ? "gtlsupb.gui.toggle.on" : "gtlsupb.gui.toggle.off"),
                                    "wireless_toggle"),
                            transferToggleText).withStyle(ChatFormatting.GRAY));

            Component tickRateLine = Component.translatable(
                    "gtlsupb.machine.dimensional_stabilizer.gen_rate",
                    RainbowText.createRainbowComponent(String.valueOf(generationTickRate))).withStyle(ChatFormatting.GRAY);
            Component tickRateButton = Component.empty()
                    .append(ComponentPanelWidget.withButton(Component.literal("§c[-]"), "rate_sub"))
                    .append(Component.literal(" ").withStyle(ChatFormatting.RESET))
                    .append(tickRateLine)
                    .append(Component.literal(" ").withStyle(ChatFormatting.RESET))
                    .append(ComponentPanelWidget.withButton(Component.literal("§a[+]"), "rate_add"));
            textList.add(tickRateButton);

            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.dimensional_stabilizer.strain_rainbow",
                            RainbowText.createRainbowComponent(dimensionalStrain + "%")).withStyle(ChatFormatting.GRAY));
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
        getParts().forEach(it -> {
            if (it instanceof IMultiPart) {
                ((IMultiPart) it).addMultiText(textList);
            }
        });
    }

    private void addMachineModeDisplay(List<Component> textList) {
        textList.add(
                Component.translatable("gtlsupb.machine.dimensional_stabilizer.mode")
                        .withStyle(ChatFormatting.AQUA));
    }

    private void addWorkingStatus(List<Component> textList) {
        if (dimensionalStability > 0) {
            textList.add(Component.translatable("gtceu.multiblock.running").withStyle(ChatFormatting.GREEN));
        } else {
            textList.add(Component.translatable("gtceu.multiblock.idling"));
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote && isFormed()) {
            int multiplier = clickData.isCtrlClick ? 4 : clickData.isShiftClick ? 2 : 1;
            switch (componentData) {
                case "oc_sub" -> decOCLevel(multiplier);
                case "oc_add" -> incOCLevel(multiplier);
                case "wireless_toggle" -> toggleWirelessTransfer();
                case "generation_toggle" -> {
                    generationEnabled = !generationEnabled;
                    lastOutputEU = BigInteger.ZERO;
                    markDirty();
                }
                case "rate_sub" -> decGenerationTickRate(multiplier);
                case "rate_add" -> incGenerationTickRate(multiplier);
            }
        }
    }

    private void incOCLevel(int increment) {
        if (!ocEnabled) return;
        int newLevel = Math.min(ocLevel + increment, MAX_OC_LEVEL);
        if (newLevel != ocLevel) {
            ocLevel = newLevel;
            lastOCLevel = -1;
        }
    }

    private void decOCLevel(int decrement) {
        if (!ocEnabled) return;
        int newLevel = Math.max(ocLevel - decrement, 1);
        if (newLevel != ocLevel) {
            ocLevel = newLevel;
            lastOCLevel = -1;
        }
    }

    private void toggleWirelessTransfer() {
        wirelessTransferEnabled = !wirelessTransferEnabled;
    }

    private void incGenerationTickRate(int increment) {
        int newVal = Math.min(generationTickRate + increment, 200);
        if (newVal != generationTickRate) {
            generationTickRate = newVal;
            generationCounter = 0;
        }
    }

    private void decGenerationTickRate(int decrement) {
        int newVal = Math.max(generationTickRate - decrement, 1);
        if (newVal != generationTickRate) {
            generationTickRate = newVal;
            generationCounter = 0;
        }
    }
}
