package com.gtlsupb.gtlsupb.common.machine.multiblock.module;

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule;

import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.gtlsupb.gtlsupb.api.gui.UniversalOrderModuleConfigurator;
import com.gtlsupb.gtlsupb.common.machine.multiblock.UniversalOrderKeeper;
import com.gtlsupb.gtlsupb.common.machine.multiblock.ZeroEnergyMultiTypeLogic;
import com.gtlsupb.gtlsupb.common.machine.multiblock.ZeroEnergyMultiTypeMachine;
import com.gtlsupb.gtlsupb.utils.RainbowText;

import java.util.ArrayList;
import java.util.List;

public abstract class UniversalOrderModuleBase extends ZeroEnergyMultiTypeMachine
                                               implements IModularMachineModule<UniversalOrderKeeper, UniversalOrderModuleBase> {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            UniversalOrderModuleBase.class,
            ZeroEnergyMultiTypeMachine.MANAGED_FIELD_HOLDER);

    private static final int[][] NORTH_COLUMNS = new int[][] {
            { -10, -11 }, { -4, -11 }, { 4, -11 }, { 10, -11 },
            { -10, -5 }, { -4, -5 }, { 4, -5 }, { 10, -5 },
            { -10, 3 }, { -4, 3 }, { 4, 3 }, { 10, 3 },
            { -10, 9 }, { -4, 9 }, { 4, 9 }, { 10, 9 },
    };

    private static final GTRecipeType[] NO_TYPES = new GTRecipeType[0];

    @Persisted
    @DescSynced
    private long enabledMask = -1L;

    @Persisted
    @DescSynced
    private BlockPos hostPosition;

    @DescSynced
    private String hostModesCsv = "";

    private UniversalOrderKeeper host;

    public UniversalOrderModuleBase(IMachineBlockEntity holder, GTRecipeType... recipeTypes) {
        super(holder, recipeTypes);
    }

    public abstract GTRecipeType[] getModuleRecipeTypes();

    public boolean isModeSelectable() {
        return false;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public GTRecipeType[] getAvailableRecipeTypes() {
        List<GTRecipeType> merged = new ArrayList<>();
        for (GTRecipeType type : getModuleRecipeTypes()) {
            if (type != null) merged.add(type);
        }
        for (GTRecipeType type : gtlsupb$hostTypes()) {
            if (type != null && !merged.contains(type)) merged.add(type);
        }
        return merged.isEmpty() ? NO_TYPES : merged.toArray(new GTRecipeType[0]);
    }

    private GTRecipeType[] gtlsupb$hostTypes() {
        if (!isRemote()) {
            UniversalOrderKeeper connected = host;
            if (connected == null) return NO_TYPES;
            return connected.getRecognizedRecipeTypes();
        }
        if (hostModesCsv.isEmpty()) return NO_TYPES;
        List<GTRecipeType> types = new ArrayList<>();
        for (String name : hostModesCsv.split(";")) {
            if (name.isEmpty()) continue;
            try {
                GTRecipeType type = GTRegistries.RECIPE_TYPES.get(new ResourceLocation(name));
                if (type != null) types.add(type);
            } catch (Throwable ignored) {}
        }
        return types.isEmpty() ? NO_TYPES : types.toArray(new GTRecipeType[0]);
    }

    public boolean isModeEnabled(int index) {
        return index < 0 || index >= Long.SIZE || (enabledMask & (1L << index)) != 0L;
    }

    public void toggleMode(int index) {
        if (index < 0 || index >= Long.SIZE) return;
        enabledMask ^= (1L << index);
        gtlsupb$syncModeMask();
    }

    public void setAllModes(boolean enabled) {
        enabledMask = enabled ? -1L : 0L;
        gtlsupb$syncModeMask();
    }

    private void gtlsupb$syncModeMask() {
        if (isRemote()) {
            com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler.CHANNEL.sendToServer(
                    new com.gtlsupb.gtlsupb.network.C2SModuleModeMaskPacket(getPos(), enabledMask));
        } else {
            gtlsupb$applyModeMask(enabledMask);
        }
    }

    public void applyModeMaskFromClient(long mask) {
        if (isRemote()) return;
        if (enabledMask == mask) return;
        enabledMask = mask;
        gtlsupb$applyModeMask(mask);
    }

    private void gtlsupb$applyModeMask(long mask) {
        markDirty();
        var logic = getRecipeLogic();
        if (logic != null) {
            logic.resetRecipeLogic();
        }
        onModeMaskApplied();
    }

    protected void onModeMaskApplied() {}

    @Override
    public GTRecipeType[] getRecipeTypes() {
        GTRecipeType[] available = getAvailableRecipeTypes();
        if (!isModeSelectable()) {

            return available;
        }
        List<GTRecipeType> enabled = new ArrayList<>();
        for (int i = 0; i < available.length && i < Long.SIZE; i++) {
            if (isModeEnabled(i)) enabled.add(available[i]);
        }

        if (enabled.isEmpty()) return available;
        return enabled.toArray(new GTRecipeType[0]);
    }

    @Override
    public List<GTRecipeType> getRecipeTypeList() {
        List<GTRecipeType> list = new ArrayList<>();
        for (GTRecipeType type : getRecipeTypes()) list.add(type);
        return list;
    }

    @Override
    public GTRecipeType getRecipeType() {
        GTRecipeType[] types = getRecipeTypes();
        return types.length == 0 ? GTRecipeTypes.DUMMY_RECIPES : types[0];
    }

    @Override
    public BlockPos getHostPosition() {
        return hostPosition;
    }

    @Override
    public void setHostPosition(BlockPos pos) {
        this.hostPosition = pos;
        markDirty();
    }

    @Override
    public UniversalOrderKeeper getHost() {
        if (host == null && hostPosition != null && isRemote() && getLevel() != null) {
            try {
                if (MetaMachine.getMachine(getLevel(), hostPosition) instanceof UniversalOrderKeeper keeper) {
                    host = keeper;
                }
            } catch (Throwable ignored) {}
        }
        return host;
    }

    @Override
    public Class<UniversalOrderKeeper> getHostType() {
        return UniversalOrderKeeper.class;
    }

    @Override
    public BlockPos[] getHostScanPositions() {
        BlockPos[] result = new BlockPos[NORTH_COLUMNS.length * 4];
        int i = 0;
        for (int[] column : NORTH_COLUMNS) {
            for (int facing = 0; facing < 4; facing++) {
                int[] offset = gtlsupb$rotate(-column[0], -column[1], facing);
                result[i++] = getPos().offset(offset[0], 0, offset[1]);
            }
        }
        return result;
    }

    private static int[] gtlsupb$rotate(int right, int back, int facing) {
        return switch (facing) {
            case 1 -> new int[] { -back, right };
            case 2 -> new int[] { -right, -back };
            case 3 -> new int[] { back, -right };
            default -> new int[] { right, back };
        };
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new UniversalOrderModuleLogic(this);
    }

    public long getHostOutputMultiplier() {
        return host != null ? host.getOutputMultiplier() : 1L;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && isFormed() && host == null) {
            findAndConnectToHost();
        }
        gtlsupb$syncHostModes();
    }

    @Override
    public void setHost(UniversalOrderKeeper host) {
        this.host = host;
        gtlsupb$syncHostModes();
    }

    public void gtlsupb$syncHostModes() {
        StringBuilder sb = new StringBuilder();
        UniversalOrderKeeper connected = host;
        if (connected != null) {
            for (GTRecipeType type : connected.getRecognizedRecipeTypes()) {
                if (type == null || type.registryName == null) continue;
                if (sb.length() > 0) sb.append(';');
                sb.append(type.registryName);
            }
        }
        String csv = sb.toString();
        if (!csv.equals(hostModesCsv)) {
            hostModesCsv = csv;
            markDirty();
            if (!isRemote()) onChanged();
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!findAndConnectToHost()) {
            removeFromHost(this.host);
        }
        gtlsupb$syncHostModes();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        removeFromHost(this.host);
        gtlsupb$syncHostModes();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        removeFromHost(this.host);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);

        sideTabs.attachSubTab(new UniversalOrderModuleConfigurator(this));
        CombinedDirectionalFancyConfigurator directionalConfigurator = CombinedDirectionalFancyConfigurator.of(this, this);
        if (directionalConfigurator != null) {
            sideTabs.attachSubTab(directionalConfigurator);
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            UniversalOrderKeeper connected = host;
            if (connected != null) {
                textList.add(
                        Component.translatable(connected.isRunning() ? "gtlsupb.module.connected_to_host" : "gtlsupb.module.host_not_running")
                                .withStyle(connected.isRunning() ? ChatFormatting.GREEN : ChatFormatting.RED));
                textList.add(
                        Component.translatable(
                                "gtlsupb.module.universal_order.multiplier",
                                RainbowText.createRainbowComponent("×" + connected.getOutputMultiplier()))
                                .withStyle(ChatFormatting.GRAY));
            } else {
                textList.add(Component.translatable("gtlsupb.module.no_host").withStyle(ChatFormatting.RED));
            }
            addWorkingStatus(textList);
        } else {
            textList.add(Component.translatable("gtceu.multiblock.invalid_structure").withStyle(ChatFormatting.RED));
        }
        getDefinition().getAdditionalDisplay().accept(this, textList);
        getParts().forEach(part -> part.addMultiText(textList));
    }
}

class UniversalOrderModuleLogic extends ZeroEnergyMultiTypeLogic<UniversalOrderModuleBase> {

    private final UniversalOrderModuleBase module;

    UniversalOrderModuleLogic(UniversalOrderModuleBase machine) {
        super(machine);
        this.module = machine;
    }

    @Override
    public boolean checkBeforeWorking() {
        UniversalOrderKeeper host = module.getHost();
        return module.hasProxies() && host != null && host.isFormed();
    }

    @Override
    protected boolean requiresOutputs() {
        return false;
    }

    @Override
    public double getDynamicOutputMultiplier() {
        UniversalOrderKeeper host = module.getHost();
        return host != null ? (double) host.getOutputMultiplier() : 1.0d;
    }
}
