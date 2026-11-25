package com.gtlsupb.gtlsupb.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.entity.player.Player;

import com.gtlsupb.gtlsupb.common.data.GTLSupbProviderManager;
import com.gtlsupb.gtlsupb.common.data.PlayerBindingHelper;

import java.util.Collection;
import java.util.UUID;

public class OverlimitDataReceiveHatchPartMachine extends MultiblockPartMachine implements IDataAccessHatch {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            OverlimitDataReceiveHatchPartMachine.class,
            MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private UUID ownerUuid;

    private boolean lastDataAvailable;

    private TickableSubscription availabilitySub;

    public OverlimitDataReceiveHatchPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public boolean isRecipeAvailable(GTRecipe recipe, Collection<IDataAccessHatch> seen) {
        seen.add(this);
        return GTLSupbProviderManager.getDataProviderCount(this.ownerUuid) > 0;
    }

    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        if (isCreative() || isRecipeAvailable(recipe)) {
            return recipe;
        }
        return null;
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
            this.lastDataAvailable = GTLSupbProviderManager.getDataProviderCount(this.ownerUuid) > 0;
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
        boolean available = GTLSupbProviderManager.getDataProviderCount(this.ownerUuid) > 0;
        if (available != this.lastDataAvailable) {
            this.lastDataAvailable = available;
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
