package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineHost;
import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule;
import org.gtlcore.gtlcore.utils.datastructure.ModuleRenderInfo;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import com.gtlsupb.gtlsupb.common.machine.GTLSupbMachines;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.MicrocosmFoundryModuleBase;
import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;
import com.gtlsupb.gtlsupb.config.ConfigHolder;
import com.gtlsupb.gtlsupb.utils.RainbowText;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MicrocosmFoundry extends ZeroEnergyMultiTypeMachine implements IMachineLife,
                              IModularMachineHost<MicrocosmFoundry> {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MicrocosmFoundry.class,
            ZeroEnergyMultiTypeMachine.MANAGED_FIELD_HOLDER);
    public static final long MULTIPLIER_PER_MODULE = 20L;

    public MicrocosmFoundry(IMachineBlockEntity holder) {
        super(holder, GTLSupbRecipeTypes.MICROCOSM_FOUNDRY_RECIPES);
    }

    private final Set<IModularMachineModule<MicrocosmFoundry, ?>> modules = new ReferenceOpenHashSet<>();

    @Override
    public Set<IModularMachineModule<MicrocosmFoundry, ?>> getModuleSet() {
        return modules;
    }

    @Override
    public BlockPos[] getModuleScanPositions() {
        int[][] offsets;
        switch (getFrontFacing()) {
            case WEST -> offsets = new int[][] { { -20, -8 }, { -12, 0 }, { -20, 8 }, { -28, 0 } };
            case SOUTH -> offsets = new int[][] { { 0, 12 }, { 8, 20 }, { 0, 28 }, { -8, 20 } };
            case NORTH -> offsets = new int[][] { { 0, -12 }, { -8, -20 }, { 0, -28 }, { 8, -20 } };
            default -> offsets = new int[][] { { 12, 0 }, { 20, 8 }, { 28, 0 }, { 20, -8 } };
        }
        BlockPos[] result = new BlockPos[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            result[i] = getPos().offset(offsets[i][0], -25, offsets[i][1]);
        }
        return result;
    }

    @Override
    public int getMaxModuleCount() {
        return 4;
    }

    @Override
    public List<ModuleRenderInfo> getModulesForRendering() {
        List<ModuleRenderInfo> list = new ArrayList<>();
        switch (getFrontFacing()) {
            case WEST -> {
                list.add(new ModuleRenderInfo(new BlockPos(-20, -25, -8), Direction.WEST, Direction.UP, Direction.SOUTH, Direction.UP, GTLSupbMachines.CREATION_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(-12, -25, 0), Direction.WEST, Direction.UP, Direction.WEST, Direction.UP, GTLSupbMachines.CREATION_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(-20, -25, 8), Direction.WEST, Direction.UP, Direction.NORTH, Direction.UP, GTLSupbMachines.CREATION_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(-28, -25, 0), Direction.WEST, Direction.UP, Direction.EAST, Direction.UP, GTLSupbMachines.CREATION_MODULE));
            }
            case SOUTH -> {
                list.add(new ModuleRenderInfo(new BlockPos(0, -25, 12), Direction.SOUTH, Direction.UP, Direction.SOUTH, Direction.UP, GTLSupbMachines.CREATION_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(8, -25, 20), Direction.SOUTH, Direction.UP, Direction.WEST, Direction.UP, GTLSupbMachines.CREATION_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(0, -25, 28), Direction.SOUTH, Direction.UP, Direction.NORTH, Direction.UP, GTLSupbMachines.CREATION_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(-8, -25, 20), Direction.SOUTH, Direction.UP, Direction.EAST, Direction.UP, GTLSupbMachines.CREATION_MODULE));
            }
            case NORTH -> {
                list.add(new ModuleRenderInfo(new BlockPos(0, -25, -12), Direction.NORTH, Direction.UP, Direction.NORTH, Direction.UP, GTLSupbMachines.CREATION_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(-8, -25, -20), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, GTLSupbMachines.CREATION_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(0, -25, -28), Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.UP, GTLSupbMachines.CREATION_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(8, -25, -20), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, GTLSupbMachines.CREATION_MODULE));
            }
            default -> {
                list.add(new ModuleRenderInfo(new BlockPos(12, -25, 0), Direction.EAST, Direction.UP, Direction.EAST, Direction.UP, GTLSupbMachines.CREATION_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(20, -25, 8), Direction.EAST, Direction.UP, Direction.NORTH, Direction.UP, GTLSupbMachines.CREATION_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(28, -25, 0), Direction.EAST, Direction.UP, Direction.WEST, Direction.UP, GTLSupbMachines.CREATION_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(20, -25, -8), Direction.EAST, Direction.UP, Direction.SOUTH, Direction.UP, GTLSupbMachines.CREATION_MODULE));
            }
        }
        return list;
    }

    public long getOutputMultiplier() {
        for (IModularMachineModule<MicrocosmFoundry, ?> m : modules) {
            if (m instanceof MicrocosmFoundryModuleBase base && base.isEternityModule()) {
                return ConfigHolder.INSTANCE.mfEternityOutputMultiplier;
            }
        }
        int count = getFormedModuleCount();
        if (count <= 0) return 1;
        return (long) count * MULTIPLIER_PER_MODULE;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new ZeroEnergyMultiTypeLogic<MicrocosmFoundry>(this) {

            {}

            @Override
            public double getDynamicOutputMultiplier() {
                return (double) MicrocosmFoundry.this.getOutputMultiplier();
            }
        };
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        safeClearModules();
        scanAndConnectModules();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        safeClearModules();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        safeClearModules();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {

            addMachineModeDisplay(textList);

            int moduleCount = getFormedModuleCount();
            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.microcosm_foundry.modules_count",
                            RainbowText.createRainbowComponent(String.valueOf(moduleCount))).withStyle(ChatFormatting.GRAY));

            long multiplier = getOutputMultiplier();
            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.microcosm_foundry.modules_multiplier",
                            RainbowText.createRainbowComponent("×" + multiplier)).withStyle(ChatFormatting.GRAY));

            for (IModularMachineModule<MicrocosmFoundry, ?> m : modules) {
                if (m instanceof MicrocosmFoundryModuleBase base && base.isEternityModule()) {
                    textList.add(
                            Component.literal("§d§k||§r §6§l超因果永恒熵逆 §d§k||§r")
                                    .withStyle(ChatFormatting.GOLD));
                    break;
                }
            }

            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused").withStyle(ChatFormatting.GOLD));
            } else if (isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running").withStyle(ChatFormatting.GREEN));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
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

        getDefinition().getAdditionalDisplay().accept(this, textList);
        getParts().forEach(it -> {
            if (it instanceof IMultiPart) {
                ((IMultiPart) it).addMultiText(textList);
            }
        });
    }
}
