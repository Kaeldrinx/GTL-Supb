package com.gtlsupb.gtlsupb.integration.ae2;

import appeng.api.stacks.AEKey;

import java.util.LinkedHashSet;
import java.util.Set;

public final class CatalystPlanNotes {

    private CatalystPlanNotes() {}

    private static final ThreadLocal<Set<AEKey>> NOTES = ThreadLocal.withInitial(LinkedHashSet::new);
    private static final ThreadLocal<Boolean> RECORDING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public static void beginPlan() {
        NOTES.get().clear();
        RECORDING.set(Boolean.TRUE);
    }

    public static void endPlan() {
        RECORDING.set(Boolean.FALSE);
    }

    public static void note(AEKey key) {
        if (key == null) return;
        try {
            if (Boolean.TRUE.equals(RECORDING.get())) NOTES.get().add(key);
        } catch (Throwable ignored) {}
    }

    public static Set<AEKey> drain() {
        Set<AEKey> keys = new LinkedHashSet<>(NOTES.get());
        NOTES.get().clear();
        return keys;
    }
}
