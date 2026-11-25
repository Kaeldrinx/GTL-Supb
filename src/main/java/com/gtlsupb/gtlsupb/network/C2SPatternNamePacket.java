package com.gtlsupb.gtlsupb.network;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.common.machine.multiblock.part.PatternAssemblyPartMachine;

import java.util.function.Supplier;

public class C2SPatternNamePacket {

    private final BlockPos pos;

    private final boolean allModes;

    public C2SPatternNamePacket(BlockPos pos, boolean allModes) {
        this.pos = pos;
        this.allModes = allModes;
    }

    public static void encode(C2SPatternNamePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.allModes);
    }

    public static C2SPatternNamePacket decode(FriendlyByteBuf buf) {
        return new C2SPatternNamePacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(C2SPatternNamePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.pos == null) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;
            if (!(level.getBlockEntity(msg.pos) instanceof MetaMachineBlockEntity be)) return;
            if (!(be.getMetaMachine() instanceof PatternAssemblyPartMachine part)) return;
            if (msg.allModes) {
                part.gtlsupb$nameByAllModes();
            } else {
                part.gtlsupb$nameByActiveMode();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
