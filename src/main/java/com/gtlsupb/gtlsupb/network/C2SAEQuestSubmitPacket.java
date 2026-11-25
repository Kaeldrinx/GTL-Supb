package com.gtlsupb.gtlsupb.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.integration.ftbquests.AEQuestSubmitHelper;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;

import java.util.function.Supplier;

public class C2SAEQuestSubmitPacket {

    private final long questId;
    private final int count;

    public C2SAEQuestSubmitPacket(long questId, int count) {
        this.questId = questId;
        this.count = count;
    }

    public static void encode(C2SAEQuestSubmitPacket msg, FriendlyByteBuf buf) {
        buf.writeVarLong(msg.questId);
        buf.writeVarInt(Math.max(1, msg.count));
    }

    public static C2SAEQuestSubmitPacket decode(FriendlyByteBuf buf) {
        return new C2SAEQuestSubmitPacket(buf.readVarLong(), buf.readVarInt());
    }

    public static void handle(C2SAEQuestSubmitPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.count <= 0) return;
            var questFile = ServerQuestFile.INSTANCE;
            if (questFile == null) return;

            TeamData teamData = TeamData.get(player);
            if (teamData == null) return;

            long totalSubmitted = 0;

            for (Task task : questFile.getAllTasks()) {
                if (task == null) continue;
                if (task.getQuest() == null || task.getQuest().id != msg.questId) continue;
                if (!(task instanceof ItemTask) && !(task instanceof FluidTask)) continue;
                if (!task.consumesResources()) continue;
                if (!task.getQuest().canBeRepeated()) continue;

                totalSubmitted += AEQuestSubmitHelper.submitCycleLoop(teamData, task, player, msg.count);
            }

            if (totalSubmitted > 0) {

                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("gtlsupb.ae_quest.submitted", totalSubmitted),
                        true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
