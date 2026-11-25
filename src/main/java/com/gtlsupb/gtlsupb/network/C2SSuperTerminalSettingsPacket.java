package com.gtlsupb.gtlsupb.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SSuperTerminalSettingsPacket {

    public static final String[] FLAG_KEYS = {
            "NoHatchMode", "ReplaceMode", "IsFlipped", "DismantleMode",
            "AEMode", "ModuleMode", "DetectMode", "ModuleFormMode"
    };

    private final InteractionHand hand;
    private final int field;
    private final int value;
    private final boolean flag;

    public C2SSuperTerminalSettingsPacket(InteractionHand hand, int field, int value, boolean flag) {
        this.hand = hand;
        this.field = field;
        this.value = value;
        this.flag = flag;
    }

    public static void encode(C2SSuperTerminalSettingsPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.hand);
        buf.writeVarInt(msg.field);
        buf.writeVarInt(msg.value);
        buf.writeBoolean(msg.flag);
    }

    public static C2SSuperTerminalSettingsPacket decode(FriendlyByteBuf buf) {
        return new C2SSuperTerminalSettingsPacket(buf.readEnum(InteractionHand.class),
                buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(C2SSuperTerminalSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ItemStack stack = player.getItemInHand(msg.hand);
            if (stack.isEmpty()) return;
            CompoundTag tag = stack.getOrCreateTag();
            if (msg.field >= 0 && msg.field < FLAG_KEYS.length) {
                tag.putBoolean(FLAG_KEYS[msg.field], msg.flag);
            } else if (msg.field == FLAG_KEYS.length) {
                tag.putInt("Tier", Math.max(0, msg.value));
            } else if (msg.field == FLAG_KEYS.length + 1) {
                tag.putInt("RepeatCount", Math.max(1, Math.min(4096, msg.value)));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
