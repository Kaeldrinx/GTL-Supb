package com.gtlsupb.gtlsupb.common.modify;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.RegistrateDisplayItemsGenerator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import com.gtlsupb.gtlsupb.GTLSupb;
import com.gtlsupb.gtlsupb.api.registry.GTLSupbRegistration;
import com.tterrag.registrate.util.entry.RegistryEntry;

public final class GTLSupbCreativeModeTabs {

    private GTLSupbCreativeModeTabs() {}

    public static final RegistryEntry<CreativeModeTab> GTLSUPB_MACHINE = GTLSupbRegistration.REGISTRATE
            .defaultCreativeTab("machine", (builder) -> {
                builder.displayItems(new RegistrateDisplayItemsGenerator("machine", GTLSupbRegistration.REGISTRATE))
                        .title(
                                GTLSupbRegistration.REGISTRATE.addLang("itemGroup",
                                        new ResourceLocation(GTLSupb.MOD_ID, "machine"), "GTLSupb 机器"))
                        .build();
            })
            .register();

    public static void init() {}
}
