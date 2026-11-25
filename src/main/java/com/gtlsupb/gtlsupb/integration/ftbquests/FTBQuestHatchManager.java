package com.gtlsupb.gtlsupb.integration.ftbquests;

import appeng.api.networking.IGrid;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitFTBQuestIntegrationHatchPartMachine;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class FTBQuestHatchManager {

    private static final Set<OverlimitFTBQuestIntegrationHatchPartMachine> HATCHES = new ObjectOpenHashSet<>();

    private FTBQuestHatchManager() {}

    public static void register(OverlimitFTBQuestIntegrationHatchPartMachine hatch) {
        if (hatch == null) return;
        HATCHES.add(hatch);
    }

    public static void unregister(OverlimitFTBQuestIntegrationHatchPartMachine hatch) {
        if (hatch == null) return;
        HATCHES.remove(hatch);
    }

    public static List<OverlimitFTBQuestIntegrationHatchPartMachine> getHatches() {
        List<OverlimitFTBQuestIntegrationHatchPartMachine> out = new ArrayList<>();
        for (OverlimitFTBQuestIntegrationHatchPartMachine hatch : HATCHES) {
            if (hatch == null || hatch.getLevel() == null) continue;
            out.add(hatch);
        }
        return out;
    }

    public static IGrid getGrid(OverlimitFTBQuestIntegrationHatchPartMachine hatch) {
        if (hatch == null) return null;
        try {
            IGrid grid = hatch.getMainNode().getGrid();
            if (grid != null) return grid;
            if (hatch.getMainNode().getNode() != null) return hatch.getMainNode().getNode().getGrid();
        } catch (Throwable ignored) {}
        return null;
    }

    public static appeng.api.networking.security.IActionSource getActionSource(
                                                                               OverlimitFTBQuestIntegrationHatchPartMachine hatch) {
        return appeng.api.networking.security.IActionSource.ofMachine(
                () -> hatch.getMainNode().getNode());
    }

    public static UUID getTeamId(UUID ownerUuid) {
        if (ownerUuid == null) return null;
        try {
            return com.hepdd.gtmthings.utils.TeamUtil.getTeamUUID(ownerUuid);
        } catch (Throwable ignored) {
            return ownerUuid;
        }
    }
}
