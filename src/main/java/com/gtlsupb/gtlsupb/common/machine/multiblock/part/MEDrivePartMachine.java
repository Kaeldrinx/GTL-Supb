package com.gtlsupb.gtlsupb.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import com.gtlsupb.gtlsupb.config.ConfigHolder;
import com.gtlsupb.gtlsupb.integration.ae2.AEGridRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MEDrivePartMachine extends TieredIOPartMachine
                                implements IGridConnectedMachine, IMachineLife, IStorageProvider {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEDrivePartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    private final int cols = 10;
    private final int rows = ConfigHolder.INSTANCE.meDriveRows;
    private final int slotCount = cols * rows;
    private final int uiWidth = cols * 18 + 16;

    @Persisted
    private final NotifiableItemStackHandler machineStorage = new NotifiableItemStackHandler(this, slotCount, IO.NONE, IO.BOTH);

    private final Map<Integer, StorageCell> cellCache = new HashMap<>();

    @Persisted
    private final GridNodeHolder nodeHolder = new GridNodeHolder(this);

    @DescSynced
    private boolean isOnline = false;

    @Persisted
    @DescSynced
    private java.util.UUID ownerUuid;

    public java.util.UUID getOwnerUuid() {
        return ownerUuid;
    }

    @Persisted
    @DescSynced
    public boolean autoFillCells = false;

    private DraggableScrollableWidgetGroup slotGroup = null;

    private final IActionSource actionSource = IActionSource.ofMachine(() -> nodeHolder.getMainNode().getNode());

    private TickableSubscription cellPullSubs = null;

    private boolean inAutoPull = false;

    private boolean gtlsupb$gridRegistered = false;

    private final Set<net.minecraft.world.item.Item> gtlsupb$knownNonCellItems = new java.util.HashSet<>();

    private static final int NON_CELL_CACHE_LIMIT = 4096;
    private static final int VISIBLE_ROWS = 5;

    @Persisted
    @DescSynced
    public int drivePriority = 0;

    private int pullCounter = 0;

    private int pullBackoffTicks = PULL_INTERVAL_MIN;

    private int pullBackoffCounter;

    private static final int PULL_INTERVAL_MIN = 20;

    private static final int PULL_INTERVAL_MAX = 200;

    public MEDrivePartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.BOTH);
        getMainNode().addService(IStorageProvider.class, this);
        machineStorage.addChangedListener(() -> onMachineStorageContentsChanged());
    }

    public StorageCell getCellInventory(int index) {
        ItemStack stack = machineStorage.getStackInSlot(index);
        if (stack.isEmpty()) return null;
        return createCellInventory(stack);
    }

    private StorageCell createCellInventory(ItemStack stack) {
        final StorageCell[] cell = new StorageCell[1];
        cell[0] = StorageCells.getCellInventory(stack, () -> {
            if (cell[0] != null) cell[0].persist();
            markDirty();
        });
        return cell[0];
    }

    private StorageCell createInnerCellInventory(ItemStack innerStack, ItemStack packStack, int cellIndex) {
        final StorageCell[] cell = new StorageCell[1];
        cell[0] = StorageCells.getCellInventory(innerStack, () -> {
            if (cell[0] != null) cell[0].persist();
            CompoundTag tag = packStack.getTag();
            if (tag != null) {
                net.minecraft.nbt.ListTag keysList = tag.getList("keys", 10);
                if (cellIndex < keysList.size()) {
                    keysList.set(cellIndex, innerStack.save(new CompoundTag()));
                }
            }
            markDirty();
        });
        return cell[0];
    }

    private void onMachineStorageContentsChanged() {
        invalidateCellCache();
        markDirty();
        if (!inAutoPull) {
            onChanged();
            IStorageProvider.requestUpdate(getMainNode());
        }
    }

    private int[] filter() {
        Set<Integer> keys = actualSlots().keySet();
        int[] arr = new int[keys.size()];
        int i = 0;
        for (Integer k : keys) {
            arr[i++] = k;
        }
        return arr;
    }

    private Map<Integer, ItemStack> actualSlots() {
        Map<Integer, ItemStack> result = new HashMap<>();
        for (int i = 0; i < machineStorage.getSlots(); i++) {
            ItemStack stack = machineStorage.getStackInSlot(i);
            if (!stack.isEmpty()) {
                result.put(i, stack);
            }
        }
        return result;
    }

    private void invalidateCellCache() {
        cellCache.clear();
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        if (!getMainNode().isOnline()) return;
        int[] slots = filter();
        for (int slot : slots) {
            ItemStack stack = machineStorage.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            List<StorageCell> innerCells = getInnerCellsFromPack(stack);
            if (innerCells != null) {
                for (StorageCell innerCell : innerCells) {
                    mounts.mount(innerCell, drivePriority);
                }
            } else {
                StorageCell cell = cellCache.computeIfAbsent(slot, k -> createCellInventory(stack));
                if (cell == null) continue;
                mounts.mount(cell, drivePriority);
            }
        }
    }

    private List<StorageCell> getInnerCellsFromPack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;
        if (!tag.contains("keys")) return null;

        net.minecraft.nbt.ListTag keysList = tag.getList("keys", 10);
        if (keysList.isEmpty()) return null;

        CompoundTag firstKey = keysList.getCompound(0);
        if (!firstKey.getString("id").equals("expatternprovider:infinity_cell")) return null;

        List<StorageCell> innerCells = new ArrayList<>();
        for (int i = 0; i < keysList.size(); i++) {
            CompoundTag keyTag = keysList.getCompound(i);
            if (!keyTag.contains("Count")) {
                keyTag.putByte("Count", (byte) 1);
            }
            ItemStack innerStack = ItemStack.of(keyTag);
            if (!innerStack.isEmpty()) {
                StorageCell innerCell = createInnerCellInventory(innerStack, stack, i);
                if (innerCell != null) {
                    innerCells.add(innerCell);
                }
            }
        }
        return innerCells.isEmpty() ? null : innerCells;
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public boolean isOnline() {
        return this.isOnline;
    }

    @Override
    public void setOnline(boolean online) {
        this.isOnline = online;
    }

    @Override
    public boolean shouldSyncME() {
        return false;
    }

    @Override
    public boolean updateMEStatus() {
        boolean online = getMainNode().isActive();
        if (this.isOnline != online) {
            this.isOnline = online;
        }
        return this.isOnline;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        boolean wasOnline = this.isOnline;
        this.isOnline = getMainNode().isActive();
        if (wasOnline != this.isOnline) {
            IStorageProvider.requestUpdate(getMainNode());

            if (this.isOnline) {
                AEGridRegistry.register(getMainNode().getGrid());
                if (ownerUuid != null) AEGridRegistry.recordOwner(getMainNode().getGrid(), ownerUuid);
                gtlsupb$gridRegistered = true;
            } else {
                AEGridRegistry.unregister(getMainNode().getGrid());
                gtlsupb$gridRegistered = false;
            }

            gtlsupb$updateTickSubscription();
        }
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
    }

    private void gtlsupb$setNodeOwner(net.minecraft.world.entity.player.Player player) {
        if (player == null || isRemote()) return;
        try {
            getMainNode().setOwningPlayer(player);
        } catch (Throwable ignored) {}
        try {
            IGrid grid = getMainNode().getGrid();
            if (grid != null) AEGridRegistry.recordOwner(grid, player.getUUID());
        } catch (Throwable ignored) {}
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && ownerUuid == null) {
            net.minecraft.world.entity.player.Player nearest = com.gtlsupb.gtlsupb.common.data.PlayerBindingHelper.findNearestPlayer(getLevel(), getPos(), 8.0d);
            if (nearest != null) {
                ownerUuid = nearest.getUUID();
                gtlsupb$setNodeOwner(nearest);
                markDirty();
            }
        } else if (!isRemote() && ownerUuid != null) {
            net.minecraft.server.level.ServerPlayer owner = getLevel() instanceof net.minecraft.server.level.ServerLevel sl ? sl.getServer().getPlayerList().getPlayer(ownerUuid) : null;
            if (owner != null) gtlsupb$setNodeOwner(owner);
        }
        this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        gtlsupb$updateTickSubscription();
    }

    @Override
    public void onMachinePlaced(net.minecraft.world.entity.LivingEntity player, net.minecraft.world.item.ItemStack stack) {
        if (player != null) {
            ownerUuid = player.getUUID();
            if (player instanceof net.minecraft.world.entity.player.Player p) gtlsupb$setNodeOwner(p);
            markDirty();
        }
    }

    private void gtlsupb$updateTickSubscription() {
        if (isRemote()) return;
        boolean need = autoFillCells || !gtlsupb$gridRegistered;
        if (need) {
            cellPullSubs = subscribeServerTick(cellPullSubs, this::gtlsupb$pullTick);
        } else if (cellPullSubs != null) {
            cellPullSubs.unsubscribe();
            cellPullSubs = null;
        }
    }

    private void gtlsupb$pullTick() {
        pullCounter++;
        if (pullCounter < 20) return;
        pullCounter = 0;

        if (!gtlsupb$gridRegistered && isOnline && getMainNode().isReady()) {
            IGrid grid = getMainNode().getGrid();
            if (grid != null) {
                AEGridRegistry.register(grid);
                gtlsupb$gridRegistered = true;
            }
        }
        if (!autoFillCells) {

            if (gtlsupb$gridRegistered) gtlsupb$updateTickSubscription();
            return;
        }

        if (++pullBackoffCounter < pullBackoffTicks / 20) return;
        pullBackoffCounter = 0;
        boolean pulled = tryPullCells();
        pullBackoffTicks = pulled ? PULL_INTERVAL_MIN : Math.min(PULL_INTERVAL_MAX, pullBackoffTicks * 2);
    }

    @Override
    public void onMachineRemoved() {
        net.minecraft.world.level.Level level = this.getLevel();
        if (level == null) return;
        net.minecraft.core.BlockPos pos = this.getPos();
        for (int i = 0; i < machineStorage.getSlots(); i++) {
            ItemStack stack = machineStorage.getStackInSlot(i);
            if (!stack.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy()));
            }
        }
    }

    @Override
    public Widget createUIWidget() {
        int slotAreaHeight = VISIBLE_ROWS * 18;
        WidgetGroup group = new WidgetGroup(0, 0, uiWidth, slotAreaHeight + 20);

        group.addWidget(
                new LabelWidget(4, 0,
                        () -> this.isOnline ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));

        this.slotGroup = new DraggableScrollableWidgetGroup(0, 12, uiWidth, slotAreaHeight)
                .setYScrollBarWidth(6)
                .setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON)
                .setScrollWheelDirection(DraggableScrollableWidgetGroup.ScrollWheelDirection.VERTICAL);
        rebuildPageSlots();
        group.addWidget(slotGroup);

        return group;
    }

    private void rebuildPageSlots() {
        if (slotGroup == null) return;
        WidgetGroup group = slotGroup;
        group.clearAllWidgets();

        int slots = machineStorage.getSlots();
        for (int i = 0; i < slots; i++) {
            int row = i / cols;
            int col = i % cols;
            group.addWidget(
                    new SlotWidget(machineStorage.storage, i, 8 + col * 18, row * 18, true, true) {

                        @Override
                        public boolean isVisible() {
                            return true;
                        }
                    }.setBackground(GuiTextures.SLOT));
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);

        configuratorPanel.attachConfigurators(
                new IFancyConfiguratorButton.Toggle(
                        GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.0, 1.0, 0.5),
                        GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.5, 1.0, 0.5),
                        () -> autoFillCells,
                        (a, b) -> {
                            autoFillCells = !autoFillCells;

                            if (autoFillCells) {
                                pullBackoffTicks = PULL_INTERVAL_MIN;
                                pullBackoffCounter = 0;
                            }
                            gtlsupb$updateTickSubscription();
                        }).setTooltipsSupplier(showAlternate -> Collections.singletonList(Component.translatable("gtlsupb.me_drive.auto_fill.tooltip"))));

        configuratorPanel.attachConfigurators(
                new com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator() {

                    @Override
                    public Component getTitle() {
                        return Component.translatable("gtlsupb.me_drive.priority");
                    }

                    @Override
                    public com.lowdragmc.lowdraglib.gui.texture.IGuiTexture getIcon() {
                        return new ResourceTexture("gtlsupb:textures/gui/icon/drive_priority.png");
                    }

                    @Override
                    public com.lowdragmc.lowdraglib.gui.widget.Widget createConfigurator() {
                        return new com.lowdragmc.lowdraglib.gui.widget.WidgetGroup(0, 0, 100, 20)
                                .addWidget(
                                        new IntInputWidget(() -> drivePriority, (value) -> {
                                            drivePriority = value != null ? value : 0;
                                            IStorageProvider.requestUpdate(getMainNode());
                                        }).setMin(Integer.MIN_VALUE).setMax(Integer.MAX_VALUE));
                    }
                });
    }

    private boolean tryPullCells() {
        if (!autoFillCells) return false;
        IManagedGridNode mainNode = getMainNode();
        appeng.api.networking.IGrid grid = mainNode.getGrid();
        if (grid == null) return false;
        appeng.api.networking.storage.IStorageService storageService = grid.getStorageService();
        appeng.api.stacks.KeyCounter cachedInv = storageService.getCachedInventory();
        appeng.api.storage.MEStorage networkStorage = storageService.getInventory();

        int slots = machineStorage.getSlots();
        it.unimi.dsi.fastutil.ints.IntArrayList emptySlots = new it.unimi.dsi.fastutil.ints.IntArrayList();
        for (int i = 0; i < slots; i++) {
            if (machineStorage.getStackInSlot(i).isEmpty()) {
                emptySlots.add(i);
            }
        }
        if (emptySlots.isEmpty()) return false;

        inAutoPull = true;
        boolean pulled = false;
        int cursor = 0;
        try {
            for (var entry : cachedInv) {
                if (cursor >= emptySlots.size()) break;
                appeng.api.stacks.AEKey key = entry.getKey();
                if (!(key instanceof AEItemKey itemKey)) continue;
                if (entry.getLongValue() <= 0) continue;

                if (gtlsupb$knownNonCellItems.contains(itemKey.getItem())) continue;
                if (!StorageCells.isCellHandled(itemKey.toStack())) {
                    if (gtlsupb$knownNonCellItems.size() >= NON_CELL_CACHE_LIMIT) {
                        gtlsupb$knownNonCellItems.clear();
                    }
                    gtlsupb$knownNonCellItems.add(itemKey.getItem());
                    continue;
                }

                int targetSlot = emptySlots.getInt(cursor);
                long extracted = networkStorage.extract(key, 1, Actionable.SIMULATE, actionSource);
                if (extracted <= 0) continue;

                long moved = networkStorage.extract(key, 1, Actionable.MODULATE, actionSource);
                if (moved <= 0) continue;
                ItemStack stack = itemKey.toStack();
                stack.setCount((int) Math.min(moved, stack.getMaxStackSize()));
                machineStorage.setStackInSlot(targetSlot, stack);
                cursor++;
                pulled = true;
            }
        } finally {
            inAutoPull = false;
            if (pulled) {
                onChanged();
                IStorageProvider.requestUpdate(getMainNode());
            }
        }
        return pulled;
    }
}
