package com.gtlsupb.gtlsupb.common.machine.multiblock.part;

import org.gtlcore.gtlcore.api.gui.MEPatternCatalystUIManager;
import org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient;
import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;
import org.gtlcore.gtlcore.integration.ae2.widget.AEPatternViewExtendSlotWidget;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.utils.ResearchManager;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.EncodedPatternItem;
import com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper;
import com.gtlsupb.gtlsupb.api.machine.IPatternAssemblyHatch;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class PatternAssemblyPartMachine extends MEPatternBufferPartMachine
                                        implements IPatternAssemblyHatch,
                                        appeng.api.networking.crafting.ICraftingRequester {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            PatternAssemblyPartMachine.class,
            MEPatternBufferPartMachine.MANAGED_FIELD_HOLDER);

    private final int uiWidth;
    private final int uiHeight;
    private final int patternsPerRow;
    private final int totalPatterns;

    @Persisted
    @DescSynced
    public int activeRecipeTypeIndex = 0;

    @Persisted
    @DescSynced
    public int patternScale = 1;

    @Persisted
    @DescSynced
    public int patternDivScale = 1;

    @Persisted
    private boolean gtlsupb$autoNameFromHostMode = true;

    @Persisted
    private String gtlsupb$lastAutoName = "";

    private int gtlsupb$autoNameTicker;

    @Persisted
    @DescSynced
    private int gtlsupb$recognizedOutputMultiplier = 1;

    @Persisted
    private boolean gtlsupb$multiplierDetected = false;

    @Persisted
    @DescSynced
    private String gtlsupb$packRecipeType = "";

    private static final int AUTO_NAME_REFRESH_TICKS = 20;
    private static final int VISIBLE_ROWS = 5;

    public PatternAssemblyPartMachine(IMachineBlockEntity holder) {
        this(holder, 10, 10);
    }

    public PatternAssemblyPartMachine(IMachineBlockEntity holder, int patternsPerRow, int rows) {
        super(holder, patternsPerRow * rows, IO.BOTH);
        this.patternsPerRow = patternsPerRow;
        this.totalPatterns = patternsPerRow * rows;
        this.uiWidth = Math.max(patternsPerRow * 18 + 16, 106);
        this.uiHeight = VISIBLE_ROWS * 18 + 28;
        getMainNode().addService(appeng.api.networking.crafting.ICraftingRequester.class, this);
    }

    private enum CatalystBacklogState {

        ORDERING,

        BLOCKED,

        MANUAL
    }

    private static final class CatalystBacklogEntry {

        CatalystBacklogState state = CatalystBacklogState.ORDERING;
        String note = "";
        long blockedUntil;
        long requestedAt;

        int failures;
    }

    private final java.util.Map<appeng.api.stacks.AEKey, CatalystBacklogEntry> gtlsupb$catalystBacklog = new java.util.LinkedHashMap<>();

    private final java.util.Map<appeng.api.stacks.AEKey, appeng.api.networking.crafting.ICraftingLink> gtlsupb$catalystLinks = new java.util.HashMap<>();

    private final java.util.Map<appeng.api.stacks.AEKey, java.util.concurrent.Future<appeng.api.networking.crafting.ICraftingPlan>> gtlsupb$catalystPlans = new java.util.HashMap<>();

    private boolean gtlsupb$catalystScanRequested = true;

    private static final long CATALYST_ORDER_TIMEOUT_MS = 15_000L;

    private static final long CATALYST_RETRY_BASE_MS = 30_000L;

    private static final long CATALYST_RETRY_MAX_MS = 600_000L;

    private static final int CATALYST_ORDER_BATCH = 2;

    private boolean gtlsupb$isCatalystOrderBlocked(appeng.api.stacks.AEKey key) {
        if (key == null) return false;
        CatalystBacklogEntry entry = gtlsupb$catalystBacklog.get(key);
        if (entry == null) return false;
        if (entry.state == CatalystBacklogState.ORDERING) return false;
        return System.currentTimeMillis() < entry.blockedUntil;
    }

    private static long gtlsupb$catalystRetryDelay(int failures) {
        int shift = Math.min(Math.max(failures - 1, 0), 5);
        return Math.min(CATALYST_RETRY_BASE_MS << shift, CATALYST_RETRY_MAX_MS);
    }

    private void gtlsupb$noteCatalystBacklog(appeng.api.stacks.AEKey key, CatalystBacklogState state, String note) {
        if (key == null) return;
        CatalystBacklogEntry entry = gtlsupb$catalystBacklog.computeIfAbsent(key, k -> new CatalystBacklogEntry());
        entry.state = state;
        entry.note = note == null ? "" : note;
        if (state == CatalystBacklogState.ORDERING) {
            entry.requestedAt = System.currentTimeMillis();
            entry.failures = 0;
        } else {
            entry.failures = Math.min(entry.failures + 1, 8);
            entry.blockedUntil = System.currentTimeMillis() + gtlsupb$catalystRetryDelay(entry.failures);
        }
    }

    private boolean gtlsupb$beginCatalystOrder(appeng.api.stacks.AEKey key) {
        if (key == null || getGrid() == null || getLevel() == null) return false;
        if (gtlsupb$catalystLinks.containsKey(key) || gtlsupb$catalystPlans.containsKey(key)) return true;

        if (gtlsupb$isCatalystOrderBlocked(key)) return false;
        try {
            var craftingService = getGrid().getCraftingService();
            if (craftingService == null) return false;

            boolean exactPattern = craftingService.canEmitFor(key);
            var source = appeng.api.networking.security.IActionSource.ofMachine(this);
            gtlsupb$catalystPlans.put(key, craftingService.beginCraftingCalculation(getLevel(), () -> source, key, 1L,
                    appeng.api.networking.crafting.CalculationStrategy.CRAFT_LESS));
            gtlsupb$noteCatalystBacklog(key, CatalystBacklogState.ORDERING, exactPattern ?
                    "已按独立任务优先下单（等待产出）" : "已按独立任务优先下单（仅有模糊/替代样板，等待计划计算）");
            return true;
        } catch (Throwable t) {
            gtlsupb$noteCatalystBacklog(key, CatalystBacklogState.BLOCKED, "下单异常：" + t.getClass().getSimpleName());
            return false;
        }
    }

    private void gtlsupb$abandonCatalystOrder(appeng.api.stacks.AEKey key, boolean cancelLink, String reason) {
        if (key == null) return;
        try {
            if (cancelLink) {
                var link = gtlsupb$catalystLinks.remove(key);
                if (link != null && !link.isDone() && !link.isCanceled()) link.cancel();
            }
        } catch (Throwable ignored) {}
        gtlsupb$catalystPlans.remove(key);
        gtlsupb$noteCatalystBacklog(key, CatalystBacklogState.BLOCKED, reason);
    }

    private void gtlsupb$orderCatalystsFirstForPattern(IPatternDetails patternDetails, int slot) {
        if (isRemote() || slot < 0) return;
        try {
            var needed = patternCatalystKeys(patternDetails, slot);
            if (needed.isEmpty()) return;
            for (appeng.api.stacks.AEKey key : needed.keySet()) {
                if (key == null) continue;
                if (slot < catalystItems.length && hasCatalystSupplier(slot, key)) continue;
                if (gtlsupb$catalystLinks.containsKey(key) || gtlsupb$catalystPlans.containsKey(key)) continue;
                gtlsupb$beginCatalystOrder(key);
            }
        } catch (Throwable ignored) {

        }
    }

    private void gtlsupb$tickCatalystRequests() {
        if (isRemote()) return;

        if (!gtlsupb$catalystScanRequested && gtlsupb$catalystBacklog.isEmpty() &&
                gtlsupb$catalystLinks.isEmpty() && gtlsupb$catalystPlans.isEmpty()) {
            return;
        }
        gtlsupb$catalystScanRequested = false;
        long now = System.currentTimeMillis();
        var grid = getGrid();
        var craftingService = grid == null ? null : grid.getCraftingService();

        java.util.Set<appeng.api.stacks.AEKey> pending = gtlsupb$collectPendingCatalystKeys();

        if (!gtlsupb$catalystBacklog.isEmpty()) {
            var backlogIt = gtlsupb$catalystBacklog.entrySet().iterator();
            while (backlogIt.hasNext()) {
                var entry = backlogIt.next();
                appeng.api.stacks.AEKey key = entry.getKey();
                if (pending.contains(key)) continue;
                backlogIt.remove();
                gtlsupb$catalystPlans.remove(key);
                try {
                    var link = gtlsupb$catalystLinks.remove(key);
                    if (link != null && !link.isDone() && !link.isCanceled()) link.cancel();
                } catch (Throwable ignored) {}
            }
        }

        if (!gtlsupb$catalystLinks.isEmpty()) {
            var it = gtlsupb$catalystLinks.entrySet().iterator();
            while (it.hasNext()) {
                var entry = it.next();
                appeng.api.stacks.AEKey key = entry.getKey();
                var link = entry.getValue();
                if (link == null || link.isDone() || link.isCanceled()) {
                    it.remove();
                    gtlsupb$catalystBacklog.remove(key);
                    continue;
                }
                if (!pending.contains(key)) {
                    it.remove();
                    gtlsupb$catalystBacklog.remove(key);
                    try {
                        link.cancel();
                    } catch (Throwable ignored) {}
                    continue;
                }
                var order = gtlsupb$catalystBacklog.get(key);
                if (order != null && order.requestedAt > 0L && now - order.requestedAt > CATALYST_ORDER_TIMEOUT_MS) {
                    gtlsupb$abandonCatalystOrder(key, true, "超时未产出，已释放网络预留");
                }
            }
        }

        if (!gtlsupb$catalystPlans.isEmpty()) {
            var iterator = gtlsupb$catalystPlans.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                appeng.api.stacks.AEKey key = entry.getKey();
                var future = entry.getValue();
                if (future == null) {
                    iterator.remove();
                    gtlsupb$noteCatalystBacklog(key, CatalystBacklogState.BLOCKED, "计划不可用，已释放网络预留");
                    continue;
                }
                if (!future.isDone()) {
                    var order = gtlsupb$catalystBacklog.get(key);
                    if (order != null && order.requestedAt > 0L && now - order.requestedAt > CATALYST_ORDER_TIMEOUT_MS) {
                        iterator.remove();
                        gtlsupb$abandonCatalystOrder(key, false, "计划计算超时，已释放网络预留");
                    }
                    continue;
                }
                iterator.remove();
                try {
                    var plan = future.get();

                    if (plan == null || plan.missingItems() == null || !plan.missingItems().isEmpty()) {
                        gtlsupb$abandonCatalystOrder(key, false, "可产出但原料不足，需先补齐原料或手动放入");
                        continue;
                    }
                    if (craftingService == null) continue;

                    var result = craftingService.submitJob(plan, this, null, true,
                            appeng.api.networking.security.IActionSource.ofMachine(this));
                    if (result != null && result.successful() && result.link() != null) {
                        gtlsupb$catalystLinks.put(key, result.link());
                        gtlsupb$noteCatalystBacklog(key, CatalystBacklogState.ORDERING, "已提交，等待产出");
                    } else {
                        gtlsupb$abandonCatalystOrder(key, false, "AE 拒绝提交合成单");
                    }
                } catch (Throwable t) {
                    gtlsupb$abandonCatalystOrder(key, false, "提交异常：" + t.getClass().getSimpleName());
                }
            }
        }

        int issued = 0;
        for (appeng.api.stacks.AEKey key : pending) {
            if (issued >= CATALYST_ORDER_BATCH) break;
            if (gtlsupb$catalystLinks.containsKey(key) || gtlsupb$catalystPlans.containsKey(key)) continue;
            var order = gtlsupb$catalystBacklog.get(key);
            if (order != null && order.state != CatalystBacklogState.ORDERING && now < order.blockedUntil) continue;
            if (gtlsupb$beginCatalystOrder(key)) issued++;
        }
    }

    private java.util.Set<appeng.api.stacks.AEKey> gtlsupb$collectPendingCatalystKeys() {
        java.util.Set<appeng.api.stacks.AEKey> pending = new java.util.LinkedHashSet<>();
        try {
            var inventory = getPatternInventory();

            if (inventory.getSlots() <= 0) return pending;
            appeng.api.storage.MEStorage storage = null;
            appeng.api.networking.security.IActionSource source = null;
            try {
                if (getGrid() != null && getGrid().getStorageService() != null) {
                    storage = getGrid().getStorageService().getInventory();
                    source = appeng.api.networking.security.IActionSource.ofMachine(this);
                }
            } catch (Throwable ignored) {}
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                net.minecraft.world.item.ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                for (appeng.api.stacks.AEKey key : patternCatalystKeysFromStack(stack).keySet()) {
                    if (key == null || pending.contains(key)) continue;
                    if (slot < catalystItems.length && hasCatalystSupplier(slot, key)) continue;
                    if (storage != null) {
                        try {
                            if (storage.extract(key, 1L, appeng.api.config.Actionable.SIMULATE, source) >= 1L) {
                                continue;
                            }
                        } catch (Throwable ignored) {}
                    }
                    pending.add(key);
                }
            }
        } catch (Throwable ignored) {}
        return pending;
    }

    public java.util.List<String> gtlsupb$describeCatalystBacklog() {
        java.util.List<String> out = new java.util.ArrayList<>();
        try {
            for (var entry : gtlsupb$catalystBacklog.entrySet()) {
                var order = entry.getValue();
                out.add(entry.getKey().getDisplayName().getString() + " -> " + order.state + "（" + order.note + "）");
            }
        } catch (Throwable ignored) {}
        return out;
    }

    @Override
    public com.google.common.collect.ImmutableSet<appeng.api.networking.crafting.ICraftingLink> getRequestedJobs() {
        if (gtlsupb$catalystLinks.isEmpty()) return com.google.common.collect.ImmutableSet.of();
        return com.google.common.collect.ImmutableSet.copyOf(gtlsupb$catalystLinks.values());
    }

    @Override
    public long insertCraftedItems(appeng.api.networking.crafting.ICraftingLink link,
                                   appeng.api.stacks.AEKey what, long amount,
                                   appeng.api.config.Actionable mode) {
        if (mode == appeng.api.config.Actionable.SIMULATE) return amount;
        try {
            long inserted = refundCatalystToNetwork(what, amount);
            if (inserted > 0L) gtlsupb$requestCatalystRefresh();
            return Math.max(inserted, 0L);
        } catch (Throwable ignored) {
            return 0L;
        }
    }

    @Override
    public void jobStateChange(appeng.api.networking.crafting.ICraftingLink link) {
        if (link == null) return;
        gtlsupb$catalystLinks.entrySet().removeIf(e -> e.getValue() == link);
        gtlsupb$requestCatalystRefresh();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        gtlsupb$requestCatalystRefresh();

        if (!isRemote()) gtlsupb$refreshCatalystMaps();
    }

    private void gtlsupb$refreshCatalystMaps() {
        for (int i = 0; i < catalystItems.length; i++) {
            try {
                catalystItems[i].onContentsChanged();
            } catch (Throwable ignored) {}
            try {
                catalystFluids[i].onContentsChanged();
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void onMainNodeStateChanged(appeng.api.networking.IGridNodeListener.State state) {
        super.onMainNodeStateChanged(state);
        gtlsupb$requestCatalystRefresh();
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
    }

    private IMultiController gtlsupb$findHostController() {
        try {
            for (IMultiController controller : getControllers()) {
                if (controller != null && controller.isFormed()) {
                    return controller;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public void gtlsupb$nameByActiveMode() {
        gtlsupb$applyHostModeName(false);
    }

    public void gtlsupb$nameByAllModes() {
        gtlsupb$applyHostModeName(true);
    }

    private void gtlsupb$applyHostModeName(boolean allModes) {
        if (isRemote()) return;
        IMultiController host = gtlsupb$findHostController();
        if (host == null) return;
        String generated = gtlsupb$buildHostModeName(host, allModes);
        if (generated.isEmpty()) return;
        gtlsupb$lastAutoName = generated;
        super.setCustomName(generated);
        markDirty();
    }

    private String gtlsupb$buildHostModeName(IMultiController host, boolean allModes) {
        String hostName = "";
        StringBuilder modes = new StringBuilder();
        try {
            if (host instanceof MetaMachine meta && meta.getDefinition() != null) {
                hostName = Component.translatable(meta.getDefinition().getDescriptionId()).getString();
            }
        } catch (Throwable ignored) {}
        try {
            if (host instanceof IRecipeLogicMachine logicMachine) {
                if (allModes) {
                    GTRecipeType[] types = logicMachine.getRecipeTypes();
                    if (types != null) {
                        for (GTRecipeType type : types) {
                            if (type == null || type.registryName == null) continue;
                            String name = Component.translatable(type.registryName.toLanguageKey()).getString();
                            if (name.isEmpty()) continue;
                            if (modes.length() > 0) modes.append('/');
                            modes.append(name);
                        }
                    }
                } else {
                    GTRecipeType type = logicMachine.getRecipeType();
                    if (type != null && type.registryName != null) {
                        modes.append(Component.translatable(type.registryName.toLanguageKey()).getString());
                    }
                }
            }
        } catch (Throwable ignored) {}
        String modeName = modes.toString();
        if (hostName.isEmpty()) return modeName;
        if (modeName.isEmpty()) return hostName;
        return Component.translatable("gtlsupb.pattern_assembly.auto_name", hostName, modeName).getString();
    }

    public void gtlsupb$setCustomNameServer(String customName) {
        gtlsupb$applyManualCustomName(customName);
    }

    private void gtlsupb$setCustomNameClient(String customName) {
        gtlsupb$applyManualCustomName(customName);
        com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler.CHANNEL.sendToServer(
                new com.gtlsupb.gtlsupb.network.C2SPatternCustomNamePacket(getPos(), customName));
    }

    private void gtlsupb$applyManualCustomName(String customName) {
        String normalized = customName == null ? "" : customName.trim();
        super.setCustomName(normalized);
        gtlsupb$lastAutoName = normalized;
        markDirty();
    }

    public String gtlsupb$getPackRecipeType() {
        return gtlsupb$packRecipeType == null ? "" : gtlsupb$packRecipeType;
    }

    public void gtlsupb$setPackRecipeType(String value) {
        this.gtlsupb$packRecipeType = value == null ? "" : value;
        markDirty();
    }

    public void gtlsupb$cutToTagFiltered(net.minecraft.nbt.CompoundTag tag, String recipeType) {
        if (recipeType == null || recipeType.isEmpty()) {
            cutToTag(tag);
            return;
        }
        var inventory = getPatternInventory();
        int slots = inventory.getSlots();
        net.minecraft.world.item.ItemStack[] keptPatterns = new net.minecraft.world.item.ItemStack[slots];
        net.minecraft.nbt.CompoundTag[][] keptItems = new net.minecraft.nbt.CompoundTag[slots][];
        net.minecraft.nbt.CompoundTag[] keptFluids = new net.minecraft.nbt.CompoundTag[slots];
        byte[] keptCounts = new byte[slots];
        for (int i = 0; i < slots; i++) {
            net.minecraft.world.item.ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty() || !gtlsupb$matchesPackRecipeType(stack, recipeType)) {
                keptPatterns[i] = stack.isEmpty() ? net.minecraft.world.item.ItemStack.EMPTY : stack.copy();
                keptItems[i] = new net.minecraft.nbt.CompoundTag[catalystItems[i].getSlots()];
                for (int j = 0; j < catalystItems[i].getSlots(); j++) {
                    keptItems[i][j] = catalystItems[i].getStackInSlot(j).save(new net.minecraft.nbt.CompoundTag());
                }
                keptFluids[i] = catalystFluids[i].serializeNBT();
                keptCounts[i] = cacheRecipeCount[i];
                inventory.setStackInSlot(i, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
        cutToTag(tag);
        for (int i = 0; i < slots; i++) {
            if (keptPatterns[i] == null) continue;
            inventory.setStackInSlot(i, keptPatterns[i]);
            for (int j = 0; j < catalystItems[i].getSlots(); j++) {
                catalystItems[i].setStackInSlot(j,
                        net.minecraft.world.item.ItemStack.of(keptItems[i][j]));
            }
            catalystFluids[i].deserializeNBT(keptFluids[i]);
            cacheRecipeCount[i] = keptCounts[i];
            catalystItems[i].onContentsChanged();
            catalystFluids[i].onContentsChanged();
        }
        markDirty();
    }

    private boolean gtlsupb$matchesPackRecipeType(net.minecraft.world.item.ItemStack stack, String recipeType) {
        if (stack.getTag() == null) return false;
        net.minecraft.nbt.ListTag types = stack.getTag().getCompound("gtlcore")
                .getList("patternQuickUploadRecipeTypes", Tag.TAG_STRING);
        for (int i = 0; i < types.size(); i++) {
            if (recipeType.equals(types.getString(i))) return true;
        }
        return false;
    }

    private Component gtlsupb$packModeName(String recipeType) {
        if (recipeType == null || recipeType.isEmpty()) {
            return Component.translatable("gtlsupb.pattern_assembly.pack_mode_all");
        }
        return Component.translatable(recipeType.replace(':', '.'));
    }

    private ButtonWidget gtlsupb$packModeButton(String recipeType, int row, int width) {
        String shown = gtlsupb$packModeName(recipeType).getString();
        return new ButtonWidget(0, row * 18, width, 16,
                new GuiTextureGroup(GuiTextures.BUTTON,
                        new TextTexture(() -> gtlsupb$getPackRecipeType().equals(recipeType) ?
                                "§a" + shown : shown)),
                clickData -> {
                    gtlsupb$setPackRecipeType(recipeType);
                    com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler.CHANNEL.sendToServer(
                            new com.gtlsupb.gtlsupb.network.C2SPatternAssemblyConfigPacket(
                                    getPos(), com.gtlsupb.gtlsupb.network.C2SPatternAssemblyConfigPacket.ACTION_SET_PACK_MODE,
                                    recipeType));
                });
    }

    private List<String> gtlsupb$availablePackRecipeTypes() {
        List<String> result = new ArrayList<>();
        var inventory = getPatternInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty() || stack.getTag() == null) continue;
            net.minecraft.nbt.CompoundTag gtlcore = stack.getTag().getCompound("gtlcore");
            net.minecraft.nbt.ListTag types = gtlcore.getList("patternQuickUploadRecipeTypes", Tag.TAG_STRING);
            for (int j = 0; j < types.size(); j++) {
                String value = types.getString(j);
                if (!value.isEmpty() && !result.contains(value)) result.add(value);
            }
        }
        return result;
    }

    private boolean gtlsupb$detectingMultiplier = false;

    public int gtlsupb$getRecognizedOutputMultiplier() {
        return gtlsupb$recognizedOutputMultiplier;
    }

    public void gtlsupb$detectHostMultiplierOnce() {
        if (gtlsupb$detectingMultiplier) return;
        gtlsupb$detectingMultiplier = true;
        int current = 1;
        for (Object controller : getControllers()) {
            current = Math.max(current,
                    com.gtlsupb.gtlsupb.integration.ae2.PatternOutputMultiplierRegistry.multiplierOf(controller));
        }
        gtlsupb$recognizedOutputMultiplier = current;
        gtlsupb$multiplierDetected = true;
        invalidatePatternCache();
        getSlotBoundRecipes();
        markDirty();

        refreshAllByProduct();

        var inventory = getPatternInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                super.onPatternChange(i);
            }
        }

        try {
            appeng.api.networking.crafting.ICraftingProvider.requestUpdate(getMainNode());
        } catch (Throwable ignored) {}
        gtlsupb$detectingMultiplier = false;
    }

    private static final int PUSH_MAX_RETRIES = 5;

    private static final java.util.Set<String> gtlsupb$pushIssueLogged = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    private void gtlsupb$logPushIssue(appeng.api.crafting.IPatternDetails pattern, String reason) {
        try {
            String key = gtlsupb$patternName(pattern) + '|' + reason;
            if (!gtlsupb$pushIssueLogged.add(key)) return;
            com.gtlsupb.gtlsupb.GTLSupb.LOGGER.warn("[样板总成] 推料未落地：样板={} 原因={}", gtlsupb$patternName(pattern), reason);
        } catch (Throwable ignored) {}
    }

    private void gtlsupb$ensurePatternRecognized(appeng.api.crafting.IPatternDetails pattern) {
        try {
            appeng.api.crafting.IPatternDetails actual = pattern instanceof com.gtlsupb.gtlsupb.integration.ae2.CatalystPatternWrapper wrapper ?
                    wrapper.gtlsupb$unwrap() : pattern;
            int slot = findSlotForPattern(actual);
            if (slot >= 0) onPatternChange(slot);
        } catch (Throwable ignored) {}
    }

    private static String gtlsupb$patternName(Object pattern) {
        try {
            return String.valueOf(((appeng.api.crafting.IPatternDetails) pattern).getPrimaryOutput());
        } catch (Throwable ignored) {
            return String.valueOf(pattern);
        }
    }

    private boolean gtlsupb$anyControllerActive() {
        try {
            for (var controller : getControllers()) {
                if (controller instanceof com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine rlm) {
                    var logic = rlm.getRecipeLogic();
                    if (logic != null && logic.isActive()) return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private int gtlsupb$catalystRefreshCursor;

    private static final int CATALYST_REFRESH_BATCH = 8;

    private boolean gtlsupb$hasAnyCatalystStack(int slot) {
        try {
            if (slot < 0 || slot >= catalystItems.length) return false;
            for (int i = 0; i < catalystItems[slot].getSlots(); i++) {
                if (!catalystItems[slot].getStackInSlot(i).isEmpty()) return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private boolean gtlsupb$hasAnyPattern() {
        try {
            var inventory = getPatternInventory();
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                if (!inventory.getStackInSlot(slot).isEmpty()) return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private boolean gtlsupb$directExecutor = false;

    private static final int DIRECT_EXECUTE_INTERVAL = 20;

    private int gtlsupb$directExecuteTicker = 0;

    private net.minecraft.world.item.ItemStack gtlsupb$lastExecuted = net.minecraft.world.item.ItemStack.EMPTY;

    public boolean gtlsupb$isDirectExecutor() {
        return gtlsupb$directExecutor;
    }

    public void gtlsupb$setDirectExecutor(boolean enabled) {
        gtlsupb$directExecutor = enabled;
        markDirty();
        onChanged();
    }

    private void gtlsupb$directExecuteTick() {
        var level = getLevel();
        if (level == null || level.isClientSide) return;
        var boundRecipes = getSlotBoundRecipes();
        if (boundRecipes == null || boundRecipes.isEmpty()) return;

        var controller = getControllers().stream()
                .filter(c -> c instanceof com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine)
                .map(c -> (com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine) c)
                .findFirst().orElse(null);
        if (controller == null) return;

        for (var entry : boundRecipes.int2ObjectEntrySet()) {
            GTRecipe recipe = entry.getValue();
            if (recipe == null) continue;

            for (int round = 0; round < 64; round++) {
                try {

                    if (!org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.matchRecipeInput(controller, recipe)) break;

                    if (!org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.handleRecipeInput(controller, recipe)) break;

                    org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.handleRecipeOutput(controller, recipe);
                } catch (Throwable ignored) {
                    break;
                }
            }
        }
    }

    private int[] gtlsupb$catalystSlotHashes = new int[0];

    private int gtlsupb$catalystSweepRound;

    private static final int CATALYST_FORCE_SWEEP_ROUNDS = 4;

    private void gtlsupb$refreshCatalystSupply() {
        gtlsupb$tickCatalystRequests();
        var inventory = getPatternInventory();
        int slots = inventory.getSlots();
        if (slots <= 0) return;

        if (++gtlsupb$catalystSweepRound >= CATALYST_FORCE_SWEEP_ROUNDS) {
            gtlsupb$catalystSweepRound = 0;
            java.util.Arrays.fill(gtlsupb$catalystSlotHashes, 0);
        }
        int batch = Math.min(slots, CATALYST_REFRESH_BATCH);
        for (int n = 0; n < batch; n++) {
            int slot = gtlsupb$catalystRefreshCursor % slots;
            gtlsupb$catalystRefreshCursor = (gtlsupb$catalystRefreshCursor + 1) % slots;
            try {
                net.minecraft.world.item.ItemStack stack = inventory.getStackInSlot(slot);

                if (stack.isEmpty() && !gtlsupb$hasAnyCatalystStack(slot)) continue;

                int slotHash = stack.isEmpty() ? 0 : stack.hashCode();
                if (slotHash != 0) {
                    if (gtlsupb$catalystSlotHashes.length < slots) {
                        gtlsupb$catalystSlotHashes = java.util.Arrays.copyOf(gtlsupb$catalystSlotHashes, slots);
                    }
                    if (gtlsupb$catalystSlotHashes[slot] == slotHash) continue;
                    gtlsupb$catalystSlotHashes[slot] = slotHash;
                }
                java.util.Set<appeng.api.stacks.AEKey> needed = patternCatalystKeysFromStack(stack).keySet();

                gtlsupb$pruneCatalystMarkers(slot, needed);
                if (stack.isEmpty()) continue;

                for (appeng.api.stacks.AEKey key : needed) {
                    ensureCatalystResident(slot, key);
                }
            } catch (Throwable ignored) {}
        }
    }

    private void gtlsupb$pruneCatalystMarkers(int slot, java.util.Set<appeng.api.stacks.AEKey> needed) {
        if (slot < 0 || slot >= catalystItems.length) return;
        try {
            boolean changed = false;
            for (int i = 0; i < catalystItems[slot].getSlots(); i++) {
                net.minecraft.world.item.ItemStack stack = catalystItems[slot].getStackInSlot(i);
                if (stack.isEmpty()) continue;
                if (!com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper.isVirtualMarker(stack)) continue;
                appeng.api.stacks.AEKey payload = com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper
                        .readVirtualMarkerPayload(stack);
                if (payload != null && needed != null && needed.contains(payload)) continue;
                catalystItems[slot].setStackInSlot(i, net.minecraft.world.item.ItemStack.EMPTY);
                changed = true;
            }
            if (changed) catalystItems[slot].onContentsChanged();
        } catch (Throwable ignored) {}

        gtlsupb$invalidateStaleRecipeOnBindingChange();
    }

    private long gtlsupb$lastBindingFingerprint;

    private void gtlsupb$invalidateStaleRecipeOnBindingChange() {
        if (isRemote()) return;
        try {
            long fingerprint = patternFingerprint();
            if (!gtlsupb$bindingFingerprintSeen) {

                gtlsupb$bindingFingerprintSeen = true;
                gtlsupb$lastBindingFingerprint = fingerprint;
                return;
            }
            if (fingerprint == gtlsupb$lastBindingFingerprint) return;
            gtlsupb$lastBindingFingerprint = fingerprint;
            Object controllerObj = getControllers().stream().findFirst().orElse(null);
            if (controllerObj instanceof IRecipeLogicMachine controller) {
                var logic = controller.getRecipeLogic();
                if (logic != null) {
                    logic.markLastRecipeDirty();

                    logic.updateTickSubscription();
                }
            }
        } catch (Throwable ignored) {}
    }

    private boolean gtlsupb$bindingFingerprintSeen;

    public void gtlsupb$notifyPatternChanged(int index) {
        onPatternChange(index);
    }

    public GTRecipeType[] gtlsupb$getHostRecipeTypes() {
        var controller = getControllers().stream().findFirst().orElse(null);
        if (controller instanceof com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine logicMachine) {
            return logicMachine.getRecipeTypes();
        }
        return new GTRecipeType[0];
    }

    protected void onPatternChange(int index) {
        if (!isRemote() && !gtlsupb$multiplierDetected && !gtlsupb$detectingMultiplier) {
            gtlsupb$detectHostMultiplierOnce();
        }
        super.onPatternChange(index);
        if (isRemote()) return;

        gtlsupb$fingerprintMemoTick = Long.MIN_VALUE;
        gtlsupb$fingerprintMemoLevel = null;

        gtlsupb$catalystScanRequested = true;
        gtlsupb$requestCatalystRefresh();
        if (index < 0 || index >= catalystItems.length) return;
        try {
            var inventory = getPatternInventory();
            net.minecraft.world.item.ItemStack stack = index < inventory.getSlots() ?
                    inventory.getStackInSlot(index) : net.minecraft.world.item.ItemStack.EMPTY;

            if (gtlsupb$hasAnyCatalystStack(index)) {
                gtlsupb$pruneCatalystMarkers(index, patternCatalystKeysFromStack(stack).keySet());
            }
        } catch (Throwable ignored) {}
    }

    private boolean gtlsupb$catalystTaskScheduled;

    private void gtlsupb$requestCatalystRefreshDelayed() {
        if (isRemote() || gtlsupb$catalystTaskScheduled) return;
        if (!(getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        gtlsupb$catalystTaskScheduled = true;
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                serverLevel.getServer().getTickCount() + 20,
                this::gtlsupb$runCatalystRefresh));
    }

    private boolean gtlsupb$hasPendingCatalystWork() {
        return gtlsupb$catalystScanRequested || !gtlsupb$catalystBacklog.isEmpty() ||
                !gtlsupb$catalystLinks.isEmpty() || !gtlsupb$catalystPlans.isEmpty();
    }

    private void gtlsupb$requestCatalystRefresh() {
        if (isRemote() || gtlsupb$catalystTaskScheduled) return;
        if (!(getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        gtlsupb$catalystTaskScheduled = true;
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                serverLevel.getServer().getTickCount() + 1,
                this::gtlsupb$runCatalystRefresh));
    }

    private void gtlsupb$runCatalystRefresh() {
        gtlsupb$catalystTaskScheduled = false;
        if (isRemote() || isInValid()) return;
        if (getMainNode() == null || !getMainNode().isOnline()) return;
        gtlsupb$tickCatalystRequests();
        if (!gtlsupb$hasAnyPattern()) return;
        gtlsupb$refreshCatalystSupply();
        if (gtlsupb$hasPendingCatalystWork()) gtlsupb$requestCatalystRefreshDelayed();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);

        configuratorPanel.attachConfigurators(
                new com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator() {

                    @Override
                    public Component getTitle() {
                        return Component.translatable("gtlsupb.pattern_assembly.scale_config");
                    }

                    @Override
                    public com.lowdragmc.lowdraglib.gui.texture.IGuiTexture getIcon() {
                        return new ResourceTexture("expatternprovider:textures/item/pattern_modifier.png");
                    }

                    @Override
                    public com.lowdragmc.lowdraglib.gui.widget.Widget createConfigurator() {
                        var scaleWidget = new IntInputWidget(4, 14, 52, 14, () -> patternScale, (value) -> {
                            patternScale = value != null ? Math.max(value, 1) : 1;
                        });
                        scaleWidget.setMin(1);
                        scaleWidget.setMax(100);

                        var divWidget = new IntInputWidget(62, 14, 52, 14, () -> patternDivScale, (value) -> {
                            patternDivScale = value != null ? Math.max(value, 1) : 1;
                        });
                        divWidget.setMin(1);
                        divWidget.setMax(100);

                        return new WidgetGroup(0, 0, 118, 54)
                                .addWidget(new LabelWidget(4, 2, Component.translatable("gtlsupb.pattern_assembly.scale_multiply_short").getString()))
                                .addWidget(scaleWidget.setHoverTooltips(Component.translatable("gtlsupb.pattern_assembly.scale_multiply")))
                                .addWidget(new LabelWidget(62, 2, Component.translatable("gtlsupb.pattern_assembly.scale_divide_short").getString()))
                                .addWidget(divWidget.setHoverTooltips(Component.translatable("gtlsupb.pattern_assembly.scale_divide")))

                                .addWidget(
                                        new ButtonWidget(62, 34, 52, 14,
                                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("✔")),
                                                (clickData) -> applyPatternScale())
                                                .setHoverTooltips(Component.translatable("gtlsupb.pattern_assembly.apply_tip")));
                    }
                });

        configuratorPanel.attachConfigurators(
                new com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator() {

                    @Override
                    public Component getTitle() {
                        return Component.translatable("gtlsupb.pattern_assembly.multiplier_config");
                    }

                    @Override
                    public com.lowdragmc.lowdraglib.gui.texture.IGuiTexture getIcon() {
                        return new ResourceTexture("gtlsupb:textures/gui/icon/wireless_on.png");
                    }

                    @Override
                    public com.lowdragmc.lowdraglib.gui.widget.Widget createConfigurator() {
                        return new WidgetGroup(0, 0, 148, 38)
                                .addWidget(new LabelWidget(4, 2, () -> Component.translatable(
                                        "gtlsupb.pattern_assembly.host_multiplier",
                                        gtlsupb$recognizedOutputMultiplier).getString()))
                                .addWidget(new ButtonWidget(4, 18, 140, 16,
                                        new GuiTextureGroup(GuiTextures.BUTTON,
                                                new TextTexture(Component.translatable(
                                                        "gtlsupb.pattern_assembly.detect_multiplier").getString())),
                                        clickData -> com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler.CHANNEL
                                                .sendToServer(new com.gtlsupb.gtlsupb.network.C2SPatternAssemblyConfigPacket(
                                                        getPos(), com.gtlsupb.gtlsupb.network.C2SPatternAssemblyConfigPacket.ACTION_DETECT_MULTIPLIER, "")))
                                        .setHoverTooltips(Component.translatable("gtlsupb.pattern_assembly.detect_multiplier_tip")));
                    }
                });

        configuratorPanel.attachConfigurators(
                new com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator() {

                    @Override
                    public Component getTitle() {
                        return Component.translatable("gtlsupb.pattern_assembly.pack_config");
                    }

                    @Override
                    public com.lowdragmc.lowdraglib.gui.texture.IGuiTexture getIcon() {
                        return new ResourceTexture("expatternprovider:textures/item/pattern_modifier.png");
                    }

                    @Override
                    public com.lowdragmc.lowdraglib.gui.widget.Widget createConfigurator() {
                        var group = new WidgetGroup(0, 0, 148, 108);
                        var list = new com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup(4, 16, 140, 52)
                                .setYScrollBarWidth(6)
                                .setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON);
                        final String[] scanStatus = {
                                Component.translatable("gtlsupb.pattern_assembly.pack_scan_ready").getString()
                        };
                        group.addWidget(new LabelWidget(4, 2, () -> Component.translatable(
                                "gtlsupb.pattern_assembly.pack_mode",
                                gtlsupb$packModeName(gtlsupb$getPackRecipeType())).getString()));
                        group.addWidget(list);
                        group.addWidget(new LabelWidget(4, 69, () -> scanStatus[0]));
                        group.addWidget(new ButtonWidget(4, 82, 140, 12,
                                new GuiTextureGroup(GuiTextures.BUTTON,
                                        new TextTexture(Component.translatable(
                                                "gtlsupb.pattern_assembly.pack_scan").getString())),
                                clickData -> {
                                    java.util.List<String> modes = gtlsupb$availablePackRecipeTypes();
                                    if (!modes.contains(gtlsupb$getPackRecipeType())) {
                                        gtlsupb$setPackRecipeType("");
                                        com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler.CHANNEL.sendToServer(
                                                new com.gtlsupb.gtlsupb.network.C2SPatternAssemblyConfigPacket(
                                                        getPos(), com.gtlsupb.gtlsupb.network.C2SPatternAssemblyConfigPacket.ACTION_SET_PACK_MODE,
                                                        ""));
                                    }
                                    list.clearAllWidgets();
                                    list.addWidget(gtlsupb$packModeButton("", 0, 140));
                                    for (int i = 0; i < modes.size(); i++) {
                                        list.addWidget(gtlsupb$packModeButton(modes.get(i), i + 1, 140));
                                    }
                                    list.setScrollYOffset(0);
                                    scanStatus[0] = Component.translatable(
                                            "gtlsupb.pattern_assembly.pack_scan_done", modes.size()).getString();
                                }));
                        group.addWidget(new ButtonWidget(4, 96, 140, 12,
                                new GuiTextureGroup(GuiTextures.BUTTON,
                                        new TextTexture(Component.translatable(
                                                "gtlsupb.pattern_assembly.pack").getString())),
                                clickData -> com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler.CHANNEL
                                        .sendToServer(new com.gtlsupb.gtlsupb.network.C2SPatternPackPacket(
                                                getPos(), gtlsupb$getPackRecipeType())))
                                .setHoverTooltips(Component.translatable("gtlsupb.pattern_assembly.pack.tip")));

                        return group;
                    }
                });
    }

    private void applyPatternScale() {
        net.minecraft.world.level.Level level = getLevel();
        if (level == null) return;
        var inventory = getPatternInventory();
        Ae2PatternHelper.ModifyMode mode = Ae2PatternHelper.ModifyMode.OUTPUT;

        Int2ObjectOpenHashMap<ItemStack> newPatternMap = new Int2ObjectOpenHashMap<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack itemStack = inventory.getStackInSlot(slot);
            if (itemStack.isEmpty()) continue;

            var aePattern = Ae2PatternHelper.decode(itemStack, level);
            if (aePattern == null) continue;

            ItemStack result = Ae2PatternHelper.modifyScale(aePattern, patternScale, false,
                    (long) Integer.MAX_VALUE, (long) Integer.MAX_VALUE, mode);
            if (result == null) continue;

            if (patternDivScale > 1) {
                var secondPattern = Ae2PatternHelper.decode(result, level);
                if (secondPattern == null) continue;
                result = Ae2PatternHelper.modifyScale(secondPattern, patternDivScale, true,
                        (long) Integer.MAX_VALUE, (long) Integer.MAX_VALUE, mode);
                if (result == null) continue;
            }

            Ae2PatternHelper.copyRecipeBinding(itemStack, result);

            newPatternMap.put(slot, result);
        }

        for (var entry : newPatternMap.int2ObjectEntrySet()) {
            inventory.extractItem(entry.getIntKey(), 1, false);
            inventory.insertItem(entry.getIntKey(), entry.getValue(), false);
        }
    }

    @Override
    public void onPatternInserted(int slot, ItemStack stack) {
        onPatternChange(slot);
    }

    @Override
    public List<GTRecipeType> getAvailableRecipeTypes() {
        Object controller = getControllers().stream().findFirst().orElse(null);
        if (!(controller instanceof IRecipeLogicMachine)) {
            return Collections.emptyList();
        }
        return Arrays.asList(((IRecipeLogicMachine) controller).getRecipeTypes());
    }

    @Override
    public int getActiveRecipeTypeIndex() {
        return this.activeRecipeTypeIndex;
    }

    @Override
    public void setActiveRecipeTypeIndex(int index) {
        this.activeRecipeTypeIndex = index;
    }

    @Override
    public GTRecipeType getActiveRecipeType() {
        List<GTRecipeType> types = getAvailableRecipeTypes();
        if (activeRecipeTypeIndex >= 0 && activeRecipeTypeIndex < types.size()) {
            return types.get(activeRecipeTypeIndex);
        }
        if (!types.isEmpty()) {
            return types.get(0);
        }
        return null;
    }

    private net.minecraft.world.level.Level cachedPatternLevel;
    private long cachedPatternFingerprint;
    private List<GTRecipe> cachedBoundRecipes;
    private it.unimi.dsi.fastutil.ints.Int2ObjectMap<GTRecipe> cachedSlotBoundRecipes;

    private long gtlsupb$fingerprintMemoTick = Long.MIN_VALUE;
    private long gtlsupb$fingerprintMemoValue;
    private net.minecraft.world.level.Level gtlsupb$fingerprintMemoLevel;

    private int cachedPatternUnresolved;

    private long cachedPatternTick = Long.MIN_VALUE;

    private String cachedPatternUnresolvedDigest;

    private static final long FINGERPRINT_MEMO_TICKS = 40L;

    private long patternFingerprint() {
        Level level = getLevel();

        if (level != null && level == gtlsupb$fingerprintMemoLevel &&
                level.getGameTime() - gtlsupb$fingerprintMemoTick < FINGERPRINT_MEMO_TICKS) {
            return gtlsupb$fingerprintMemoValue;
        }
        var inventory = getPatternInventory();
        long h = 0L;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                h = h * 31L + slot * 1000003L + stack.hashCode();
            }
        }
        if (level != null) {
            gtlsupb$fingerprintMemoLevel = level;
            gtlsupb$fingerprintMemoTick = level.getGameTime();
            gtlsupb$fingerprintMemoValue = h;
        }
        return h;
    }

    private boolean patternCacheValid() {
        if (cachedPatternLevel != getLevel() || cachedPatternFingerprint != patternFingerprint()) return false;

        if (cachedPatternUnresolved > 0) {
            var level = getLevel();
            if (level == null) return false;
            return level.getGameTime() - cachedPatternTick < 100L;
        }
        return true;
    }

    private void invalidatePatternCache() {
        cachedPatternLevel = null;
        cachedBoundRecipes = null;
        cachedSlotBoundRecipes = null;
        cachedPatternUnresolved = 0;
        cachedPatternTick = Long.MIN_VALUE;
        cachedPatternUnresolvedDigest = null;
        wrappedPatterns = null;
        wrappedPatternsFingerprint = Long.MIN_VALUE;

        gtlsupb$fingerprintMemoTick = Long.MIN_VALUE;
        gtlsupb$fingerprintMemoLevel = null;
    }

    private void gtlsupb$wakeControllers() {
        try {

            gtlsupb$stockedCache = null;
            for (Object controllerObj : getControllers()) {
                if (controllerObj instanceof IRecipeLogicMachine controller) {
                    var logic = controller.getRecipeLogic();
                    if (logic != null) {
                        logic.markLastRecipeDirty();
                        logic.updateTickSubscription();
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public List<GTRecipe> getBoundRecipes() {
        if (cachedBoundRecipes != null && cachedSlotBoundRecipes != null && patternCacheValid()) {
            return cachedBoundRecipes;
        }
        it.unimi.dsi.fastutil.ints.Int2ObjectMap<GTRecipe> map = getSlotBoundRecipes();
        if (map.isEmpty()) return java.util.Collections.emptyList();
        List<GTRecipe> bound = new java.util.ArrayList<>(map.size());
        for (GTRecipe recipe : map.values()) {
            if (recipe != null) bound.add(recipe);
        }
        cachedBoundRecipes = bound;
        return bound;
    }

    @Override
    public it.unimi.dsi.fastutil.ints.Int2ObjectMap<GTRecipe> getSlotBoundRecipes() {
        if (patternCacheValid() && cachedSlotBoundRecipes != null) {
            int cachedMultiplier = 1;
            for (Object controllerObj : getControllers()) {
                cachedMultiplier = Math.max(cachedMultiplier,
                        com.gtlsupb.gtlsupb.integration.ae2.PatternOutputMultiplierRegistry.multiplierOf(controllerObj));
            }
            for (GTRecipe recipe : cachedSlotBoundRecipes.values()) {
                com.gtlsupb.gtlsupb.integration.ae2.PatternOutputMultiplierRegistry.register(recipe, cachedMultiplier);
            }
            return cachedSlotBoundRecipes;
        }
        it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<GTRecipe> map = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>();
        List<GTRecipeType> boundTypes = new java.util.ArrayList<>();
        List<String> unresolved = new java.util.ArrayList<>();
        Level level = getLevel();
        if (level != null) {
            var inventory = getPatternInventory();
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                var recipe = Ae2PatternHelper.readBindingRecipe(stack, level);
                if (recipe != null) {
                    map.put(slot, recipe);
                } else {

                    String declaredId = gtlsupb$declaredBindingId(stack);
                    if (declaredId != null && !declaredId.isEmpty()) {
                        unresolved.add(declaredId + '@' + slot);
                    }
                }

                GTRecipeType boundType = Ae2PatternHelper.readBindingRecipeType(stack, level);
                if (boundType != null && !boundTypes.contains(boundType)) boundTypes.add(boundType);
            }
        }
        int outputMultiplier = 1;
        for (Object controllerObj : getControllers()) {
            outputMultiplier = Math.max(outputMultiplier,
                    com.gtlsupb.gtlsupb.integration.ae2.PatternOutputMultiplierRegistry.multiplierOf(controllerObj));
        }
        for (GTRecipe recipe : map.values()) {
            com.gtlsupb.gtlsupb.integration.ae2.PatternOutputMultiplierRegistry.register(recipe, outputMultiplier);
        }

        cachedPatternLevel = level;
        cachedPatternFingerprint = patternFingerprint();
        cachedSlotBoundRecipes = map;
        cachedSlotBoundTypes = boundTypes;
        cachedBoundRecipes = null;
        cachedPatternUnresolved = unresolved.size();
        cachedPatternTick = level == null ? Long.MIN_VALUE : level.getGameTime();
        if (unresolved.isEmpty()) {
            cachedPatternUnresolvedDigest = null;
        } else {
            StringBuilder sb = new StringBuilder();
            int cap = Math.min(8, unresolved.size());
            for (int i = 0; i < cap; i++) {
                if (i > 0) sb.append(", ");
                sb.append(unresolved.get(i));
            }
            if (unresolved.size() > cap) sb.append(" …(共").append(unresolved.size()).append(')');
            String digest = sb.toString();
            if (!digest.equals(cachedPatternUnresolvedDigest)) {
                cachedPatternUnresolvedDigest = digest;
                com.gtlsupb.gtlsupb.GTLSupb.LOGGER.warn(
                        "[样板绑定] 绑定配方未解析（机器将\"没有合成配方\"）：{} —— 可能配方尚未加载/已被改名；每 5 秒自动重试",
                        digest);
            }
        }
        return map;
    }

    private static String gtlsupb$declaredBindingId(ItemStack stack) {
        try {
            var tag = stack.getTag();
            if (tag == null) return null;
            if (tag.contains("gtlsupb.recipe_id")) return tag.getString("gtlsupb.recipe_id");
            if (tag.contains("gtlsupb")) return tag.getCompound("gtlsupb").getString("recipe_id");
        } catch (Throwable ignored) {}
        return null;
    }

    private List<GTRecipeType> cachedSlotBoundTypes;

    @Override
    public List<GTRecipeType> getBoundRecipeTypes() {
        if (!patternCacheValid() || cachedSlotBoundTypes == null) {
            getSlotBoundRecipes();
        }
        return cachedSlotBoundTypes == null ? Collections.emptyList() : cachedSlotBoundTypes;
    }

    private java.util.Map<Integer, GTRecipe> gtlsupb$stockedCache;

    private long gtlsupb$stockedCacheFingerprint = Long.MIN_VALUE;

    private long gtlsupb$stockedCacheTick = Long.MIN_VALUE;

    private boolean gtlsupb$stockedDegraded;

    @Override
    public java.util.Map<Integer, GTRecipe> getStockedBoundRecipes() {
        try {
            Level level = getLevel();
            if (level != null && gtlsupb$stockedCache != null) {
                if (gtlsupb$stockedCacheFingerprint == patternFingerprint() && level.getGameTime() - gtlsupb$stockedCacheTick < 10L) {
                    return java.util.Collections.unmodifiableMap(gtlsupb$stockedCache);
                }
            }
        } catch (Throwable ignored) {}
        java.util.Map<Integer, GTRecipe> stocked = gtlsupb$computeStockedBoundRecipes();
        try {
            Level level = getLevel();
            if (level != null) {
                gtlsupb$stockedCache = stocked;
                gtlsupb$stockedCacheFingerprint = patternFingerprint();
                gtlsupb$stockedCacheTick = level.getGameTime();
            }
        } catch (Throwable ignored) {}
        return new java.util.LinkedHashMap<>(stocked);
    }

    @Override
    public boolean isStockedByFallback() {
        return gtlsupb$stockedDegraded;
    }

    private java.util.Map<Integer, GTRecipe> gtlsupb$computeStockedBoundRecipes() {
        java.util.Map<Integer, GTRecipe> stocked = new java.util.LinkedHashMap<>();
        try {
            for (var entry : getSlotBoundRecipes().int2ObjectEntrySet()) {
                GTRecipe recipe = entry.getValue();
                if (recipe == null) continue;
                if (getSlotBoundRecipeParallel(entry.getIntKey(), recipe, Long.MAX_VALUE) > 0L) {
                    stocked.put(entry.getIntKey(), recipe);
                }
            }
        } catch (Throwable ignored) {}

        gtlsupb$stockedDegraded = false;
        if (stocked.isEmpty()) {
            for (var entry : getSlotBoundRecipes().int2ObjectEntrySet()) {
                if (entry.getValue() != null) stocked.put(entry.getIntKey(), entry.getValue());
            }

            gtlsupb$stockedDegraded = !stocked.isEmpty();
        }
        return stocked;
    }

    @Override
    public long getSlotBoundRecipeParallel(int slot, GTRecipe recipe, long limit) {
        var pair = getMERecipeHandlerTraits();
        if (pair == null) return 0L;
        long p = limit;

        java.util.List<Content> itemInputsProp = recipe.inputs.get(ItemRecipeCapability.CAP);
        if (itemInputsProp != null && !itemInputsProp.isEmpty()) {
            Object2LongMap<Ingredient> need = buildItemNeed(itemInputsProp);
            if (!need.isEmpty()) {
                Object2LongMap<ItemStack> stock = pair.left().getSingleSlotStackMap(slot);
                for (var needed : Object2LongMaps.fastIterable(need)) {
                    long neededAmount = needed.getLongValue();
                    if (neededAmount <= 0) continue;
                    long avail = 0L;
                    for (var it = Object2LongMaps.fastIterator(stock); it.hasNext();) {
                        var st = it.next();
                        if (needed.getKey().test(st.getKey())) {
                            avail = st.getLongValue();
                            break;
                        }
                    }
                    if (avail <= 0L) return 0L;
                    p = Math.min(p, avail / neededAmount);
                }
            }
        }

        java.util.List<Content> fluidInputsProp = recipe.inputs.get(FluidRecipeCapability.CAP);
        if (fluidInputsProp != null && !fluidInputsProp.isEmpty()) {
            Object2LongMap<FluidIngredient> need = buildFluidNeed(fluidInputsProp);
            if (!need.isEmpty()) {
                Object2LongMap<FluidStack> stock = pair.right().getSingleSlotStackMap(slot);
                for (var needed : Object2LongMaps.fastIterable(need)) {
                    long neededAmount = needed.getLongValue();
                    if (neededAmount <= 0) continue;
                    long avail = 0L;
                    for (var it = Object2LongMaps.fastIterator(stock); it.hasNext();) {
                        var st = it.next();
                        if (needed.getKey().test(st.getKey())) {
                            avail = st.getLongValue();
                            break;
                        }
                    }
                    if (avail <= 0L) return 0L;
                    p = Math.min(p, avail / neededAmount);
                }
            }
        }
        return p;
    }

    @Override
    public boolean verifySlotBoundRecipes(Int2ObjectMap<GTRecipe> paralleledBySlot) {
        return handleSlotBoundRecipes(paralleledBySlot, true);
    }

    @Override
    public boolean commitSlotBoundRecipes(Int2ObjectMap<GTRecipe> paralleledBySlot) {
        boolean committed = handleSlotBoundRecipes(paralleledBySlot, false);
        if (committed) {

            gtlsupb$stockedCache = null;
        }
        return committed;
    }

    private boolean handleSlotBoundRecipes(Int2ObjectMap<GTRecipe> paralleledBySlot, boolean simulate) {
        var pair = getMERecipeHandlerTraits();
        if (pair == null) return false;
        var itemHandler = pair.left();
        var fluidHandler = pair.right();
        for (var entry : Int2ObjectMaps.fastIterable(paralleledBySlot)) {
            int slot = entry.getIntKey();
            GTRecipe recipe = entry.getValue();

            java.util.List<Content> itemInputsProp = recipe.inputs.get(ItemRecipeCapability.CAP);
            if (itemInputsProp != null && !itemInputsProp.isEmpty()) {
                ObjectArrayList<Object> itemContent = new ObjectArrayList<>();
                for (Content c : itemInputsProp) {
                    if (c.chance <= 0) continue;
                    itemContent.add(ItemRecipeCapability.CAP.of(c.content));
                }
                if (!itemContent.isEmpty()) {
                    itemHandler.initMEHandleContents(recipe, itemContent, simulate);
                    if (!itemHandler.meHandleRecipe(recipe, simulate, slot)) return false;
                }
            }

            java.util.List<Content> fluidInputsProp = recipe.inputs.get(FluidRecipeCapability.CAP);
            if (fluidInputsProp != null && !fluidInputsProp.isEmpty()) {
                ObjectArrayList<Object> fluidContent = new ObjectArrayList<>();
                for (Content c : fluidInputsProp) {
                    if (c.chance <= 0) continue;
                    fluidContent.add(FluidRecipeCapability.CAP.of(c.content));
                }
                if (!fluidContent.isEmpty()) {
                    fluidHandler.initMEHandleContents(recipe, fluidContent, simulate);
                    if (!fluidHandler.meHandleRecipe(recipe, simulate, slot)) return false;
                }
            }
        }
        return true;
    }

    private static Object2LongMap<Ingredient> buildItemNeed(java.util.List<Content> contents) {
        Object2LongOpenHashMap<Ingredient> need = new Object2LongOpenHashMap<>();
        for (Content c : contents) {
            if (c.chance <= 0) continue;
            Ingredient ing = ItemRecipeCapability.CAP.of(c.content);
            if (isCircuitIngredient(ing)) continue;
            long amount;
            if (ing instanceof LongIngredient longIngredient) amount = longIngredient.getActualAmount();
            else if (ing instanceof SizedIngredient sizedIngredient) amount = sizedIngredient.getAmount();
            else amount = 1L;
            need.addTo(ing, amount);
        }
        return need;
    }

    private static Object2LongMap<FluidIngredient> buildFluidNeed(java.util.List<Content> contents) {
        Object2LongOpenHashMap<FluidIngredient> need = new Object2LongOpenHashMap<>();
        for (Content c : contents) {
            if (c.chance <= 0) continue;
            FluidIngredient fluid = FluidRecipeCapability.CAP.of(c.content);
            long amount = fluid.getAmount();
            if (amount <= 0) continue;
            need.addTo(fluid, amount);
        }
        return need;
    }

    private static boolean isCircuitIngredient(Ingredient ing) {
        for (ItemStack stack : ing.getItems()) {
            if (IntCircuitBehaviour.isIntegratedCircuit(stack)) return true;
        }
        return false;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        boolean ok = gtlsupb$doPushPattern(patternDetails, inputHolder);
        for (int tries = 1; !ok && tries <= PUSH_MAX_RETRIES; tries++) {
            gtlsupb$ensurePatternRecognized(patternDetails);
            ok = gtlsupb$doPushPattern(patternDetails, inputHolder);
            if (ok) {
                com.gtlsupb.gtlsupb.GTLSupb.LOGGER.info("[样板总成] 推料重试第 {} 次成功：样板={}",
                        tries, gtlsupb$patternName(patternDetails));
            }
        }
        if (!ok) {
            gtlsupb$logPushIssue(patternDetails,
                    "重试 " + PUSH_MAX_RETRIES + " 次仍被拒（样板未识别或槽位不可用）→ 该批料未落地，请在 AE 终端重下");
        }
        return ok;
    }

    private boolean gtlsupb$doPushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (isRemote()) return super.pushPattern(patternDetails, inputHolder);

        IPatternDetails actual = patternDetails instanceof com.gtlsupb.gtlsupb.integration.ae2.CatalystPatternWrapper wrapper ?
                wrapper.gtlsupb$unwrap() : patternDetails;

        int slot = findSlotForPattern(actual);

        try {

            if (slot < 0) {
                com.gtlsupb.gtlsupb.GTLSupb.LOGGER.warn(
                        "[样板总成] 推料样板未映射到任何槽位（-1）：样板={} —— 本单不做催化剂供给，该配方会一直匹配不上",
                        gtlsupb$patternName(actual));
            }
        } catch (Throwable ignored) {}

        gtlsupb$orderCatalystsFirstForPattern(actual, slot);

        ensureCatalystsForPattern(actual, slot);

        virtualizePushedCatalysts(actual, slot, inputHolder);
        boolean result = super.pushPattern(actual, inputHolder);

        try {
            if (!result) {
                com.gtlsupb.gtlsupb.GTLSupb.LOGGER.warn("[样板总成] 推料被拒收：样板={} 识别槽位={}", gtlsupb$patternName(actual), slot);
            }
        } catch (Throwable ignored) {}
        if (result) {

            gtlsupb$wakeControllers();
        }

        return result;
    }

    private GTRecipeType findBoundRecipeType(List<GTRecipeType> types) {
        if (types == null || types.isEmpty()) return null;
        try {
            var inventory = getPatternInventory();
            Level level = getLevel();
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                GTRecipeType bound = Ae2PatternHelper.readBindingRecipeType(stack, level);
                if (bound != null && types.contains(bound)) return bound;
            }
        } catch (Throwable ignored) {}
        for (GTRecipe recipe : getBoundRecipes()) {
            if (recipe != null && recipe.recipeType != null && types.contains(recipe.recipeType)) {
                return recipe.recipeType;
            }
        }
        return null;
    }

    private void ensureCatalystsForPattern(IPatternDetails patternDetails, int slot) {
        if (slot < 0 || slot >= catalystItems.length) return;
        it.unimi.dsi.fastutil.objects.Object2LongMap<appeng.api.stacks.AEKey> needed = patternCatalystKeys(patternDetails, slot);
        for (var entry : it.unimi.dsi.fastutil.objects.Object2LongMaps.fastIterable(needed)) {
            ensureCatalystResident(slot, entry.getKey());
        }
    }

    private it.unimi.dsi.fastutil.objects.Object2LongMap<appeng.api.stacks.AEKey> patternCatalystKeys(
                                                                                                      IPatternDetails patternDetails, int slot) {
        net.minecraft.world.item.ItemStack patternStack = null;
        try {
            if (patternDetails != null) patternStack = patternDetails.getDefinition().toStack();
        } catch (Throwable ignored) {}

        if (!com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper.hasCustomKeys(patternStack) && slot >= 0) {
            var inventory = getPatternInventory();
            if (slot < inventory.getSlots()) {
                net.minecraft.world.item.ItemStack slotStack = inventory.getStackInSlot(slot);
                if (!slotStack.isEmpty()) patternStack = slotStack;
            }
        }
        return patternCatalystKeysFromStack(patternStack);
    }

    private it.unimi.dsi.fastutil.objects.Object2LongMap<appeng.api.stacks.AEKey> patternCatalystKeysFromStack(
                                                                                                               net.minecraft.world.item.ItemStack patternStack) {
        it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<appeng.api.stacks.AEKey> needed = new it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<>();
        if (patternStack == null || patternStack.isEmpty()) return needed;
        needed.putAll(com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper.collectCatalystKeysFromPattern(patternStack));
        net.minecraft.world.level.Level level = getLevel();
        if (level != null) {
            GTRecipe recipe = com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper.readBindingRecipe(patternStack, level);
            if (recipe != null) {
                needed.putAll(com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper.collectPatternCatalystKeys(recipe, patternStack, level));
            }
        }
        return needed;
    }

    private void ensureCatalystResident(int slot, appeng.api.stacks.AEKey key) {
        if (key == null || slot < 0 || slot >= catalystItems.length) return;
        if (!com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper.isVirtualSupplyAvailable()) return;
        if (getGrid() == null || getGrid().getStorageService() == null) return;
        try {
            long available = getGrid().getStorageService().getInventory().extract(key, 1L,
                    appeng.api.config.Actionable.SIMULATE,
                    appeng.api.networking.security.IActionSource.ofMachine(this));
            if (available < 1L) {

                gtlsupb$catalystScanRequested = true;
                gtlsupb$beginCatalystOrder(key);
                return;
            }
        } catch (Throwable ignored) {
            return;
        }
        if (hasCatalystSupplier(slot, key)) return;
        net.minecraft.world.item.ItemStack marker = com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper
                .createVirtualMarker(key);
        if (marker.isEmpty()) return;
        if (!storeCatalystItem(slot, marker)) return;

        gtlsupb$catalystScanRequested = true;
    }

    private boolean hasCatalystSupplier(int slot, appeng.api.stacks.AEKey key) {
        for (int i = 0; i < catalystItems[slot].getSlots(); i++) {
            net.minecraft.world.item.ItemStack stack = catalystItems[slot].getStackInSlot(i);
            if (stack.isEmpty()) continue;
            appeng.api.stacks.AEKey payload = com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper
                    .readVirtualMarkerPayload(stack);
            if (payload != null && payload.equals(key)) return true;
            if (payload == null && appeng.api.stacks.AEItemKey.of(stack).equals(key)) return true;
        }
        return false;
    }

    private void virtualizePushedCatalysts(IPatternDetails patternDetails, int slot, KeyCounter[] inputHolder) {
        if (inputHolder == null || slot < 0 || slot >= catalystItems.length) return;
        java.util.Set<appeng.api.stacks.AEKey> catalysts = patternCatalystKeys(patternDetails, slot).keySet();
        if (catalysts.isEmpty()) return;
        for (KeyCounter counter : inputHolder) {
            if (counter == null) continue;
            for (appeng.api.stacks.AEKey key : catalysts) {
                long amount = counter.get(key);
                if (amount <= 0L) continue;
                try {

                    if (!hasCatalystSupplier(slot, key)) {
                        net.minecraft.world.item.ItemStack marker = com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper
                                .createVirtualMarker(key);
                        if (marker.isEmpty() || !storeCatalystItem(slot, marker)) continue;
                        gtlsupb$catalystScanRequested = true;
                    }

                    long refunded = refundCatalystToNetwork(key, amount);
                    if (refunded > 0L) {
                        counter.remove(key, refunded);

                        com.gtlsupb.gtlsupb.GTLSupb.LOGGER.warn(
                                "[样板总成] 推料被判定为催化剂并退回 ME：槽位={} 键={} 退回={}（判定来源=样板绑定配方的\"不消耗输入\"；若这是该配方的消耗输入即误判）",
                                slot, key, refunded);
                    }
                } catch (Throwable ignored) {

                }
            }
        }
    }

    private long refundCatalystToNetwork(appeng.api.stacks.AEKey key, long amount) {
        try {
            if (key == null || amount <= 0L) return 0L;
            if (getGrid() == null || getGrid().getStorageService() == null || getGrid().getEnergyService() == null) {
                return 0L;
            }
            appeng.api.storage.MEStorage meInventory = getGrid().getStorageService().getInventory();
            appeng.api.networking.security.IActionSource actionSource = appeng.api.networking.security.IActionSource.ofMachine(this);
            return appeng.api.storage.StorageHelper.poweredInsert(getGrid().getEnergyService(), meInventory,
                    key, amount, actionSource);
        } catch (Throwable ignored) {
            return 0L;
        }
    }

    private boolean storeCatalystItem(int slot, net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (int i = 0; i < catalystItems[slot].getSlots(); i++) {
            if (!catalystItems[slot].getStackInSlot(i).isEmpty()) continue;
            catalystItems[slot].setStackInSlot(i, stack);

            catalystItems[slot].onContentsChanged();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    private boolean storeCatalystFluid(int slot, com.lowdragmc.lowdraglib.side.fluid.FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) return false;
        for (int i = 0; i < catalystFluids[slot].getTanks(); i++) {
            if (!catalystFluids[slot].getFluidInTank(i).isEmpty()) continue;
            catalystFluids[slot].setFluidInTank(i, fluid);
            catalystFluids[slot].onContentsChanged();
            return true;
        }
        return false;
    }

    private int findSlotForPattern(IPatternDetails patternDetails) {
        if (patternDetails == null) return -1;
        net.minecraft.world.item.ItemStack definition;
        try {
            definition = patternDetails.getDefinition().toStack();
        } catch (Throwable t) {
            return -1;
        }
        if (definition.isEmpty()) return -1;
        var inventory = getPatternInventory();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            net.minecraft.world.item.ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (net.minecraft.world.item.ItemStack.matches(stack, definition)) return slot;
        }
        if (!com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper.hasCustomKeys(definition)) return -1;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            net.minecraft.world.item.ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper.sameCustomKeys(stack, definition)) return slot;
        }
        return -1;
    }

    private List<IPatternDetails> wrappedPatterns;
    private long wrappedPatternsFingerprint;

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        List<IPatternDetails> originals = super.getAvailablePatterns();
        long fingerprint = patternFingerprint() * 31L + gtlsupb$getRecognizedOutputMultiplier();
        if (wrappedPatterns != null && wrappedPatternsFingerprint == fingerprint &&
                wrappedPatterns.size() == originals.size()) {
            return wrappedPatterns;
        }
        List<IPatternDetails> wrapped = new java.util.ArrayList<>(originals.size());
        for (IPatternDetails pattern : originals) {
            try {
                int slot = findSlotForPattern(pattern);
                java.util.Set<appeng.api.stacks.AEKey> catalysts = catalystKeysForPattern(pattern, slot);
                wrapped.add(catalysts.isEmpty() ? pattern :
                        new com.gtlsupb.gtlsupb.integration.ae2.CatalystPatternWrapper(pattern, catalysts,
                                getLevel(), key -> isCatalystSelfSupplied(slot, key),
                                this::gtlsupb$canAutoCraft));
            } catch (Throwable t) {

                wrapped.add(pattern);
            }
        }
        wrappedPatterns = wrapped;
        wrappedPatternsFingerprint = fingerprint;
        return wrapped;
    }

    private boolean gtlsupb$canAutoCraft(appeng.api.stacks.AEKey key) {
        if (key == null || getGrid() == null) return false;
        if (gtlsupb$isCatalystOrderBlocked(key)) return false;
        try {
            var craftingService = getGrid().getCraftingService();
            return craftingService != null && craftingService.canEmitFor(key);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean isCatalystSelfSupplied(int slot, appeng.api.stacks.AEKey key) {
        if (key == null) return false;
        if (slot >= 0 && slot < catalystItems.length && hasCatalystSupplier(slot, key)) return true;
        if (getGrid() == null || getGrid().getStorageService() == null) return false;
        try {
            return getGrid().getStorageService().getInventory().extract(key, 1L,
                    appeng.api.config.Actionable.SIMULATE,
                    appeng.api.networking.security.IActionSource.ofMachine(this)) >= 1L;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private java.util.Set<appeng.api.stacks.AEKey> catalystKeysForPattern(IPatternDetails patternDetails, int slot) {
        java.util.Set<appeng.api.stacks.AEKey> keys = new java.util.HashSet<>();
        net.minecraft.world.level.Level level = getLevel();
        if (patternDetails == null || level == null) return keys;
        net.minecraft.world.item.ItemStack patternStack = patternDetails.getDefinition().toStack();
        if (slot >= 0) {
            var inventory = getPatternInventory();
            if (slot < inventory.getSlots()) {
                net.minecraft.world.item.ItemStack slotStack = inventory.getStackInSlot(slot);
                if (!slotStack.isEmpty()) patternStack = slotStack;
            }
        }
        GTRecipe recipe = com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper.readBindingRecipe(patternStack, level);
        if (recipe != null) {
            keys.addAll(com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper
                    .collectPatternCatalystKeys(recipe, patternStack, level).keySet());
        }
        return keys;
    }

    @Override
    public InteractionResult onUse(
                                   BlockState state,
                                   Level world,
                                   BlockPos pos,
                                   Player player,
                                   InteractionHand hand,
                                   BlockHitResult hit) {
        net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return InteractionResult.PASS;

        if (stack.is(GTItems.TOOL_DATA_STICK.asItem())) {
            if (!world.isClientSide) {
                var researchData = ResearchManager.readResearchId(stack);
                if (researchData != null) {
                    return InteractionResult.PASS;
                }

                if (stack.getOrCreateTag().contains("pos", (int) Tag.TAG_INT_ARRAY)) {
                    stack.getOrCreateTag().remove("pos");
                }

                stack.getOrCreateTag().putIntArray("superPos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
                player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_copy_settings"));
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);
        sideTabs.attachSubTab(new NamePage(this));
    }

    private static class NamePage implements IFancyUIProvider {

        private final PatternAssemblyPartMachine machine;

        NamePage(PatternAssemblyPartMachine machine) {
            this.machine = machine;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gtlsupb.pattern_assembly.name_page");
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new com.lowdragmc.lowdraglib.gui.texture.ResourceTexture(
                    "gtlsupb:textures/gui/icon/pattern_name.png");
        }

        @Override
        public boolean hasPlayerInventory() {
            return true;
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            WidgetGroup group = new WidgetGroup(0, 0, 220, 64);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            group.addWidget(new LabelWidget(6, 6, "gtlsupb.pattern_assembly.name_page.hint"));
            group.addWidget(new TextFieldWidget(6, 26, 136, 16,
                    machine::getCustomName, value -> machine.gtlsupb$setCustomNameClient(value)));
            group.addWidget(new ButtonWidget(146, 26, 32, 16,
                    buttonTexture("gtlsupb.pattern_assembly.name.single"),
                    cd -> com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler.CHANNEL.sendToServer(
                            new com.gtlsupb.gtlsupb.network.C2SPatternNamePacket(machine.getPos(), false))));
            group.addWidget(new ButtonWidget(182, 26, 32, 16,
                    buttonTexture("gtlsupb.pattern_assembly.name.all"),
                    cd -> com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler.CHANNEL.sendToServer(
                            new com.gtlsupb.gtlsupb.network.C2SPatternNamePacket(machine.getPos(), true))));
            return group;
        }
    }

    private static IGuiTexture buttonTexture(String key) {
        return new GuiTextureGroup(GuiTextures.BUTTON,
                new TextTexture(Component.translatable(key).getString()));
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, uiWidth, uiHeight);

        group.addWidget(
                new LabelWidget(4, 3, () -> isOnline ?
                        Component.translatable("gtlsupb.gui.me_network.online").getString() :
                        Component.translatable("gtlsupb.gui.me_network.offline").getString())
                        .setHoverTooltips(Component.translatable(isOnline ?
                                "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline")));

        MEPatternCatalystUIManager catalystUIManager = new MEPatternCatalystUIManager(
                group.getSizeWidth() + 4,
                catalystItems,
                catalystFluids,
                cacheRecipeCount,
                (slot) -> this.removeSlotFromGTRecipeCache(slot));
        group.waitToAdded(catalystUIManager);

        var slotGroup = new DraggableScrollableWidgetGroup(0, 16, uiWidth, uiHeight - 28)
                .setYScrollBarWidth(6)
                .setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON)
                .setScrollWheelDirection(DraggableScrollableWidgetGroup.ScrollWheelDirection.VERTICAL);
        int slots = Math.min(totalPatterns, getPatternInventory().getSlots());
        for (int index = 0; index < slots; index++) {
            final int i = index;
            int col = i % patternsPerRow;
            int row = i / patternsPerRow;
            int x = uiWidth == 106 ? (106 - patternsPerRow * 18) / 2 + col * 18 : 8 + col * 18;
            int y = row * 18;

            var slotWidget = (AEPatternViewExtendSlotWidget) new AEPatternViewExtendSlotWidget(
                    getPatternInventory(), i, x, y) {

                @Override
                public boolean isVisible() {
                    return true;
                }
            }
                    .setOnPatternSlotChanged(() -> this.onPatternChange(i))
                    .setOccupiedTexture(GuiTextures.SLOT)
                    .setItemHook(stack -> {
                        if (!stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem pattern) {
                            var output = pattern.getOutput(stack);
                            if (!output.isEmpty()) return output;
                        }
                        return stack;
                    })
                    .setBackground(GuiTextures.SLOT, GuiTextures.PATTERN_OVERLAY);
            slotWidget.setOnMiddleClick(() -> catalystUIManager.toggleFor(i));
            slotWidget.setOnAddedTooltips((widget, tooltips) -> {
                if (cacheRecipe[i]) {
                    tooltips.add(Component.translatable("gtceu.machine.pattern.recipe.cache"));
                }
            });
            slotGroup.addWidget(slotWidget);
        }
        group.addWidget(slotGroup);

        return group;
    }
}
