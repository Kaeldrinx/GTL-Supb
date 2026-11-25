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
import com.gtlsupb.gtlsupb.common.machine.multiblock.MicrocosmFoundry;
import com.gtlsupb.gtlsupb.common.machine.multiblock.ZeroEnergyMultiTypeLogic;
import com.gtlsupb.gtlsupb.common.machine.multiblock.ZeroEnergyMultiTypeMachine;
import com.gtlsupb.gtlsupb.utils.RainbowText;
import com.gtlsupb.gtlsupb.utils.RecipeCalculationHelper;

import java.util.List;

public abstract class MicrocosmFoundryModuleBase extends ZeroEnergyMultiTypeMachine
                                                 implements IModularMachineModule<MicrocosmFoundry, MicrocosmFoundryModuleBase> {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MicrocosmFoundryModuleBase.class,
            ZeroEnergyMultiTypeMachine.MANAGED_FIELD_HOLDER);

    public MicrocosmFoundryModuleBase(IMachineBlockEntity holder, GTRecipeType... recipeTypes) {
        super(holder, recipeTypes);
    }

    public abstract GTRecipeType[] getModuleRecipeTypes();

    public boolean isEternityModule() {
        return false;
    }

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

    private MicrocosmFoundry host;

    @Override
    public BlockPos getHostPosition() {
        return hostPosition;
    }

    @Override
    public void setHostPosition(BlockPos pos) {
        this.hostPosition = pos;
    }

    @Override
    public MicrocosmFoundry getHost() {
        return host;
    }

    @Override
    public void setHost(MicrocosmFoundry host) {
        this.host = host;
    }

    @Override
    public Class<MicrocosmFoundry> getHostType() {
        return MicrocosmFoundry.class;
    }

    @Override
    public BlockPos[] getHostScanPositions() {
        return new BlockPos[] {

                getPos().offset(-12, 25, 0), getPos().offset(-20, 25, -8),
                getPos().offset(-28, 25, 0), getPos().offset(-20, 25, 8),

                getPos().offset(20, 25, 8), getPos().offset(12, 25, 0),
                getPos().offset(20, 25, -8), getPos().offset(28, 25, 0),

                getPos().offset(0, 25, -12), getPos().offset(-8, 25, -20),
                getPos().offset(0, 25, -28), getPos().offset(8, 25, -20),

                getPos().offset(0, 25, 12), getPos().offset(8, 25, 20),
                getPos().offset(0, 25, 28), getPos().offset(-8, 25, 20),
        };
    }

    @Override
    public int getMaxParallel() {
        return Integer.MAX_VALUE;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new MicrocosmFoundryModuleLogic(this);
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
    public void onConnected(MicrocosmFoundry host) {}

    @Override
    public void onDisconnected() {}

    public long getHostOutputMultiplier() {
        return host != null ? host.getOutputMultiplier() : 1L;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            MicrocosmFoundry h = host;
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

class MicrocosmFoundryModuleLogic extends ZeroEnergyMultiTypeLogic<MicrocosmFoundryModuleBase> {

    private static final int INPUT_CHANCE_RATIO = 10;

    private final MicrocosmFoundryModuleBase module;

    public MicrocosmFoundryModuleLogic(MicrocosmFoundryModuleBase machine) {
        super(machine);
        this.module = machine;
    }

    @Override
    public boolean checkBeforeWorking() {
        MicrocosmFoundry host = module.getHost();
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
