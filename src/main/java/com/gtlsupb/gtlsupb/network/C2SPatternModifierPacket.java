package com.gtlsupb.gtlsupb.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.common.item.PatternModifier;

import java.util.function.Supplier;

public class C2SPatternModifierPacket {

    public static final int FIELD_SYNC = -1;

    public static final int FIELD_SCALE = 0;

    public static final int FIELD_DIV = 1;

    public static final int FIELD_TIMES = 2;

    private static int cachedScale = 1;

    private static int cachedDiv = 1;

    private static int cachedTimes = 1;

    private final int field;
    private final int value;

    public C2SPatternModifierPacket(int field, int value) {
        this.field = field;
        this.value = value;
    }

    public static void encode(C2SPatternModifierPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.field + 1);
        buf.writeVarInt(Math.max(0, msg.value));
    }

    public static C2SPatternModifierPacket decode(FriendlyByteBuf buf) {
        return new C2SPatternModifierPacket(buf.readVarInt() - 1, buf.readVarInt());
    }

    public static void handle(C2SPatternModifierPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (msg.field == FIELD_SCALE) cachedScale = Math.max(1, msg.value);
            if (msg.field == FIELD_DIV) cachedDiv = Math.max(1, msg.value);
            if (msg.field == FIELD_TIMES) cachedTimes = Math.max(1, Math.min(16, msg.value));
            PatternModifier.INSTANCE.gtlsupb$applySettings(cachedScale, cachedDiv, cachedTimes);

            GTLSupbNetworkHandler.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new S2CPatternModifierPacket(cachedScale, cachedDiv, cachedTimes));
        });
        ctx.get().setPacketHandled(true);
    }
}
