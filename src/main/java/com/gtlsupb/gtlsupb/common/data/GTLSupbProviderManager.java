package com.gtlsupb.gtlsupb.common.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GTLSupbProviderManager {

    private static final Map<UUID, Integer> DATA_PROVIDER_COUNT = new HashMap<>();

    private static final Map<UUID, Integer> COMPUTE_PROVIDER_COUNT = new HashMap<>();

    public static final int CWU_PER_ENGINE = 1000;

    private GTLSupbProviderManager() {}

    public static void registerDataProvider(UUID owner) {
        if (owner == null) return;
        DATA_PROVIDER_COUNT.merge(owner, 1, Integer::sum);
    }

    public static void unregisterDataProvider(UUID owner) {
        if (owner == null) return;
        int count = DATA_PROVIDER_COUNT.merge(owner, -1, Integer::sum);
        if (count <= 0) {
            DATA_PROVIDER_COUNT.remove(owner);
        }
    }

    public static int getDataProviderCount(UUID owner) {
        return owner == null ? 0 : DATA_PROVIDER_COUNT.getOrDefault(owner, 0);
    }

    public static void registerComputeProvider(UUID owner) {
        if (owner == null) return;
        COMPUTE_PROVIDER_COUNT.merge(owner, 1, Integer::sum);
    }

    public static void unregisterComputeProvider(UUID owner) {
        if (owner == null) return;
        int count = COMPUTE_PROVIDER_COUNT.merge(owner, -1, Integer::sum);
        if (count <= 0) {
            COMPUTE_PROVIDER_COUNT.remove(owner);
        }
    }

    public static int getComputeProviderCount(UUID owner) {
        return owner == null ? 0 : COMPUTE_PROVIDER_COUNT.getOrDefault(owner, 0);
    }

    public static int getAvailableCWU(UUID owner) {
        return getComputeProviderCount(owner) * CWU_PER_ENGINE;
    }
}
