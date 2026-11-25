package com.gtlsupb.gtlsupb.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CPatternModifierPacket {

    private final int scale;
    private final int div;
    private final int times;

    public S2CPatternModifierPacket(int scale, int div, int times) {
        this.scale = scale;
        this.div = div;
        this.times = times;
    }

    public static void encode(S2CPatternModifierPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.scale);
        buf.writeVarInt(msg.div);
        buf.writeVarInt(msg.times);
    }

    public static S2CPatternModifierPacket decode(FriendlyByteBuf buf) {
        return new S2CPatternModifierPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(S2CPatternModifierPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.gtlsupb.gtlsupb.client.screen.PatternModifierScreen
                        .onSync(msg.scale, msg.div, msg.times)));
        ctx.get().setPacketHandled(true);
    }
}
