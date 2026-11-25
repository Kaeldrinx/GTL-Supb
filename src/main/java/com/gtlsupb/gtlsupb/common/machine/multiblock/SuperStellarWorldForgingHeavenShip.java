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
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.SingularityModule;
import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;
import com.gtlsupb.gtlsupb.config.ConfigHolder;
import com.gtlsupb.gtlsupb.utils.RainbowText;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SuperStellarWorldForgingHeavenShip extends ZeroEnergyMultiTypeMachine implements IMachineLife,
                                                IModularMachineHost<SuperStellarWorldForgingHeavenShip> {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SuperStellarWorldForgingHeavenShip.class,
            ZeroEnergyMultiTypeMachine.MANAGED_FIELD_HOLDER);
    public static final long OUTPUT_MULTIPLIER = 20L;
    public static final long MAX_OUTPUT_MULTIPLIER = 1000L;

    public SuperStellarWorldForgingHeavenShip(IMachineBlockEntity holder) {
        super(holder, GTLSupbRecipeTypes.FORGING_HEAVEN_SHIP_RECIPES);
    }

    private final Set<IModularMachineModule<SuperStellarWorldForgingHeavenShip, ?>> modules = new ReferenceOpenHashSet<>();

    @Override
    public Set<IModularMachineModule<SuperStellarWorldForgingHeavenShip, ?>> getModuleSet() {
        return modules;
    }

    @Override
    public BlockPos[] getModuleScanPositions() {
        int[][] offsets;
        switch (getFrontFacing()) {
            case WEST -> offsets = new int[][] { { 26, -1 }, { 26, 1 }, { 31, -1 }, { 31, 1 } };
            case SOUTH -> offsets = new int[][] { { -1, -26 }, { 1, -26 }, { -1, -31 }, { 1, -31 } };
            case NORTH -> offsets = new int[][] { { 1, 26 }, { -1, 26 }, { 1, 31 }, { -1, 31 } };
            default -> offsets = new int[][] { { -26, 1 }, { -26, -1 }, { -31, 1 }, { -31, -1 } };
        }
        BlockPos[] result = new BlockPos[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            result[i] = getPos().offset(offsets[i][0], -2, offsets[i][1]);
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
                list.add(new ModuleRenderInfo(new BlockPos(26, -2, -1), Direction.WEST, Direction.UP, Direction.SOUTH, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(26, -2, 1), Direction.WEST, Direction.UP, Direction.NORTH, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(31, -2, -1), Direction.WEST, Direction.UP, Direction.SOUTH, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(31, -2, 1), Direction.WEST, Direction.UP, Direction.NORTH, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
            }
            case SOUTH -> {
                list.add(new ModuleRenderInfo(new BlockPos(-1, -2, -26), Direction.SOUTH, Direction.UP, Direction.EAST, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(1, -2, -26), Direction.SOUTH, Direction.UP, Direction.WEST, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(-1, -2, -31), Direction.SOUTH, Direction.UP, Direction.EAST, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(1, -2, -31), Direction.SOUTH, Direction.UP, Direction.WEST, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
            }
            case NORTH -> {
                list.add(new ModuleRenderInfo(new BlockPos(1, -2, 26), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(-1, -2, 26), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(1, -2, 31), Direction.NORTH, Direction.UP, Direction.WEST, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(-1, -2, 31), Direction.NORTH, Direction.UP, Direction.EAST, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
            }
            default -> {
                list.add(new ModuleRenderInfo(new BlockPos(-26, -2, 1), Direction.EAST, Direction.UP, Direction.NORTH, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(-26, -2, -1), Direction.EAST, Direction.UP, Direction.SOUTH, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(-31, -2, 1), Direction.EAST, Direction.UP, Direction.NORTH, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
                list.add(new ModuleRenderInfo(new BlockPos(-31, -2, -1), Direction.EAST, Direction.UP, Direction.SOUTH, Direction.UP, GTLSupbMachines.STAR_FORGE_MODULE));
            }
        }
        return list;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        SuperStellarWorldForgingHeavenShip machine = this;
        return new ZeroEnergyMultiTypeLogic<SuperStellarWorldForgingHeavenShip>(machine) {

            {}

            @Override
            public double getDynamicOutputMultiplier() {
                return SuperStellarWorldForgingHeavenShip.this.getOutputMultiplier();
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

    public long getOutputMultiplier() {
        for (IModularMachineModule<SuperStellarWorldForgingHeavenShip, ?> m : modules) {
            if (m instanceof SingularityModule) {
                return ConfigHolder.INSTANCE.fhsSingularityOutputMultiplier;
            }
        }
        int count = getFormedModuleCount();
        if (count <= 0) return 1;
        return Math.min((long) count * OUTPUT_MULTIPLIER, MAX_OUTPUT_MULTIPLIER);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {

            addMachineModeDisplay(textList);

            int moduleCount = getFormedModuleCount();
            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.forging_ship.modules_count",
                            RainbowText.createRainbowComponent(String.valueOf(moduleCount))).withStyle(ChatFormatting.GRAY));

            long multiplier = getOutputMultiplier();
            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.forging_ship.output_multiplier",
                            RainbowText.createRainbowComponent("×" + multiplier)).withStyle(ChatFormatting.GRAY));

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
