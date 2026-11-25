package com.gtlsupb.gtlsupb.common.machine;

import org.gtlcore.gtlcore.GTLCore;
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;
import org.gtlcore.gtlcore.utils.Registries;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.OverlaySteamMachineRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer;
import com.gregtechceu.gtceu.common.data.GCyMRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.resources.ResourceLocation;

import com.gtlsupb.gtlsupb.GTLSupb;
import com.gtlsupb.gtlsupb.api.machine.GTLSupbPartAbilities;
import com.gtlsupb.gtlsupb.api.pattern.GTLSupbPredicates;
import com.gtlsupb.gtlsupb.api.registry.GTLSupbRegistration;
import com.gtlsupb.gtlsupb.client.renderer.machine.MicrocosmFoundryRenderer;
import com.gtlsupb.gtlsupb.common.data.machines.multiblock.ChronoModulePattern;
import com.gtlsupb.gtlsupb.common.data.machines.multiblock.CosmicMatterSiphonerPattern;
import com.gtlsupb.gtlsupb.common.data.machines.multiblock.MaxPattern;
import com.gtlsupb.gtlsupb.common.data.machines.multiblock.MicrocosmPattern;
import com.gtlsupb.gtlsupb.common.data.machines.multiblock.QFTPattern;
import com.gtlsupb.gtlsupb.common.data.machines.multiblock.RankDerivationAssemblyMatrixPattern;
import com.gtlsupb.gtlsupb.common.data.machines.multiblock.SuperStellarWorldForgingHeavenShipPattern;
import com.gtlsupb.gtlsupb.common.data.machines.multiblock.TianjiPattern;
import com.gtlsupb.gtlsupb.common.data.machines.multiblock.UniversalOrderKeeperPattern;
import com.gtlsupb.gtlsupb.common.data.machines.multiblock.XuyanhuaxueshihaiyiPattern;
import com.gtlsupb.gtlsupb.common.machine.generator.VoidEnergyAbsorberMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.AbyssalChemicalEroderMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.AlchemicalForgeMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.CosmicMatterSiphonerMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.FloatingPointEngineMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.FragmentWorldCollector;
import com.gtlsupb.gtlsupb.common.machine.multiblock.GTLSupbMolecularAssembler;
import com.gtlsupb.gtlsupb.common.machine.multiblock.GTLSupbNoEnergyIntegratedProcessor;
import com.gtlsupb.gtlsupb.common.machine.multiblock.HolographicStorageArrayMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.HypercubePhaseStirringCriticalPolymerizationCoreTower;
import com.gtlsupb.gtlsupb.common.machine.multiblock.HyperdimensionalEnergyStabilizationHub;
import com.gtlsupb.gtlsupb.common.machine.multiblock.LingZhuMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.MicrocosmFoundry;
import com.gtlsupb.gtlsupb.common.machine.multiblock.MoWanMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.OmniversalEye;
import com.gtlsupb.gtlsupb.common.machine.multiblock.PrimitiveStoneFurnace;
import com.gtlsupb.gtlsupb.common.machine.multiblock.RankDerivationAssemblyMatrix;
import com.gtlsupb.gtlsupb.common.machine.multiblock.SteamModularUniversalFactory;
import com.gtlsupb.gtlsupb.common.machine.multiblock.SuperStellarWorldForgingHeavenShip;
import com.gtlsupb.gtlsupb.common.machine.multiblock.UniversalFactoryMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.UniversalOrderKeeper;
import com.gtlsupb.gtlsupb.common.machine.multiblock.UniversalVoltageRegulator;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.CelestialModule;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.ChronoDeductionArray;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.ChronoResonanceAmplifier;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.CreationModule;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.EternityModule;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.GenesisModule;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.SingularityModule;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.StarForgeModule;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.SteamUniversalModule;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.VoidModule;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.WorldModule;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.EfficiencyHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.EnergyBoostHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.HugeSteamHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.MEDrivePartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.MEMufflerPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverclockHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitComputeReceiveHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitDataReceiveHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitFTBQuestIntegrationHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitIntegratedHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitQuantumEntanglementHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.PatternAssemblyPartMachine;
import com.gtlsupb.gtlsupb.common.modify.GTLSupbCreativeModeTabs;
import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;
import com.gtlsupb.gtlsupb.config.ConfigHolder;
import com.gtlsupb.gtlsupb.util.GTLSupbTips;

import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

public class GTLSupbMachines {

    public static MultiblockMachineDefinition STEAM_ORE_INTEGRATED_HUB;

    public static MachineDefinition HUGE_STEAM_HATCH;

    public static MachineDefinition ULTIMATE_MAINTENANCE_HATCH;

    public static MultiblockMachineDefinition ABYSSAL_CHEMICAL_ERODER;

    public static MultiblockMachineDefinition RANK_DERIVATION_ASSEMBLY_MATRIX;

    public static MultiblockMachineDefinition OMNIVERSAL_EYE;

    public static MultiblockMachineDefinition PRIMITIVE_STONE_FURNACE;
    public static MultiblockMachineDefinition FRAGMENT_WORLD_COLLECTOR;

    public static MachineDefinition[] VOID_ENERGY_ABSORBER;

    public static MultiblockMachineDefinition QUANTUM_WEAVER;

    public static MultiblockMachineDefinition UNIVERSAL_FACTORY;

    public static MultiblockMachineDefinition UNIVERSAL_VOLTAGE_REGULATOR;

    public static MultiblockMachineDefinition HYPERCUBE_CORE_TOWER;

    public static MultiblockMachineDefinition DIMENSIONAL_ENERGY_STABILIZATION_HUB;

    public static MultiblockMachineDefinition INFINITE_WIRELESS_ENERGY_HUB;

    public static MultiblockMachineDefinition MICROCOSM_FOUNDRY;

    public static MultiblockMachineDefinition FORGING_HEAVEN_SHIP;

    public static MultiblockMachineDefinition STAR_FORGE_MODULE;
    public static MultiblockMachineDefinition CELESTIAL_MODULE;
    public static MultiblockMachineDefinition WORLD_MODULE;
    public static MultiblockMachineDefinition SINGULARITY_MODULE;

    public static MultiblockMachineDefinition LING_ZHU;

    public static MultiblockMachineDefinition MO_WAN;

    public static MultiblockMachineDefinition ALCHEMICAL_FORGE;

    public static MultiblockMachineDefinition CREATION_MODULE;
    public static MultiblockMachineDefinition ETERNITY_MODULE;
    public static MultiblockMachineDefinition GENESIS_MODULE;
    public static MultiblockMachineDefinition VOID_MODULE;

    public static MultiblockMachineDefinition STEAM_MODULAR_FACTORY;
    public static MultiblockMachineDefinition STEAM_MODULE;

    public static MachineDefinition ME_DRIVE;
    public static MachineDefinition ME_MUFFLER_HATCH;
    public static MachineDefinition OVERLIMIT_INTEGRATED_HATCH;
    public static MachineDefinition PATTERN_ASSEMBLY;

    public static MachineDefinition[] OVERCLOCK_HATCH;
    public static MachineDefinition[] EFFICIENCY_HATCH;
    public static MachineDefinition[] ENERGY_BOOST_HATCH;

    public static MachineDefinition OVERLIMIT_FTB_QUEST_INTEGRATION_HATCH;

    public static MachineDefinition OVERLIMIT_QUANTUM_ENTANGLEMENT_HATCH;

    public static MultiblockMachineDefinition HOLOGRAPHIC_STORAGE_ARRAY;
    public static MultiblockMachineDefinition FLOATING_POINT_ENGINE;

    public static MachineDefinition OVERLIMIT_DATA_RECEIVE_HATCH;
    public static MachineDefinition OVERLIMIT_COMPUTE_RECEIVE_HATCH;

    public static MultiblockMachineDefinition COSMIC_MATTER_SIPHONER;

    public static MultiblockMachineDefinition CHRONO_DEDUCTION_ARRAY;
    public static MultiblockMachineDefinition CHRONO_RESONANCE_AMPLIFIER;
    public static MultiblockMachineDefinition UNIVERSAL_ORDER_KEEPER;

    private static final ResourceLocation MOLECULAR_CASING = GTLCore.id("block/molecular_casing");

    private static OverlayTieredMachineRenderer molecularCasingPartRenderer(ResourceLocation frontModel) {
        OverlayTieredMachineRenderer renderer = new OverlayTieredMachineRenderer(GTValues.MAX, frontModel);
        renderer.setTextureOverride(Map.of(
                "bottom", MOLECULAR_CASING,
                "top", MOLECULAR_CASING,
                "side", MOLECULAR_CASING));
        return renderer;
    }

