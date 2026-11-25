package com.gtlsupb.gtlsupb.integration.jei;

import com.lowdragmc.lowdraglib.LDLib;

import net.minecraft.resources.ResourceLocation;

import com.gtlsupb.gtlsupb.GTLSupb;
import com.gtlsupb.gtlsupb.common.machine.GTLSupbMachines;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class GTLSupbJeiPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return GTLSupb.id("jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (LDLib.isReiLoaded() || LDLib.isEmiLoaded()) return;
        registration.addRecipeCatalyst(GTLSupbMachines.PRIMITIVE_STONE_FURNACE.asStack(), RecipeTypes.SMELTING);
    }
}
