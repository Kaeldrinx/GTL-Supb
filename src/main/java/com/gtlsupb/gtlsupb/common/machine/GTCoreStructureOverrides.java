package com.gtlsupb.gtlsupb.common.machine;

import org.gtlcore.gtlcore.common.data.GTLBlocks;
import org.gtlcore.gtlcore.common.data.machines.AdvancedMultiBlockMachineA;
import org.gtlcore.gtlcore.common.data.machines.MultiBlockMachineA;
import org.gtlcore.gtlcore.common.data.machines.structure.AdvancedMultiBlockMachineStructureA;
import org.gtlcore.gtlcore.common.data.machines.structure.MultiBlockMachineAStructureA;
import org.gtlcore.gtlcore.utils.Registries;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.hepdd.gtmthings.data.CustomMachines;

import java.util.List;
import java.util.function.BiConsumer;

public final class GTCoreStructureOverrides {

    private GTCoreStructureOverrides() {}

    public static void init() {
        overrideCircuitAssemblyLine();
        overrideAggregationDevice();
    }

    private static void overrideCircuitAssemblyLine() {
        MultiblockMachineDefinition definition = AdvancedMultiBlockMachineA.CIRCUIT_ASSEMBLY_LINE;
        definition.setPatternFactory(() -> AdvancedMultiBlockMachineStructureA.CIRCUIT_ASSEMBLY_LINE
                .where("a", Predicates.controller(Predicates.blocks(definition.get())))
                .where("b", Predicates.blocks(GTLBlocks.PIKYONIUM_MACHINE_CASING.get())
                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                .where("c", Predicates.blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                .where("d", Predicates.blocks(GTMachines.ITEM_IMPORT_BUS[0].get())
                        .or(Predicates.blocks(CustomMachines.HUGE_ITEM_IMPORT_BUS[0].get()))
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS, PartAbility.EXPORT_ITEMS,
                                PartAbility.IMPORT_FLUIDS, PartAbility.EXPORT_FLUIDS,
                                PartAbility.IMPORT_FLUIDS_4X, PartAbility.EXPORT_FLUIDS_4X).setPreviewCount(1)))
                .where("e", Predicates.blocks(Registries.getBlock("kubejs:machine_casing_circuit_assembly_line")))
                .where("f", Predicates.abilities(PartAbility.IMPORT_ITEMS, PartAbility.EXPORT_ITEMS,
                        PartAbility.IMPORT_FLUIDS, PartAbility.EXPORT_FLUIDS,
                        PartAbility.IMPORT_FLUIDS_4X, PartAbility.EXPORT_FLUIDS_4X).setPreviewCount(1))
                .where("g", Predicates.abilities(PartAbility.IMPORT_ITEMS, PartAbility.EXPORT_ITEMS,
                        PartAbility.IMPORT_FLUIDS, PartAbility.EXPORT_FLUIDS,
                        PartAbility.IMPORT_FLUIDS_4X, PartAbility.EXPORT_FLUIDS_4X).setPreviewCount(1))
                .build());
        appendTooltip(definition);
    }

    private static void overrideAggregationDevice() {
        MultiblockMachineDefinition definition = MultiBlockMachineA.AGGREGATION_DEVICE;
        definition.setPatternFactory(() -> MultiBlockMachineAStructureA.AGGREGATION_DEVICE
                .where("~", Predicates.controller(Predicates.blocks(definition.get())))
                .where("c", Predicates.blocks(GTBlocks.FUSION_CASING.get())
                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(32)))
                .where("d", Predicates.blocks(GTBlocks.FUSION_CASING_MK3.get()))
                .where("b", Predicates.blocks(GTBlocks.FUSION_COIL.get()))
                .where("e", Predicates.blocks(Registries.getBlock("kubejs:aggregatione_core")))
                .where("a", Predicates.blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.NaquadahEnriched)))
                .where("i", Predicates.blocks(GTMachines.ITEM_IMPORT_BUS[0].get())
                        .or(Predicates.blocks(CustomMachines.HUGE_ITEM_IMPORT_BUS[0].get()))
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS, PartAbility.EXPORT_ITEMS,
                                PartAbility.IMPORT_FLUIDS, PartAbility.EXPORT_FLUIDS).setPreviewCount(1)))
                .where("g", Predicates.abilities(PartAbility.IMPORT_ITEMS, PartAbility.EXPORT_ITEMS,
                        PartAbility.IMPORT_FLUIDS, PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                .where(" ", Predicates.any())
                .build());
        appendTooltip(definition);
    }

    private static void appendTooltip(MultiblockMachineDefinition definition) {
        BiConsumer<ItemStack, List<Component>> original = definition.getTooltipBuilder();
        definition.setTooltipBuilder((stack, tooltip) -> {
            if (original != null) {
                original.accept(stack, tooltip);
            }
            tooltip.add(Component.translatable("gtlsupb.machine.extra_io_hatches.tooltip"));
        });
    }
}
