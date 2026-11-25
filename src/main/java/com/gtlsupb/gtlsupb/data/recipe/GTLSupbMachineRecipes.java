package com.gtlsupb.gtlsupb.data.recipe;

import org.gtlcore.gtlcore.utils.Registries;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import com.gtlsupb.gtlsupb.common.machine.GTLSupbMachines;
import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;

import java.util.Locale;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;

public final class GTLSupbMachineRecipes {

    private GTLSupbMachineRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        GTLSupbRecipeTypes.HOLOGRAPHIC_DATA_RECIPES.recipeBuilder("base_data_generation")
                .circuitMeta(1)
                .EUt(32)
                .duration(2000)
                .save(provider);
        GTLSupbRecipeTypes.FLOATING_POINT_COMPUTE_RECIPES.recipeBuilder("base_compute_generation")
                .circuitMeta(1)
                .EUt(32)
                .duration(2000)
                .save(provider);

        GTLSupbRecipeTypes.UNIVERSAL_ORDER_RECIPES.recipeBuilder("universal_order_base")
                .circuitMeta(1)
                .duration(2000)
                .save(provider);
        GTLSupbRecipeTypes.CHRONO_RESONANCE_RECIPES.recipeBuilder("chrono_resonance_base")
                .circuitMeta(1)
                .duration(2000)
                .save(provider);

        assembler(provider, "abyssal_chemical_eroder", GTLSupbMachines.ABYSSAL_CHEMICAL_ERODER, 32,
                GTValues.VA[GTValues.OpV], 1200,
                stack("gtceu:opv_machine_casing", 4),
                stack("gtceu:opv_field_generator", 4),
                stack("gtceu:opv_sensor", 4),
                stack("gtceu:opv_emitter", 4),
                stack("gtceu:opv_conveyor_module", 4),
                stack("gtceu:opv_robot_arm", 4),
                stack("gtceu:large_chemical_plant"),
                stack("gtceu:decay_hastener"));

        assembler(provider, "hypercube_core_tower", GTLSupbMachines.HYPERCUBE_CORE_TOWER, 25,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtlcore:max_robot_arm", 4),
                stack("gtceu:dimensionally_transcendent_mixer"),
                stack("gtceu:advanced_sps_crafting"));

        assembler(provider, "omniversal_eye", GTLSupbMachines.OMNIVERSAL_EYE, 27,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtlcore:max_robot_arm", 4),
                stack("gtceu:eye_of_harmony", 4));

        assembler(provider, "quantum_weaver", GTLSupbMachines.QUANTUM_WEAVER, 28,
                GTValues.VA[GTValues.ZPM], 600,
                stack("gtceu:molecular_assembler_matrix"),
                stack("gtceu:me_craft_parallel_core", 16),
                stack("gtceu:me_craft_speed_core", 16),
                stack("gtceu:zpm_robot_arm", 4));

        assembler(provider, "rank_derivation_assembly_matrix", GTLSupbMachines.RANK_DERIVATION_ASSEMBLY_MATRIX, 29,
                GTValues.VA[GTValues.OpV], 1200,
                stack("gtceu:opv_machine_casing", 4),
                stack("gtceu:opv_field_generator", 4),
                stack("gtceu:opv_sensor", 4),
                stack("gtceu:opv_emitter", 4),
                stack("gtceu:opv_conveyor_module", 4),
                stack("gtceu:opv_robot_arm", 4),
                stack("gtceu:suprachronal_assembly_line"),
                stack("gtceu:mage_assembler"));

        assembler(provider, "celestial_module", GTLSupbMachines.CELESTIAL_MODULE, 3,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtlcore:max_robot_arm", 4),
                stack("gtceu:super_blast_smelter"));

        assembler(provider, "creation_module", GTLSupbMachines.CREATION_MODULE, 1,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtceu:door_of_create"),
                stack("gtceu:create_aggregation"),
                stack("gtceu:nano_core"));

        assembler(provider, "eternity_module", GTLSupbMachines.ETERNITY_MODULE, 2,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                GTLSupbMachines.VOID_MODULE.asStack(),
                GTLSupbMachines.CREATION_MODULE.asStack(),
                GTLSupbMachines.GENESIS_MODULE.asStack(),
                GTLSupbMachines.MICROCOSM_FOUNDRY.asStack());

        assembler(provider, "forging_heaven_ship", GTLSupbMachines.FORGING_HEAVEN_SHIP, 5,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                GTLSupbMachines.SINGULARITY_MODULE.asStack(),
                GTLSupbMachines.WORLD_MODULE.asStack(),
                GTLSupbMachines.CELESTIAL_MODULE.asStack(),
                GTLSupbMachines.STAR_FORGE_MODULE.asStack());

        assembler(provider, "genesis_module", GTLSupbMachines.GENESIS_MODULE, 6,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtlcore:max_robot_arm", 4),
                stack("gtceu:magic_manufacturer", 2),
                stack("gtceu:space_cosmic_probe_receivers", 2));

