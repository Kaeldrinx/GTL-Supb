package com.gtlsupb.gtlsupb.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.entity.player.Player;

import com.gtlsupb.gtlsupb.common.data.PlayerBindingHelper;

import java.util.UUID;

public abstract class AbstractProviderMachine extends WorkableElectricMultiblockMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AbstractProviderMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private UUID ownerUuid;

    private boolean providerRegistered;

    public AbstractProviderMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            if (this.ownerUuid == null) {
                Player nearest = PlayerBindingHelper.findNearestPlayer(getLevel(), getPos(), 8.0d);
                if (nearest != null) {
                    this.ownerUuid = nearest.getUUID();
                    markDirty();
                }
            }
            if (this.ownerUuid != null && getRecipeLogic() != null && getRecipeLogic().isWorking()) {
                registerProvider();
                providerRegistered = true;
            }
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (!isRemote() && providerRegistered) {
            unregisterProvider();
            providerRegistered = false;
        }
    }

    @Override
    public void notifyStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {
        super.notifyStatusChanged(oldStatus, newStatus);
        if (isRemote()) {
            return;
        }
        if (newStatus == RecipeLogic.Status.WORKING) {
            if (!providerRegistered) {
                registerProvider();
                providerRegistered = true;
            }
        } else if (oldStatus == RecipeLogic.Status.WORKING && providerRegistered) {
            unregisterProvider();
            providerRegistered = false;
        }
    }

    protected abstract void registerProvider();

    protected abstract void unregisterProvider();
}
