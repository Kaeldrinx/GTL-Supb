package com.gtlsupb.gtlsupb.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

public final class RainbowText {

    public static final RainbowText INSTANCE = new RainbowText();

    private RainbowText() {}

    public static String stripLegacyCodes(String text) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\u00A7' && i + 1 < text.length()) {
                i++;
                continue;
            }
            result.append(ch);
        }
        return result.toString();
    }

    private static final int CACHE_SIZE = 32;

    private static final long FRAME_MS = 100L;

    private static final String[] CACHE_KEYS = new String[CACHE_SIZE];
    private static final ChatFormatting[] CACHE_EXTRAS = new ChatFormatting[CACHE_SIZE];
    private static final long[] CACHE_FRAMES = new long[CACHE_SIZE];
    private static final MutableComponent[] CACHE_VALUES = new MutableComponent[CACHE_SIZE];

    private static MutableComponent perCharRainbow(String text, ChatFormatting extra) {
        String key = text == null ? "" : text;
        long frame = System.currentTimeMillis() / FRAME_MS;
        int slot = (key.hashCode() & 0x7FFFFFFF) % CACHE_SIZE;
        if (CACHE_FRAMES[slot] == frame && CACHE_EXTRAS[slot] == extra && key.equals(CACHE_KEYS[slot])) {
            return CACHE_VALUES[slot].copy();
        }
        MutableComponent built = buildPerCharRainbow(key, extra);
        CACHE_KEYS[slot] = key;
        CACHE_EXTRAS[slot] = extra;
        CACHE_FRAMES[slot] = frame;
        CACHE_VALUES[slot] = built;
        return built.copy();
    }

    private static MutableComponent buildPerCharRainbow(String text, ChatFormatting extra) {
        MutableComponent result = Component.empty();
        String cleaned = stripLegacyCodes(text);
        int[] codePoints = cleaned.codePoints().toArray();
        if (codePoints.length == 0) return result;

        long now = System.currentTimeMillis();
        double timePhase = (now % 4000L) / 4000.0;
        double sparkPhase = (now % 2000L) / 2000.0;
        double hueSpan = 1.15;
        int length = Math.max(1, codePoints.length);
        for (int i = 0; i < codePoints.length; i++) {
            double position = length <= 1 ? 0.0 : (double) i / (length - 1);
            double hue = (timePhase + position * hueSpan) % 1.0;

            hue = (hue + 0.045 * Math.sin(position * Math.PI * 2.0 + timePhase * Math.PI * 4.0) + 1.0) % 1.0;

            double sparkDistance = position - sparkPhase;
            double spark = Math.exp(-(sparkDistance * sparkDistance) / 0.018);
            double shimmer = 0.5 + 0.5 * Math.sin(position * Math.PI * 6.0 + timePhase * Math.PI * 6.0);
            float saturation = (0.62f + 0.08f * (float) shimmer) * (float) (1.0 - 0.78 * spark);
            int rgb = hsvToRgb(hue, saturation, 1.0f);
            MutableComponent ch = Component.literal(new String(Character.toChars(codePoints[i])))
                    .withStyle(style -> style.withColor(TextColor.fromRgb(rgb)));
            if (extra != null) ch.withStyle(extra);
            result.append(ch);
        }
        return result;
    }

    private static int hsvToRgb(double hue, float saturation, float value) {
        double h = ((hue % 1.0) + 1.0) % 1.0;
        float s = Math.min(1.0f, Math.max(0.0f, saturation));
        float v = Math.min(1.0f, Math.max(0.0f, value));
        int index = (int) (h * 6.0) % 6;
        float fraction = (float) (h * 6.0 - (int) (h * 6.0));
        float p = v * (1.0f - s);
        float q = v * (1.0f - s * fraction);
        float t = v * (1.0f - s * (1.0f - fraction));
        float r;
        float g;
        float b;
        switch (index) {
            case 0 -> {
                r = v;
                g = t;
                b = p;
            }
            case 1 -> {
                r = q;
                g = v;
                b = p;
            }
            case 2 -> {
                r = p;
                g = v;
                b = t;
            }
            case 3 -> {
                r = p;
                g = q;
                b = v;
            }
            case 4 -> {
                r = t;
                g = p;
                b = v;
            }
            default -> {
                r = v;
                g = p;
                b = q;
            }
        }
        int ri = Math.round(r * 255.0f);
        int gi = Math.round(g * 255.0f);
        int bi = Math.round(b * 255.0f);
        return (ri << 16) | (gi << 8) | bi;
    }

    public static Component createRainbowComponent(String text) {
        return perCharRainbow(text, null);
    }

    public static Component createLanguageRainbowComponent(MutableComponent component) {
        return perCharRainbow(component.getString(), null);
    }

    public static Component createLanguageRainbowComponentOnServer(MutableComponent component) {
        return perCharRainbow(component.getString(), null);
    }

    public static Component createPerCharRainbowComponent(String text) {
        return perCharRainbow(text, null);
    }

    public static Component createPerCharRainbowBoldComponent(String text) {
        return perCharRainbow(text, ChatFormatting.BOLD);
    }

    public static Component createObfuscatedRainbowComponent(String text) {
        return perCharRainbow(text, ChatFormatting.OBFUSCATED);
    }

    public static Component createObfuscatedDeleteComponent(String text) {
        return perCharRainbow(text, ChatFormatting.OBFUSCATED).copy().withStyle(ChatFormatting.STRIKETHROUGH);
    }

    public static Component createRainbowLabeledValue(String labelKey, String value) {
        MutableComponent translated = Component.translatable(labelKey, value);
        return createLanguageRainbowComponent(translated);
    }

    public static Component createRainbowDualValue(String labelKey, String value1, String value2) {
        MutableComponent translated = Component.translatable(labelKey, value1, value2);
        return createLanguageRainbowComponent(translated);
    }

    public static Component createRainbowTitle(String text) {
        return createPerCharRainbowBoldComponent(text);
    }
}
