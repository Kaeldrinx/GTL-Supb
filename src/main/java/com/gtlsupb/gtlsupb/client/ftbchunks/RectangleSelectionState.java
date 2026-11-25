package com.gtlsupb.gtlsupb.client.ftbchunks;

import dev.ftb.mods.ftblibrary.math.XZ;

public final class RectangleSelectionState {

    private static XZ firstChunk = null;

    private RectangleSelectionState() {}

    public static XZ getFirstChunk() {
        return firstChunk;
    }

    public static void setFirstChunk(XZ pos) {
        firstChunk = pos;
    }

    public static void reset() {
        firstChunk = null;
    }
}
