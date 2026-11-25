package com.gtlsupb.gtlsupb.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.gtlsupb.gtlsupb.network.C2SPatternModifierPacket;
import com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler;

public class PatternModifierScreen extends Screen {

    private static final int PANEL_W = 220;

    private static final int PANEL_H = 132;

    private int scale = 1;

    private int divScale = 1;

    private int applied = 1;

    public PatternModifierScreen() {
        super(Component.translatable("item.gtlsupb.pattern_modifier"));
    }

    public static void onSync(int scale, int div, int times) {
        if (Minecraft.getInstance().screen instanceof PatternModifierScreen screen) {
            screen.scale = Math.max(1, scale);
            screen.divScale = Math.max(1, div);
            screen.applied = Math.max(1, times);
            screen.rebuildWidgets();
        }
    }

    private int left() {
        return (this.width - PANEL_W) / 2;
    }

    private int top() {
        return (this.height - PANEL_H) / 2;
    }

    @Override
    protected void init() {
        int left = left();
        int top = top();

        GTLSupbNetworkHandler.CHANNEL.sendToServer(
                new C2SPatternModifierPacket(C2SPatternModifierPacket.FIELD_SYNC, 0));

        row(left + 12, top + 40, C2SPatternModifierPacket.FIELD_SCALE, scale, 1, 1 << 20,
                "gtlsupb.pattern_modifier.scale");
        row(left + 12, top + 64, C2SPatternModifierPacket.FIELD_DIV, divScale, 1, 1 << 20,
                "gtlsupb.pattern_modifier.div_scale");
        row(left + 12, top + 88, C2SPatternModifierPacket.FIELD_TIMES, applied, 1, 16,
                "gtlsupb.pattern_modifier.applied_number");
    }

    private void row(int x, int y, int field, int value, int min, int max, String labelKey) {
        addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            int next = Math.max(min, value - 1);
            sendSet(field, next);
        }).bounds(x + 70, y, 20, 18).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            int next = Math.min(max, value + 1);
            sendSet(field, next);
        }).bounds(x + 152, y, 20, 18).build());
    }

    private void sendSet(int field, int value) {
        GTLSupbNetworkHandler.CHANNEL.sendToServer(new C2SPatternModifierPacket(field, value));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int left = left();
        int top = top();

        graphics.fill(left, top, left + PANEL_W, top + PANEL_H, 0xF00B0F14);
        graphics.fill(left, top, left + PANEL_W, top + 1, 0xFF3A6EA5);
        graphics.fill(left, top + PANEL_H - 1, left + PANEL_W, top + PANEL_H, 0xFF3A6EA5);
        graphics.fill(left, top, left + 1, top + PANEL_H, 0xFF3A6EA5);
        graphics.fill(left + PANEL_W - 1, top, left + PANEL_W, top + PANEL_H, 0xFF3A6EA5);

        graphics.drawString(this.font, this.title, left + 12, top + 10, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gtlsupb.pattern_modifier.hint"),
                left + 12, top + 24, 0x9CC7E8, false);

        graphics.drawString(this.font, Component.translatable("gtlsupb.pattern_modifier.scale"),
                left + 12, top + 45, 0xCCE3F5, false);
        graphics.drawString(this.font, String.valueOf(scale), left + 110, top + 45, 0xFFFFFF, false);

        graphics.drawString(this.font, Component.translatable("gtlsupb.pattern_modifier.div_scale"),
                left + 12, top + 69, 0xCCE3F5, false);
        graphics.drawString(this.font, String.valueOf(divScale), left + 110, top + 69, 0xFFFFFF, false);

        graphics.drawString(this.font, Component.translatable("gtlsupb.pattern_modifier.applied_number"),
                left + 12, top + 93, 0xCCE3F5, false);
        graphics.drawString(this.font, String.valueOf(applied), left + 110, top + 93, 0xFFFFFF, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
