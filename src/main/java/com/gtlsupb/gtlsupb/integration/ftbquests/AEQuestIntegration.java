package com.gtlsupb.gtlsupb.integration.ftbquests;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import com.gtlsupb.gtlsupb.GTLSupb;

public class AEQuestIntegration {

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        AEQuestRewardHandler.init();

        var eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(AEQuestIntegration::onServerTick);

        GTLSupb.LOGGER.info("AE2-FTB Quests integration initialized");
    }

    private static void onServerTick(TickEvent.ServerTickEvent event) {
        AEQuestDetector.onServerTick(event);
    }
}
