package com.gtlsupb.gtlsupb.api.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;

import com.gtlsupb.gtlsupb.common.machine.multiblock.UniversalOrderKeeper;

import java.util.List;

public class UniversalOrderModeConfigurator implements IFancyUIProvider {

    private static final int COLS = 7;
    private static final int SLOT_SIZE = 18;
    private static final int VIEW_HEIGHT = 132;

    private final UniversalOrderKeeper host;

    public UniversalOrderModeConfigurator(UniversalOrderKeeper host) {
        this.host = host;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtlsupb.gui.universal_order.mode_slot");
    }

    @Override
    public IGuiTexture getTabIcon() {
        return GuiTextures.INT_CIRCUIT_OVERLAY;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        try {

            int slots = Math.max(0, Math.min(UniversalOrderKeeper.MODE_SLOTS, host.getModeSlot().getSlots()));

            var group = new WidgetGroup(0, 0, 140, VIEW_HEIGHT + 34);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            group.addWidget(new LabelWidget(4, 4, "gtlsupb.gui.universal_order.mode_slot.hint"));

            var slotGroup = new DraggableScrollableWidgetGroup(4, 16, 132, VIEW_HEIGHT)
                    .setYScrollBarWidth(6)
                    .setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON);
            for (int i = 0; i < slots; i++) {
                int col = i % COLS;
                int row = i / COLS;

                slotGroup.addWidget(new SlotWidget(host.getModeSlot().storage, i, col * SLOT_SIZE, row * SLOT_SIZE, true, true));
            }

            group.addWidget(slotGroup);
            group.addWidget(new LabelWidget(4, VIEW_HEIGHT + 20, "gtlsupb.gui.universal_order.mode_slot.note"));
            return group;
        } catch (Throwable ignored) {
            return new WidgetGroup(0, 0, 140, 24);
        }
    }

    @Override
    public List<Component> getTabTooltips() {
        return List.of(Component.translatable("gtlsupb.gui.universal_order.mode_slot.tooltip"));
    }
}
