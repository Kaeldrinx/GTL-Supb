package com.gtlsupb.gtlsupb.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.ae2.machine.MEHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HugeSteamHatchPartMachine extends MEHatchPartMachine {

    private SteamExportOnlyFluidList aeFluidHandler;

    public HugeSteamHatchPartMachine(IMachineBlockEntity holder) {
        super(holder, IO.IN);
    }

    @Override
    public int getTier() {
        return GTValues.HV;
    }

    @Override
    protected NotifiableFluidTank createTank(long initialCapacity, int slots, Object... args) {
        this.aeFluidHandler = new SteamExportOnlyFluidList(this, 1);
        return this.aeFluidHandler;
    }

    @Override
    protected void autoIO() {
        if (isWorkingEnabled() && shouldSyncME() && updateMEStatus()) {
            syncME();
            updateTankSubscription();
        }
    }

    protected void syncME() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) {
            return;
        }
        MEStorage networkInv = grid.getStorageService().getInventory();
        AEFluidKey steamKey = AEFluidKey.of(GTMaterials.Steam.getFluid());
        long extracted = networkInv.extract(steamKey, Long.MAX_VALUE, Actionable.SIMULATE, this.actionSource);
        ExportOnlyAEFluidSlot slot = this.aeFluidHandler.getInventory()[0];
        if (extracted > 0) {
            slot.setStock(new GenericStack(steamKey, extracted));
        } else {
            slot.setStock(null);
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(false))
                .widget(new ImageWidget(7, 16, 81, 55, GuiTextures.DISPLAY_STEAM.get(false)))
                .widget(new LabelWidget(11, 20, "gtceu.gui.fluid_amount"))
                .widget(new LabelWidget(11, 30,
                        () -> {
                            FluidStack steam = tank.getFluidInTank(0);
                            return (steam.isEmpty() ? "0" : steam.getAmount() + "");
                        }).setTextColor(0xFF404040).setDropShadow(true))
                .widget(new LabelWidget(6, 6,
                        () -> this.isOnline ? Component.translatable("gtlsupb.machine.me_steam_hatch.online").getString() : Component.translatable("gtlsupb.machine.me_steam_hatch.offline").getString()))
                .widget(new TankWidget(tank.getStorages()[0], 90, 35, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(false), 7, 84, true));
    }

    private static class SteamExportOnlyFluidList extends ExportOnlyAEFluidList {

        SteamExportOnlyFluidList(HugeSteamHatchPartMachine machine, int slots) {
            super(machine, slots, () -> new SteamExportOnlyFluidSlot(machine));
            for (ExportOnlyAEFluidSlot slot : getInventory()) {
                slot.setConfig(new GenericStack(AEFluidKey.of(GTMaterials.Steam.getFluid()), 1L));
            }
        }

        @Override
        public long getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }
    }

    private static class SteamExportOnlyFluidSlot extends ExportOnlyAEFluidSlot {

        private final HugeSteamHatchPartMachine machine;

        SteamExportOnlyFluidSlot(HugeSteamHatchPartMachine machine) {
            this.machine = machine;
        }

        @Override
        public FluidStack getFluid() {
            if (machine.isRemote() || !machine.isOnline() || machine.getMainNode().getGrid() == null) {
                return super.getFluid();
            }
            MEStorage aeNetwork = machine.getMainNode().getGrid().getStorageService().getInventory();
            AEFluidKey steamKey = AEFluidKey.of(GTMaterials.Steam.getFluid());
            long available = aeNetwork.extract(steamKey, Long.MAX_VALUE, Actionable.SIMULATE, machine.actionSource);
            if (available > 0) {
                return FluidStack.create(GTMaterials.Steam.getFluid(), available);
            }
            return FluidStack.empty();
        }

        @Override
        public long getFluidAmount() {
            return getFluid().getAmount();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            if (stack.isEmpty() || this.config == null || !(this.config.what() instanceof AEFluidKey key)) {
                return false;
            }
            return key.getFluid() == stack.getFluid();
        }

        @Override
        public FluidStack drain(long maxDrain, boolean simulate, boolean notifyChanges) {
            if (!machine.isOnline() || machine.getMainNode().getGrid() == null) {
                return FluidStack.empty();
            }
            MEStorage aeNetwork = machine.getMainNode().getGrid().getStorageService().getInventory();
            Actionable action = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
            AEFluidKey steamKey = AEFluidKey.of(GTMaterials.Steam.getFluid());
            long extracted = aeNetwork.extract(steamKey, maxDrain, action, machine.actionSource);
            if (extracted > 0) {
                return FluidStack.create(GTMaterials.Steam.getFluid(), extracted);
            }
            return FluidStack.empty();
        }
    }
}
