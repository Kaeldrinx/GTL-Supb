package com.gtlsupb.gtlsupb.api.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;

import com.gtlsupb.gtlsupb.api.machine.ILimitedDurationMachine;

public class LimitedDurationConfigurator implements IFancyConfigurator {

    private final ILimitedDurationMachine machine;

    public LimitedDurationConfigurator(ILimitedDurationMachine machine) {
        this.machine = machine;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtlsupb.machine.limited_duration_configurator");
    }

    @Override
    public IGuiTexture getIcon() {
        return GuiTextures.BUTTON_INT_CIRCUIT_PLUS;
    }

    @Override
    public Widget createConfigurator() {
        return new WidgetGroup(0, 0, 100, 20)
                .addWidget(
                        new IntInputWidget(
                                () -> machine.getLimitedDuration(),
                                (duration) -> machine.setLimitedDuration(duration != null ? duration : ILimitedDurationMachine.DEFAULT_DURATION)).setMin(ILimitedDurationMachine.MIN_DURATION)
                                .setMax(ILimitedDurationMachine.MAX_DURATION));
    }
}
