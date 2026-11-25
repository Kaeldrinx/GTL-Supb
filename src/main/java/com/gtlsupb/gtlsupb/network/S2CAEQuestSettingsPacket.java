package com.gtlsupb.gtlsupb.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CAEQuestSettingsPacket {

    private final boolean detectionEnabled;
    private final boolean rewardEnabled;
    private final int detectionInterval;

    public S2CAEQuestSettingsPacket(boolean detectionEnabled, boolean rewardEnabled, int detectionInterval) {
        this.detectionEnabled = detectionEnabled;
        this.rewardEnabled = rewardEnabled;
        this.detectionInterval = detectionInterval;
    }

    public static void encode(S2CAEQuestSettingsPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.detectionEnabled);
        buf.writeBoolean(msg.rewardEnabled);
        buf.writeVarInt(msg.detectionInterval);
    }

    public static S2CAEQuestSettingsPacket decode(FriendlyByteBuf buf) {
        return new S2CAEQuestSettingsPacket(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readVarInt());
    }

    public static void handle(S2CAEQuestSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> updateClient(msg));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void updateClient(S2CAEQuestSettingsPacket msg) {
        com.gtlsupb.gtlsupb.client.ftbquests.AEQuestClientSettings.update(
                msg.detectionEnabled, msg.rewardEnabled, msg.detectionInterval);
    }
}
