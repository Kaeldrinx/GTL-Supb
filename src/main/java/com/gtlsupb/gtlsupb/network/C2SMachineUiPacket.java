package com.gtlsupb.gtlsupb.network;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.api.machine.ui.MachineUiHost;

import java.util.function.Supplier;

public class C2SMachineUiPacket {

    public static final int OPEN = 0;
    public static final int TAKE = 1;
    public static final int PUT = 2;
    public static final int BUTTON = 3;

    private final BlockPos pos;
    private final int action;
    private final int index;
    private final boolean shift;

    public C2SMachineUiPacket(BlockPos pos, int action, int index, boolean shift) {
        this.pos = pos;
        this.action = action;
        this.index = index;
        this.shift = shift;
    }

    public static void encode(C2SMachineUiPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.action);
        buf.writeVarInt(msg.index);
        buf.writeBoolean(msg.shift);
    }

    public static C2SMachineUiPacket decode(FriendlyByteBuf buf) {
        return new C2SMachineUiPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(C2SMachineUiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;
            if (!(player.level().getBlockEntity(msg.pos) instanceof MetaMachineBlockEntity be)) return;
            if (!(be.getMetaMachine() instanceof MachineUiHost host)) return;

            switch (msg.action) {
                case OPEN -> {}
                case TAKE -> take(player, host, msg.index, msg.shift);
                case PUT -> put(player, host, msg.index, msg.shift);
                case BUTTON -> host.uiOnButton(buttonId(host, msg.index), msg.shift);
                default -> {}
            }
            MachineUiSync.send(player, msg.pos, host);
        });
        ctx.get().setPacketHandled(true);
    }

    private static String buttonId(MachineUiHost host, int index) {
        var buttons = host.uiButtons();
        return index >= 0 && index < buttons.size() ? buttons.get(index).id() : "";
    }

    private static void take(ServerPlayer player, MachineUiHost host, int slot, boolean shift) {
        if (slot < 0 || slot >= host.uiSlotCount()) return;
        ItemStack stack = host.uiGetSlot(slot);
        if (stack.isEmpty()) return;
        ItemStack moving = shift ? stack.copy() : stack.copyWithCount(1);
        host.uiSetSlot(slot, shift ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - 1));
        if (!player.getInventory().add(moving)) player.drop(moving, false);
    }

    private static void put(ServerPlayer player, MachineUiHost host, int slot, boolean shift) {
        if (slot < 0 || slot >= host.uiSlotCount()) return;
        ItemStack current = host.uiGetSlot(slot);
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;
            if (!current.isEmpty() && !ItemStack.isSameItemSameTags(current, stack)) continue;
            int space = current.isEmpty() ? stack.getMaxStackSize() : current.getMaxStackSize() - current.getCount();
            if (space <= 0) return;
            int move = shift ? Math.min(space, stack.getCount()) : 1;
            if (current.isEmpty()) {
                host.uiSetSlot(slot, stack.copyWithCount(move));
            } else {
                current.grow(move);
                host.uiSetSlot(slot, current);
            }
            stack.shrink(move);
            return;
        }
    }
}