    public static void init() {
        GTLSupbRegistration.REGISTRATE.creativeModeTab(() -> GTLSupbCreativeModeTabs.GTLSUPB_MACHINE);

        HUGE_STEAM_HATCH = GTLSupbRegistration.REGISTRATE
                .machine("ultimate_steam_boost_hatch", (holder) -> new HugeSteamHatchPartMachine(holder))
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.STEAM)
                .renderer(() -> new OverlaySteamMachineRenderer(
                        GTLSupb.id("block/machine/part/ultimate_steam_boost_hatch")))
                .register();

        STEAM_ORE_INTEGRATED_HUB = GTLSupbRegistration.REGISTRATE.multiblock("steam_ore_integrated_hub", GTLSupbNoEnergyIntegratedProcessor::new)
                .rotationState(RotationState.ALL)
                .recipeTypes(GTLSupbRecipeTypes.STEAM_SPACE_ORE_PROCESSOR)
                .appearanceBlock(() -> Registries.getBlock("gtceu:steam_machine_casing"))

                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("BBBCBBB", " DE ED ", " D   D ", " D   D ", " D   D ", " DE ED ", "BBBCBBB")
                        .aisle("BBBCBBB", "D     D", "D     D", "D     D", "D     D", "D     D", "BBBCBBB")
                        .aisle("BBBCBBB", "E     E", "       ", "       ", "       ", "E     E", "BBBCBBB")
                        .aisle("CCCCCCC", "       ", "       ", "       ", "       ", "       ", "CCCCCCC")
                        .aisle("BBBCBBB", "E     E", "       ", "       ", "       ", "E     E", "BBBCBBB")
                        .aisle("BBBCBBB", "D     D", "D     D", "D     D", "D     D", "D     D", "BBBCBBB")
                        .aisle("BBBCBBB", " DEAED ", " DAAAD ", " DA~AD ", " DAAAD ", " DEAED ", "BBBCBBB")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('C', Predicates.blocks(Registries.getBlock("gtceu:bronze_pipe_casing")))
                        .where('D', Predicates.blocks(Registries.getBlock("gtceu:bronze_frame")))
                        .where('E', Predicates.blocks(Registries.getBlock("kubejs:steam_assembly_block")))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:steam_machine_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(GTLSupbPartAbilities.OVERCLOCK).setPreviewCount(1))
                                .or(Predicates.abilities(GTLSupbPartAbilities.EFFICIENCY).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtceu:bronze_firebox_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                        GTCEu.id("block/multiblock/steam_mixer"),
                        false)
                .tooltips(
                        GTLSupbTips.tip("steam_ore_integrated_hub", 0))
                .register();

        ABYSSAL_CHEMICAL_ERODER = GTLSupbRegistration.REGISTRATE.multiblock("abyssal_chemical_eroder", AbyssalChemicalEroderMachine::new)
                .rotationState(RotationState.ALL)
                .recipeTypes(GTLSupbRecipeTypes.DISTORT, GTRecipeTypes.LARGE_CHEMICAL_RECIPES, GTLRecipeTypes.DECAY_HASTENER_RECIPES, GTLRecipeTypes.MASS_FABRICATOR_RECIPES, GTLRecipeTypes.NEUTRON_ACTIVATOR_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtceu:inert_machine_casing"))
                .pattern((definition) -> XuyanhuaxueshihaiyiPattern.PATTERN
                        .where('F', Predicates.blocks(Registries.getBlock("kubejs:annihilate_core")))
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('G', Predicates.blocks(Registries.getBlock("gtceu:ptfe_pipe_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtceu:polytetrafluoroethylene_frame")))
                        .where('C', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('D', Predicates.blocks(Registries.getBlock("kubejs:dimensional_stability_casing")))
                        .where('E', Predicates.blocks(Registries.getBlock("kubejs:dimensional_bridge_casing")))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('H', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('I', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('J', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('K', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('L', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('M', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('N', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('O', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('P', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('Q', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('R', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('S', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('T', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('U', Predicates.blocks(Registries.getBlock("kubejs:containment_field_generator")))
                        .where('V', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('W', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('X', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('Y', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where('Z', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('a', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('b', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('c', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('d', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('e', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('f', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('g', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where('h', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where('i', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where('j', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('k', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('l', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('m', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('n', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('o', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('p', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        RANK_DERIVATION_ASSEMBLY_MATRIX = GTLSupbRegistration.REGISTRATE.multiblock("rank_derivation_assembly_matrix", RankDerivationAssemblyMatrix::new)
                .rotationState(RotationState.ALL)
                .recipeTypes(
                        GTRecipeTypes.ASSEMBLER_RECIPES,
                        GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                        GTLRecipeTypes.PRECISION_ASSEMBLER_RECIPES,
                        GTLSupbRecipeTypes.ADVANCED_CIRCUIT_ASSEMBLY_LINE,
                        GTLRecipeTypes.COMPONENT_ASSEMBLY_LINE_RECIPES,
                        GTRecipeTypes.ASSEMBLY_LINE_RECIPES,
                        GTLRecipeTypes.SUPRACHRONAL_ASSEMBLY_LINE_RECIPES,
                        GTLRecipeTypes.ASSEMBLER_MODULE_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:dimensionally_transcendent_casing"))
                .pattern((definition) -> RankDerivationAssemblyMatrixPattern.PATTERN
                        .where('A', Predicates.blocks(Registries.getBlock("kubejs:hollow_casing")))
                        .where('B', Predicates.blocks(Registries.getBlock("kubejs:annihilate_core")))
                        .where('C', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('D', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('E', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('F', Predicates.blocks(Registries.getBlock("kubejs:dimensional_stability_casing")))
                        .where('G', Predicates.blocks(Registries.getBlock("kubejs:dimensional_bridge_casing")))
                        .where('H', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('I', Predicates.blocks(Registries.getBlock("gtlcore:enhance_hyper_mechanical_casing")))
                        .where('J', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where('K', Predicates.blocks(Registries.getBlock("gtlcore:qft_coil")))
                        .where('L', Predicates.blocks(Registries.getBlock("gtlcore:enhance_hyper_mechanical_casing")))
                        .where('M', Predicates.blocks(Registries.getBlock("gtceu:mithril_frame")))
                        .where('N', Predicates.blocks(Registries.getBlock("gtlcore:dimensionally_transcendent_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('O', Predicates.blocks(Registries.getBlock("kubejs:containment_field_generator")))
                        .where('P', Predicates.blocks(Registries.getBlock("gtceu:vibranium_frame")))
                        .where('Q', Predicates.blocks(Registries.getBlock("gtceu:quantanium_frame")))
                        .where('f', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('S', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('T', Predicates.blocks(Registries.getBlock("gtceu:advanced_computer_casing")))
                        .where('U', Predicates.blocks(Registries.getBlock("gtlcore:sps_casing")))
                        .where('V', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('W', Predicates.blocks(Registries.getBlock("gtlcore:enhance_hyper_mechanical_casing")))
                        .where('X', Predicates.blocks(Registries.getBlock("gtlcore:dimension_injection_casing")))
                        .where('Y', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('Z', Predicates.blocks(Registries.getBlock("kubejs:containment_field_generator")))
                        .where('a', Predicates.blocks(Registries.getBlock("kubejs:dimensional_bridge_casing")))
                        .where('b', Predicates.blocks(Registries.getBlock("gtlcore:molecular_casing")))
                        .where('c', Predicates.blocks(Registries.getBlock("gtlcore:enhance_hyper_mechanical_casing")))
                        .where('d', Predicates.blocks(Registries.getBlock("kubejs:spacetime_assembly_line_casing")))
                        .where('e', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('g', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('h', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('i', Predicates.blocks(Registries.getBlock("gtlcore:advanced_assembly_line_unit")))
                        .where('j', Predicates.blocks(Registries.getBlock("kubejs:machine_casing_circuit_assembly_line")))
                        .where('k', Predicates.blocks(Registries.getBlock("kubejs:spacetime_assembly_line_unit")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTLCore.id("block/casings/dimensionally_transcendent_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        OMNIVERSAL_EYE = GTLSupbRegistration.REGISTRATE.multiblock("omniversal_eye", OmniversalEye::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeTypes(GTLSupbRecipeTypes.COSMOS_SIMULATION)
                .appearanceBlock(() -> Registries.getBlock("gtceu:high_power_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "               A A               ", "            AAAAAAAAA            ", "               A A               ", "            AAAAAAAAA            ", "               A A               ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "               A A               ", "               A A               ", "              DDDDD              ", "             DDADADD             ", "         AAAADAADAADAAAA         ", "             DDDDDDD             ", "         AAAADAADAADAAAA         ", "             DDADADD             ", "              DDDDD              ", "               A A               ", "               A A               ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "               A A               ", "                D                ", "                D                ", "             DDDDDDD             ", "            DD     DD            ", "            D  EEE  D            ", "       AAA  D EFFFE D  AAA       ", "          DDD EFFFE DDD          ", "       AAA  D EFFFE D  AAA       ", "            D  EEE  D            ", "            DD     DD            ", "             DDDDDDD             ", "                D                ", "                D                ", "               A A               ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "                D                ", "                D                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "      AA                 AA      ", "        DD             DD        ", "      AA                 AA      ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                D                ", "                D                ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "              AAAAA              ", "                D                ", "                F                ", "                F                ", "                                 ", "                                 ", "                                 ", "                                 ", "      A                   A      ", "     AA                   AA     ", "      ADFF             FFDA      ", "     AA                   AA     ", "      A                   A      ", "                                 ", "                                 ", "                                 ", "                                 ", "                F                ", "                F                ", "                D                ", "              AAAAA              ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "                D                ", "             GEEFEEG             ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "       G                 G       ", "       E                 E       ", "    AA E                 E AA    ", "      DF                 FD      ", "    AA E                 E AA    ", "       E                 E       ", "       G                 G       ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "             GEEFEEG             ", "                D                ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "               A A               ", "              AAAAA              ", "                D                ", "                F                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "    A                       A    ", "   AA                       AA   ", "    ADF                   FDA    ", "   AA                       AA   ", "    A                       A    ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                F                ", "                D                ", "              AAAAA              ", "               A A               ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "               A A               ", "               A A               ", "                D                ", "             GEEFEEG             ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "     G                     G     ", "     E                     E     ", "  AA E                     E AA  ", "    DF                     FD    ", "  AA E                     E AA  ", "     E                     E     ", "     G                     G     ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "             GEEFEEG             ", "                D                ", "               A A               ", "               A A               ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "               A A               ", "                D                ", "                F                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  A                           A  ", "   DF                       FD   ", "  A                           A  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                F                ", "                D                ", "               A A               ", "                                 ", "                                 ")
                        .aisle("                                 ", "               A A               ", "               A A               ", "                D                ", "                F                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " AA                           AA ", "   DF                       FD   ", " AA                           AA ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                F                ", "                D                ", "               A A               ", "               A A               ", "                                 ")
                        .aisle("                                 ", "               A A               ", "                D                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " A                             A ", "  D                           D  ", " A                             A ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                D                ", "               A A               ", "                                 ")
                        .aisle("                                 ", "               A A               ", "                D                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " A                             A ", "  D                           D  ", " A                             A ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                D                ", "               A A               ", "                                 ")
                        .aisle("               A A               ", "               A A               ", "             DDDDDDD             ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  D                           D  ", "  D                           D  ", "AAD                           DAA", "  D                           D  ", "AAD                           DAA", "  D                           D  ", "  D                           D  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "             DDDDDDD             ", "               A A               ", "               A A               ")
                        .aisle("               A A               ", "              DDDDD              ", "            DD     DD            ", "                                 ", "                                 ", "       G                 G       ", "                                 ", "     G                     G     ", "                                 ", "                                 ", "                                 ", "                                 ", "  D                           D  ", "  D                           D  ", " D                             D ", "AD                             DA", " D                             D ", "AD                             DA", " D                             D ", "  D                           D  ", "  D                           D  ", "                                 ", "                                 ", "                                 ", "                                 ", "     G                     G     ", "                                 ", "       G                 G       ", "                                 ", "                                 ", "            DD     DD            ", "              DDDDD              ", "               A A               ")
                        .aisle("               A A               ", "             DDADADD             ", "            D  EEE  D            ", "                                 ", "      A                   A      ", "       E                 E       ", "    A                       A    ", "     E                     E     ", "                                 ", "                                 ", "                                 ", "                                 ", "  D                           D  ", " D                             D ", " D                             D ", "AAE                           EAA", " DE                           ED ", "AAE                           EAA", " D                             D ", " D                             D ", "  D                           D  ", "                                 ", "                                 ", "                                 ", "                                 ", "     E                     E     ", "    A                       A    ", "       E                 E       ", "      A                   A      ", "                                 ", "            D  EEE  D            ", "             DDADADD             ", "               A A               ")
                        .aisle("            AAAAAAAAA            ", "         AAAADAADAADAAAA         ", "       AAA  D EFFFE D  AAA       ", "      AA                 AA      ", "     AA                   AA     ", "    AA E                 E AA    ", "   AA                       AA   ", "  AA E                     E AA  ", "  A                           A  ", " AA                           AA ", " A                             A ", " A                             A ", "AAD                           DAA", "AD                             DA", "AAE                           EAA", "AAF                           FAA", "ADF                           FDA", "AAF                           FAA", "AAE                           EAA", "AD                             DA", "AAD                           DAA", " A                             A ", " A                             A ", " AA                           AA ", "  A                           A  ", "  AA E                     E AA  ", "   AA                       AA   ", "    AA E                 E AA    ", "     AA                   AA     ", "      AA                 AA      ", "       AAA  D EFFFE D  AAA       ", "         AAAADAADAADAAAA         ", "            AAAAAAAAA            ")
                        .aisle("               A A               ", "             DDDDDDD             ", "          DDD EFFFE DDD          ", "        DD             DD        ", "      ADFF             FFDA      ", "      DF                 FD      ", "    ADF                   FDA    ", "    DF                     FD    ", "   DF                       FD   ", "   DF                       FD   ", "  D                           D  ", "  D                           D  ", "  D                           D  ", " D                             D ", " DE                           ED ", "ADF                           FDA", " DF                           FD ", "ADF                           FDA", " DE                           ED ", " D                             D ", "  D                           D  ", "  D                           D  ", "  D                           D  ", "   DF                       FD   ", "   DF                       FD   ", "    DF                     FD    ", "    ADF                   FDA    ", "      DF                 FD      ", "      ADFF             FFDA      ", "        DD             DD        ", "          DDD EFFFE DDD          ", "             DDDDDDD             ", "               A A               ")
                        .aisle("            AAAAAAAAA            ", "         AAAADAADAADAAAA         ", "       AAA  D EFFFE D  AAA       ", "      AA                 AA      ", "     AA                   AA     ", "    AA E                 E AA    ", "   AA                       AA   ", "  AA E                     E AA  ", "  A                           A  ", " AA                           AA ", " A                             A ", " A                             A ", "AAD                           DAA", "AD                             DA", "AAE                           EAA", "AAF                           FAA", "ADF                           FDA", "AAF                           FAA", "AAE                           EAA", "AD                             DA", "AAD                           DAA", " A                             A ", " A                             A ", " AA                           AA ", "  A                           A  ", "  AA E                     E AA  ", "   AA                       AA   ", "    AA E                 E AA    ", "     AA                   AA     ", "      AA                 AA      ", "       AAA  D EFFFE D  AAA       ", "         AAAADAADAADAAAA         ", "            AAAAAAAAA            ")
                        .aisle("               A A               ", "             DDADADD             ", "            D  EEE  D            ", "                                 ", "      A                   A      ", "       E                 E       ", "    A                       A    ", "     E                     E     ", "                                 ", "                                 ", "                                 ", "                                 ", "  D                           D  ", " D                             D ", " D                             D ", "AAE                           EAA", " DE                           ED ", "AAE                           EAA", " D                             D ", " D                             D ", "  D                           D  ", "                                 ", "                                 ", "                                 ", "                                 ", "     E                     E     ", "    A                       A    ", "       E                 E       ", "      A                   A      ", "                                 ", "            D  EEE  D            ", "             DDADADD             ", "               A A               ")
                        .aisle("               A A               ", "              DDDDD              ", "            DD     DD            ", "                                 ", "                                 ", "       G                 G       ", "                                 ", "     G                     G     ", "                                 ", "                                 ", "                                 ", "                                 ", "  D                           D  ", "  D                           D  ", " D                             D ", "AD                             DA", " D                             D ", "AD                             DA", " D                             D ", "  D                           D  ", "  D                           D  ", "                                 ", "                                 ", "                                 ", "                                 ", "     G                     G     ", "                                 ", "       G                 G       ", "                                 ", "                                 ", "            DD     DD            ", "              DDDDD              ", "               A A               ")
                        .aisle("               A A               ", "               A A               ", "             DDDDDDD             ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  D                           D  ", "  D                           D  ", "AAD                           DAA", "  D                           D  ", "AAD                           DAA", "  D                           D  ", "  D                           D  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "             DDDDDDD             ", "               A A               ", "               A A               ")
                        .aisle("                                 ", "               A A               ", "                D                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " A                             A ", "  D                           D  ", " A                             A ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                D                ", "               A A               ", "                                 ")
                        .aisle("                                 ", "               A A               ", "                D                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " A                             A ", "  D                           D  ", " A                             A ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                D                ", "               A A               ", "                                 ")
                        .aisle("                                 ", "               A A               ", "               A A               ", "                D                ", "                F                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " AA                           AA ", "   DF                       FD   ", " AA                           AA ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                F                ", "                D                ", "               A A               ", "               A A               ", "                                 ")
                        .aisle("                                 ", "                                 ", "               A A               ", "                D                ", "                F                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  A                           A  ", "   DF                       FD   ", "  A                           A  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                F                ", "                D                ", "               A A               ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "               A A               ", "               A A               ", "                D                ", "             GEEFEEG             ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "     G                     G     ", "     E                     E     ", "  AA E                     E AA  ", "    DF                     FD    ", "  AA E                     E AA  ", "     E                     E     ", "     G                     G     ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "             GEEFEEG             ", "                D                ", "               A A               ", "               A A               ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "               A A               ", "              AAAAA              ", "                D                ", "                F                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "    A                       A    ", "   AA                       AA   ", "    ADF                   FDA    ", "   AA                       AA   ", "    A                       A    ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                F                ", "                D                ", "              AAAAA              ", "               A A               ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "                D                ", "             GEEFEEG             ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "       G                 G       ", "       E                 E       ", "    AA E                 E AA    ", "      DF                 FD      ", "    AA E                 E AA    ", "       E                 E       ", "       G                 G       ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "             GEEFEEG             ", "                D                ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "              AAAAA              ", "                D                ", "                F                ", "                F                ", "                                 ", "                                 ", "                                 ", "                                 ", "      A                   A      ", "     AA                   AA     ", "      ADFF             FFDA      ", "     AA                   AA     ", "      A                   A      ", "                                 ", "                                 ", "                                 ", "                                 ", "                F                ", "                F                ", "                D                ", "              AAAAA              ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "                D                ", "                D                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "      AA                 AA      ", "        DD             DD        ", "      AA                 AA      ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                D                ", "                D                ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "               A A               ", "                D                ", "                D                ", "             DDDDDDD             ", "            DD     DD            ", "            D  EEE  D            ", "       AAA  D EFFFE D  AAA       ", "          DDD EFFFE DDD          ", "       AAA  D EFFFE D  AAA       ", "            D  EEE  D            ", "            DD     DD            ", "             DDDDDDD             ", "                D                ", "                D                ", "               A A               ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "               A A               ", "               A A               ", "              DDDDD              ", "             DDADADD             ", "         AAAADAADAADAAAA         ", "             DDDDDDD             ", "         AAAADAADAADAAAA         ", "             DDADADD             ", "              DDDDD              ", "               A A               ", "               A A               ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ")
                        .aisle("                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "             AAAAAAA             ", "            AABBBBBAA            ", "            ABBBBBBBA            ", "            ABBAAABBA            ", "            ABBA~ABBA            ", "            ABBAAABBA            ", "            ABBBBBBBA            ", "            AABBBBBAA            ", "             AAAAAAA             ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:high_power_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtceu:high_power_casing")))
                        .where('D', Predicates.blocks(Registries.getBlock("gtceu:high_power_casing")))
                        .where('E', Predicates.heatingCoils())
                        .where('F', Predicates.blocks(Registries.getBlock("gtceu:high_power_casing")))
                        .where('G', Predicates.blocks(Registries.getBlock("gtceu:high_power_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/hpca/high_power_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        PRIMITIVE_STONE_FURNACE = GTLSupbRegistration.REGISTRATE.multiblock("primitive_stone_furnace", PrimitiveStoneFurnace::new)
                .rotationState(RotationState.ALL)
                .recipeTypes(GTRecipeTypes.FURNACE_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("minecraft:stone"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("AAA", "AAA", "AAA")
                        .aisle("AAA", "A A", "AAA")
                        .aisle("AAA", "A~A", "AAA")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("minecraft:stone"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        new ResourceLocation("minecraft", "block/stone"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        FRAGMENT_WORLD_COLLECTOR = GTLSupbRegistration.REGISTRATE.multiblock("fragment_world_collector", FragmentWorldCollector::new)
                .rotationState(RotationState.ALL)
                .recipeTypes(GTLRecipeTypes.FRAGMENT_WORLD_COLLECTION)
                .appearanceBlock(() -> Registries.getBlock("minecraft:dirt"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("AAA", "AAA", "AAA")
                        .aisle("AAA", "A#A", "AAA")
                        .aisle("AAA", "A~A", "AAA")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("minecraft:dirt"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1)))
                        .where('#', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        new ResourceLocation("minecraft", "block/dirt"),
                        GTCEu.id("block/multiblock/primitive_blast_furnace"))
                .tooltips(
                        GTLSupbTips.tip("fragment_world_collector", 0))
                .register();

        VOID_ENERGY_ABSORBER = registerTieredMachines(
                "void_energy_absorber",
                (holder, tier) -> (MetaMachine) new VoidEnergyAbsorberMachine(holder, tier),
                (tier, builder) -> builder
                        .langValue(GTValues.VLVH[tier] + " Void Energy Absorber " + GTValues.VLVT[tier])
                        .rotationState(RotationState.NON_Y_AXIS)

                        .renderer(() -> new OverlayTieredMachineRenderer(tier,
                                GTLSupb.id("block/machine/part/void_energy_absorber")))
                        .register(),
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);

        QUANTUM_WEAVER = GTLSupbRegistration.REGISTRATE.multiblock("quantum_weaver", GTLSupbMolecularAssembler::new)
                .rotationState(RotationState.ALL)
                .recipeType(GTLRecipeTypes.MOLECULAR_ASSEMBLER)
                .appearanceBlock(() -> Registries.getBlock("gtceu:high_power_casing"))
                .pattern((definition) -> QFTPattern.PATTERN
                        .where('C', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('B', Predicates.blocks(Registries.getBlock("gtceu:high_power_casing")))
                        .where('D', Predicates.blocks(Registries.getBlock("gtceu:advanced_computer_casing")))
                        .where('E', Predicates.blocks(Registries.getBlock("gtceu:superconducting_coil")))
                        .where('F', Predicates.blocks(Registries.getBlock("gtceu:assembly_line_casing")))
                        .where('G', Predicates.blocks(Registries.getBlock("gtceu:assembly_line_grating")))
                        .where('H', Predicates.blocks(Registries.getBlock("gtceu:large_scale_assembler_casing")))
                        .where('I', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('J', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('K', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('L', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('M', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('N', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('O', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('P', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('Q', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('R', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('S', Predicates.blocks(Registries.getBlock("gtceu:europium_frame")))
                        .where('T', Predicates.blocks(Registries.getBlock("gtceu:me_molecular_assembler_io")))
                        .where('U', Predicates.blocks(Registries.getBlock("gtceu:trinium_frame")))
                        .where('V', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where('W', Predicates.blocks(Registries.getBlock("gtceu:naquadah_alloy_frame")))
                        .where('X', Predicates.blocks(Registries.getBlock("gtceu:hsse_frame")))
                        .where('Y', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where('Z', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where('a', Predicates.blocks(Registries.getBlock("gtceu:me_craft_pattern_container")).or(Predicates.air()))
                        .where('b', Predicates.blocks(Registries.getBlock("gtlcore:molecular_casing")))
                        .where('c', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('d', Predicates.blocks(Registries.getBlock("gtlcore:advanced_assembly_line_unit")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/hpca/high_power_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .tooltips(
                        GTLSupbTips.tip("quantum_weaver", 0))
                .register();

        UNIVERSAL_FACTORY = GTLSupbRegistration.REGISTRATE.multiblock("universal_factory", UniversalFactoryMachine::new)
                .rotationState(RotationState.ALL)
                .recipeTypes(GTRecipeTypes.BENDER_RECIPES, GTRecipeTypes.COMPRESSOR_RECIPES,
                        GTRecipeTypes.FORGE_HAMMER_RECIPES, GTRecipeTypes.CUTTER_RECIPES,
                        GTRecipeTypes.EXTRUDER_RECIPES, GTRecipeTypes.LATHE_RECIPES,
                        GTRecipeTypes.WIREMILL_RECIPES, GTRecipeTypes.FORMING_PRESS_RECIPES,
                        GTRecipeTypes.POLARIZER_RECIPES, GTRecipeTypes.LASER_ENGRAVER_RECIPES,
                        GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES,
                        GTRecipeTypes.ARC_FURNACE_RECIPES, GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                        GTRecipeTypes.CANNER_RECIPES, GTRecipeTypes.CENTRIFUGE_RECIPES,
                        GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES, GTRecipeTypes.ELECTROLYZER_RECIPES,
                        GTRecipeTypes.SIFTER_RECIPES, GTRecipeTypes.MACERATOR_RECIPES,
                        GTRecipeTypes.EXTRACTOR_RECIPES, GTRecipeTypes.CHEMICAL_RECIPES,
                        GTRecipeTypes.MIXER_RECIPES, GTRecipeTypes.CHEMICAL_BATH_RECIPES,
                        GTRecipeTypes.ORE_WASHER_RECIPES, GTLRecipeTypes.DEHYDRATOR_RECIPES,
                        GTRecipeTypes.LARGE_CHEMICAL_RECIPES, GTRecipeTypes.PACKER_RECIPES,
                        GTRecipeTypes.DISTILLERY_RECIPES, GTRecipeTypes.AUTOCLAVE_RECIPES,
                        GTRecipeTypes.FLUID_HEATER_RECIPES, GTRecipeTypes.BREWING_RECIPES,
                        GTRecipeTypes.FERMENTING_RECIPES, GTLRecipeTypes.LIGHTNING_PROCESSOR_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:multi_functional_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("AAA", "AAA", "AAA")
                        .aisle("AAA", "ABA", "AAA")
                        .aisle("AAA", "A~A", "AAA")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtceu:steel_frame")))
                        .where('#', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTLCore.id("block/multi_functional_casing"),
                        GTCEu.id("block/multiblock/assembly_line"))
                .tooltips(
                        GTLSupbTips.tip("universal_factory", 0))
                .register();

        UNIVERSAL_VOLTAGE_REGULATOR = GTLSupbRegistration.REGISTRATE.multiblock("universal_voltage_regulator", UniversalVoltageRegulator::new)
                .rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:multi_functional_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("AAA", "AAA", "AAA")
                        .aisle("AAA", "ABA", "AAA")
                        .aisle("AAA", "A~A", "AAA")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('B', Predicates.blocks(Registries.getBlock("gtceu:steel_frame")))
                        .where('#', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTLCore.id("block/multi_functional_casing"),
                        GTCEu.id("block/multiblock/assembly_line"))
                .tooltips(
                        GTLSupbTips.tip("universal_voltage_regulator", 0),
                        GTLSupbTips.tip("universal_voltage_regulator", 1))
                .register();

        DIMENSIONAL_ENERGY_STABILIZATION_HUB = GTLSupbRegistration.REGISTRATE.multiblock("dimensional_energy_stabilization_hub", HyperdimensionalEnergyStabilizationHub::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .tooltips(GTLSupbTips.tip("dimensional_stabilizer", 0))
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:multi_functional_casing"))
                .pattern((definition) -> TianjiPattern.PATTERN
                        .where('c', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('B', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('C', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('D', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('E', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('F', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('G', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('I', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('J', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('K', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('L', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('M', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('N', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('O', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('P', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('Q', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('R', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('S', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('T', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('U', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('V', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('W', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('X', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('Y', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('Z', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('a', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('b', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('H', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('d', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('e', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where('f', Predicates.blocks(Registries.getBlock("gtlcore:multi_functional_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTLCore.id("block/multi_functional_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        HYPERCUBE_CORE_TOWER = GTLSupbRegistration.REGISTRATE.multiblock("hypercube_core_tower", HypercubePhaseStirringCriticalPolymerizationCoreTower::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeTypes(GTLSupbRecipeTypes.SUPERCRITICAL_SYNTHESIS, GTLSupbRecipeTypes.HYPERDIMENSIONAL_STIRRING)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:dimensionally_transcendent_casing"))
                .pattern((definition) -> MaxPattern.PATTERN
                        .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtlcore:dimensionally_transcendent_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('C', Predicates.blocks(Registries.getBlock("kubejs:annihilate_core")))
                        .where('D', Predicates.blocks(Registries.getBlock("gtlcore:dimension_connection_casing")))
                        .where('E', Predicates.blocks(Registries.getBlock("gtlcore:space_elevator_support")))
                        .where('F', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('G', Predicates.blocks(Registries.getBlock("gtlcore:ultimate_stellar_containment_casing")))
                        .where('H', Predicates.blocks(Registries.getBlock("gtlcore:graviton_field_constraint_casing")))
                        .where('I', Predicates.blocks(Registries.getBlock("kubejs:containment_field_generator")))
                        .where('J', Predicates.blocks(Registries.getBlock("gtlcore:infinity_glass")))
                        .where('K', Predicates.blocks(Registries.getBlock("kubejs:hollow_casing")))
                        .where('L', Predicates.blocks(Registries.getBlock("gtlcore:dimension_injection_casing")))
                        .where('M', Predicates.blocks(Registries.getBlock("kubejs:dimension_creation_casing")))
                        .where('N', Predicates.blocks(Registries.getBlock("gtlcore:qft_coil")))
                        .where('O', Predicates.blocks(Registries.getBlock("kubejs:eternity_coil_block")))
                        .where('P', Predicates.blocks(Registries.getBlock("kubejs:dimensional_bridge_casing")))
                        .where('U', Predicates.blocks(Registries.getBlock("kubejs:spacetime_compression_field_generator")))
                        .where('T', Predicates.blocks(Registries.getBlock("kubejs:dimensional_stability_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTLCore.id("block/casings/dimensionally_transcendent_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .tooltips(
                        GTLSupbTips.tip("hypercube_core_tower", 0))
                .register();

        MICROCOSM_FOUNDRY = GTLSupbRegistration.REGISTRATE
                .multiblock("microcosm_foundry", MicrocosmFoundry::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeType(GTLSupbRecipeTypes.MICROCOSM_FOUNDRY_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:molecular_casing"))
                .pattern((definition) -> MicrocosmPattern.PATTERN
                        .where('l', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:inert_machine_casing")))
                        .where('B', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('k', Predicates.blocks(Registries.getBlock("gtlcore:molecular_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('D', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('E', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('F', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('G', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('H', Predicates.blocks(Registries.getBlock("kubejs:dimensional_bridge_casing")))
                        .where('I', Predicates.blocks(Registries.getBlock("kubejs:dimensional_stability_casing")))
                        .where('J', Predicates.blocks(Registries.getBlock("kubejs:dimensional_stability_casing")))
                        .where('K', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('L', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('M', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('N', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('O', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where('P', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('Q', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where('R', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('S', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('T', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('U', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('V', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('W', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('X', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('Y', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('Z', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('a', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('b', Predicates.blocks(Registries.getBlock("kubejs:containment_field_generator")))
                        .where('d', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('e', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('f', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('g', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('h', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where('i', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('j', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('C', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('c', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTLCore.id("block/molecular_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .renderer(MicrocosmFoundryRenderer::new)
                .hasTESR(true)
                .tooltips(
                        GTLSupbTips.tip("microcosm_foundry", 0),
                        GTLSupbTips.tip("microcosm_foundry", 1))
                .register();

        FORGING_HEAVEN_SHIP = GTLSupbRegistration.REGISTRATE
                .multiblock("forging_heaven_ship", SuperStellarWorldForgingHeavenShip::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeType(GTLSupbRecipeTypes.FORGING_HEAVEN_SHIP_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:hyper_mechanical_casing"))
                .pattern((definition) -> SuperStellarWorldForgingHeavenShipPattern.PATTERN
                        .where('Y', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtlcore:iridium_casing")))
                        .where('B', Predicates.blocks(Registries.getBlock("gtlcore:fusion_casing_mk5")))
                        .where('C', Predicates.blocks(Registries.getBlock("gtceu:high_temperature_smelting_casing")))
                        .where('D', Predicates.blocks(Registries.getBlock("gtlcore:compressed_fusion_coil_mk2")))
                        .where('E', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('F', Predicates.blocks(Registries.getBlock("gtceu:heat_vent")))
                        .where('G', Predicates.blocks(Registries.getBlock("gtceu:naquadah_alloy_frame")))
                        .where('H', Predicates.blocks(Registries.getBlock("gtceu:atomic_casing")))
                        .where('I', Predicates.blocks(Registries.getBlock("gtceu:tungsten_frame")))
                        .where('J', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where('K', Predicates.blocks(Registries.getBlock("kubejs:neutronium_pipe_casing")))
                        .where('L', Predicates.blocks(Registries.getBlock("gtlcore:dimension_injection_casing")))
                        .where('M', Predicates.blocks(Registries.getBlock("gtceu:high_power_casing")))
                        .where('N', Predicates.blocks(Registries.getBlock("gtlcore:enhance_hyper_mechanical_casing")))
                        .where('O', Predicates.blocks(Registries.getBlock("gtlcore:molecular_casing")))
                        .where('P', Predicates.blocks(Registries.getBlock("gtlcore:sps_casing")))
                        .where('Q', Predicates.blocks(Registries.getBlock("gtlcore:naquadah_alloy_casing")))
                        .where('R', Predicates.blocks(Registries.getBlock("gtlcore:hyper_core")))
                        .where('S', Predicates.blocks(Registries.getBlock("kubejs:containment_field_generator")))
                        .where('T', Predicates.blocks(Registries.getBlock("gtceu:advanced_computer_casing")))
                        .where('U', Predicates.blocks(Registries.getBlock("kubejs:hollow_casing")))
                        .where('V', Predicates.blocks(Registries.getBlock("kubejs:molecular_coil")))
                        .where('W', Predicates.blocks(Registries.getBlock("kubejs:annihilate_core")))
                        .where('X', Predicates.blocks(Registries.getBlock("gtceu:computer_heat_vent")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTLCore.id("block/casings/hyper_mechanical_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .tooltips(
                        GTLSupbTips.tip("forging_heaven_ship", 0))
                .register();

        ETERNITY_MODULE = GTLSupbRegistration.REGISTRATE
                .multiblock("eternity_module", EternityModule::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:molecular_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("BBBBB", "AAAAA", "AAAAA", "AAAAA")
                        .aisle("BBBBB", "AAAAA", "AAAAA", "AAAAA")
                        .aisle("BBBBB", "AAAAA", "AA~AA", "AAAAA")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtlcore:molecular_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTLCore.id("block/molecular_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .tooltips(
                        GTLSupbTips.tip("eternity_module", 0))
                .register();

        LING_ZHU = GTLSupbRegistration.REGISTRATE.multiblock("ling_zhu", LingZhuMachine::new)
                .rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.VACUUM_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtceu:solid_machine_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("CCCDAAAD", "BBBDBBBD", "CCCDAAAD")
                        .aisle("CCCDAAAD", "B BDB BD", "CCCDAAAD")
                        .aisle("CMCDA~AD", "BBBDBBBD", "CCCDAAAD")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('D', Predicates.blocks(Registries.getBlock("gtceu:solid_machine_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setMinGlobalLimited(1).setMaxGlobalLimited(1)))
                        .where('B', Predicates.heatingCoils())
                        .where('C', Predicates.blocks(Registries.getBlock("gtceu:heatproof_machine_casing")).or(Predicates.air()))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:frostproof_machine_casing")))
                        .where('M', Predicates.blocks(Registries.getBlock("gtlsupb:mo_wan")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                        GTCEu.id("block/multiblock/vacuum_freezer"))
                .tooltips(
                        GTLSupbTips.tip("ling_zhu", 0))
                .register();

        MO_WAN = GTLSupbRegistration.REGISTRATE.multiblock("mo_wan", MoWanMachine::new)
                .rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.BLAST_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtceu:solid_machine_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("DCCCDAAA", "DBBBDBBB", "DCCCDAAA")
                        .aisle("DCCCDAAA", "DB BDB B", "DCCCDAAA")
                        .aisle("DC~CDALA", "DBBBDBBB", "DCCCDAAA")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('D', Predicates.blocks(Registries.getBlock("gtceu:solid_machine_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setMinGlobalLimited(1).setMaxGlobalLimited(1)))
                        .where('B', Predicates.heatingCoils())
                        .where('C', Predicates.blocks(Registries.getBlock("gtceu:heatproof_machine_casing")))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:frostproof_machine_casing")).or(Predicates.air()))
                        .where('L', Predicates.blocks(Registries.getBlock("gtlsupb:ling_zhu")).or(Predicates.air()))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                        GTCEu.id("block/multiblock/electric_blast_furnace"))
                .tooltips(
                        GTLSupbTips.tip("mo_wan", 0))
                .register();

        ALCHEMICAL_FORGE = GTLSupbRegistration.REGISTRATE.multiblock("alchemical_forge", AlchemicalForgeMachine::new)
                .rotationState(RotationState.ALL)
                .recipeTypes(
                        GTRecipeTypes.ALLOY_SMELTER_RECIPES,
                        GCyMRecipeTypes.ALLOY_BLAST_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtceu:high_temperature_smelting_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("DDDDD", "CBBBC", "CBBBC", "CBBBC", "DDDDD")
                        .aisle("DBBBD", "B   B", "B   B", "B   B", "DBBBD")
                        .aisle("DBBBD", "B   B", "B   B", "B   B", "DBBBD")
                        .aisle("DBBBD", "B   B", "B   B", "B   B", "DBBBD")
                        .aisle("DD~DD", "CBBBC", "CBBBC", "CBBBC", "DDDDD")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('D', Predicates.blocks(Registries.getBlock("gtceu:high_temperature_smelting_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setMinGlobalLimited(1).setMaxGlobalLimited(1)))
                        .where('B', Predicates.heatingCoils())
                        .where('C', Predicates.blocks(Registries.getBlock("gtceu:stainless_steel_frame")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                        GTCEu.id("block/multiblock/electric_blast_furnace"))
                .tooltips(
                        GTLSupbTips.tip("alchemical_forge", 0))
                .register();

        CREATION_MODULE = GTLSupbRegistration.REGISTRATE
                .multiblock("creation_module", CreationModule::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeTypes(GTLRecipeTypes.QFT_RECIPES, GTLRecipeTypes.NANO_FORGE_RECIPES, GTLRecipeTypes.DOOR_OF_CREATE_RECIPES, GTLRecipeTypes.CREATE_AGGREGATION_RECIPES, GTLRecipeTypes.PCB_FACTORY_RECIPES, GTLRecipeTypes.AGGREGATION_DEVICE_RECIPES, GTLRecipeTypes.NEUTRON_COMPRESSOR_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:molecular_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("BBBBB", "AAAAA", "AAAAA", "AAAAA")
                        .aisle("BBBBB", "AAAAA", "AAAAA", "AAAAA")
                        .aisle("BBBBB", "AAAAA", "AA~AA", "AAAAA")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtlcore:molecular_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTLCore.id("block/molecular_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        GENESIS_MODULE = GTLSupbRegistration.REGISTRATE
                .multiblock("genesis_module", GenesisModule::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeTypes(GTLRecipeTypes.COSMOS_SIMULATION_RECIPES, GTLRecipeTypes.SPACE_COSMIC_PROBE_RECEIVERS_RECIPES, GTLRecipeTypes.MAGIC_MANUFACTURER_RECIPES, GTLRecipeTypes.DRILLING_MODULE_RECIPES, GTLRecipeTypes.MINER_MODULE_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:molecular_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("BBBBB", "AAAAA", "AAAAA", "AAAAA")
                        .aisle("BBBBB", "AAAAA", "AAAAA", "AAAAA")
                        .aisle("BBBBB", "AAAAA", "AA~AA", "AAAAA")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtlcore:molecular_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTLCore.id("block/molecular_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        VOID_MODULE = GTLSupbRegistration.REGISTRATE
                .multiblock("void_module", VoidModule::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeTypes(GTLRecipeTypes.ULTIMATE_MATERIAL_FORGE_RECIPES, GTLRecipeTypes.DIMENSIONALLY_TRANSCENDENT_PLASMA_FORGE_RECIPES, GTLRecipeTypes.STELLAR_FORGE_RECIPES, GTLRecipeTypes.GRAVITATION_SHOCKBURST_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:molecular_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("BBBBB", "AAAAA", "AAAAA", "AAAAA")
                        .aisle("BBBBB", "AAAAA", "AAAAA", "AAAAA")
                        .aisle("BBBBB", "AAAAA", "AA~AA", "AAAAA")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtlcore:molecular_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtceu:fusion_glass")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTLCore.id("block/molecular_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        STAR_FORGE_MODULE = GTLSupbRegistration.REGISTRATE
                .multiblock("star_forge_module", StarForgeModule::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeTypes(
                        GTRecipeTypes.BLAST_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtceu:high_power_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("BBBBB", "BAAAB", "BA~AB", "BAAAB", "BBBBB")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:high_power_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/hpca/high_power_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        CELESTIAL_MODULE = GTLSupbRegistration.REGISTRATE
                .multiblock("celestial_module", CelestialModule::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeTypes(
                        GTRecipeTypes.ALLOY_SMELTER_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtceu:high_power_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("BBBBB", "BAAAB", "BA~AB", "BAAAB", "BBBBB")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:high_power_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/hpca/high_power_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        WORLD_MODULE = GTLSupbRegistration.REGISTRATE
                .multiblock("world_module", WorldModule::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeTypes(
                        GCyMRecipeTypes.ALLOY_BLAST_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtceu:high_power_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("BBBBB", "BAAAB", "BA~AB", "BAAAB", "BBBBB")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:high_power_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/hpca/high_power_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        SINGULARITY_MODULE = GTLSupbRegistration.REGISTRATE
                .multiblock("singularity_module", SingularityModule::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtceu:high_power_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("BBBBB", "BAAAB", "BA~AB", "BAAAB", "BBBBB")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:high_power_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtlcore:hyper_mechanical_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/hpca/high_power_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        CHRONO_DEDUCTION_ARRAY = GTLSupbRegistration.REGISTRATE
                .multiblock("chrono_deduction_array", ChronoDeductionArray::new)

                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:molecular_casing"))
                .pattern((definition) -> ChronoModulePattern.PATTERN
                        .where('C', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("kubejs:module_base")))
                        .where('B', Predicates.blocks(Registries.getBlock("gtlcore:molecular_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('D', Predicates.blocks(Registries.getBlock("kubejs:module_connector")))
                        .where('E', Predicates.air())
                        .build())

                .workableCasingRenderer(
                        GTLCore.id("block/molecular_casing"),
                        GTCEu.id("block/multiblock/fusion_reactor"))
                .register();

        CHRONO_RESONANCE_AMPLIFIER = GTLSupbRegistration.REGISTRATE
                .multiblock("chrono_resonance_amplifier", ChronoResonanceAmplifier::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeType(GTLSupbRecipeTypes.CHRONO_RESONANCE_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:molecular_casing"))
                .pattern((definition) -> ChronoModulePattern.PATTERN
                        .where('C', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("kubejs:module_base")))
                        .where('B', Predicates.blocks(Registries.getBlock("gtlcore:molecular_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('D', Predicates.blocks(Registries.getBlock("kubejs:module_connector")))
                        .where('E', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTLCore.id("block/molecular_casing"),
                        GTCEu.id("block/multiblock/fusion_reactor"))
                .register();

        UNIVERSAL_ORDER_KEEPER = GTLSupbRegistration.REGISTRATE
                .multiblock("universal_order_keeper", UniversalOrderKeeper::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeType(GTLSupbRecipeTypes.UNIVERSAL_ORDER_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtlcore:molecular_casing"))
                .pattern((definition) -> UniversalOrderKeeperPattern.PATTERN
                        .where('J', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:advanced_computer_casing")))
                        .where('B', Predicates.blocks(Registries.getBlock("kubejs:space_elevator_internal_support")))
                        .where('C', Predicates.blocks(Registries.getBlock("kubejs:containment_field_generator")))
                        .where('D', Predicates.blocks(Registries.getBlock("kubejs:accelerated_pipeline")))
                        .where('E', Predicates.blocks(Registries.getBlock("gtlcore:space_elevator_support")))
                        .where('F', Predicates.blocks(Registries.getBlock("kubejs:dimensional_stability_casing")))

                        .where('G', Predicates.blocks(Registries.getBlock("gtlcore:molecular_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS)))

                        .where('K', Predicates.blocks(Registries.getBlock("gtlcore:molecular_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS))
                                .or(Predicates.abilities(GTLSupbPartAbilities.OVERCLOCK))
                                .or(Predicates.abilities(GTLSupbPartAbilities.EFFICIENCY))
                                .or(Predicates.abilities(GTLSupbPartAbilities.ENERGY_BOOST)))

                        .where('H', Predicates.blocks(Registries.getBlock("kubejs:module_base")))
                        .where('I', Predicates.blocks(Registries.getBlock("kubejs:module_connector")))
                        .where(' ', Predicates.any())
                        .build())

                .workableCasingRenderer(
                        GTLCore.id("block/molecular_casing"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();

        STEAM_MODULAR_FACTORY = GTLSupbRegistration.REGISTRATE
                .multiblock("steam_modular_factory", SteamModularUniversalFactory::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtceu:steam_machine_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("BBBCBBB", " DE ED ", " D   D ", " D   D ", " D   D ", " DE ED ", "BBBCBBB")
                        .aisle("BBBCBBB", "D     D", "D     D", "D     D", "D     D", "D     D", "BBBCBBB")
                        .aisle("BBBCBBB", "E     E", "       ", "       ", "       ", "E     E", "BBBCBBB")
                        .aisle("CCCCCCC", "       ", "       ", "       ", "       ", "       ", "CCCCCCC")
                        .aisle("BBBCBBB", "E     E", "       ", "       ", "       ", "E     E", "BBBCBBB")
                        .aisle("BBBCBBB", "D     D", "D     D", "D     D", "D     D", "D     D", "BBBCBBB")
                        .aisle("BBBCBBB", " DEAED ", " DAAAD ", " DA~AD ", " DAAAD ", " DEAED ", "BBBCBBB")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('C', Predicates.blocks(Registries.getBlock("gtceu:bronze_pipe_casing")))
                        .where('D', Predicates.blocks(Registries.getBlock("gtceu:bronze_frame")))
                        .where('E', Predicates.blocks(Registries.getBlock("kubejs:steam_assembly_block")))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:steam_machine_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtceu:bronze_firebox_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                        GTCEu.id("block/multiblock/steam_mixer"))
                .tooltips(
                        GTLSupbTips.tip("steam_modular_factory", 0))
                .register();

        STEAM_MODULE = GTLSupbRegistration.REGISTRATE
                .multiblock("steam_universal_module", SteamUniversalModule::new)
                .rotationState(RotationState.NON_Y_AXIS).allowExtendedFacing(false)
                .recipeTypes(
                        GTRecipeTypes.ORE_WASHER_RECIPES,
                        GTRecipeTypes.CHEMICAL_BATH_RECIPES,
                        GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES,
                        GTRecipeTypes.CENTRIFUGE_RECIPES,
                        GTRecipeTypes.MIXER_RECIPES,
                        GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                        GTRecipeTypes.MACERATOR_RECIPES,
                        GTRecipeTypes.ALLOY_SMELTER_RECIPES,
                        GTRecipeTypes.COMPRESSOR_RECIPES,
                        GTRecipeTypes.FORGE_HAMMER_RECIPES,
                        GTRecipeTypes.EXTRACTOR_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtceu:steam_machine_casing"))
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("BBBCBBB", " DE ED ", " D   D ", " D   D ", " D   D ", " DE ED ", "BBBCBBB")
                        .aisle("BBBCBBB", "D     D", "D     D", "D     D", "D     D", "D     D", "BBBCBBB")
                        .aisle("BBBCBBB", "E     E", "       ", "       ", "       ", "E     E", "BBBCBBB")
                        .aisle("CCCCCCC", "       ", "       ", "       ", "       ", "       ", "CCCCCCC")
                        .aisle("BBBCBBB", "E     E", "       ", "       ", "       ", "E     E", "BBBCBBB")
                        .aisle("BBBCBBB", "D     D", "D     D", "D     D", "D     D", "D     D", "BBBCBBB")
                        .aisle("BBBCBBB", " DEAED ", " DAAAD ", " DA~AD ", " DAAAD ", " DEAED ", "BBBCBBB")
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('C', Predicates.blocks(Registries.getBlock("gtceu:bronze_pipe_casing")))
                        .where('D', Predicates.blocks(Registries.getBlock("gtceu:bronze_frame")))
                        .where('E', Predicates.blocks(Registries.getBlock("kubejs:steam_assembly_block")))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:steam_machine_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(GTLSupbPartAbilities.OVERCLOCK).setPreviewCount(1))
                                .or(Predicates.abilities(GTLSupbPartAbilities.EFFICIENCY).setPreviewCount(1)))
                        .where('B', Predicates.blocks(Registries.getBlock("gtceu:bronze_firebox_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                        GTCEu.id("block/multiblock/steam_mixer"))
                .tooltips(
                        GTLSupbTips.tip("steam_universal_module", 0))
                .register();

        ME_DRIVE = GTLSupbRegistration.REGISTRATE
                .machine("overlimit_me_drive", (holder) -> new MEDrivePartMachine(holder))
                .rotationState(RotationState.ALL)
                .tier(GTValues.MAX)
                .renderer(() -> molecularCasingPartRenderer(GTLSupb.id("block/machine/part/overlimit_me_drive")))
                .register();

        ME_MUFFLER_HATCH = GTLSupbRegistration.REGISTRATE
                .machine("overlimit_me_muffler_hatch", MEMufflerPartMachine::new)
                .rotationState(RotationState.ALL)
                .tier(GTValues.MAX)
                .abilities(PartAbility.MUFFLER)
                .renderer(() -> molecularCasingPartRenderer(GTLSupb.id("block/machine/part/overlimit_me_muffler_hatch")))
                .tooltips(GTLSupbTips.partTip("overlimit_me_muffler_hatch", 0))
                .register();

        OVERLIMIT_INTEGRATED_HATCH = GTLSupbRegistration.REGISTRATE
                .machine("overlimit_integrated_hatch", OverlimitIntegratedHatchPartMachine::new)
                .rotationState(RotationState.ALL)
                .tier(GTValues.MAX)
                .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS,
                        PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS)
                .renderer(() -> molecularCasingPartRenderer(GTLSupb.id("block/machine/part/overlimit_integrated_hatch")))
                .tooltips(GTLSupbTips.partTip("overlimit_integrated_hatch", 0))
                .register();

        OVERCLOCK_HATCH = registerTieredPart("overclock_hatch",
                OverclockHatchPartMachine::new,
                GTLSupbPartAbilities.OVERCLOCK,
                "optical_data_hatch",
                "Overclock Hatch");
        EFFICIENCY_HATCH = registerTieredPart("efficiency_hatch",
                EfficiencyHatchPartMachine::new,
                GTLSupbPartAbilities.EFFICIENCY,
                "computation_data_hatch",
                "Efficiency Hatch");
        ENERGY_BOOST_HATCH = registerTieredPart("energy_boost_hatch",
                EnergyBoostHatchPartMachine::new,
                GTLSupbPartAbilities.ENERGY_BOOST,
                "object_holder",
                "Energy Boost Hatch");
        OVERLIMIT_FTB_QUEST_INTEGRATION_HATCH = GTLSupbRegistration.REGISTRATE
                .machine("overlimit_ftb_quest_integration_hatch", OverlimitFTBQuestIntegrationHatchPartMachine::new)
                .rotationState(RotationState.ALL)
                .tier(GTValues.MAX)
                .renderer(() -> molecularCasingPartRenderer(GTLSupb.id("block/machine/part/ftb_quest_integration")))
                .register();

        OVERLIMIT_QUANTUM_ENTANGLEMENT_HATCH = GTLSupbRegistration.REGISTRATE
                .machine("overlimit_quantum_entanglement_hatch", OverlimitQuantumEntanglementHatchPartMachine::new)
                .rotationState(RotationState.ALL)
                .tier(GTValues.MAX)
                .renderer(() -> molecularCasingPartRenderer(GTLSupb.id("block/machine/part/quantum_entanglement")))
                .tooltips(
                        GTLSupbTips.partTip("overlimit_quantum_entanglement_hatch", 0),
                        GTLSupbTips.partTip("overlimit_quantum_entanglement_hatch", 1),
                        GTLSupbTips.partTip("overlimit_quantum_entanglement_hatch", 2),
                        GTLSupbTips.partTip("overlimit_quantum_entanglement_hatch", 3))
                .register();

        HOLOGRAPHIC_STORAGE_ARRAY = GTLSupbRegistration.REGISTRATE
                .multiblock("holographic_storage_array", HolographicStorageArrayMachine::new)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTLSupbRecipeTypes.HOLOGRAPHIC_DATA_RECIPES)
                .tooltips(GTLSupbTips.tip("holographic_storage_array", 0))
                .appearanceBlock(() -> GTBlocks.COMPUTER_CASING.get())
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("XDDDX", "XDDDX", "XDDDX")
                        .aisle("XDDDX", "XDDDX", "XDDDX")
                        .aisle("XCCCX", "XCSCX", "XCCCX")
                        .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('X', Predicates.blocks(GTBlocks.COMPUTER_HEAT_VENT.get()))
                        .where('D', Predicates.blocks(GTBlocks.COMPUTER_CASING.get()))
                        .where('C', Predicates.blocks(GTBlocks.HIGH_POWER_CASING.get())
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setPreviewCount(1)))
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/hpca/high_power_casing"),
                        GTCEu.id("block/multiblock/data_bank"))
                .register();

        FLOATING_POINT_ENGINE = GTLSupbRegistration.REGISTRATE
                .multiblock("floating_point_engine", FloatingPointEngineMachine::new)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTLSupbRecipeTypes.FLOATING_POINT_COMPUTE_RECIPES)
                .tooltips(GTLSupbTips.tip("floating_point_engine", 0))
                .appearanceBlock(() -> GTBlocks.COMPUTER_CASING.get())
                .pattern((definition) -> FactoryBlockPattern.start()
                        .aisle("AA", "CC", "CC", "CC", "AA")
                        .aisle("AA", "VV", "VV", "VV", "AA")
                        .aisle("AA", "VV", "VV", "VV", "AA")
                        .aisle("AA", "VV", "VV", "VV", "AA")
                        .aisle("SA", "CC", "CC", "CC", "AA")
                        .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('A', Predicates.blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
                        .where('V', Predicates.blocks(GTBlocks.COMPUTER_HEAT_VENT.get()))
                        .where('C', Predicates.blocks(GTBlocks.COMPUTER_CASING.get())
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setPreviewCount(1)))
                        .build())
                .sidedWorkableCasingRenderer(
                        "block/casings/hpca/computer_casing",
                        GTCEu.id("block/multiblock/power_substation"))
                .register();

        OVERLIMIT_DATA_RECEIVE_HATCH = GTLSupbRegistration.REGISTRATE
                .machine("overlimit_data_receive_hatch", OverlimitDataReceiveHatchPartMachine::new)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.OPTICAL_DATA_RECEPTION)
                .renderer(() -> new OverlayTieredMachineRenderer(3,
                        GTCEu.id("block/machine/part/me_pattern_buffer")))
                .tooltips(
                        GTLSupbTips.partTip("overlimit_data_receive_hatch", 0),
                        GTLSupbTips.partTip("overlimit_data_receive_hatch", 1))
                .register();

        OVERLIMIT_COMPUTE_RECEIVE_HATCH = GTLSupbRegistration.REGISTRATE
                .machine("overlimit_compute_receive_hatch", OverlimitComputeReceiveHatchPartMachine::new)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.COMPUTATION_DATA_RECEPTION)
                .renderer(() -> new OverlayTieredMachineRenderer(3,
                        GTCEu.id("block/machine/part/optical_data_hatch")))
                .tooltips(
                        GTLSupbTips.partTip("overlimit_compute_receive_hatch", 0),
                        GTLSupbTips.partTip("overlimit_compute_receive_hatch", 1))
                .register();

        PATTERN_ASSEMBLY = GTLSupbRegistration.REGISTRATE
                .machine("weihe_pattern_assembly", (holder) -> new PatternAssemblyPartMachine(holder, 10, ConfigHolder.INSTANCE.patternAssemblyRows))
                .rotationState(RotationState.ALL)
                .abilities(
                        PartAbility.IMPORT_ITEMS,
                        PartAbility.EXPORT_ITEMS,
                        PartAbility.IMPORT_FLUIDS,
                        PartAbility.EXPORT_FLUIDS)
                .tier(GTValues.MAX)
                .renderer(() -> molecularCasingPartRenderer(GTLSupb.id("block/machine/part/pattern_assembly")))
                .register();

        COSMIC_MATTER_SIPHONER = GTLSupbRegistration.REGISTRATE
                .multiblock("cosmic_matter_siphoner", CosmicMatterSiphonerMachine::new)
                .rotationState(RotationState.ALL)
                .recipeTypes(GTLSupbRecipeTypes.COSMIC_SIPHON_RECIPES)
                .appearanceBlock(() -> Registries.getBlock("gtceu:steam_machine_casing"))
                .pattern((definition) -> CosmicMatterSiphonerPattern.PATTERN
                        .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                        .where('C', GTLSupbPredicates.tierCasings(
                                CosmicMatterSiphonerPattern.TIER_CASING_MAP,
                                CosmicMatterSiphonerPattern.TIER_KEY))
                        .where('A', Predicates.blocks(Registries.getBlock("gtceu:bronze_machine_casing"))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                        .where('D', Predicates.blocks(Registries.getBlock("gtceu:bronze_frame")))
                        .where('E', Predicates.blocks(Registries.getBlock("gtceu:bronze_firebox_casing")))
                        .where('F', Predicates.blocks(Registries.getBlock("gtceu:steam_machine_casing")))
                        .where('H', Predicates.blocks(Registries.getBlock("gtceu:bronze_pipe_casing")))
                        .where(' ', Predicates.any())
                        .build())
                .workableCasingRenderer(
                        GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                        GTCEu.id("block/multiblock/cosmos_simulation"))
                .register();
    }

    private static MachineDefinition[] registerTieredPart(String name,
                                                          BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                          PartAbility ability,
                                                          String overlayName,
                                                          String displayName) {
        return registerTieredPart(name, factory, new PartAbility[] { ability }, overlayName, displayName);
    }

    private static MachineDefinition[] registerTieredPart(String name,
                                                          BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                          PartAbility[] abilities,
                                                          String overlayName,
                                                          String displayName) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.VN.length];
        for (int tier : GTValues.tiersBetween(0, GTValues.MAX)) {
            definitions[tier] = GTLSupbRegistration.REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .rotationState(RotationState.ALL)
                    .tier(tier)
                    .abilities(abilities)
                    .langValue(GTValues.VN[tier] + " " + displayName)

                    .renderer(() -> new OverlayTieredMachineRenderer(tier,
                            GTLSupb.id("block/machine/part/" + name)))
                    .tooltips(
                            GTLSupbTips.partTip(name, 0),
                            GTLSupbTips.partTip(name, 1),
                            GTLSupbTips.partTip(name, 2))
                    .register();
        }
        return definitions;
    }

    @SuppressWarnings("unchecked")
    private static MachineDefinition[] registerTieredMachines(String name,
                                                              BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                              BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                              int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            MachineBuilder<MachineDefinition> machineBuilder = GTLSupbRegistration.REGISTRATE
                    .machine(
                            GTValues.VN[tier].toLowerCase() + "_" + name,
                            (holder) -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, machineBuilder);
        }
        return definitions;
    }
}
