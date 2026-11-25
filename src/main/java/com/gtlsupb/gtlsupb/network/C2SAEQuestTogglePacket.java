package com.gtlsupb.gtlsupb.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import com.gtlsupb.gtlsupb.integration.ftbquests.AEQuestSettings;

import java.util.function.Supplier;

public class C2SAEQuestTogglePacket {

    public static final int ACTION_TOGGLE_DETECTION = 0;
    public static final int ACTION_TOGGLE_REWARD = 1;
    public static final int ACTION_INTERVAL_PLUS = 3;
    public static final int ACTION_INTERVAL_MINUS = 4;
    public static final int ACTION_QUERY = 5;

    private final int action;

    public C2SAEQuestTogglePacket(int action) {
        this.action = action;
    }

    public static void encode(C2SAEQuestTogglePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action);
    }

    public static C2SAEQuestTogglePacket decode(FriendlyByteBuf buf) {
        return new C2SAEQuestTogglePacket(buf.readVarInt());
    }

    public static void handle(C2SAEQuestTogglePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            var server = player.getServer();
            var uuid = player.getUUID();

            switch (msg.action) {
                case ACTION_TOGGLE_DETECTION -> AEQuestSettings.toggleDetection(server, uuid);
                case ACTION_TOGGLE_REWARD -> AEQuestSettings.toggleReward(server, uuid);
                case ACTION_INTERVAL_PLUS -> AEQuestSettings.stepInterval(server, uuid, AEQuestSettings.INTERVAL_STEP);
                case ACTION_INTERVAL_MINUS -> AEQuestSettings.stepInterval(server, uuid, -AEQuestSettings.INTERVAL_STEP);
                case ACTION_QUERY -> {

                }
                default -> {
                    return;
                }
            }

            GTLSupbNetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    GTLSupbNetworkHandler.buildAEQuestSettingsPacket(player));
        });
        ctx.get().setPacketHandled(true);
    }
}
