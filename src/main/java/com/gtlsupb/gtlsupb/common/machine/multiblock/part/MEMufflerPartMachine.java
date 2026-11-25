package com.gtlsupb.gtlsupb.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MufflerPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.gtlsupb.gtlsupb.client.gui.widget.BigIntegerMEOutListGridWidget;
import com.gtlsupb.gtlsupb.integration.ae2.MEBigIntegerTransfer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.EnumSet;

public class MEMufflerPartMachine extends MufflerPartMachine
                                  implements IGridConnectedMachine, IMachineLife, IGridTickable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEMufflerPartMachine.class, MufflerPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private final GridNodeHolder nodeHolder = new GridNodeHolder(this);

    @Persisted
    @DescSynced
    private java.util.UUID ownerUuid;

    @DescSynced
    private boolean isOnline;

    private final Object2ObjectMap<AEKey, BigInteger> buffer = new Object2ObjectOpenHashMap<>();

    private final IActionSource actionSource = IActionSource.ofMachine(() -> getMainNode().getNode());

    public MEMufflerPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.MAX);
        getMainNode().addService(IGridTickable.class, this);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .widget(new FancyMachineUIWidget(this, 176, 166));
    }

    @Override
    public @NotNull Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 158, 146);
        group.addWidget(new LabelWidget(0, 0, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline").setTextColor(0xFF303541).setDropShadow(false));
        group.addWidget(new BigIntegerMEOutListGridWidget(5, 20, 7, buffer));
        return group;
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
    public boolean shouldSyncME() {
        return false;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        IGridConnectedMachine.super.onMainNodeStateChanged(reason);
        boolean wasOnline = isOnline;
        isOnline = getMainNode().isReady() && getMainNode().isActive() && getMainNode().isOnline() && getMainNode().isPowered();
        if (wasOnline != isOnline) onChanged();
        if (isOnline) gtlsupb$wakeNode();
    }

    @Override
    public @NotNull TickingRequest getTickingRequest(@NotNull IGridNode node) {
        return new TickingRequest(gtlsupb$minFrequency(), gtlsupb$maxFrequency(), false, true);
    }

    @Override
    public @NotNull TickRateModulation tickingRequest(@NotNull IGridNode node, int ticksSinceLastCall) {
        if (!getMainNode().isReady() || !getMainNode().isOnline() || !getMainNode().isPowered() || buffer.isEmpty()) return TickRateModulation.SLEEP;
        var grid = getMainNode().getGrid();
        if (grid == null) return TickRateModulation.SLOWER;
        boolean changed = false;
        for (var iterator = buffer.entrySet().iterator(); iterator.hasNext();) {
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
    public void recoverItemsTable(ItemStack... recoveryItems) {
        boolean changed = false;
        for (ItemStack stack : recoveryItems) {
            if (stack == null || stack.isEmpty()) continue;
            if (getRecoveryChance() < 100 && getRecoveryChance() < GTValues.RNG.nextInt(100)) continue;
            buffer.merge(AEItemKey.of(stack), BigInteger.valueOf(stack.getCount()), BigInteger::add);
            changed = true;
        }
        if (changed) {
            markDirty();
            onChanged();
            gtlsupb$wakeNode();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        if (!isRemote() && ownerUuid == null) {
            Player nearest = com.gtlsupb.gtlsupb.common.data.PlayerBindingHelper.findNearestPlayer(getLevel(), getPos(), 8.0d);
            if (nearest != null) {
                ownerUuid = nearest.getUUID();
                markDirty();
                gtlsupb$setNodeOwner(nearest);
            }
        }
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity placer, ItemStack stack) {
        if (!isRemote() && placer instanceof Player player) {
            ownerUuid = player.getUUID();
            markDirty();
            gtlsupb$setNodeOwner(player);
        }
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        MEBigIntegerTransfer.writeBuffer(tag, "gtlsupb_buffer", buffer);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        buffer.clear();
        MEBigIntegerTransfer.readBuffer(tag.getList("gtlsupb_buffer", net.minecraft.nbt.Tag.TAG_COMPOUND), buffer);
    }

    private int gtlsupb$minFrequency() {
        return 20;
    }

    private int gtlsupb$maxFrequency() {
        return 20;
    }

    private void gtlsupb$wakeNode() {
        if (isRemote() || !getMainNode().isReady()) return;
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
    }

    private void gtlsupb$setNodeOwner(Player player) {
        if (player == null || isRemote()) return;
        try {
            getMainNode().setOwningPlayer(player);
        } catch (Throwable ignored) {}
    }
}