        assembler(provider, "microcosm_foundry", GTLSupbMachines.MICROCOSM_FOUNDRY, 7,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtceu:qft"),
                stack("gtceu:a_mass_fabricator"),
                stack("gtceu:space_elevator"));

        assembler(provider, "singularity_module", GTLSupbMachines.SINGULARITY_MODULE, 8,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtlcore:max_robot_arm", 4),
                stack("gtceu:super_blast_smelter"));

        assembler(provider, "star_forge_module", GTLSupbMachines.STAR_FORGE_MODULE, 9,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtlcore:max_robot_arm", 4),
                stack("gtceu:super_blast_smelter"));

        assembler(provider, "void_module", GTLSupbMachines.VOID_MODULE, 10,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtlcore:max_robot_arm", 4),
                stack("gtceu:gravitation_shockburst"));

        assembler(provider, "world_module", GTLSupbMachines.WORLD_MODULE, 11,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtlcore:max_robot_arm", 4),
                stack("gtceu:super_blast_smelter"));

        assembler(provider, "chrono_deduction_array", GTLSupbMachines.CHRONO_DEDUCTION_ARRAY, 12,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtlcore:max_robot_arm", 4),
                stack("gtceu:quantum_processor_assembly", 4));

        assembler(provider, "chrono_resonance_amplifier", GTLSupbMachines.CHRONO_RESONANCE_AMPLIFIER, 13,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtlcore:max_robot_arm", 4));

        assembler(provider, "cosmic_matter_siphoner", GTLSupbMachines.COSMIC_MATTER_SIPHONER, 14,
                GTValues.VA[GTValues.LV], 200,
                stack("gtceu:bronze_machine_casing", 8),
                stack("gtceu:bronze_frame", 8),
                stack("gtceu:bronze_firebox_casing", 4),
                stack("gtceu:bronze_pipe_casing", 4),
                stack("gtceu:bronze_plate", 16));

        assembler(provider, "universal_order_keeper", GTLSupbMachines.UNIVERSAL_ORDER_KEEPER, 16,
                GTValues.VA[GTValues.MAX], 2400,
                stack("gtceu:max_machine_casing", 4),
                stack("gtlcore:max_field_generator", 4),
                stack("gtlcore:max_sensor", 4),
                stack("gtlcore:max_emitter", 4),
                stack("gtlcore:max_conveyor_module", 4),
                stack("gtlcore:max_robot_arm", 4));

        String[][] siphonOutputs = {
                { "f:minecraft:water", "f:minecraft:lava", "i:gtceu:rubber_log", "i:minecraft:cobblestone" },
                { "f:gtceu:steam", "i:gtceu:iron_dust", "i:gtceu:copper_dust", "i:gtceu:tin_dust", "i:gtceu:diamond_dust",
                        "i:gtceu:lead_dust", "i:gtceu:silver_dust", "i:gtceu:gold_dust", "i:gtceu:nickel_dust",
                        "i:gtceu:carbon_dust", "i:minecraft:redstone", "i:minecraft:glowstone_dust",
                        "i:gtceu:nether_quartz_dust", "i:gtceu:certus_quartz_dust", "i:gtceu:sticky_resin" },
                { "i:gtceu:aluminium_dust", "i:gtceu:ender_pearl_dust", "i:gtceu:arsenic_dust", "i:gtceu:gallium_dust",
                        "i:gtceu:sulfur_dust", "i:gtceu:phosphorus_dust", "f:gtceu:nitrogen", "f:gtceu:oxygen",
                        "f:gtceu:hydrogen", "f:gtceu:ethylene" },
                { "i:gtceu:silicon_dust", "i:gtceu:chromium_dust", "i:gtceu:vanadium_dust", "i:gtceu:manganese_dust",
                        "i:gtceu:emerald_dust", "f:gtceu:fluorine", "i:gtceu:sodium_dust", "i:gtceu:lithium_dust",
                        "i:gtceu:antimony_dust", "i:gtceu:magnesium_dust" },
                { "i:minecraft:blaze_powder", "i:gtceu:titanium_dust", "f:gtceu:methane" },
                { "f:gtceu:helium", "f:gtceu:argon", "f:gtceu:carbon_dioxide", "i:gtceu:tungsten_dust",
                        "i:gtceu:ruthenium_dust", "i:gtceu:molybdenum_dust", "i:gtceu:platinum_dust",
                        "i:gtceu:uranium_dust", "i:gtceu:palladium_dust", "i:gtceu:plutonium_dust",
                        "f:gtceu:sulfuric_acid", "i:gtceu:erbium_dust", "f:gtceu:radon" },
                { "i:gtceu:cobalt_dust", "i:gtceu:indium_dust", "i:gtceu:rhodium_dust", "i:gtceu:tantalum_dust",
                        "i:gtceu:iridium_dust" },
                { "i:gtceu:naquadah_dust", "i:gtceu:enriched_naquadah_dust", "i:gtceu:trinium_dust",
                        "i:gtceu:barium_dust", "i:gtceu:duranium_dust", "i:gtceu:europium_dust" },
                { "i:gtceu:zirconium_dust", "i:gtceu:tellurium_dust", "i:gtceu:tritanium_dust",
                        "i:gtceu:americium_dust", "i:gtceu:darmstadtium_dust" },
                { "i:gtceu:actinium_dust", "i:gtceu:iodine_dust", "i:gtceu:germanium_dust", "i:gtceu:hafnium_dust",
                        "i:gtceu:orichalcum_dust", "i:gtceu:mithril_dust", "i:gtceu:cerium_dust" },
                { "i:gtceu:rutherfordium_dust", "i:gtceu:francium_dust", "i:gtceu:dubnium_dust",
                        "i:gtceu:seaborgium_dust", "f:gtceu:uu_amplifier" },
                { "i:gtceu:adamantine_dust", "i:gtceu:bedrock_dust", "i:gtceu:vibranium_dust",
                        "i:gtceu:taranium_dust", "f:gtceu:mana" },
                { "i:gtceu:oganesson_dust", "i:gtceu:hassium_dust", "i:gtceu:flerovium_dust", "f:gtceu:uu_matter",
                        "f:gtceu:heavy_lepton_mixture" },
                { "i:gtceu:draconium_dust", "i:gtceu:starmetal_dust", "f:gtceu:starlight" },
                { "f:gtceu:cosmic_element", "f:gtceu:spacetime", "f:gtceu:raw_star_matter_plasma" } };

        for (int tier = 0; tier < siphonOutputs.length; tier++) {
            GTRecipeBuilder siphonBuilder = GTLSupbRecipeTypes.COSMIC_SIPHON_RECIPES
                    .recipeBuilder("cosmic_siphon_" + GTValues.VN[tier].toLowerCase(Locale.ROOT))
                    .circuitMeta(tier + 1)
                    .duration(400 - tier * 15)
                    .EUt(GTValues.VA[tier]);
            siphonBuilder.data.putInt(com.gtlsupb.gtlsupb.common.data.machines.multiblock.CosmicMatterSiphonerPattern.TIER_KEY,
                    tier);
            for (String output : siphonOutputs[tier]) {
                String id = output.substring(2);
                if (output.startsWith("i:")) {
                    ItemStack item = stack(id, 64);
                    if (!item.isEmpty()) {
                        siphonBuilder.chancedOutput(item, 2000, 0);
                    }
                } else {
                    net.minecraft.world.level.material.Fluid fluid = ForgeRegistries.FLUIDS
                            .getValue(ResourceLocation.tryParse(id));
                    if (fluid != null) {
                        siphonBuilder.chancedOutput(
                                com.lowdragmc.lowdraglib.side.fluid.FluidStack.create(fluid, 64000L), 2000, 0);
                    }
                }
            }
            siphonBuilder.save(provider);
        }

        for (int tier = 0; tier <= GTValues.MAX; tier++) {
            String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
            if (!hasItem("gtceu:" + tierName + "_conveyor_module")) continue;

            tieredHatch(provider, tier, "overclock_hatch", GTLSupbMachines.OVERCLOCK_HATCH[tier],
                    stack("gtceu:" + tierName + "_field_generator", 2),
                    stack("gtceu:" + tierName + "_emitter", 2));
            tieredHatch(provider, tier, "efficiency_hatch", GTLSupbMachines.EFFICIENCY_HATCH[tier],
                    stack("gtceu:" + tierName + "_electric_pump", 2),
                    stack("gtceu:" + tierName + "_sensor", 2));
            tieredHatch(provider, tier, "energy_boost_hatch", GTLSupbMachines.ENERGY_BOOST_HATCH[tier],
                    stack("gtceu:" + tierName + "_field_generator", 2),
                    stack("gtceu:" + tierName + "_sensor", 2));
        }
    }

