package com.gtlsupb.gtlsupb.network;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitIntegratedHatchPartMachine;

import java.util.function.Supplier;

public class C2SIntegratedTagFilterPacket {

    private final BlockPos pos;
    private final boolean item;
    private final boolean whitelist;
    private final String value;

    public C2SIntegratedTagFilterPacket(BlockPos pos, boolean item, boolean whitelist, String value) {
        this.pos = pos;
        this.item = item;
        this.whitelist = whitelist;
        this.value = value == null ? "" : value;
    }

    public static void encode(C2SIntegratedTagFilterPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.item);
        buf.writeBoolean(msg.whitelist);
        buf.writeUtf(msg.value, 32767);
    }

    public static C2SIntegratedTagFilterPacket decode(FriendlyByteBuf buf) {
        return new C2SIntegratedTagFilterPacket(buf.readBlockPos(), buf.readBoolean(), buf.readBoolean(),
                buf.readUtf(32767));
    }

    public static void handle(C2SIntegratedTagFilterPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.pos == null) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;
            if (!(level.getBlockEntity(msg.pos) instanceof MetaMachineBlockEntity be)) return;
            if (!(be.getMetaMachine() instanceof OverlimitIntegratedHatchPartMachine part)) return;
            part.gtlsupb$setTagFilter(msg.item, msg.whitelist, msg.value);
        });
        ctx.get().setPacketHandled(true);
    }
}
