package com.gtlsupb.gtlsupb.api.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import net.minecraft.network.chat.Component;

import com.gtlsupb.gtlsupb.api.machine.IBatchProcessingMachine;

import java.util.List;

public final class BatchProcessingFancyConfigurator {

    private static final IGuiTexture BATCH_DISABLED_TEXTURE = GuiTextures.BUTTON_WORKING_ENABLE.getSubTexture(0.0, 0.5, 1.0, 0.5);
    private static final IGuiTexture BATCH_ENABLED_TEXTURE = GuiTextures.BUTTON_WORKING_ENABLE.getSubTexture(0.0, 0.0, 1.0, 0.5);

    private BatchProcessingFancyConfigurator() {}

    public static IFancyConfigurator create(IBatchProcessingMachine machine) {
        return new IFancyConfiguratorButton.Toggle(
                BATCH_DISABLED_TEXTURE,
                BATCH_ENABLED_TEXTURE,
                machine::isBatchEnabled,
                (clickData, pressed) -> machine.setBatchEnabled(pressed))
                .setTooltipsSupplier(pressed -> List.of(Component.translatable(
                        pressed ? "gtlsupb.gui.batch_processing.enabled" : "gtlsupb.gui.batch_processing.disabled")));
    }
}
