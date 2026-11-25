package com.gtlsupb.gtlsupb.common.recipe;

import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;

public final class GTLSupbRecipeTypes {

    private GTLSupbRecipeTypes() {}

    public static final GTRecipeType INTEGRATED_ORE_PROCESSOR = GTLRecipeTypes.INTEGRATED_ORE_PROCESSOR;

    public static final GTRecipeType DISTORT = GTLRecipeTypes.DISTORT_RECIPES;

    public static final GTRecipeType LARGE_CHEMICAL_REACTOR = GTRecipeTypes.LARGE_CHEMICAL_RECIPES;

    public static final GTRecipeType ADVANCED_CIRCUIT_ASSEMBLY_LINE = GTLRecipeTypes.CIRCUIT_ASSEMBLY_LINE_RECIPES;

    public static final GTRecipeType COSMOS_SIMULATION = GTLRecipeTypes.COSMOS_SIMULATION_RECIPES;

    public static final GTRecipeType SUPERCRITICAL_SYNTHESIS = GTLRecipeTypes.SPS_CRAFTING_RECIPES;

    public static final GTRecipeType HYPERDIMENSIONAL_STIRRING = GTLRecipeTypes.DIMENSIONALLY_TRANSCENDENT_MIXER_RECIPES;

    public static final GTRecipeType HOLOGRAPHIC_DATA_RECIPES = GTRecipeTypes.register("holographic_data_storage", GTRecipeTypes.ELECTRIC)
            .setMaxIOSize(1, 0, 0, 0)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CRYSTALLIZATION, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    public static final GTRecipeType FLOATING_POINT_COMPUTE_RECIPES = GTRecipeTypes.register("floating_point_computation", GTRecipeTypes.ELECTRIC)
            .setMaxIOSize(1, 0, 0, 0)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CRYSTALLIZATION, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COMPUTATION);

    public static final GTRecipeType MICROCOSM_FOUNDRY_RECIPES = GTRecipeTypes.register("microcosm_foundry", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(18, 18, 18, 18)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.SCIENCE);

    public static final GTRecipeType FORGING_HEAVEN_SHIP_RECIPES = GTRecipeTypes.register("forging_heaven_ship", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(18, 18, 18, 18)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.SCIENCE);

    public static final GTRecipeType COSMIC_SIPHON_RECIPES = GTRecipeTypes.register("cosmic_siphon", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(1, 16, 0, 16)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.SCIENCE);

    public static final GTRecipeType STEAM_SPACE_ORE_PROCESSOR = GTRecipeTypes.register("steam_space_ore_processor", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(2, 9, 0, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MACERATOR);

    public static final GTRecipeType UNIVERSAL_ORDER_RECIPES = GTRecipeTypes.register("universal_order", GTRecipeTypes.ELECTRIC)
            .setMaxIOSize(1, 0, 0, 0)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CRYSTALLIZATION, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    public static final GTRecipeType CHRONO_RESONANCE_RECIPES = GTRecipeTypes.register("chrono_resonance", GTRecipeTypes.ELECTRIC)
            .setMaxIOSize(1, 0, 0, 0)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CRYSTALLIZATION, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL);

    public static void init() {}
}
