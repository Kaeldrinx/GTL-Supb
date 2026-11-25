package com.gtlsupb.gtlsupb.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.common.machine.multiblock.module.UniversalOrderModuleBase;

import java.util.function.Supplier;

public class C2SModuleModeMaskPacket {

    private final BlockPos pos;
    private final long mask;

    public C2SModuleModeMaskPacket(BlockPos pos, long mask) {
        this.pos = pos;
        this.mask = mask;
    }

    public static void encode(C2SModuleModeMaskPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeLong(msg.mask);
    }

    public static C2SModuleModeMaskPacket decode(FriendlyByteBuf buf) {
        return new C2SModuleModeMaskPacket(buf.readBlockPos(), buf.readLong());
    }

    public static void handle(C2SModuleModeMaskPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.pos == null) return;
            if (!(player.level() instanceof ServerLevel level)) return;

            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;
            if (level.getBlockEntity(msg.pos) instanceof com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity be && be.getMetaMachine() instanceof UniversalOrderModuleBase module) {
                module.applyModeMaskFromClient(msg.mask);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
