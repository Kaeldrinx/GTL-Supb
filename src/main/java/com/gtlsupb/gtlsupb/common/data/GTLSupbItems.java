package com.gtlsupb.gtlsupb.common.data;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.data.GTItems;

import com.gtlsupb.gtlsupb.api.registry.GTLSupbRegistration;
import com.gtlsupb.gtlsupb.common.item.OverlimitQuantumEntanglementTool;
import com.gtlsupb.gtlsupb.common.item.PatternModifier;
import com.gtlsupb.gtlsupb.common.item.StructureExporterItem;
import com.gtlsupb.gtlsupb.common.item.SuperTerminalCellItem;
import com.gtlsupb.gtlsupb.common.item.UltimateTerminalBehavior;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

public final class GTLSupbItems {

    private GTLSupbItems() {}

    public static ItemEntry<ComponentItem> PATTERN_MODIFIER;

    public static ItemEntry<ComponentItem> QUANTUM_ENTANGLEMENT_TOOL;

    public static ItemEntry<ComponentItem> SUPER_TERMINAL;

    public static ItemEntry<StructureExporterItem> STRUCTURE_EXPORTER;

    public static ItemEntry<ComponentItem> PATTERN_CUT_TOOL;

    public static ItemEntry<ComponentItem> PATTERN_DEBUGGER;

    public static ItemEntry<SuperTerminalCellItem> SUPER_TERMINAL_CELL;

    @SuppressWarnings("unchecked")
    public static void init() {
        PATTERN_MODIFIER = GTLSupbRegistration.REGISTRATE
                .item("pattern_modifier", ComponentItem::create)
                .onRegister(GTItems.attach(PatternModifier.INSTANCE))
                .model(NonNullBiConsumer.noop())
                .register();

        QUANTUM_ENTANGLEMENT_TOOL = GTLSupbRegistration.REGISTRATE
                .item("overlimit_quantum_entanglement_tool", ComponentItem::create)
                .properties(p -> p.stacksTo(1))
                .onRegister(GTItems.attach(OverlimitQuantumEntanglementTool.INSTANCE))
                .model(NonNullBiConsumer.noop())
                .register();

        SUPER_TERMINAL = GTLSupbRegistration.REGISTRATE
                .item("super_terminal", ComponentItem::create)
                .properties(p -> p.stacksTo(1))
                .onRegister(GTItems.attach(UltimateTerminalBehavior.INSTANCE))
                .model(NonNullBiConsumer.noop())
                .register();

        PATTERN_CUT_TOOL = GTLSupbRegistration.REGISTRATE
                .item("pattern_cut_tool", ComponentItem::create)
                .properties(p -> p.stacksTo(1))
                .onRegister(GTItems.attach(com.gtlsupb.gtlsupb.common.item.PatternCutToolBehavior.INSTANCE))
                .model(NonNullBiConsumer.noop())
                .register();

        PATTERN_DEBUGGER = GTLSupbRegistration.REGISTRATE
                .item("pattern_debugger", ComponentItem::create)
                .properties(p -> p.stacksTo(1))
                .onRegister(GTItems.attach(com.gtlsupb.gtlsupb.common.item.PatternDebuggerBehavior.INSTANCE))
                .model(NonNullBiConsumer.noop())
                .register();

        STRUCTURE_EXPORTER = GTLSupbRegistration.REGISTRATE
                .item("structure_exporter", StructureExporterItem::new)
                .properties(p -> p.stacksTo(1))
                .model(NonNullBiConsumer.noop())
                .register();

        SUPER_TERMINAL_CELL = GTLSupbRegistration.REGISTRATE
                .item("super_terminal_cell", SuperTerminalCellItem::new)
                .properties(p -> p.stacksTo(1))
                .register();
    }
}
