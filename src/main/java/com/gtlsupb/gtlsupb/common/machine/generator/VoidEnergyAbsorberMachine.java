package com.gtlsupb.gtlsupb.common.machine.generator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.gtlsupb.gtlsupb.utils.EnergyBroadcastHelper;
import com.hepdd.gtmthings.api.misc.WirelessEnergyManager;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.UUID;

public class VoidEnergyAbsorberMachine extends TieredEnergyMachine
                                       implements IFancyUIMachine, IMachineLife {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            VoidEnergyAbsorberMachine.class,
            TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    private long lastOutputEU = 0L;

    private static final int SETTLE_INTERVAL = 20;

    private int settleCounter = 0;

    private long gtlsupb$energyCapacity = -1L;

    private long gtlsupb$pendingEnergy = 0L;

    private TickableSubscription energyTickSub = null;

    @Persisted
    private boolean broadcastEnabled = false;

    @Persisted
    private boolean gridPullEnabled = false;

    @Persisted
    @com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
    private UUID userId = null;

    public UUID gtlsupb$getOwnerUuid() {
        return userId;
    }

    public VoidEnergyAbsorberMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public NotifiableEnergyContainer createEnergyContainer(Object[] args) {
        long tierVoltage = GTValues.V[getTier()];

        return NotifiableEnergyContainer.emitterContainer(this,
                tierVoltage * 64L, tierVoltage, 1);
    }

    @Override
    public boolean isEnergyEmitter() {
        return true;
    }

    @Override
    public long getMaxInputOutputAmperage() {
        return 1L;
    }

    public long getLastOutputEU() {
        return lastOutputEU;
    }

    public String getVoltageTierName() {
        return GTValues.VN[getTier()];
    }

    public boolean isBroadcastEnabled() {
        return broadcastEnabled;
    }

    public boolean isGridPullEnabled() {
        return gridPullEnabled;
    }

    public void requestToggle(int toggle, boolean value) {
        if (toggle == com.gtlsupb.gtlsupb.network.C2SVoidEnergyAbsorberTogglePacket.TOGGLE_BROADCAST) {
            this.broadcastEnabled = value;
        } else {
            this.gridPullEnabled = value;
        }
        if (isRemote()) {

            com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler.CHANNEL.sendToServer(
                    new com.gtlsupb.gtlsupb.network.C2SVoidEnergyAbsorberTogglePacket(
                            getPos(), toggle, value));
        } else {
            markDirty();
            gtlsupb$updateTickSubscription();
        }
    }

    public void applyToggleFromClient(int toggle, boolean value) {
        if (isRemote()) return;
        if (toggle == com.gtlsupb.gtlsupb.network.C2SVoidEnergyAbsorberTogglePacket.TOGGLE_BROADCAST) {
            this.broadcastEnabled = value;
        } else if (toggle == com.gtlsupb.gtlsupb.network.C2SVoidEnergyAbsorberTogglePacket.TOGGLE_GRID_PULL) {
            this.gridPullEnabled = value;
        } else {
            return;
        }
        markDirty();
        notifyBlockUpdate();
        gtlsupb$updateTickSubscription();
    }

    private boolean gtlsupb$isActive() {
        return broadcastEnabled || gridPullEnabled;
    }

    private void gtlsupb$updateTickSubscription() {
        if (isRemote()) return;
        if (gtlsupb$isActive()) {
            if (energyTickSub == null) {
                energyTickSub = subscribeServerTick(energyTickSub, this::checkEnergy);
            }
        } else {
            if (energyTickSub != null) {
                unsubscribe(energyTickSub);
                energyTickSub = null;
            }
            lastOutputEU = 0L;
            settleCounter = 0;
            gtlsupb$pendingEnergy = 0L;
        }
    }

    private long gtlsupb$capacity() {
        if (gtlsupb$energyCapacity <= 0 && energyContainer != null) {
            gtlsupb$energyCapacity = energyContainer.getEnergyCapacity();
        }
        return gtlsupb$energyCapacity;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            if (userId == null) {
                tryAutoBindUserId();
            }
            gtlsupb$updateTickSubscription();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energyTickSub != null) {
            unsubscribe(energyTickSub);
            energyTickSub = null;
        }
    }

    private void tryAutoBindUserId() {
        if (!(getLevel() instanceof ServerLevel)) return;
        ServerLevel level = (ServerLevel) getLevel();
        var nearest = level.players().stream()
                .min(Comparator.comparingDouble(p -> p.distanceToSqr(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5)))
                .orElse(null);
        if (nearest != null && nearest.distanceToSqr(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5) <= 256.0) {
            userId = nearest.getUUID();
            markDirty();
        }
    }

    @Override
    public void onMachinePlaced(LivingEntity player, ItemStack stack) {
        if (player != null) {
            userId = player.getUUID();
            markDirty();
            gtlsupb$updateTickSubscription();
        }
    }

    private void checkEnergy() {
        Level lvl = getLevel();
        if (lvl == null || lvl.isClientSide()) return;
        if (energyContainer == null) return;
        if (userId == null && getOffsetTimer() % 20L == 0L) tryAutoBindUserId();
        if (!gtlsupb$isActive()) {
            gtlsupb$updateTickSubscription();
            return;
        }

        long generated = GTValues.V[getTier()];
        gtlsupb$pendingEnergy += generated;

        if (++settleCounter < SETTLE_INTERVAL) return;
        settleCounter = 0;

        long capacity = gtlsupb$capacity();
        long stored = energyContainer.getEnergyStored();
        if (stored < capacity && gtlsupb$pendingEnergy > 0L) {
            energyContainer.addEnergy(Math.min(gtlsupb$pendingEnergy, capacity - stored));
            lastOutputEU = generated;
        } else {
            lastOutputEU = 0L;
        }
        gtlsupb$pendingEnergy = 0L;

        long available = energyContainer.getEnergyStored();
        if (available <= 0) return;

        if (broadcastEnabled && userId != null) {
            try {
                long leftover = EnergyBroadcastHelper.broadcastEnergy(this, available, userId);
                long moved = available - leftover;
                if (moved > 0) {
                    energyContainer.removeEnergy(moved);
                    available -= moved;
                }
            } catch (Throwable ignored) {

            }
        }

        if (gridPullEnabled && userId != null && available > 0) {
            try {
                WirelessEnergyManager.addEUToGlobalEnergyMap(userId, BigInteger.valueOf(available), this);
                energyContainer.removeEnergy(available);
            } catch (Throwable ignored) {

            }
        }
    }

    @Override
    public Widget createUIWidget() {
        int groupWidth = 176;
        WidgetGroup group = new WidgetGroup(0, 0, groupWidth, 72);

        group.addWidget(new LabelWidget(6, 4,
                () -> Component.translatable("gtlsupb.machine.void_energy_absorber.title",
                        getVoltageTierName(), GTValues.V[getTier()]).getString()));

        group.addWidget(
                new ButtonWidget(8, 26, 96, 16,
                        new GuiTextureGroup(GuiTextures.BUTTON,
                                new TextTexture(() -> Component.translatable(
                                        "gtlsupb.machine.void_energy_absorber.broadcast",
                                        this.broadcastEnabled ? "§a已启用" : "§c已禁用").getString())),
                        clickData -> requestToggle(
                                com.gtlsupb.gtlsupb.network.C2SVoidEnergyAbsorberTogglePacket.TOGGLE_BROADCAST,
                                !this.broadcastEnabled)));

        group.addWidget(
                new ButtonWidget(8, 48, 96, 16,
                        new GuiTextureGroup(GuiTextures.BUTTON,
                                new TextTexture(() -> Component.translatable(
                                        "gtlsupb.machine.void_energy_absorber.grid_pull",
                                        this.gridPullEnabled ? "§a已启用" : "§c已禁用").getString())),
                        clickData -> requestToggle(
                                com.gtlsupb.gtlsupb.network.C2SVoidEnergyAbsorberTogglePacket.TOGGLE_GRID_PULL,
                                !this.gridPullEnabled)));

        return group;
    }
}
