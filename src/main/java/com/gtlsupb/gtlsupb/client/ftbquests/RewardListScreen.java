package com.gtlsupb.gtlsupb.client.ftbquests;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.gtlsupb.gtlsupb.integration.ftbquests.AEQuestSubmitHelper;

import java.util.List;

public class RewardListScreen extends Screen {

    private static final int VISIBLE_ROWS = 8;
    private static final int ROW_HEIGHT = 18;
    private static final int TOP_PADDING = 30;
    private static final int BOTTOM_PADDING = 30;
    private static final int ROW_X = 30;

    private final List<AEQuestSubmitHelper.RewardEntry> entries;
    private int scrollOffset = 0;

    public RewardListScreen(List<AEQuestSubmitHelper.RewardEntry> entries) {
        super(Component.translatable("gtlsupb.aequest.reward.list.title"));
        this.entries = entries;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                b -> this.onClose())
                .bounds(this.width / 2 - 40, this.height - 24, 80, 20)
                .build());

        clampScroll();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);

        int listTop = TOP_PADDING;
        int listBottom = listTop + VISIBLE_ROWS * ROW_HEIGHT;
        int listWidth = 220;
        int listLeft = (this.width - listWidth) / 2;
        graphics.fill(listLeft - 2, listTop - 2, listLeft + listWidth + 2, listBottom + 2, 0x80000000);

        int maxRow = Math.min(VISIBLE_ROWS, entries.size() - scrollOffset);
        for (int i = 0; i < maxRow; i++) {
            AEQuestSubmitHelper.RewardEntry entry = entries.get(scrollOffset + i);
            int y = listTop + i * ROW_HEIGHT;

            ItemStack stack = entry.getStack();
            graphics.renderItem(stack, listLeft + ROW_X - 18, y + 1);

            Component name = stack.getHoverName();
            Component line = Component.literal(name.getString() + " §ex" + entry.getCount());
            graphics.drawString(this.font, line, listLeft + ROW_X, y + 5, 0xFFFFFF);
        }

        if (entries.size() > VISIBLE_ROWS) {
            String indicator = (scrollOffset + 1) + "-" +
                    Math.min(scrollOffset + VISIBLE_ROWS, entries.size()) +
                    "/" + entries.size();
            graphics.drawCenteredString(this.font, indicator,
                    this.width / 2, listBottom + 6, 0xA0A0A0);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (delta < 0) {
            int maxScroll = Math.max(0, entries.size() - VISIBLE_ROWS);
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void clampScroll() {
        int maxScroll = Math.max(0, entries.size() - VISIBLE_ROWS);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;
    }
}
