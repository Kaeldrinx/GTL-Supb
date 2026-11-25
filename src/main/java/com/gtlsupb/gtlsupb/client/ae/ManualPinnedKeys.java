package com.gtlsupb.gtlsupb.client.ae;

import appeng.api.stacks.AEKey;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ManualPinnedKeys {

    private static final Set<AEKey> MANUAL = new LinkedHashSet<>();

    private ManualPinnedKeys() {}

    public static boolean isManual(AEKey key) {
        return key != null && MANUAL.contains(key);
    }

    public static boolean isEmpty() {
        return MANUAL.isEmpty();
    }

    public static void add(AEKey key) {
        if (key != null) {
            MANUAL.add(key);
        }
    }

    public static void remove(AEKey key) {
        if (key != null) {
            MANUAL.remove(key);
        }
    }

    public static Set<AEKey> all() {
        return Collections.unmodifiableSet(MANUAL);
    }
}
