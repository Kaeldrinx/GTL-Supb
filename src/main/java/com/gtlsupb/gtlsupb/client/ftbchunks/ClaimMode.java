package com.gtlsupb.gtlsupb.client.ftbchunks;

public enum ClaimMode {

    FREEHAND("freehand"),
    RECTANGLE("rectangle");

    private final String key;

    ClaimMode(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public ClaimMode next() {
        ClaimMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public String translationKey() {
        return "gtlsupb.claim_mode." + key;
    }
}
