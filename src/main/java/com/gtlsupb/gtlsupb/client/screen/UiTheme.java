package com.gtlsupb.gtlsupb.client.screen;

import net.minecraft.client.gui.GuiGraphics;

public final class UiTheme {

    public static final int BG = 0xF00B0F14;

    public static final int BORDER = 0xFF3A6EA5;

    public static final int DIVIDER = 0xFF24405C;

    public static final int TEXT = 0xFFFFFF;

    public static final int ACCENT = 0x9CC7E8;

    public static final int LABEL = 0xCCE3F5;

    public static final int VALUE = 0xFFFFFF;

    public static final int SWITCH_ON = 0xFF6BE07A;

    public static final int SWITCH_OFF = 0xFF9A9A9A;

    private UiTheme() {}

    public static void panel(GuiGraphics graphics, int x, int y, int w, int h, int titleH) {
        graphics.fill(x, y, x + w, y + h, BG);
        graphics.fill(x, y, x + w, y + 1, BORDER);
        graphics.fill(x, y + h - 1, x + w, y + h, BORDER);
        graphics.fill(x, y, x + 1, y + h, BORDER);
        graphics.fill(x + w - 1, y, x + w, y + h, BORDER);
        if (titleH > 0) {
            graphics.fill(x + 8, y + titleH, x + w - 8, y + titleH + 1, DIVIDER);
        }
    }
}
