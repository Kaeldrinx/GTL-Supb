package com.gtlsupb.gtlsupb.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class GTLSupbTips {

    private GTLSupbTips() {}

    public static MutableComponent tip(String machine, int index) {
        return Component.translatable("gtlsupb.machine." + machine + ".tooltip." + index);
    }

    public static MutableComponent partTip(String machine, int index) {
        return Component.translatable("gtlsupb.machine." + machine + ".tooltip." + index);
    }

    public static MutableComponent field(String machine, String name, Object... args) {
        return Component.translatable("gtlsupb.machine." + machine + "." + name, args);
    }

    public static Component rainbow(String machine, int index) {
        return com.gtlsupb.gtlsupb.utils.RainbowText.createLanguageRainbowComponentOnServer(tip(machine, index));
    }

    public static Component[] tips(String machine, int... indices) {
        Component[] out = new Component[indices.length];
        for (int i = 0; i < indices.length; i++) {
            out[i] = tip(machine, indices[i]);
        }
        return out;
    }
}
