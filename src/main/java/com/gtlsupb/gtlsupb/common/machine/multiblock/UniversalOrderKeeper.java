package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineHost;
import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule;
import org.gtlcore.gtlcore.utils.datastructure.ModuleRenderInfo;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.gtlsupb.gtlsupb.api.gui.UniversalOrderModeConfigurator;
import com.gtlsupb.gtlsupb.common.data.machines.multiblock.UniversalOrderKeeperPattern;
import com.gtlsupb.gtlsupb.common.machine.GTLSupbMachines;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.ChronoDeductionArray;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.ChronoResonanceAmplifier;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.UniversalOrderModuleBase;
import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;
import com.gtlsupb.gtlsupb.utils.RainbowText;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UniversalOrderKeeper extends ZeroEnergyMultiTypeMachine
                                  implements IModularMachineHost<UniversalOrderKeeper>, IMachineLife {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            UniversalOrderKeeper.class,
            ZeroEnergyMultiTypeMachine.MANAGED_FIELD_HOLDER);

    public static final long OUTPUT_MULTIPLIER_PER_AMPLIFIER = 20L;

    public static final int MAX_MODULES = 16;

    public static final int MODE_SLOTS = 56;

    private static final int[][] NORTH_COLUMNS = UniversalOrderKeeperPattern.MODULE_SOCKET_OFFSETS;

    private static final GTRecipeType[] NO_TYPES = new GTRecipeType[0];

    private final Set<IModularMachineModule<UniversalOrderKeeper, ?>> modules = new ReferenceOpenHashSet<>();

    @Persisted
    private final NotifiableItemStackHandler modeSlot = new NotifiableItemStackHandler(this, MODE_SLOTS, IO.NONE, IO.BOTH);

    private String cachedFingerprint = "";

    private GTRecipeType[] cachedRecognized = NO_TYPES;

    private long cachedQuickHash;

    private ISubscription modeSlotSub;

    public UniversalOrderKeeper(IMachineBlockEntity holder) {
        super(holder, GTLSupbRecipeTypes.UNIVERSAL_ORDER_RECIPES);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new UniversalOrderHostLogic(this);
    }

    public NotifiableItemStackHandler getModeSlot() {
        return modeSlot;
    }

    public GTRecipeType[] getRecognizedRecipeTypes() {
        int slotCount = modeSlot.getSlots();
        long quickHash = 0L;
        for (int slot = 0; slot < slotCount; slot++) {
            ItemStack stack = modeSlot.getStackInSlot(slot);
            quickHash = quickHash * 31L + (stack.isEmpty() ? 0 : System.identityHashCode(stack.getItem()));
        }
        if (quickHash == cachedQuickHash) {
            return cachedRecognized;
        }
        cachedQuickHash = quickHash;

        StringBuilder fingerprint = new StringBuilder();
        List<GTRecipeType> merged = new ArrayList<>();
        for (int slot = 0; slot < modeSlot.getSlots(); slot++) {
            ItemStack stack = modeSlot.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();
            fingerprint.append(item).append(';');
            try {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                MachineDefinition definition = id == null ? null : GTRegistries.MACHINES.get(id);
                if (definition == null) continue;
                GTRecipeType[] types = definition.getRecipeTypes();
                if (types == null) continue;
                for (GTRecipeType type : types) {
                    if (type == null || type == org.gtlcore.gtlcore.common.data.GTLRecipeTypes.MOLECULAR_ASSEMBLER) continue;
                    if (!merged.contains(type)) merged.add(type);
                }
            } catch (Throwable ignored) {}
        }
        String key = fingerprint.toString();
        if (key.equals(cachedFingerprint)) {
            return cachedRecognized;
        }
        cachedFingerprint = key;
        cachedRecognized = merged.isEmpty() ? NO_TYPES : merged.toArray(new GTRecipeType[0]);
        return cachedRecognized;
    }

    public boolean isRunning() {
        return isFormed() && isWorkingEnabled();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        gtlsupb$ensureModeSlotSize();
        if (!isRemote()) {
            if (modeSlotSub == null) {
                modeSlotSub = modeSlot.addChangedListener(this::gtlsupb$onModeSlotChanged);
            }
            scanAndConnectModules();
        }
    }

    private void gtlsupb$onModeSlotChanged() {
        if (isRemote()) return;
        cachedQuickHash = Long.MIN_VALUE;
        cachedFingerprint = "";
        if (isFormed()) scanAndConnectModules();
        gtlsupb$syncConnectedModules();
        onChanged();
    }

    private void gtlsupb$syncConnectedModules() {
        for (IModularMachineModule<UniversalOrderKeeper, ?> module : modules) {
            if (module instanceof UniversalOrderModuleBase orderModule) {
                orderModule.gtlsupb$syncHostModes();
            }
        }
    }

    private void gtlsupb$ensureModeSlotSize() {
        try {
            int current = modeSlot.getSlots();
            if (current != MODE_SLOTS) {
                modeSlot.storage.setSize(MODE_SLOTS);
                if (!isRemote()) {
                    com.gtlsupb.gtlsupb.GTLSupb.LOGGER.info("[样板绑定] 模式识别栏位格数已修复：{} → {}（旧存档兼容）",
                            current, MODE_SLOTS);
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onUnload() {
        super.onUnload();
        safeClearModules();
        if (modeSlotSub != null) {
            modeSlotSub.unsubscribe();
            modeSlotSub = null;
        }
    }

    @Override
    public Set<IModularMachineModule<UniversalOrderKeeper, ?>> getModuleSet() {
        return modules;
    }

    @Override
    public int getMaxModuleCount() {
        return MAX_MODULES;
    }

    private int[] gtlsupb$rotate(int right, int back) {
        Direction facing = getFrontFacing();
        return switch (facing) {
            case EAST -> new int[] { -back, right };
            case SOUTH -> new int[] { -right, -back };
            case WEST -> new int[] { back, -right };
            default -> new int[] { right, back };
        };
    }

    @Override
    public BlockPos[] getModuleScanPositions() {
        BlockPos[] result = new BlockPos[NORTH_COLUMNS.length];
        for (int i = 0; i < NORTH_COLUMNS.length; i++) {
            int[] rotated = gtlsupb$rotate(NORTH_COLUMNS[i][0], NORTH_COLUMNS[i][1]);
            result[i] = getPos().offset(rotated[0], 0, rotated[1]);
        }
        return result;
    }

    @Override
    public List<ModuleRenderInfo> getModulesForRendering() {
        List<ModuleRenderInfo> list = new ArrayList<>(NORTH_COLUMNS.length);
        Direction hostFront = getFrontFacing();
        Direction hostUp = getUpwardsFacing();
        for (int i = 0; i < NORTH_COLUMNS.length; i++) {
            int[] rotated = gtlsupb$rotate(NORTH_COLUMNS[i][0], NORTH_COLUMNS[i][1]);
            MultiblockMachineDefinition moduleDefinition = (i % 2 == 0) ? GTLSupbMachines.CHRONO_DEDUCTION_ARRAY : GTLSupbMachines.CHRONO_RESONANCE_AMPLIFIER;
            list.add(new ModuleRenderInfo(
                    new BlockPos(rotated[0], 0, rotated[1]),
                    hostFront, hostUp, hostFront, hostUp, moduleDefinition));
        }
        return list;
    }

    public int countModules(Class<?> moduleClass) {
        int count = 0;
        for (IModularMachineModule<UniversalOrderKeeper, ?> module : modules) {
            if (moduleClass.isInstance(module) && module.isFormed()) count++;
        }
        return count;
    }

    public int getAmplifierCount() {
        int count = 0;
        for (IModularMachineModule<UniversalOrderKeeper, ?> module : modules) {
            if (module instanceof ChronoResonanceAmplifier amplifier && amplifier.isFormed() && amplifier.isAmplifying()) {
                count++;
            }
        }
        return count;
    }

    public int getDeductionCount() {
        return countModules(ChronoDeductionArray.class);
    }

    public long getOutputMultiplier() {
        return 1L + (long) getAmplifierCount() * OUTPUT_MULTIPLIER_PER_AMPLIFIER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        safeClearModules();
        scanAndConnectModules();
        gtlsupb$syncConnectedModules();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        safeClearModules();
    }

    @Override
    public void onMachineRemoved() {
        if (isRemote() || getLevel() == null) return;
        for (int i = 0; i < modeSlot.getSlots(); i++) {
            ItemStack stack = modeSlot.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            Block.popResource(getLevel(), getPos(), stack.copy());
        }
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);
        sideTabs.attachSubTab(new UniversalOrderModeConfigurator(this));
        CombinedDirectionalFancyConfigurator directionalConfigurator = CombinedDirectionalFancyConfigurator.of(this, this);
        if (directionalConfigurator != null) {
            sideTabs.attachSubTab(directionalConfigurator);
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            textList.add(
                    Component.translatable("gtlsupb.machine.universal_order_keeper.modules",
                            RainbowText.createRainbowComponent(getDeductionCount() + " / " + getAmplifierCount()),
                            RainbowText.createRainbowComponent("×" + getOutputMultiplier()))
                            .withStyle(ChatFormatting.GRAY));
        } else {
            textList.add(
                    Component.translatable("gtceu.multiblock.invalid_structure")
                            .withStyle(Style.EMPTY
                                    .withColor(ChatFormatting.RED)
                                    .withHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                                                    .withStyle(ChatFormatting.GRAY)))));
        }
        getDefinition().getAdditionalDisplay().accept(this, textList);
        getParts().forEach(part -> part.addMultiText(textList));
    }
}

class UniversalOrderHostLogic extends ZeroEnergyMultiTypeLogic<UniversalOrderKeeper> {

    UniversalOrderHostLogic(UniversalOrderKeeper machine) {
        super(machine);
    }

    @Override
    protected boolean requiresOutputs() {
        return false;
    }

    @Override
    public double getDynamicOutputMultiplier() {
        return (double) getMachine().getOutputMultiplier();
    }
}