    private static void assembler(Consumer<FinishedRecipe> provider, String id, MachineDefinition output,
                                  int circuit, long eut, int duration, ItemStack... inputs) {
        GTRecipeBuilder builder = ASSEMBLER_RECIPES.recipeBuilder(id).circuitMeta(circuit);
        for (ItemStack input : inputs) {
            if (!input.isEmpty()) builder.inputItems(input);
        }
        builder.outputItems(output).EUt(eut).duration(duration).save(provider);
    }

    private static void tieredHatch(Consumer<FinishedRecipe> provider, int tier, String name,
                                    MachineDefinition definition, ItemStack... components) {
        if (definition == null) return;
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        GTRecipeBuilder builder = ASSEMBLER_RECIPES.recipeBuilder(tierName + "_" + name)
                .circuitMeta(1)
                .inputItems(GTMachines.HULL[tier]);
        for (ItemStack component : components) {
            if (!component.isEmpty()) builder.inputItems(component);
        }
        builder.outputItems(definition).EUt(GTValues.VA[tier]).duration(200).save(provider);
    }

    private static ItemStack stack(String id) {
        return stack(id, 1);
    }

    private static ItemStack stack(String id, int count) {
        if (!hasItem(id)) return ItemStack.EMPTY;
        return Registries.getItemStack(id, count);
    }

    private static boolean hasItem(String id) {
        return ForgeRegistries.ITEMS.containsKey(new ResourceLocation(id));
    }
}
