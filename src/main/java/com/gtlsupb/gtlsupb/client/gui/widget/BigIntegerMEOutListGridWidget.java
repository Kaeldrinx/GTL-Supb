package com.gtlsupb.gtlsupb.client.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.List;

import static com.gtlsupb.gtlsupb.utils.NumberFormatUtil.scientific;
import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawItemStack;
import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawText;

public class BigIntegerMEOutListGridWidget extends DraggableScrollableWidgetGroup {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FLUID = 1;
    private static final int UPDATE_ID = 3;

    protected final Object2ObjectMap<AEKey, BigInteger> list;
    protected final List<BigIntegerStack> displayList = new ObjectArrayList<>();
    private final int visibleRows;
    private int contentRows;

    public BigIntegerMEOutListGridWidget(int x, int y, int visibleRows, Object2ObjectMap<AEKey, BigInteger> internalList) {
        super(x, y, 18 + 140, visibleRows * 18);
        this.list = internalList;
        this.visibleRows = Math.max(1, visibleRows);
        this.contentRows = 0;
        refreshDisplayList();
        syncRows();
    }

    public BigIntegerStack getAt(int index) {
        return index >= 0 && index < displayList.size() ? displayList.get(index) : null;
    }

    private void refreshDisplayList() {
        displayList.clear();
        for (var entry : list.entrySet()) {
            if (entry.getValue() == null || entry.getValue().signum() <= 0) continue;
            displayList.add(new BigIntegerStack(entry.getKey(), entry.getValue()));
        }
    }

    private void syncRows() {
        int required = Math.max(visibleRows, displayList.size());
        if (required < contentRows) {
            while (contentRows > required) {
                contentRows--;
                Widget widget = widgets.remove(widgets.size() - 1);
                removeWidget(widget);
            }
        } else if (required > contentRows) {
            while (contentRows < required) {
                addWidget(new DisplaySlot(0, contentRows * 18, this, contentRows));
                contentRows++;
            }
        }
    }

    private static void writeKey(FriendlyByteBuf buf, AEKey key) {
        if (key instanceof AEItemKey) {
            buf.writeVarInt(TYPE_ITEM);
        } else {
            buf.writeVarInt(TYPE_FLUID);
        }
        key.writeToPacket(buf);
    }

    private static AEKey readKey(FriendlyByteBuf buf) {
        int type = buf.readVarInt();
        return type == TYPE_ITEM ? AEItemKey.fromPacket(buf) : AEFluidKey.fromPacket(buf);
    }

    private void writeList(FriendlyByteBuf buf) {
        refreshDisplayList();
        buf.writeVarInt(displayList.size());
        for (BigIntegerStack stack : displayList) {
            writeKey(buf, stack.what());
            buf.writeUtf(stack.amount().toString(), 32767);
        }
    }

    private void readList(FriendlyByteBuf buf) {
        displayList.clear();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            AEKey key = readKey(buf);
            BigInteger amount = new BigInteger(buf.readUtf(32767));
            displayList.add(new BigIntegerStack(key, amount));
        }
        syncRows();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        writeUpdateInfo(UPDATE_ID, this::writeList);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == UPDATE_ID) readList(buffer);
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        writeList(buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        readList(buffer);
    }

    public record BigIntegerStack(AEKey what, BigInteger amount) {}

    public static class DisplaySlot extends Widget {

        private final BigIntegerMEOutListGridWidget gridWidget;
        private final int index;

        DisplaySlot(int x, int y, BigIntegerMEOutListGridWidget gridWidget, int index) {
            super(new Position(x, y), new Size(18, 18));
            this.gridWidget = gridWidget;
            this.index = index;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            Position pos = getPosition();
            BigIntegerStack stack = gridWidget.getAt(index);
            GuiTextures.NUMBER_BACKGROUND.draw(graphics, mouseX, mouseY, pos.x + 18, pos.y, 140, 18);
            if (stack == null) {
                GuiTextures.SLOT.draw(graphics, mouseX, mouseY, pos.x, pos.y, 18, 18);
                return;
            }
            if (stack.what() instanceof AEFluidKey) {
                GuiTextures.FLUID_SLOT.draw(graphics, mouseX, mouseY, pos.x, pos.y, 18, 18);
            } else {
                GuiTextures.SLOT.draw(graphics, mouseX, mouseY, pos.x, pos.y, 18, 18);
            }
            if (stack.what() instanceof AEItemKey itemKey) {
                ItemStack rendered = new ItemStack(itemKey.getItem());
                drawItemStack(graphics, rendered, pos.x + 1, pos.y + 1, -1, null);
                drawText(graphics, scientific(stack.amount()), pos.x + 21, pos.y + 5, 1, 0xFFFFFFFF);
            } else if (stack.what() instanceof AEFluidKey fluidKey) {
                long amount = stack.amount().min(BigInteger.valueOf(Long.MAX_VALUE)).longValueExact();
                FluidStack rendered = FluidStack.create(fluidKey.getFluid(), amount, fluidKey.getTag());
                DrawerHelper.drawFluidForGui(graphics, rendered, amount, pos.x + 1, pos.y + 1, 16, 16);
                drawText(graphics, scientific(stack.amount()), pos.x + 21, pos.y + 5, 1, 0xFFFFFFFF);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (!isMouseOverElement(mouseX, mouseY)) return;
            BigIntegerStack stack = gridWidget.getAt(index);
            if (stack == null) return;
            Component title = stack.what() instanceof AEItemKey itemKey ? itemKey.toStack(1).getHoverName() : Component.literal(stack.what().toString());
            graphics.renderTooltip(Minecraft.getInstance().font,
                    Component.literal(title.getString() + " x" + scientific(stack.amount())), mouseX, mouseY);
        }
    }
}
