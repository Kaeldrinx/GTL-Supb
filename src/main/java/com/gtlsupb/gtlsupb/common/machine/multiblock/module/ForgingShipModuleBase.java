package com.gtlsupb.gtlsupb.common.machine.multiblock.module;

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import com.gtlsupb.gtlsupb.common.data.ParallelData;
import com.gtlsupb.gtlsupb.common.machine.multiblock.SuperStellarWorldForgingHeavenShip;
import com.gtlsupb.gtlsupb.common.machine.multiblock.ZeroEnergyMultiTypeLogic;
import com.gtlsupb.gtlsupb.common.machine.multiblock.ZeroEnergyMultiTypeMachine;
import com.gtlsupb.gtlsupb.utils.RainbowText;
import com.gtlsupb.gtlsupb.utils.RecipeCalculationHelper;

import java.util.List;

public abstract class ForgingShipModuleBase extends ZeroEnergyMultiTypeMachine
                                            implements IModularMachineModule<SuperStellarWorldForgingHeavenShip, ForgingShipModuleBase> {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ForgingShipModuleBase.class,
            ZeroEnergyMultiTypeMachine.MANAGED_FIELD_HOLDER);

    public ForgingShipModuleBase(IMachineBlockEntity holder, GTRecipeType... recipeTypes) {
        super(holder, recipeTypes);
    }

    public abstract GTRecipeType[] getModuleRecipeTypes();

    @Override
    public GTRecipeType[] getRecipeTypes() {
        return getModuleRecipeTypes();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Persisted
    private BlockPos hostPosition;

    private SuperStellarWorldForgingHeavenShip host;

    @Override
    public BlockPos getHostPosition() {
        return hostPosition;
    }

    @Override
    public void setHostPosition(BlockPos pos) {
        this.hostPosition = pos;
    }

    @Override
    public SuperStellarWorldForgingHeavenShip getHost() {
        return host;
    }

    @Override
    public void setHost(SuperStellarWorldForgingHeavenShip host) {
        this.host = host;
    }

    @Override
    public Class<SuperStellarWorldForgingHeavenShip> getHostType() {
        return SuperStellarWorldForgingHeavenShip.class;
    }

    @Override
    public BlockPos[] getHostScanPositions() {
        return new BlockPos[] {

                getPos().offset(26, 2, -1), getPos().offset(26, 2, 1),
                getPos().offset(31, 2, -1), getPos().offset(31, 2, 1),

                getPos().offset(-26, 2, 1), getPos().offset(-26, 2, -1),
                getPos().offset(-31, 2, 1), getPos().offset(-31, 2, -1),

                getPos().offset(1, 2, 26), getPos().offset(-1, 2, 26),
                getPos().offset(1, 2, 31), getPos().offset(-1, 2, 31),

                getPos().offset(-1, 2, -26), getPos().offset(1, 2, -26),
                getPos().offset(-1, 2, -31), getPos().offset(1, 2, -31),
        };
    }

    @Override
    public int getMaxParallel() {
        return Integer.MAX_VALUE;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new ForgingShipModuleLogic(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!findAndConnectToHost()) {
            removeFromHost(this.host);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        removeFromHost(this.host);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        removeFromHost(this.host);
    }

    @Override
    public void onConnected(SuperStellarWorldForgingHeavenShip host) {}

    @Override
    public void onDisconnected() {}

    public long getHostOutputMultiplier() {
        return host != null ? host.getOutputMultiplier() : 1L;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            SuperStellarWorldForgingHeavenShip h = host;
            if (h != null) {
                addMachineModeDisplay(textList);
                textList.add(
                        Component.translatable("gtlsupb.module.connected_to_host")
                                .withStyle(ChatFormatting.GREEN));
                textList.add(
                        Component.translatable(
                                "gtlsupb.module.output_multiplier",
                                RainbowText.createRainbowComponent("×" + h.getOutputMultiplier())).withStyle(ChatFormatting.GRAY));
            } else {
                textList.add(
                        Component.translatable("gtlsupb.module.no_host")
                                .withStyle(ChatFormatting.RED));
            }
        } else {
            textList.add(
                    Component.translatable("gtceu.multiblock.invalid_structure")
                            .withStyle(
                                    Style.EMPTY.withColor(ChatFormatting.RED)
                                            .withHoverEvent(
                                                    new HoverEvent(
                                                            HoverEvent.Action.SHOW_TEXT,
                                                            Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                                                                    .withStyle(ChatFormatting.GRAY)))));
        }
    }
}

class ForgingShipModuleLogic extends ZeroEnergyMultiTypeLogic<ForgingShipModuleBase> {

    private static final int INPUT_CHANCE_RATIO = 10;

    private final ForgingShipModuleBase module;

    public ForgingShipModuleLogic(ForgingShipModuleBase machine) {
        super(machine);
        this.module = machine;
    }

    @Override
    public boolean checkBeforeWorking() {
        SuperStellarWorldForgingHeavenShip host = module.getHost();
        return module.hasProxies() && host != null && host.isFormed();
    }

    @Override
    public ParallelData calculateParallels() {
        return RecipeCalculationHelper.calculateChanceParallels(
                lookupRecipeSet(), module, Long.MAX_VALUE, INPUT_CHANCE_RATIO, module.getTier());
    }

    @Override
    public double getDynamicOutputMultiplier() {
        return (double) module.getHostOutputMultiplier();
    }
}
