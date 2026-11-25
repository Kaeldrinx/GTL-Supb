package com.gtlsupb.gtlsupb.common.machine.multiblock.part;

import org.gtlcore.gtlcore.api.gui.TagFilterConfigurator;
import org.gtlcore.gtlcore.api.machine.trait.MEStock.ExportOnlyAEConfigureFluidSlot;
import org.gtlcore.gtlcore.api.machine.trait.MEStock.ExportOnlyAEConfigureItemSlot;
import org.gtlcore.gtlcore.api.machine.trait.MEStock.IOptimizedMEList;
import org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient;
import org.gtlcore.gtlcore.client.gui.widget.AEDualConfigWidget;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.ICapabilityTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import com.gtlsupb.gtlsupb.client.gui.widget.BigIntegerMEOutListGridWidget;
import com.gtlsupb.gtlsupb.integration.ae2.MEBigIntegerTransfer;
import com.gtlsupb.gtlsupb.network.C2SIntegratedHatchPacket;
import com.gtlsupb.gtlsupb.network.C2SIntegratedRequesterPacket;
import com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class OverlimitIntegratedHatchPartMachine extends MultiblockPartMachine
                                                 implements IMachineLife, IGridConnectedMachine, IGridTickable,
                                                 IDistinctPart {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            OverlimitIntegratedHatchPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private long selectedCircuitsV2 = 0L;

    @Persisted
    @DescSynced
    private int outputFrequency = 20;

    @Persisted
    @DescSynced
    private int tagRefreshTicks = 20;

    @Persisted
    @DescSynced
    private int stockPages = 3;

    @Persisted
    @DescSynced
    private int tagPages = 3;

    @Persisted
    @DescSynced
    private boolean itemTagPullEnabled = false;

    @Persisted
    @DescSynced
    private boolean fluidTagPullEnabled = false;

    @Persisted
    @DescSynced
    private boolean outputToMENetwork = true;

    @Persisted
    @DescSynced
    private boolean requesterAutoMode = false;

    @Persisted
    @DescSynced
    private int requesterFrequency = 20;

    @Persisted
    @DescSynced
    private long requesterPerPull = 1L;

    @Persisted
    @DescSynced
    private long requesterPullCount = 1L;

    @DescSynced
    private String requesterSummary = "";

    @Persisted
    private final com.lowdragmc.lowdraglib.misc.ItemStackTransfer requesterPatterns = new com.lowdragmc.lowdraglib.misc.ItemStackTransfer(9);

    @Persisted
    private String itemTagWhite = "";

    @Persisted
    private String itemTagBlack = "";

    @Persisted
    private String fluidTagWhite = "";

    @Persisted
    private String fluidTagBlack = "";

    @Persisted
    private final GridNodeHolder nodeHolder = new GridNodeHolder(this);

    @DescSynced
    private boolean isOnline;

    private final IActionSource actionSource;
    private final Object2ObjectLinkedOpenHashMap<AEKey, BigInteger> outputBuffer = new Object2ObjectLinkedOpenHashMap<>();
    private final IntegratedItemOutputHandler itemOutputHandler;
    private final IntegratedFluidOutputHandler fluidOutputHandler;
    private final IntegratedCircuitHandler circuitHandler;
    private final IntegratedItemStockHandler itemStockHandler;
    private final IntegratedFluidStockHandler fluidStockHandler;
    private final IntegratedItemStockHandler itemTagHandler;
    private final IntegratedFluidStockHandler fluidTagHandler;
    private final RequestCacheItemHandler requesterItemCacheHandler;
    private final RequestCacheFluidHandler requesterFluidCacheHandler;
    private final Object2ObjectLinkedOpenHashMap<AEKey, BigInteger> requesterCache = new Object2ObjectLinkedOpenHashMap<>();

    private int refreshCounter;
    private int requesterCounter;
    private final java.util.Map<appeng.api.stacks.AEKey, java.util.concurrent.Future<appeng.api.networking.crafting.ICraftingPlan>> requesterPlans = new java.util.HashMap<>();

    public OverlimitIntegratedHatchPartMachine(IMachineBlockEntity holder) {
        super(holder);
        this.actionSource = IActionSource.ofMachine(nodeHolder.getMainNode()::getNode);
        itemOutputHandler = new IntegratedItemOutputHandler(this);
        fluidOutputHandler = new IntegratedFluidOutputHandler(this);
        circuitHandler = new IntegratedCircuitHandler(this);
        itemStockHandler = new IntegratedItemStockHandler(this, false);
        fluidStockHandler = new IntegratedFluidStockHandler(this, false);
        itemTagHandler = new IntegratedItemStockHandler(this, true);
        fluidTagHandler = new IntegratedFluidStockHandler(this, true);
        requesterItemCacheHandler = new RequestCacheItemHandler(this);
        requesterFluidCacheHandler = new RequestCacheFluidHandler(this);
        requesterPatterns.setFilter(stack -> stack.getItem() instanceof appeng.crafting.pattern.EncodedPatternItem);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public void setOnline(boolean online) {
        this.isOnline = online;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        IGridConnectedMachine.super.onMainNodeStateChanged(reason);
        if (isOnline()) gtlsupb$wakeNode();
    }

    @Override
    public @NotNull TickingRequest getTickingRequest(@NotNull appeng.api.networking.IGridNode node) {
        int frequency = Math.max(1, outputFrequency);
        return new TickingRequest(frequency, frequency, false, true);
    }

    @Override
    public @NotNull TickRateModulation tickingRequest(@NotNull appeng.api.networking.IGridNode node,
                                                      int ticksSinceLastCall) {
        if (!getMainNode().isReady() || !getMainNode().isOnline() || !getMainNode().isPowered()) {
            return TickRateModulation.SLEEP;
        }
        IGrid grid = getMainNode().getGrid();
        if (grid == null) return TickRateModulation.SLOWER;

        int interval = Math.max(1, tagRefreshTicks);
        if (++refreshCounter >= interval) {
            refreshCounter = 0;
            gtlsupb$syncStock(grid, itemStockHandler, fluidStockHandler);
            gtlsupb$refreshTagSlots(grid);
            gtlsupb$syncStock(grid, itemTagHandler, fluidTagHandler);
        }

        if (requesterAutoMode && ++requesterCounter >= Math.max(1, requesterFrequency)) {
            requesterCounter = 0;
            gtlsupb$runRequester(grid);
        }
        gtlsupb$tickRequesterPlans(grid);

        if (!outputToMENetwork || outputBuffer.isEmpty()) return TickRateModulation.SLOWER;
        boolean changed = false;
        for (var iterator = outputBuffer.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next();
            BigInteger inserted = MEBigIntegerTransfer.insert(grid, entry.getKey(), entry.getValue(), actionSource);
            if (inserted.signum() <= 0) continue;
            BigInteger left = entry.getValue().subtract(inserted);
            if (left.signum() <= 0) iterator.remove();
            else entry.setValue(left);
            changed = true;
        }
        if (changed) {
            markDirty();
            onChanged();
            gtlsupb$wakeNode();
            return TickRateModulation.URGENT;
        }
        return TickRateModulation.SLOWER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        getMainNode().addService(IGridTickable.class, this);
        itemStockHandler.onConfigChanged();
        fluidStockHandler.onConfigChanged();
        itemTagHandler.onConfigChanged();
        fluidTagHandler.onConfigChanged();
    }

    @Override
    public void onRotated(@NotNull Direction oldFacing, @NotNull Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
    }

    @Override
    public boolean isDistinct() {
        return true;
    }

    @Override
    public void setDistinct(boolean distinct) {}

    @Override
    public @NotNull Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 220, 200);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(new LabelWidget(6, 6, "gtlsupb.integrated_hatch.config.hint"));
        group.addWidget(new LabelWidget(6, 24, "gtlsupb.integrated_hatch.config.output_frequency"));
        group.addWidget(configInput(120, 22, () -> outputFrequency,
                value -> machineClientConfig(this, C2SIntegratedHatchPacket.ACTION_SET_OUTPUT_FREQUENCY, value)));
        group.addWidget(new LabelWidget(6, 42, "gtlsupb.integrated_hatch.config.tag_refresh"));
        group.addWidget(configInput(120, 40, () -> tagRefreshTicks,
                value -> machineClientConfig(this, C2SIntegratedHatchPacket.ACTION_SET_TAG_REFRESH, value)));
        group.addWidget(new LabelWidget(6, 60, "gtlsupb.integrated_hatch.config.stock_pages"));
        group.addWidget(configInput(120, 58, 8, () -> stockPages,
                value -> machineClientConfig(this, C2SIntegratedHatchPacket.ACTION_SET_STOCK_PAGES, value)));
        group.addWidget(new LabelWidget(6, 78, "gtlsupb.integrated_hatch.config.tag_pages"));
        group.addWidget(configInput(120, 76, 8, () -> tagPages,
                value -> machineClientConfig(this, C2SIntegratedHatchPacket.ACTION_SET_TAG_PAGES, value)));
        group.addWidget(new ButtonWidget(6, 96, 208, 14,
                dynamicToggleTexture("gtlsupb.integrated_hatch.config.output_me", () -> outputToMENetwork),
                cd -> {
                    outputToMENetwork = !outputToMENetwork;
                    gtlsupb$sendRequester(C2SIntegratedRequesterPacket.ACTION_TOGGLE_OUTPUT_ME,
                            outputToMENetwork ? 1L : 0L);
                }));
        return group;
    }

    private static void machineClientConfig(OverlimitIntegratedHatchPartMachine machine, int action, int value) {
        machine.gtlsupb$sendConfig(action, value);
    }

    private static IntInputWidget configInput(int x, int y, java.util.function.Supplier<Integer> supplier,
                                              java.util.function.Consumer<Integer> consumer) {
        return configInput(x, y, 200, supplier, consumer);
    }

    private static com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget longInput(
                                                                                 int x, int y, java.util.function.LongSupplier supplier,
                                                                                 java.util.function.LongConsumer consumer) {
        return new com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget(x, y, 96, 14,
                () -> String.valueOf(supplier.getAsLong()),
                value -> {
                    try {
                        consumer.accept(Long.parseLong(value.trim()));
                    } catch (NumberFormatException ignored) {}
                });
    }

    private static IntInputWidget configInput(int x, int y, int max,
                                              java.util.function.Supplier<Integer> supplier,
                                              java.util.function.Consumer<Integer> consumer) {
        IntInputWidget widget = new IntInputWidget(new Position(x, y), supplier, consumer);
        widget.setMin(1);
        widget.setMax(max);
        return widget;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);
        sideTabs.attachSubTab(new RequesterPage(this));
        sideTabs.attachSubTab(new RequestCachePage(this));
        sideTabs.attachSubTab(new OutputPage(this));
        sideTabs.attachSubTab(new StockPage(this));
        sideTabs.attachSubTab(new TagItemPage(this));
        sideTabs.attachSubTab(new TagFluidPage(this));
        sideTabs.attachSubTab(new CircuitPage(this));
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        MEBigIntegerTransfer.writeBuffer(tag, "gtlsupb_big_buffer", outputBuffer);
        MEBigIntegerTransfer.writeBuffer(tag, "gtlsupb_requester_cache", requesterCache);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        outputBuffer.clear();
        MEBigIntegerTransfer.readBuffer(tag.getList("gtlsupb_big_buffer", Tag.TAG_COMPOUND), outputBuffer);
        requesterCache.clear();
        MEBigIntegerTransfer.readBuffer(tag.getList("gtlsupb_requester_cache", Tag.TAG_COMPOUND), requesterCache);
    }

    private void gtlsupb$mergeOutput(AEKey key, BigInteger amount) {
        outputBuffer.merge(key, amount, BigInteger::add);
        markDirty();
        onChanged();
        gtlsupb$wakeNode();
    }

    private void gtlsupb$wakeNode() {
        if (isRemote() || !getMainNode().isReady()) return;
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
    }

    private void gtlsupb$runRequester(IGrid grid) {
        if (isRemote() || getLevel() == null || grid.getStorageService() == null) return;
        if (requesterPullCount <= 0L) {
            requesterSummary = Component.translatable("gtlsupb.integrated_hatch.requester.summary", 0, 0, 0).getString();
            return;
        }
        var wanted = gtlsupb$collectRequesterKeys(grid);
        if (wanted.isEmpty()) {
            gtlsupb$applyRequesterCache(grid, wanted);
            requesterSummary = Component.translatable("gtlsupb.integrated_hatch.requester.summary", 0, 0, 0).getString();
            return;
        }
        var targets = new Object2ObjectOpenHashMap<AEKey, Long>();
        for (var entry : wanted.entrySet()) {
            long amount = gtlsupb$saturatedMultiply(entry.getValue().longValue(), Math.max(1L, requesterPerPull));
            amount = gtlsupb$saturatedMultiply(amount, requesterPullCount);
            targets.put(entry.getKey(), amount);
        }
        gtlsupb$applyRequesterCache(grid, targets);
        var craftingService = grid.getCraftingService();
        MEStorage inventory = grid.getStorageService().getInventory();
        int submitted = 0;
        if (craftingService != null && inventory != null) {
            for (var entry : targets.entrySet()) {
                AEKey key = entry.getKey();
                if (key == null || requesterPlans.containsKey(key)) continue;
                long available = inventory.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
                long cached = gtlsupb$getCachedAmount(key);
                if (BigInteger.valueOf(available).add(BigInteger.valueOf(cached)).compareTo(BigInteger.valueOf(entry.getValue())) >= 0) continue;
                long missing = Math.max(1L, entry.getValue() - available - cached);
                try {
                    requesterPlans.put(key, craftingService.beginCraftingCalculation(getLevel(), () -> actionSource,
                            key, missing, appeng.api.networking.crafting.CalculationStrategy.CRAFT_LESS));
                    submitted++;
                } catch (Throwable ignored) {}
            }
        }
        requesterSummary = Component.translatable("gtlsupb.integrated_hatch.requester.summary",
                targets.size(), requesterCache.size(), submitted).getString();
        requesterPullCount = Math.max(0L, requesterPullCount - 1L);
        markDirty();
    }

    private Object2ObjectOpenHashMap<AEKey, Long> gtlsupb$collectRequesterKeys(IGrid grid) {
        Object2ObjectOpenHashMap<AEKey, Long> result = new Object2ObjectOpenHashMap<>();
        for (int slot = 0; slot < requesterPatterns.getSlots(); slot++) {
            ItemStack stack = requesterPatterns.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            var pattern = com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper.decode(stack, getLevel());
            if (pattern == null) continue;
            for (var input : pattern.getSparseInputs()) {
                if (input == null || input.what() == null || input.amount() <= 0L) continue;
                result.merge(input.what(), input.amount(), Math::addExact);
            }
        }
        return result;
    }

    private void gtlsupb$tickRequesterPlans(IGrid grid) {
        if (requesterPlans.isEmpty()) return;
        var craftingService = grid.getCraftingService();
        for (var iterator = requesterPlans.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next();
            var future = entry.getValue();
            if (future == null || !future.isDone()) continue;
            iterator.remove();
            if (craftingService == null) continue;
            try {
                var plan = future.get();
                if (plan == null || (plan.missingItems() != null && !plan.missingItems().isEmpty())) continue;
                craftingService.submitJob(plan, null, null, true, actionSource);
            } catch (Throwable ignored) {}
        }
    }

    private static long gtlsupb$saturatedMultiply(long a, long b) {
        if (a == 0L || b == 0L) return 0L;
        long result = a * b;
        return result / a == b ? result : Long.MAX_VALUE;
    }

    private void gtlsupb$applyRequesterCache(IGrid grid, Object2ObjectOpenHashMap<AEKey, Long> wanted) {
        MEStorage inventory = grid.getStorageService() == null ? null : grid.getStorageService().getInventory();
        if (inventory == null) return;
        boolean changed = false;
        for (var iterator = requesterCache.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next();
            if (!wanted.containsKey(entry.getKey())) {
                iterator.remove();
                changed = true;
            }
        }
        for (var entry : wanted.entrySet()) {
            AEKey key = entry.getKey();
            if (key == null) continue;
            BigInteger target = BigInteger.valueOf(entry.getValue());
            BigInteger cached = requesterCache.getOrDefault(key, BigInteger.ZERO);
            if (cached.compareTo(target) >= 0) continue;
            long missing = target.subtract(cached).min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
            long extracted = inventory.extract(key, missing, Actionable.MODULATE, actionSource);
            if (extracted <= 0L) continue;
            requesterCache.merge(key, BigInteger.valueOf(extracted), BigInteger::add);
            changed = true;
        }
        if (changed) gtlsupb$notifyCacheChanged();
    }

    public long gtlsupb$getCachedAmount(AEKey key) {
        return requesterCache.getOrDefault(key, BigInteger.ZERO)
                .min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }

    public void gtlsupb$notifyCacheChanged() {
        markDirty();
        onChanged();
        gtlsupb$refreshControllers();
        gtlsupb$wakeNode();
    }

    private void gtlsupb$returnCacheToME() {
        if (isRemote() || requesterCache.isEmpty()) return;
        IGrid grid = getMainNode().getGrid();
        if (grid == null) return;
        boolean changed = false;
        for (var iterator = requesterCache.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next();
            BigInteger inserted = MEBigIntegerTransfer.insert(grid, entry.getKey(), entry.getValue(), actionSource);
            if (inserted.signum() <= 0) continue;
            BigInteger left = entry.getValue().subtract(inserted);
            if (left.signum() <= 0) iterator.remove();
            else entry.setValue(left);
            changed = true;
        }
        if (changed) gtlsupb$notifyCacheChanged();
    }

    private void gtlsupb$syncStock(IGrid grid, ExportOnlyAEItemList items, ExportOnlyAEFluidList fluids) {
        MEStorage inventory = grid.getStorageService() == null ? null : grid.getStorageService().getInventory();
        if (inventory == null) return;
        for (int i = 0; i < items.getConfigurableSlots(); i++) {
            var config = items.getConfigurableSlot(i).getConfig();
            if (config == null || !(config.what() instanceof AEItemKey key)) {
                items.getConfigurableSlot(i).setStock(null);
                continue;
            }
            long amount = inventory.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
            items.getConfigurableSlot(i).setStock(amount <= 0 ? null : new GenericStack(key, amount));
        }
        for (int i = 0; i < fluids.getConfigurableSlots(); i++) {
            var config = fluids.getConfigurableSlot(i).getConfig();
            if (config == null || !(config.what() instanceof AEFluidKey key)) {
                fluids.getConfigurableSlot(i).setStock(null);
                continue;
            }
            long amount = inventory.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
            fluids.getConfigurableSlot(i).setStock(amount <= 0 ? null : new GenericStack(key, amount));
        }
    }

    private void gtlsupb$refreshTagSlots(IGrid grid) {
        var storage = grid.getStorageService();
        if (storage == null) return;
        if (!itemTagPullEnabled) itemTagHandler.clearInventory(0);
        if (!fluidTagPullEnabled) fluidTagHandler.clearInventory(0);
        if (itemTagPullEnabled) {
            int itemIndex = 0;
            for (var entry : storage.getCachedInventory()) {
                if (itemIndex >= itemTagHandler.getConfigurableSlots()) break;
                AEKey key = entry.getKey();
                if (!(key instanceof AEItemKey itemKey) || !itemTagMatches(itemKey, itemTagWhite, itemTagBlack)) continue;
                itemTagHandler.getConfigurableSlot(itemIndex++).setConfig(new GenericStack(itemKey, 1));
            }
            for (int i = itemIndex; i < itemTagHandler.getConfigurableSlots(); i++) {
                itemTagHandler.clearInventory(i);
            }
        }
        if (fluidTagPullEnabled) {
            int fluidIndex = 0;
            for (var entry : storage.getCachedInventory()) {
                if (fluidIndex >= fluidTagHandler.getConfigurableSlots()) break;
                AEKey key = entry.getKey();
                if (!(key instanceof AEFluidKey fluidKey) || !fluidTagMatches(fluidKey, fluidTagWhite, fluidTagBlack)) continue;
                fluidTagHandler.getConfigurableSlot(fluidIndex++).setConfig(new GenericStack(fluidKey, 1));
            }
            for (int i = fluidIndex; i < fluidTagHandler.getConfigurableSlots(); i++) {
                fluidTagHandler.clearInventory(i);
            }
        }
        itemTagHandler.onConfigChanged();
        fluidTagHandler.onConfigChanged();
    }

    private static boolean itemTagMatches(AEItemKey key, String white, String black) {
        var tags = key.toStack().getItem().builtInRegistryHolder().tags().toList();
        boolean matched = white == null || white.isBlank() || white.equals("*") || hasAnyTag(tags, white);
        if (black != null && !black.isBlank() && hasAnyTag(tags, black)) return false;
        return matched;
    }

    private static boolean fluidTagMatches(AEFluidKey key, String white, String black) {
        var tags = key.getFluid().builtInRegistryHolder().tags().toList();
        boolean matched = white == null || white.isBlank() || white.equals("*") || hasAnyTag(tags, white);
        if (black != null && !black.isBlank() && hasAnyTag(tags, black)) return false;
        return matched;
    }

    private static boolean hasAnyTag(List<? extends net.minecraft.tags.TagKey<?>> tags, String expression) {
        for (net.minecraft.tags.TagKey<?> tag : tags) {
            for (String part : expression.split("[,\\s]+")) {
                if (!part.isEmpty() && part.equals(tag.location().toString())) return true;
            }
        }
        return false;
    }

    private static class RequesterPage implements IFancyUIProvider {

        private final OverlimitIntegratedHatchPartMachine machine;

        RequesterPage(OverlimitIntegratedHatchPartMachine machine) {
            this.machine = machine;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gtlsupb.integrated_hatch.page.requester");
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ResourceTexture("gtlsupb:textures/gui/icon/integrated_requester.png");
        }

        @Override
        public boolean hasPlayerInventory() {
            return true;
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            WidgetGroup group = new WidgetGroup(0, 0, 220, 200);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            group.addWidget(new LabelWidget(4, 4, "gtlsupb.integrated_hatch.requester.hint"));
            group.addWidget(new ButtonWidget(4, 18, 104, 14,
                    dynamicToggleTexture("gtlsupb.integrated_hatch.requester.auto",
                            () -> machine.requesterAutoMode),
                    cd -> machine.gtlsupb$requesterClient(C2SIntegratedRequesterPacket.ACTION_TOGGLE_AUTO,
                            machine.requesterAutoMode ? 0L : 1L)));
            group.addWidget(new ButtonWidget(112, 18, 104, 14,
                    buttonTexture("gtlsupb.integrated_hatch.requester.once"),
                    cd -> machine.gtlsupb$requesterClient(C2SIntegratedRequesterPacket.ACTION_RUN_ONCE, 0L)));
            group.addWidget(new LabelWidget(4, 38, "gtlsupb.integrated_hatch.requester.frequency"));
            group.addWidget(configInput(120, 36, () -> machine.requesterFrequency,
                    value -> machine.gtlsupb$requesterClient(C2SIntegratedRequesterPacket.ACTION_SET_FREQUENCY,
                            value)));
            group.addWidget(new LabelWidget(4, 58, "gtlsupb.integrated_hatch.requester.per_pull"));
            group.addWidget(longInput(120, 56,
                    () -> machine.requesterPerPull,
                    value -> machine.gtlsupb$requesterClient(C2SIntegratedRequesterPacket.ACTION_SET_PER_PULL, value)));
            group.addWidget(new LabelWidget(4, 78, "gtlsupb.integrated_hatch.requester.count_input"));
            group.addWidget(longInput(120, 76,
                    () -> machine.requesterPullCount,
                    value -> machine.gtlsupb$requesterClient(C2SIntegratedRequesterPacket.ACTION_SET_COUNT, value)));
            for (int slot = 0; slot < machine.requesterPatterns.getSlots(); slot++) {
                int col = slot % 3;
                int row = slot / 3;
                group.addWidget(new com.lowdragmc.lowdraglib.gui.widget.SlotWidget(
                        machine.requesterPatterns, slot, 14 + col * 20, 96 + row * 20, true, true)
                        .setChangeListener(() -> {
                            machine.markDirty();
                            machine.gtlsupb$wakeNode();
                        }));
            }
            group.addWidget(new LabelWidget(80, 140, () -> machine.requesterSummary));
            return group;
        }
    }

    private static class RequestCachePage implements IFancyUIProvider {

        private final OverlimitIntegratedHatchPartMachine machine;

        RequestCachePage(OverlimitIntegratedHatchPartMachine machine) {
            this.machine = machine;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gtlsupb.integrated_hatch.page.request_cache");
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ResourceTexture("gtlsupb:textures/gui/icon/integrated_request_cache.png");
        }

        @Override
        public boolean hasPlayerInventory() {
            return true;
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            WidgetGroup group = new WidgetGroup(0, 0, 220, 200);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            group.addWidget(new LabelWidget(4, 4, "gtlsupb.integrated_hatch.request_cache.hint"));
            group.addWidget(new BigIntegerMEOutListGridWidget(5, 22, 7, machine.requesterCache));
            group.addWidget(new ButtonWidget(5, 150, 104, 18,
                    buttonTexture("gtlsupb.integrated_hatch.request_cache.return"),
                    cd -> machine.gtlsupb$requesterClient(
                            C2SIntegratedRequesterPacket.ACTION_RETURN_CACHE, 0L)));
            return group;
        }
    }

    private static class OutputPage implements IFancyUIProvider {

        private final OverlimitIntegratedHatchPartMachine machine;

        OutputPage(OverlimitIntegratedHatchPartMachine machine) {
            this.machine = machine;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gtlsupb.integrated_hatch.page.output");
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ResourceTexture("gtlsupb:textures/gui/icon/integrated_output.png");
        }

        @Override
        public boolean hasPlayerInventory() {
            return true;
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            WidgetGroup group = new WidgetGroup(0, 0, 220, 200);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            group.addWidget(new LabelWidget(4, 4, "gtlsupb.integrated_hatch.output_page.hint"));
            group.addWidget(new BigIntegerMEOutListGridWidget(5, 20, 7, machine.outputBuffer));
            return group;
        }
    }

    private static class PagedConfigWidget extends AEDualConfigWidget {

        private final java.util.function.IntSupplier maxPages;

        PagedConfigWidget(int x, int y, ExportOnlyAEItemList item, ExportOnlyAEFluidList fluid,
                          java.util.function.IntSupplier maxPages) {
            super(x, y, item, fluid, value -> {}, 1, true, 3);
            this.maxPages = maxPages;
            for (var widget : widgets) {
                if (widget instanceof LabelWidget label) label.setVisible(false);
            }
            addWidget(new LabelWidget(66, 3 * 38 + 6, () -> safePage() + " / " + maxPages()));
            if (page > maxPages()) page = maxPages();
        }

        private int maxPages() {
            return Math.max(1, Math.min(8, maxPages.getAsInt()));
        }

        private int safePage() {
            return Math.max(1, Math.min(page, maxPages()));
        }

        @Override
        public int getSlotIndex(int visibleIndex) {
            return (safePage() - 1) * 24 + visibleIndex;
        }

        @Override
        protected void pageUp(com.lowdragmc.lowdraglib.gui.util.ClickData data) {
            if (safePage() < 2) return;
            disableAmount();
            page = safePage() - 1;
        }

        @Override
        protected void pageDown(com.lowdragmc.lowdraglib.gui.util.ClickData data) {
            if (safePage() >= maxPages()) return;
            disableAmount();
            page = safePage() + 1;
        }

        @Override
        protected boolean isAutoPull() {
            return false;
        }
    }

    private static class StockPage implements IFancyUIProvider {

        private final OverlimitIntegratedHatchPartMachine machine;

        StockPage(OverlimitIntegratedHatchPartMachine machine) {
            this.machine = machine;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gtlsupb.integrated_hatch.page.stock");
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ResourceTexture("gtlsupb:textures/gui/icon/integrated_stock.png");
        }

        @Override
        public boolean hasPlayerInventory() {
            return true;
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            WidgetGroup group = new WidgetGroup(0, 0, 220, 200);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            group.addWidget(new LabelWidget(4, 4, "gtlsupb.integrated_hatch.stock_page.hint"));
            group.addWidget(new PagedConfigWidget(5, 22, machine.itemStockHandler, machine.fluidStockHandler,
                    () -> machine.stockPages));
            return group;
        }
    }

    private static class TagItemPage implements IFancyUIProvider {

        private final OverlimitIntegratedHatchPartMachine machine;
        private final ExportOnlyAEFluidList emptyFluids;

        TagItemPage(OverlimitIntegratedHatchPartMachine machine) {
            this.machine = machine;
            this.emptyFluids = new ExportOnlyAEFluidList(machine, 192);
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gtlsupb.integrated_hatch.page.tag_item");
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ResourceTexture("gtlsupb:textures/gui/icon/integrated_tag_item.png");
        }

        @Override
        public boolean hasPlayerInventory() {
            return true;
        }

        @Override
        public void attachConfigurators(ConfiguratorPanel panel) {
            panel.attachConfigurators(new TagFilterConfigurator(
                    () -> machine.itemTagWhite,
                    value -> machine.gtlsupb$setTagFilterClient(true, true, value),
                    () -> machine.itemTagBlack,
                    value -> machine.gtlsupb$setTagFilterClient(true, false, value)));
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            WidgetGroup group = new WidgetGroup(0, 0, 220, 200);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            group.addWidget(new LabelWidget(4, 4, "gtlsupb.integrated_hatch.tag_item_page.hint"));
            group.addWidget(new ButtonWidget(140, 4, 72, 14,
                    dynamicToggleTexture("gtlsupb.integrated_hatch.pull", () -> machine.itemTagPullEnabled),
                    cd -> machine.gtlsupb$setTagPullClient(true)));
            group.addWidget(new PagedConfigWidget(5, 22, machine.itemTagHandler, emptyFluids,
                    () -> machine.tagPages));
            return group;
        }
    }

    private static class TagFluidPage implements IFancyUIProvider {

        private final OverlimitIntegratedHatchPartMachine machine;
        private final ExportOnlyAEItemList emptyItems;

        TagFluidPage(OverlimitIntegratedHatchPartMachine machine) {
            this.machine = machine;
            this.emptyItems = new ExportOnlyAEItemList(machine, 192);
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gtlsupb.integrated_hatch.page.tag_fluid");
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ResourceTexture("gtlsupb:textures/gui/icon/integrated_tag_fluid.png");
        }

        @Override
        public boolean hasPlayerInventory() {
            return true;
        }

        @Override
        public void attachConfigurators(ConfiguratorPanel panel) {
            panel.attachConfigurators(new TagFilterConfigurator(
                    () -> machine.fluidTagWhite,
                    value -> machine.gtlsupb$setTagFilterClient(false, true, value),
                    () -> machine.fluidTagBlack,
                    value -> machine.gtlsupb$setTagFilterClient(false, false, value)));
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            WidgetGroup group = new WidgetGroup(0, 0, 220, 200);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            group.addWidget(new LabelWidget(4, 4, "gtlsupb.integrated_hatch.tag_fluid_page.hint"));
            group.addWidget(new ButtonWidget(140, 4, 72, 14,
                    dynamicToggleTexture("gtlsupb.integrated_hatch.pull", () -> machine.fluidTagPullEnabled),
                    cd -> machine.gtlsupb$setTagPullClient(false)));
            group.addWidget(new PagedConfigWidget(5, 22, emptyItems, machine.fluidTagHandler,
                    () -> machine.tagPages));
            return group;
        }
    }

    private static class CircuitPage implements IFancyUIProvider {

        private final OverlimitIntegratedHatchPartMachine machine;

        CircuitPage(OverlimitIntegratedHatchPartMachine machine) {
            this.machine = machine;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gtlsupb.integrated_hatch.page.circuit");
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ResourceTexture("gtlsupb:textures/gui/icon/integrated_circuit.png");
        }

        @Override
        public boolean hasPlayerInventory() {
            return true;
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            WidgetGroup group = new WidgetGroup(0, 0, 184, 200);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            group.addWidget(new LabelWidget(9, 8,
                    Component.translatable("gtlsupb.integrated_hatch.circuit_page.hint").getString()));
            group.addWidget(new ButtonWidget(100, 24, 36, 12,
                    buttonTexture("gtlsupb.integrated_hatch.select_all"),
                    cd -> machine.gtlsupb$clientSetAllCircuits(true)));
            group.addWidget(new ButtonWidget(140, 24, 36, 12,
                    buttonTexture("gtlsupb.integrated_hatch.clear_all"),
                    cd -> machine.gtlsupb$clientSetAllCircuits(false)));
            group.addWidget(machine.circuitHandler.createWidgetGroup());
            return group;
        }
    }

    private static IGuiTexture buttonTexture(String key) {
        return new GuiTextureGroup(GuiTextures.BUTTON,
                new com.lowdragmc.lowdraglib.gui.texture.TextTexture(Component.translatable(key).getString()));
    }

    private static IGuiTexture toggleTexture(String key, boolean enabled) {
        return dynamicToggleTexture(key, () -> enabled);
    }

    private static IGuiTexture dynamicToggleTexture(String key, java.util.function.BooleanSupplier enabled) {
        return new GuiTextureGroup(GuiTextures.BUTTON, new com.lowdragmc.lowdraglib.gui.texture.TextTexture(
                () -> Component.translatable(key).getString() + ": " + Component.translatable(
                        enabled.getAsBoolean() ? "gtlsupb.integrated_hatch.toggle.on" : "gtlsupb.integrated_hatch.toggle.off").getString()));
    }

    private static class IntegratedItemOutputHandler extends NotifiableRecipeHandlerTrait<Ingredient>
                                                     implements ICapabilityTrait {

        private final OverlimitIntegratedHatchPartMachine machine;

        IntegratedItemOutputHandler(OverlimitIntegratedHatchPartMachine machine) {
            super(machine);
            this.machine = machine;
        }

        @Override
        public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left,
                                                  @Nullable String slotName, boolean simulate) {
            if (io != IO.OUT || left == null || left.isEmpty()) return left;
            if (simulate) return null;
            for (Ingredient ingredient : left) {
                ItemStack[] items = ingredient.getItems();
                if (items.length == 0 || items[0].isEmpty()) continue;
                long amount = ingredient instanceof LongIngredient longIngredient ? longIngredient.getActualAmount() : ingredient instanceof SizedIngredient sized ? sized.getAmount() : items[0].getCount();
                machine.gtlsupb$mergeOutput(AEItemKey.of(items[0]), BigInteger.valueOf(amount));
            }
            return null;
        }

        @Override
        public List<Object> getContents() {
            return List.of();
        }

        @Override
        public double getTotalContentAmount() {
            return Double.MAX_VALUE;
        }

        @Override
        public RecipeCapability<Ingredient> getCapability() {
            return ItemRecipeCapability.CAP;
        }

        @Override
        public IO getHandlerIO() {
            return IO.OUT;
        }

        @Override
        public IO getCapabilityIO() {
            return IO.OUT;
        }
    }

    private static class IntegratedFluidOutputHandler extends NotifiableRecipeHandlerTrait<FluidIngredient>
                                                      implements ICapabilityTrait {

        private final OverlimitIntegratedHatchPartMachine machine;

        IntegratedFluidOutputHandler(OverlimitIntegratedHatchPartMachine machine) {
            super(machine);
            this.machine = machine;
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                       @Nullable String slotName, boolean simulate) {
            if (io != IO.OUT || left == null || left.isEmpty()) return left;
            if (simulate) return null;
            for (FluidIngredient ingredient : left) {
                com.lowdragmc.lowdraglib.side.fluid.FluidStack[] fluids = ingredient.getStacks();
                if (fluids.length == 0 || fluids[0].isEmpty()) continue;
                machine.gtlsupb$mergeOutput(AEFluidKey.of(fluids[0].getFluid(), fluids[0].getTag()),
                        BigInteger.valueOf(fluids[0].getAmount()));
            }
            return null;
        }

        @Override
        public List<Object> getContents() {
            return List.of();
        }

        @Override
        public double getTotalContentAmount() {
            return Double.MAX_VALUE;
        }

        @Override
        public RecipeCapability<FluidIngredient> getCapability() {
            return FluidRecipeCapability.CAP;
        }

        @Override
        public IO getHandlerIO() {
            return IO.OUT;
        }

        @Override
        public IO getCapabilityIO() {
            return IO.OUT;
        }
    }

    private static class RequestCacheItemHandler extends NotifiableRecipeHandlerTrait<Ingredient>
                                                 implements ICapabilityTrait {

        private final OverlimitIntegratedHatchPartMachine machine;

        RequestCacheItemHandler(OverlimitIntegratedHatchPartMachine machine) {
            super(machine);
            this.machine = machine;
        }

        @Override
        public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left,
                                                  @Nullable String slotName, boolean simulate) {
            if (io != IO.IN || left == null || left.isEmpty() || machine.requesterCache.isEmpty()) return left;
            var iterator = left.listIterator();
            while (iterator.hasNext()) {
                Ingredient ingredient = iterator.next();
                if (ingredient.isEmpty()) {
                    iterator.remove();
                    continue;
                }
                long needed = ingredient instanceof LongIngredient longIngredient ? longIngredient.getActualAmount() : ingredient instanceof SizedIngredient sized ? sized.getAmount() : 1L;
                if (needed < 1L) {
                    iterator.remove();
                    continue;
                }
                for (var entry : machine.requesterCache.entrySet()) {
                    if (!(entry.getKey() instanceof AEItemKey key)) continue;
                    boolean matched = false;
                    for (ItemStack stack : ingredient.getItems()) {
                        if (!stack.isEmpty() && (key.matches(stack) || ingredient.test(key.toStack(1)))) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) continue;
                    long available = entry.getValue().min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
                    long used = Math.min(needed, available);
                    if (used <= 0L) continue;
                    needed -= used;
                    if (!simulate) {
                        BigInteger leftAmount = entry.getValue().subtract(BigInteger.valueOf(used));
                        if (leftAmount.signum() <= 0) machine.requesterCache.remove(entry.getKey());
                        else entry.setValue(leftAmount);
                        machine.gtlsupb$notifyCacheChanged();
                    }
                    if (needed <= 0L) {
                        iterator.remove();
                        break;
                    }
                }
            }
            return left.isEmpty() ? null : left;
        }

        @Override
        public List<Object> getContents() {
            List<Object> result = new ArrayList<>();
            for (var entry : machine.requesterCache.entrySet()) {
                if (entry.getKey() instanceof AEItemKey key && entry.getValue().signum() > 0) {
                    ItemStack stack = key.toStack(1);
                    stack.setCount(Math.toIntExact(entry.getValue().min(BigInteger.valueOf(Integer.MAX_VALUE)).longValue()));
                    result.add(stack);
                }
            }
            return result;
        }

        @Override
        public double getTotalContentAmount() {
            long total = 0L;
            for (var entry : machine.requesterCache.entrySet()) {
                if (entry.getKey() instanceof AEItemKey) {
                    total += entry.getValue().min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
                }
            }
            return total;
        }

        @Override
        public RecipeCapability<Ingredient> getCapability() {
            return ItemRecipeCapability.CAP;
        }

        @Override
        public IO getHandlerIO() {
            return IO.IN;
        }

        @Override
        public IO getCapabilityIO() {
            return IO.IN;
        }
    }

    private static class RequestCacheFluidHandler extends NotifiableRecipeHandlerTrait<FluidIngredient>
                                                  implements ICapabilityTrait {

        private final OverlimitIntegratedHatchPartMachine machine;

        RequestCacheFluidHandler(OverlimitIntegratedHatchPartMachine machine) {
            super(machine);
            this.machine = machine;
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                       @Nullable String slotName, boolean simulate) {
            if (io != IO.IN || left == null || left.isEmpty() || machine.requesterCache.isEmpty()) return left;
            var iterator = left.listIterator();
            while (iterator.hasNext()) {
                FluidIngredient ingredient = iterator.next();
                long needed = ingredient.getAmount();
                if (needed < 1L) {
                    iterator.remove();
                    continue;
                }
                for (var entry : machine.requesterCache.entrySet()) {
                    if (!(entry.getKey() instanceof AEFluidKey key)) continue;
                    if (!org.gtlcore.gtlcore.integration.ae2.AEUtils.testFluidIngredient(ingredient, key)) continue;
                    long available = entry.getValue().min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
                    long used = Math.min(needed, available);
                    if (used <= 0L) continue;
                    needed -= used;
                    if (!simulate) {
                        BigInteger leftAmount = entry.getValue().subtract(BigInteger.valueOf(used));
                        if (leftAmount.signum() <= 0) machine.requesterCache.remove(key);
                        else entry.setValue(leftAmount);
                        machine.gtlsupb$notifyCacheChanged();
                    }
                    if (needed <= 0L) {
                        iterator.remove();
                        break;
                    }
                }
            }
            return left.isEmpty() ? null : left;
        }

        @Override
        public List<Object> getContents() {
            List<Object> result = new ArrayList<>();
            for (var entry : machine.requesterCache.entrySet()) {
                if (entry.getKey() instanceof AEFluidKey key && entry.getValue().signum() > 0) {
                    result.add(com.lowdragmc.lowdraglib.side.fluid.FluidStack.create(key.getFluid(),
                            entry.getValue().min(BigInteger.valueOf(Long.MAX_VALUE)).longValue(), key.getTag()));
                }
            }
            return result;
        }

        @Override
        public double getTotalContentAmount() {
            long total = 0L;
            for (var entry : machine.requesterCache.entrySet()) {
                if (entry.getKey() instanceof AEFluidKey) {
                    total += entry.getValue().min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
                }
            }
            return total;
        }

        @Override
        public RecipeCapability<FluidIngredient> getCapability() {
            return FluidRecipeCapability.CAP;
        }

        @Override
        public IO getHandlerIO() {
            return IO.IN;
        }

        @Override
        public IO getCapabilityIO() {
            return IO.IN;
        }
    }

    private static class IntegratedItemStockHandler extends ExportOnlyAEItemList implements IOptimizedMEList {

        private final OverlimitIntegratedHatchPartMachine machine;
        private final boolean tagFiltered;
        private final Object2ObjectOpenHashMap<AEItemKey, Long> cache = new Object2ObjectOpenHashMap<>();
        private final it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<ItemStack> meItems = new it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<>();

        IntegratedItemStockHandler(OverlimitIntegratedHatchPartMachine machine, boolean tagFiltered) {
            super(machine, 192, ExportOnlyAEConfigureItemSlot::new);
            this.machine = machine;
            this.tagFiltered = tagFiltered;
            for (var slot : getInventory()) {
                ((ExportOnlyAEConfigureItemSlot) slot).setOnConfigChanged(this::onConfigChanged);
            }
        }

        @Override
        public void clearInventory(int startIndex) {
            for (int i = startIndex; i < getConfigurableSlots(); i++) {
                ((ExportOnlyAEConfigureItemSlot) getConfigurableSlot(i)).setConfigWithoutNotify(null);
                getConfigurableSlot(i).setStock(null);
            }
            onConfigChanged();
        }

        @Override
        public boolean isAutoPull() {
            return true;
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left,
                                                  @Nullable String slotName, boolean simulate) {
            if (io != IO.IN || left == null || left.isEmpty()) return left;
            IGrid grid = machine.getMainNode().getGrid();
            if (grid == null || grid.getStorageService() == null) return left;
            MEStorage network = grid.getStorageService().getInventory();
            var iterator = left.listIterator();
            while (iterator.hasNext()) {
                Ingredient ingredient = iterator.next();
                if (ingredient.isEmpty()) {
                    iterator.remove();
                    continue;
                }
                long amount = ingredient instanceof LongIngredient longIngredient ? longIngredient.getActualAmount() : ingredient instanceof SizedIngredient sized ? sized.getAmount() : 1L;
                if (amount < 1L) {
                    iterator.remove();
                    continue;
                }
                for (var entry : cache.entrySet()) {
                    AEItemKey key = entry.getKey();
                    if (tagFiltered && !machine.itemTagPullEnabled) continue;
                    if (!matches(key, ingredient) || (tagFiltered && !machine.itemTagMatches(key,
                            machine.itemTagWhite, machine.itemTagBlack)))
                        continue;
                    long extracted = network.extract(key, amount,
                            simulate ? Actionable.SIMULATE : Actionable.MODULATE, machine.actionSource);
                    if (extracted <= 0L) continue;
                    amount -= extracted;
                    if (!simulate) setChanged(true);
                    if (amount <= 0L) {
                        iterator.remove();
                        break;
                    }
                }
            }
            return left.isEmpty() ? null : left;
        }

        private boolean matches(AEItemKey key, Ingredient ingredient) {
            for (ItemStack stack : ingredient.getItems()) {
                if (!stack.isEmpty() && (key.matches(stack) || ingredient.test(key.toStack(1)))) return true;
            }
            return false;
        }

        @Override
        public void onConfigChanged() {
            cache.clear();
            for (int i = 0; i < getConfigurableSlots(); i++) {
                var config = getConfigurableSlot(i).getConfig();
                if (config != null && config.what() instanceof AEItemKey key) {
                    cache.put(key, config.amount());
                } else {
                    getConfigurableSlot(i).setStock(null);
                }
            }
        }

        @Override
        public @Nullable it.unimi.dsi.fastutil.objects.Object2LongMap<ItemStack> getMEItemMap() {
            meItems.clear();
            IGrid grid = machine.getMainNode().getGrid();
            if (grid == null || grid.getStorageService() == null) return null;
            MEStorage network = grid.getStorageService().getInventory();
            for (AEItemKey key : cache.keySet()) {
                long amount = network.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, machine.actionSource);
                if (amount > 0L) meItems.put(key.toStack(1), amount);
            }
            return meItems.isEmpty() ? null : meItems;
        }

        @Override
        public @NotNull List<com.lowdragmc.lowdraglib.side.fluid.FluidStack> getMEFluidList() {
            return List.of();
        }
    }

    private static class IntegratedFluidStockHandler extends ExportOnlyAEFluidList implements IOptimizedMEList {

        private final OverlimitIntegratedHatchPartMachine machine;
        private final boolean tagFiltered;
        private final Object2ObjectOpenHashMap<AEFluidKey, Long> cache = new Object2ObjectOpenHashMap<>();
        private final List<com.lowdragmc.lowdraglib.side.fluid.FluidStack> meFluids = new ArrayList<>();

        IntegratedFluidStockHandler(OverlimitIntegratedHatchPartMachine machine, boolean tagFiltered) {
            super(machine, 192, ExportOnlyAEConfigureFluidSlot::new);
            this.machine = machine;
            this.tagFiltered = tagFiltered;
            for (var slot : getInventory()) {
                ((ExportOnlyAEConfigureFluidSlot) slot).setOnConfigChanged(this::onConfigChanged);
            }
        }

        @Override
        public void clearInventory(int startIndex) {
            for (int i = startIndex; i < getConfigurableSlots(); i++) {
                ((ExportOnlyAEConfigureFluidSlot) getConfigurableSlot(i)).setConfigWithoutNotify(null);
                getConfigurableSlot(i).setStock(null);
            }
            onConfigChanged();
        }

        @Override
        public boolean isAutoPull() {
            return true;
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                       @Nullable String slotName, boolean simulate) {
            if (io != IO.IN || left == null || left.isEmpty()) return left;
            IGrid grid = machine.getMainNode().getGrid();
            if (grid == null || grid.getStorageService() == null) return left;
            MEStorage network = grid.getStorageService().getInventory();
            var iterator = left.listIterator();
            while (iterator.hasNext()) {
                FluidIngredient ingredient = iterator.next();
                long amount = ingredient.getAmount();
                if (amount < 1L) {
                    iterator.remove();
                    continue;
                }
                for (var entry : cache.entrySet()) {
                    AEFluidKey key = entry.getKey();
                    if (!org.gtlcore.gtlcore.integration.ae2.AEUtils.testFluidIngredient(ingredient, key)) continue;
                    if (tagFiltered && !machine.fluidTagPullEnabled) continue;
                    if (tagFiltered && !machine.fluidTagMatches(key, machine.fluidTagWhite, machine.fluidTagBlack)) continue;
                    long extracted = network.extract(key, amount,
                            simulate ? Actionable.SIMULATE : Actionable.MODULATE, machine.actionSource);
                    if (extracted <= 0L) continue;
                    amount -= extracted;
                    if (!simulate) setChanged(true);
                    if (amount <= 0L) {
                        iterator.remove();
                        break;
                    }
                }
            }
            return left.isEmpty() ? null : left;
        }

        @Override
        public void onConfigChanged() {
            cache.clear();
            for (int i = 0; i < getConfigurableSlots(); i++) {
                var config = getConfigurableSlot(i).getConfig();
                if (config != null && config.what() instanceof AEFluidKey key) {
                    cache.put(key, config.amount());
                } else {
                    getConfigurableSlot(i).setStock(null);
                }
            }
        }

        @Override
        public @Nullable it.unimi.dsi.fastutil.objects.Object2LongMap<ItemStack> getMEItemMap() {
            return null;
        }

        @Override
        public @NotNull List<com.lowdragmc.lowdraglib.side.fluid.FluidStack> getMEFluidList() {
            meFluids.clear();
            IGrid grid = machine.getMainNode().getGrid();
            if (grid == null || grid.getStorageService() == null) return meFluids;
            MEStorage network = grid.getStorageService().getInventory();
            for (AEFluidKey key : cache.keySet()) {
                long amount = network.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, machine.actionSource);
                if (amount > 0L) {
                    meFluids.add(com.lowdragmc.lowdraglib.side.fluid.FluidStack.create(key.getFluid(), amount, key.getTag()));
                }
            }
            return meFluids;
        }
    }

    private static class RefreshWidget extends Widget {

        private final IntegratedCircuitHandler handler;

        RefreshWidget(IntegratedCircuitHandler handler) {
            super(new Position(0, 0), new Size(0, 0));
            this.handler = handler;
        }

        @Override
        public void updateScreen() {
            for (int i = 0; i < handler.buttons.length; i++) {
                if (handler.buttons[i] != null) handler.buttons[i].setButtonTexture(handler.circuitTexture(i));
            }
        }
    }

    private static class IntegratedCircuitHandler extends NotifiableRecipeHandlerTrait<Ingredient>
                                                  implements ICapabilityTrait {

        private final OverlimitIntegratedHatchPartMachine machine;
        private final ButtonWidget[] buttons = new ButtonWidget[33];

        IntegratedCircuitHandler(OverlimitIntegratedHatchPartMachine machine) {
            super(machine);
            this.machine = machine;
        }

        @Override
        public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left,
                                                  @Nullable String slotName, boolean simulate) {
            if (io != IO.IN || left == null || left.isEmpty()) return left;
            var iterator = left.iterator();
            while (iterator.hasNext()) {
                Ingredient ingredient = iterator.next();
                int configuration = circuitConfiguration(ingredient);
                if (configuration >= 0 && machine.isCircuitSelected(configuration)) iterator.remove();
            }
            return left.isEmpty() ? null : left;
        }

        private static int circuitConfiguration(Ingredient ingredient) {
            Ingredient inner = ingredient instanceof SizedIngredient sized ? sized.getInner() : ingredient;
            ItemStack[] items = inner.getItems();
            if (items.length == 0 || items[0].isEmpty() || !GTItems.INTEGRATED_CIRCUIT.is(items[0].getItem())) return -1;
            return IntCircuitBehaviour.getCircuitConfiguration(items[0]);
        }

        @Override
        public List<Object> getContents() {
            List<Object> contents = new ArrayList<>();
            for (int i = 0; i < 33; i++) {
                if (machine.isCircuitSelected(i)) contents.add(IntCircuitIngredient.circuitInput(i));
            }
            return contents;
        }

        @Override
        public int getSize() {
            return 33;
        }

        @Override
        public double getTotalContentAmount() {
            return Long.bitCount(machine.selectedCircuitsV2);
        }

        @Override
        public RecipeCapability<Ingredient> getCapability() {
            return ItemRecipeCapability.CAP;
        }

        @Override
        public IO getHandlerIO() {
            return IO.IN;
        }

        @Override
        public IO getCapabilityIO() {
            return IO.IN;
        }

        WidgetGroup createWidgetGroup() {
            WidgetGroup group = new WidgetGroup(0, 0, 184, 132);
            int index = 0;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) addCircuitButton(group, index++, col, row);
            }
            for (int col = 0; col < 6; col++) addCircuitButton(group, index++, col, 3);
            group.addWidget(new RefreshWidget(this));
            return group;
        }

        private void addCircuitButton(WidgetGroup group, int circuit, int col, int row) {
            ButtonWidget button = new ButtonWidget(10 + 18 * col, 48 + 18 * row, 18, 18,
                    circuitTexture(circuit), cd -> {});
            button.setOnPressCallback(cd -> {
                machine.gtlsupb$clientToggleCircuit(circuit);
                button.setButtonTexture(circuitTexture(circuit));
            });
            buttons[circuit] = button;
            group.addWidget(button);
        }

        private IGuiTexture circuitTexture(int circuit) {
            IGuiTexture icon = new ItemStackTexture(IntCircuitBehaviour.stack(circuit)).scale(0.888f);
            return machine.isCircuitSelected(circuit) ? new GuiTextureGroup(GuiTextures.SLOT, icon) : new GuiTextureGroup(GuiTextures.SLOT, new ColorRectTexture(0x80000000), icon);
        }
    }

    public void gtlsupb$setOutputFrequency(int value) {
        outputFrequency = Math.max(1, Math.min(200, value));
        markDirty();
        gtlsupb$wakeNode();
    }

    public void gtlsupb$setTagRefreshTicks(int value) {
        tagRefreshTicks = Math.max(1, Math.min(200, value));
        markDirty();
    }

    public void gtlsupb$setStockPages(int value) {
        stockPages = Math.max(1, Math.min(8, value));
        markDirty();
    }

    public void gtlsupb$setTagPages(int value) {
        tagPages = Math.max(1, Math.min(8, value));
        markDirty();
    }

    public void gtlsupb$setItemTagPull(boolean value) {
        itemTagPullEnabled = value;
        markDirty();
        if (!value) itemTagHandler.clearInventory(0);
        gtlsupb$refreshControllers();
    }

    private void gtlsupb$setTagPullClient(boolean item) {
        boolean value;
        if (item) {
            itemTagPullEnabled = !itemTagPullEnabled;
            value = itemTagPullEnabled;
        } else {
            fluidTagPullEnabled = !fluidTagPullEnabled;
            value = fluidTagPullEnabled;
        }
        markDirty();
        gtlsupb$sendConfig(item ? C2SIntegratedHatchPacket.ACTION_SET_ITEM_TAG_PULL : C2SIntegratedHatchPacket.ACTION_SET_FLUID_TAG_PULL, value ? 1 : 0);
        gtlsupb$refreshControllers();
    }

    public void gtlsupb$setFluidTagPull(boolean value) {
        fluidTagPullEnabled = value;
        markDirty();
        if (!value) fluidTagHandler.clearInventory(0);
        gtlsupb$refreshControllers();
    }

    public void gtlsupb$handleRequester(int action, long value) {
        switch (action) {
            case C2SIntegratedRequesterPacket.ACTION_TOGGLE_AUTO -> requesterAutoMode = value != 0L;
            case C2SIntegratedRequesterPacket.ACTION_SET_FREQUENCY -> requesterFrequency = (int) Math.max(1L, Math.min(200L, value));
            case C2SIntegratedRequesterPacket.ACTION_SET_PER_PULL -> requesterPerPull = Math.max(1L, value);
            case C2SIntegratedRequesterPacket.ACTION_ADD_COUNT -> requesterPullCount = gtlsupb$saturatedAdd(requesterPullCount, value);
            case C2SIntegratedRequesterPacket.ACTION_SET_COUNT -> requesterPullCount = Math.max(0L, value);
            case C2SIntegratedRequesterPacket.ACTION_RUN_ONCE -> {
                if (!isRemote()) {
                    IGrid grid = getMainNode().getGrid();
                    if (grid != null) gtlsupb$runRequester(grid);
                }
            }
            case C2SIntegratedRequesterPacket.ACTION_TOGGLE_OUTPUT_ME -> outputToMENetwork = value != 0L;
            case C2SIntegratedRequesterPacket.ACTION_RETURN_CACHE -> {
                if (!isRemote()) gtlsupb$returnCacheToME();
            }
            default -> {}
        }
        markDirty();
        gtlsupb$wakeNode();
    }

    private static long gtlsupb$saturatedAdd(long a, long b) {
        if (b > 0L && a > Long.MAX_VALUE - b) return Long.MAX_VALUE;
        if (b < 0L && a < Long.MIN_VALUE - b) return 0L;
        return Math.max(0L, a + b);
    }

    private void gtlsupb$sendRequester(int action, long value) {
        GTLSupbNetworkHandler.CHANNEL.sendToServer(
                new com.gtlsupb.gtlsupb.network.C2SIntegratedRequesterPacket(getPos(), action, value));
    }

    private void gtlsupb$requesterClient(int action, long value) {
        gtlsupb$handleRequester(action, value);
        gtlsupb$sendRequester(action, value);
    }

    public void gtlsupb$setTagFilter(boolean item, boolean whitelist, String value) {
        if (item) {
            if (whitelist) itemTagWhite = value;
            else itemTagBlack = value;
        } else {
            if (whitelist) fluidTagWhite = value;
            else fluidTagBlack = value;
        }
        markDirty();
        gtlsupb$refreshControllers();
    }

    private void gtlsupb$setTagFilterClient(boolean item, boolean whitelist, String value) {
        gtlsupb$setTagFilter(item, whitelist, value);
        GTLSupbNetworkHandler.CHANNEL.sendToServer(
                new com.gtlsupb.gtlsupb.network.C2SIntegratedTagFilterPacket(getPos(), item, whitelist, value));
    }

    private void gtlsupb$refreshControllers() {
        itemTagHandler.onConfigChanged();
        fluidTagHandler.onConfigChanged();
        for (IMultiController controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine logicMachine) {
                logicMachine.getRecipeLogic().markLastRecipeDirty();
                logicMachine.getRecipeLogic().updateTickSubscription();
            }
        }
    }

    private void gtlsupb$sendConfig(int action, int value) {
        GTLSupbNetworkHandler.CHANNEL.sendToServer(new C2SIntegratedHatchPacket(getPos(), action, value));
    }

    public boolean isCircuitSelected(int configuration) {
        return configuration >= 0 && configuration <= 32 && (selectedCircuitsV2 & (1L << configuration)) != 0L;
    }

    public boolean gtlsupb$clientToggleCircuit(int configuration) {
        if (configuration < 0 || configuration > 32) return false;
        selectedCircuitsV2 ^= 1L << configuration;
        markDirty();
        GTLSupbNetworkHandler.CHANNEL.sendToServer(
                new C2SIntegratedHatchPacket(getPos(), C2SIntegratedHatchPacket.ACTION_TOGGLE_CIRCUIT, configuration));
        circuitHandler.notifyListeners();
        return true;
    }

    public void gtlsupb$clientSetAllCircuits(boolean value) {
        selectedCircuitsV2 = value ? (1L << 33) - 1L : 0L;
        markDirty();
        GTLSupbNetworkHandler.CHANNEL.sendToServer(new C2SIntegratedHatchPacket(getPos(),
                value ? C2SIntegratedHatchPacket.ACTION_SET_ALL_CIRCUITS : C2SIntegratedHatchPacket.ACTION_CLEAR_CIRCUITS, 0));
        circuitHandler.notifyListeners();
    }

    public void gtlsupb$toggleCircuitServer(int configuration) {
        if (configuration < 0 || configuration > 32) return;
        selectedCircuitsV2 ^= 1L << configuration;
        gtlsupb$onCircuitSelectionChanged();
    }

    public void gtlsupb$setAllCircuitsServer(boolean value) {
        selectedCircuitsV2 = value ? (1L << 33) - 1L : 0L;
        gtlsupb$onCircuitSelectionChanged();
    }

    private void gtlsupb$onCircuitSelectionChanged() {
        markDirty();
        circuitHandler.notifyListeners();
        for (IMultiController controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine logicMachine) {
                logicMachine.getRecipeLogic().markLastRecipeDirty();
                logicMachine.getRecipeLogic().updateTickSubscription();
            }
        }
    }
}
