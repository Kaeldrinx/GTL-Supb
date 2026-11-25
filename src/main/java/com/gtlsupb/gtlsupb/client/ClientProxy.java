package com.gtlsupb.gtlsupb.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.gtlsupb.gtlsupb.client.keybind.AEPatternKeyBindings;
import com.gtlsupb.gtlsupb.common.CommonProxy;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(AEPatternKeyBindings::register);

        MinecraftForge.EVENT_BUS.addListener(AEPatternKeyBindings::onScreenKeyPressed);
    }
}
