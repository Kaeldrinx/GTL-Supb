package com.gtlsupb.gtlsupb.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.integration.ftbquests.AEQuestSubmitHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2CRewardListPacket {

    private final List<AEQuestSubmitHelper.RewardEntry> entries;

    public S2CRewardListPacket(List<AEQuestSubmitHelper.RewardEntry> entries) {
        this.entries = entries;
    }

    public static void encode(S2CRewardListPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entries.size());
        for (AEQuestSubmitHelper.RewardEntry e : msg.entries) {
            buf.writeItem(e.getStack());
            buf.writeVarLong(e.getCount());
        }
    }

    public static S2CRewardListPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<AEQuestSubmitHelper.RewardEntry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ItemStack stack = buf.readItem();
            long count = buf.readVarLong();
            entries.add(new AEQuestSubmitHelper.RewardEntry(stack, count));
        }
        return new S2CRewardListPacket(entries);
    }

    public static void handle(S2CRewardListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> openScreen(msg));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen(S2CRewardListPacket msg) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();

        mc.setScreen(new com.gtlsupb.gtlsupb.client.ftbquests.RewardListScreen(msg.entries));
    }
}
