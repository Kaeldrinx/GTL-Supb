package com.gtlsupb.gtlsupb.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.entity.player.Player;

import com.gtlsupb.gtlsupb.common.data.GTLSupbProviderManager;
import com.gtlsupb.gtlsupb.common.data.PlayerBindingHelper;
import com.gtlsupb.gtlsupb.common.machine.trait.BoundCWUContainer;

import java.util.UUID;

public class OverlimitComputeReceiveHatchPartMachine extends MultiblockPartMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            OverlimitComputeReceiveHatchPartMachine.class,
            MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private UUID ownerUuid;

    private boolean lastComputeAvailable;

    private TickableSubscription availabilitySub;

    public OverlimitComputeReceiveHatchPartMachine(IMachineBlockEntity holder) {
        super(holder);
        attachTraits(new BoundCWUContainer(this));
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    @Override
    public boolean canShared() {
        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            if (this.ownerUuid == null) {
                Player nearest = PlayerBindingHelper.findNearestPlayer(getLevel(), getPos(), 8.0d);
                if (nearest != null) {
                    this.ownerUuid = nearest.getUUID();
                }
            }
            this.lastComputeAvailable = GTLSupbProviderManager.getComputeProviderCount(this.ownerUuid) > 0;
            this.availabilitySub = subscribeServerTick(this::checkAvailability);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (this.availabilitySub != null) {
            this.availabilitySub.unsubscribe();
            this.availabilitySub = null;
        }
    }

    private void checkAvailability() {
        if (isRemote()) {
            return;
        }
        if (getOffsetTimer() % 20 != 0) {
            return;
        }
        boolean available = GTLSupbProviderManager.getComputeProviderCount(this.ownerUuid) > 0;
        if (available != this.lastComputeAvailable) {
            this.lastComputeAvailable = available;
            refreshControllerRecipes();
        }
    }

    private void refreshControllerRecipes() {
        for (IMultiController controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine rlm) {
                RecipeLogic logic = rlm.getRecipeLogic();
                logic.markLastRecipeDirty();
                logic.updateTickSubscription();
            }
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
