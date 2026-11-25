package com.gtlsupb.gtlsupb.api.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;

import com.gtlsupb.gtlsupb.common.machine.multiblock.module.UniversalOrderModuleBase;

import java.util.List;

public class UniversalOrderModuleConfigurator implements IFancyUIProvider {

    private static final int VIEW_HEIGHT = 150;

    private static final int ROW_HEIGHT = 20;

    private static final int BAR_WIDTH = 6;

    private static final int LIST_WIDTH = 132;

    private static final int ROW_WIDTH = LIST_WIDTH - BAR_WIDTH - 2;

    private final UniversalOrderModuleBase module;

    public UniversalOrderModuleConfigurator(UniversalOrderModuleBase module) {
        this.module = module;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtlsupb.gui.universal_order.module_modes");
    }

    @Override
    public IGuiTexture getTabIcon() {
        return GuiTextures.INT_CIRCUIT_OVERLAY;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        try {
            WidgetGroup group = new WidgetGroup(0, 0, 140, VIEW_HEIGHT + 30);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            group.addWidget(new LabelWidget(4, 4, "gtlsupb.gui.universal_order.available_modes"));

            if (!module.isModeSelectable()) {
                group.addWidget(new LabelWidget(4, 20, "gtlsupb.gui.universal_order.no_mode_select"));
                return group;
            }

            GTRecipeType[] available = module.getAvailableRecipeTypes();
            DraggableScrollableWidgetGroup list = new DraggableScrollableWidgetGroup(4, 16, LIST_WIDTH, VIEW_HEIGHT)
                    .setYScrollBarWidth(BAR_WIDTH)
                    .setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON);

            int count = Math.min(available.length, Long.SIZE);
            for (int i = 0; i < count; i++) {
                int index = i;
                GTRecipeType type = available[i];
                String name = type == null || type.registryName == null ? "?" : Component.translatable(type.registryName.toLanguageKey()).getString();

                IGuiTexture enabled = new GuiTextureGroup(
                        ResourceBorderTexture.BUTTON_COMMON.copy().setColor(ColorPattern.CYAN.color),
                        new TextTexture(name).setWidth(ROW_WIDTH).setType(TextTexture.TextType.ROLL));
                IGuiTexture disabled = new GuiTextureGroup(
                        ResourceBorderTexture.BUTTON_COMMON.copy(),
                        new TextTexture(name).setWidth(ROW_WIDTH).setType(TextTexture.TextType.ROLL));

                int y = i * ROW_HEIGHT;
                list.addWidget(new ButtonWidget(0, y, ROW_WIDTH, ROW_HEIGHT, IGuiTexture.EMPTY,
                        clickData -> module.toggleMode(index)));
                list.addWidget(new ImageWidget(0, y, ROW_WIDTH, ROW_HEIGHT,
                        () -> module.isModeEnabled(index) ? enabled : disabled));
            }

            group.addWidget(list);

            int buttonY = VIEW_HEIGHT + 20;
            group.addWidget(new ButtonWidget(4, buttonY, 64, 18, GuiTextures.BUTTON,
                    clickData -> module.setAllModes(true)));
            group.addWidget(new ImageWidget(4, buttonY, 64, 18,
                    new TextTexture("gtlsupb.gui.universal_order.select_all").setWidth(64)));
            group.addWidget(new ButtonWidget(72, buttonY, 64, 18, GuiTextures.BUTTON,
                    clickData -> module.setAllModes(false)));
            group.addWidget(new ImageWidget(72, buttonY, 64, 18,
                    new TextTexture("gtlsupb.gui.universal_order.select_none").setWidth(64)));
            return group;
        } catch (Throwable ignored) {
            return new WidgetGroup(0, 0, 140, 24);
        }
    }

    @Override
    public List<Component> getTabTooltips() {
        return List.of(Component.translatable("gtlsupb.gui.universal_order.module_modes.tooltip"));
    }
}
