package com.gtlsupb.gtlsupb.client.ftbchunks;

public final class ClaimModeState {

    private static ClaimMode current = ClaimMode.FREEHAND;

    private ClaimModeState() {}

    public static ClaimMode current() {
        return current;
    }

    public static ClaimMode cycle() {
        current = current.next();
        return current;
    }

    public static void set(ClaimMode mode) {
        current = mode;
    }
}
