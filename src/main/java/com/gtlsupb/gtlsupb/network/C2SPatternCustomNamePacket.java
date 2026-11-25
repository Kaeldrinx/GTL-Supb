package com.gtlsupb.gtlsupb.network;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.common.machine.multiblock.part.PatternAssemblyPartMachine;

import java.util.function.Supplier;

public class C2SPatternCustomNamePacket {

    private final BlockPos pos;
    private final String name;

    public C2SPatternCustomNamePacket(BlockPos pos, String name) {
        this.pos = pos;
        this.name = name == null ? "" : name;
    }

    public static void encode(C2SPatternCustomNamePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.name, 32767);
    }

    public static C2SPatternCustomNamePacket decode(FriendlyByteBuf buf) {
        return new C2SPatternCustomNamePacket(buf.readBlockPos(), buf.readUtf(32767));
    }

    public static void handle(C2SPatternCustomNamePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.pos == null) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;
            if (!(level.getBlockEntity(msg.pos) instanceof MetaMachineBlockEntity be)) return;
            if (!(be.getMetaMachine() instanceof PatternAssemblyPartMachine part)) return;
            part.gtlsupb$setCustomNameServer(msg.name);
        });
        ctx.get().setPacketHandled(true);
    }
}
