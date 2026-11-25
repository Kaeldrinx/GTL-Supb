package com.gtlsupb.gtlsupb.client.ftbquests;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.gtlsupb.gtlsupb.network.C2SAEQuestSubmitPacket;
import com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import org.lwjgl.glfw.GLFW;

public class AEQuestSubmitScreen extends Screen {

    private static final int BOX_W = 90;
    private static final int BOX_H = 20;
    private static final int BTN_H = 20;
    private static final int BTN_GAP = 4;

    private final long questId;
    private final Component questTitle;

    private EditBox countBox;
    private Button confirmBtn;

    public AEQuestSubmitScreen(long questId, Component questTitle) {
        super(Component.translatable("gtlsupb.aequest.submit.screen_title"));
        this.questId = questId;
        this.questTitle = questTitle;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int baseY = this.height / 2 - 4;

        countBox = new EditBox(this.font, cx - BOX_W / 2, baseY, BOX_W, BOX_H,
                Component.translatable("gtlsupb.aequest.submit.count_label"));
        countBox.setMaxLength(6);
        countBox.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
        countBox.setValue(String.valueOf(AEQuestClientSettings.getSubmitCount()));
        countBox.setResponder(s -> {
            try {
                AEQuestClientSettings.setSubmitCount(Integer.parseInt(s));
            } catch (NumberFormatException ignored) {

            }
            updateConfirmState();
        });
        this.addRenderableWidget(countBox);
        this.setInitialFocus(countBox);

        int btnY = baseY + BOX_H + 6;
        int adjW = 50;
        int adjTotal = adjW * 3 + BTN_GAP * 2;
        int ax = cx - adjTotal / 2;
        this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> {
            AEQuestClientSettings.stepSubmitCount(-1);
            syncCountBox();
        }).bounds(ax, btnY, adjW, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> {
            AEQuestClientSettings.stepSubmitCount(1);
            syncCountBox();
        }).bounds(ax + adjW + BTN_GAP, btnY, adjW, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("×10"), b -> {
            AEQuestClientSettings.stepSubmitCount(10);
            syncCountBox();
        }).bounds(ax + (adjW + BTN_GAP) * 2, btnY, adjW, BTN_H).build());

        int bottomY = btnY + BTN_H + 6;
        int confirmW = 130;
        int closeW = 70;
        int bottomTotal = confirmW + BTN_GAP + closeW;
        int btx = cx - bottomTotal / 2;
        confirmBtn = Button.builder(
                Component.translatable("gtlsupb.aequest.submit.confirm"),
                b -> onConfirm())
                .bounds(btx, bottomY, confirmW, BTN_H).build();
        this.addRenderableWidget(confirmBtn);
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                b -> this.onClose())
                .bounds(btx + confirmW + BTN_GAP, bottomY, closeW, BTN_H).build());

        updateConfirmState();
    }

    private void onConfirm() {
        int count = AEQuestClientSettings.getSubmitCount();
        if (count <= 0) return;
        GTLSupbNetworkHandler.CHANNEL.sendToServer(
                new C2SAEQuestSubmitPacket(questId, count));
    }

    private void syncCountBox() {
        if (countBox != null && !countBox.isFocused()) {
            countBox.setValue(String.valueOf(AEQuestClientSettings.getSubmitCount()));
        }
        updateConfirmState();
    }

    private void updateConfirmState() {
        if (confirmBtn != null) {
            confirmBtn.active = AEQuestClientSettings.getSubmitCount() > 0;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int cx = this.width / 2;
        graphics.drawCenteredString(this.font, this.title, cx, this.height / 2 - 50, 0xFFFFFF);
        if (questTitle != null) {
            graphics.drawCenteredString(this.font, questTitle, cx, this.height / 2 - 36, 0xA0A0FF);
        }
        graphics.drawCenteredString(this.font,
                Component.translatable("gtlsupb.aequest.submit.count_label"),
                cx, this.height / 2 - 16, 0xC0C0C0);
        graphics.drawCenteredString(this.font,
                Component.translatable("gtlsupb.aequest.submit.hint"),
                cx, this.height / 2 + 78, 0x808080);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            onConfirm();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        try {
            if (ClientQuestFile.exists()) {
                ClientQuestFile.openBookToQuestObject(questId);
                return;
            }
        } catch (Throwable ignored) {

        }
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(null);
    }
}
