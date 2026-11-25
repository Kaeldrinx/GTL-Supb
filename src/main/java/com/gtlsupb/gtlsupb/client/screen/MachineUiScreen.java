package com.gtlsupb.gtlsupb.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.gtlsupb.gtlsupb.network.C2SMachineUiPacket;
import com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler;
import com.gtlsupb.gtlsupb.network.S2CMachineUiPacket;

public class MachineUiScreen extends Screen {

    private final BlockPos pos;

    private CompoundTag payload;

    private int panelW = 200;

    private int panelH = 160;

    public MachineUiScreen(BlockPos pos, CompoundTag payload) {
        super(Component.literal(""));
        this.pos = pos;
        this.payload = payload;
    }

    public static void onSnapshot(BlockPos pos, CompoundTag payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof MachineUiScreen screen && screen.pos.equals(pos)) {
            screen.payload = payload;
            screen.rebuildWidgets();
            return;
        }
        mc.setScreen(new MachineUiScreen(pos, payload));
    }

    private int slotCount() {
        ListTag items = payload.getList(S2CMachineUiPacket.KEY_ITEMS, 10);
        return items.size();
    }

    private int perRow() {
        return Math.max(1, Math.min(9, payload.getInt(S2CMachineUiPacket.KEY_ROWS)));
    }

    private int left() {
        return (this.width - panelW) / 2;
    }

    private int top() {
        return (this.height - panelH) / 2;
    }

    @Override
    protected void init() {
        if (payload.isEmpty()) {
            GTLSupbNetworkHandler.CHANNEL.sendToServer(new C2SMachineUiPacket(pos, C2SMachineUiPacket.OPEN, 0, false));
            return;
        }
        int slots = slotCount();
        int rows = (slots + perRow() - 1) / perRow();
        panelW = Math.max(180, perRow() * 18 + 24);
        panelH = 34 + rows * 18 + 8 + 44;

        CompoundTag flags = payload.getCompound(S2CMachineUiPacket.KEY_FLAGS);
        int i = 0;
        for (String id : flags.getAllKeys()) {
            boolean on = flags.getBoolean(id);
            int idx = i++;
            int y = top() + panelH - 40;
            addRenderableWidget(Button.builder(Component.literal((on ? "● " : "○ ") + id),
                    b -> GTLSupbNetworkHandler.CHANNEL.sendToServer(
                            new C2SMachineUiPacket(pos, C2SMachineUiPacket.BUTTON, idx, false)))
                    .bounds(left() + 12 + (idx % 3) * 62, y + (idx / 3) * 20, 58, 18).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int left = left();
        int top = top();

        graphics.fill(left, top, left + panelW, top + panelH, 0xF00B0F14);
        graphics.fill(left, top, left + panelW, top + 1, 0xFF3A6EA5);
        graphics.fill(left, top + panelH - 1, left + panelW, top + panelH, 0xFF3A6EA5);
        graphics.fill(left, top, left + 1, top + panelH, 0xFF3A6EA5);
        graphics.fill(left + panelW - 1, top, left + panelW, top + panelH, 0xFF3A6EA5);

        String titleKey = payload.getString(S2CMachineUiPacket.KEY_TITLE);
        if (!titleKey.isEmpty()) {
            graphics.drawString(this.font, Component.translatable(titleKey), left + 8, top + 8, 0xFFFFFF, false);
        }

        ListTag items = payload.getList(S2CMachineUiPacket.KEY_ITEMS, 10);
        int perRow = perRow();
        for (int i = 0; i < items.size(); i++) {
            int col = i % perRow;
            int row = i / perRow;
            int x = left() + 12 + col * 18;
            int y = top + 34 + row * 18;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF2A3A4A);
            ItemStack stack = S2CMachineUiPacket.readItem(items, i);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, x, y);
                graphics.renderItemDecorations(this.font, stack, x, y);
            }
        }

        ListTag info = payload.getList(S2CMachineUiPacket.KEY_INFO, 8);
        for (int i = 0; i < info.size(); i++) {
            graphics.drawString(this.font, info.getString(i), left + 8,
                    top + panelH - 56 - (info.size() - i) * 10, 0x9CC7E8, false);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int perRow = perRow();
        int slots = slotCount();
        for (int i = 0; i < slots; i++) {
            int col = i % perRow;
            int row = i / perRow;
            int x = left() + 12 + col * 18;
            int y = top() + 34 + row * 18;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                int action = button == 0 ? C2SMachineUiPacket.TAKE : C2SMachineUiPacket.PUT;
                boolean shift = hasShiftDown();
                GTLSupbNetworkHandler.CHANNEL.sendToServer(new C2SMachineUiPacket(pos, action, i, shift));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
