package com.gtlsupb.gtlsupb.api.registry;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;

import net.minecraft.network.chat.Component;

public final class GTLSupbMultiblockMachineBuilder {

    private GTLSupbMultiblockMachineBuilder() {}

    public static MultiblockMachineBuilder allRotation(MultiblockMachineBuilder builder) {
        return builder.rotationState(RotationState.ALL);
    }

    public static MultiblockMachineBuilder nonYAxisRotation(MultiblockMachineBuilder builder) {
        return builder.rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false);
    }

    public static MultiblockMachineBuilder noneRotation(MultiblockMachineBuilder builder) {
        return builder.rotationState(RotationState.NONE).allowExtendedFacing(false).allowFlip(false);
    }

    public static MultiblockMachineBuilder tooltipTextKey(MultiblockMachineBuilder builder, Component key) {
        return builder.tooltips(key);
    }

    public static MultiblockMachineBuilder tooltipTextMaxParallels(MultiblockMachineBuilder builder, Object parallel) {
        return builder.tooltips(Component.translatable("gtceu.multiblock.max_parallel", parallel));
    }

    public static MultiblockMachineBuilder tooltipTextRecipeTypes(MultiblockMachineBuilder builder, GTRecipeType... recipeTypes) {
        Component[] components = new Component[recipeTypes.length];
        for (int i = 0; i < recipeTypes.length; i++) {
            components[i] = Component.translatable(recipeTypes[i].registryName.toLanguageKey());
        }
        return builder.tooltips(Component.translatable("gtceu.machine.available_recipe_map_" + recipeTypes.length + ".tooltip", (Object[]) components));
    }

    public static MultiblockMachineBuilder tooltipTextCoilParallel(MultiblockMachineBuilder builder) {
        return builder.tooltips(Component.translatable("gtceu.multiblock.coil_parallel"));
    }

    public static MultiblockMachineBuilder tooltipTextLaser(MultiblockMachineBuilder builder) {
        return builder.tooltips(Component.translatable("gtceu.multiblock.laser.tooltip"));
    }

    public static MultiblockMachineBuilder tooltipOnlyTextLaser(MultiblockMachineBuilder builder) {
        return builder.tooltips(Component.translatable("gtceu.multiblock.only.laser.tooltip"));
    }

    public static MultiblockMachineBuilder tooltipTextMultiRecipes(MultiblockMachineBuilder builder) {
        return builder.tooltips(Component.translatable("gtceu.machine.multiple_recipes.tooltip"));
    }

    public static MultiblockMachineBuilder tooltipTextParallelHatch(MultiblockMachineBuilder builder) {
        return builder.tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"));
    }

    public static MultiblockMachineBuilder tooltipTextPerfectOverclock(MultiblockMachineBuilder builder) {
        return builder.tooltips(Component.translatable("gtceu.machine.perfect_oc"));
    }
}
