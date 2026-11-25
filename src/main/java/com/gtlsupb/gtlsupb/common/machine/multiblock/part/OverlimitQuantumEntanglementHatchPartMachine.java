package com.gtlsupb.gtlsupb.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import com.gtlsupb.gtlsupb.integration.ae2.OverlimitQuantumEntanglementWirelessManager;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

public class OverlimitQuantumEntanglementHatchPartMachine extends TieredIOPartMachine
                                                          implements IGridConnectedMachine, IMachineLife {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            OverlimitQuantumEntanglementHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    private final GridNodeHolder nodeHolder = new GridNodeHolder(this);

    @DescSynced
    @Persisted
    private boolean isOnline = false;

    @DescSynced
    @Persisted
    private boolean wirelessEnabled = true;

    @DescSynced
    @Persisted
    private boolean skipCables = false;

    @Persisted
    @DescSynced
    private UUID ownerUuid;

    public OverlimitQuantumEntanglementHatchPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.BOTH);
    }

    public boolean isWirelessEnabled() {
        return wirelessEnabled;
    }

    public boolean isSkipCables() {
        return skipCables;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
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
        this.isOnline = getMainNode().isActive() && getMainNode().isOnline();
        return this.isOnline;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        updateMEStatus();
        OverlimitQuantumEntanglementWirelessManager.onNodeStateChanged(this);
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private net.minecraft.nbt.ListTag gtlsupb$cancelledTargets = new net.minecraft.nbt.ListTag();

    public void gtlsupb$writeCancelled(java.util.Set<net.minecraft.core.GlobalPos> targets) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (net.minecraft.core.GlobalPos target : targets) {
            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
            tag.putString("dim", target.dimension().location().toString());
            tag.putInt("x", target.pos().getX());
            tag.putInt("y", target.pos().getY());
            tag.putInt("z", target.pos().getZ());
            list.add(tag);
        }
        this.gtlsupb$cancelledTargets = list;
        markDirty();
    }

    @Override
    public void saveCustomPersistedData(net.minecraft.nbt.CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.put("gtlsupb_cancelled", gtlsupb$cancelledTargets);
    }

    @Override
    public void loadCustomPersistedData(net.minecraft.nbt.CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        this.gtlsupb$cancelledTargets = tag.getList("gtlsupb_cancelled", net.minecraft.nbt.Tag.TAG_COMPOUND);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            OverlimitQuantumEntanglementWirelessManager.restoreBlacklist(this, gtlsupb$cancelledTargets);
        }
        if (!isRemote() && ownerUuid == null) {
            Player nearest = com.gtlsupb.gtlsupb.common.data.PlayerBindingHelper.findNearestPlayer(getLevel(), getPos(), 8.0d);
            if (nearest != null) {
                ownerUuid = nearest.getUUID();
                markDirty();
            }
        }
        this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        OverlimitQuantumEntanglementWirelessManager.onNodeStateChanged(this);
    }

    @Override
    public void onMachineRemoved() {
        OverlimitQuantumEntanglementWirelessManager.unregister(this);
    }

    @Override
    public Widget createUIWidget() {
        int groupWidth = 176;
        WidgetGroup group = new WidgetGroup(0, 0, groupWidth, 92);

        group.addWidget(new LabelWidget(6, 4,
                () -> this.isOnline ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));

        group.addWidget(
                new ButtonWidget(8, 26, 76, 16,
                        new GuiTextureGroup(GuiTextures.BUTTON,
                                new TextTexture(() -> Component.translatable(
                                        wirelessEnabled ? "gtlsupb.qe.wireless.on" : "gtlsupb.qe.wireless.off")
                                        .getString())),
                        clickData -> {
                            this.wirelessEnabled = !this.wirelessEnabled;
                            markDirty();
                            OverlimitQuantumEntanglementWirelessManager.onNodeStateChanged(this);
                        }));

        group.addWidget(
                new ButtonWidget(92, 26, 76, 16,
                        new GuiTextureGroup(GuiTextures.BUTTON,
                                new TextTexture(() -> Component.translatable(
                                        skipCables ? "gtlsupb.qe.skip_cables.on" : "gtlsupb.qe.skip_cables.off")
                                        .getString())),
                        clickData -> {
                            this.skipCables = !this.skipCables;
                            markDirty();
                            OverlimitQuantumEntanglementWirelessManager.onNodeStateChanged(this);
                        }));

        group.addWidget(new LabelWidget(6, 52,
                () -> Component.translatable("gtlsupb.machine.overlimit_quantum_entanglement_hatch.connection_count",
                        OverlimitQuantumEntanglementWirelessManager.getConnectionCount(this)).getString()));

        return group;
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(
                new IFancyConfiguratorButton.Toggle(

                        new ResourceTexture("gtlsupb:textures/gui/icon/wireless_off.png"),
                        new ResourceTexture("gtlsupb:textures/gui/icon/wireless_on.png"),
                        () -> wirelessEnabled,
                        (a, b) -> {
                            this.wirelessEnabled = !this.wirelessEnabled;
                            markDirty();
                            OverlimitQuantumEntanglementWirelessManager.onNodeStateChanged(this);
                        })
                        .setTooltipsSupplier(showAlternate -> Collections.singletonList(
                                Component.translatable("gtlsupb.machine.overlimit_quantum_entanglement_hatch.wireless.tooltip"))));
    }
}
