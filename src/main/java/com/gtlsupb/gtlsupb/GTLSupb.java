package com.gtlsupb.gtlsupb;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import com.gtlsupb.gtlsupb.client.ClientProxy;
import com.gtlsupb.gtlsupb.common.CommonProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(GTLSupb.MOD_ID)
public class GTLSupb {

    public static final String MOD_ID = "gtlsupb";
    public static final String NAME = "GTLSupb";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public GTLSupb() {
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("{} initialized successfully!", NAME);
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MOD_ID, name);
    }
}
