package com.gtlsupb.gtlsupb.network;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SCosmicSiphonerOverclockPacket {

    private final BlockPos pos;
    private final boolean enabled;

    public C2SCosmicSiphonerOverclockPacket(BlockPos pos, boolean enabled) {
        this.pos = pos;
        this.enabled = enabled;
    }

    public static void encode(C2SCosmicSiphonerOverclockPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.enabled);
    }

    public static C2SCosmicSiphonerOverclockPacket decode(FriendlyByteBuf buf) {
        return new C2SCosmicSiphonerOverclockPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(C2SCosmicSiphonerOverclockPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.pos == null) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;
            if (!(level.getBlockEntity(msg.pos) instanceof MetaMachineBlockEntity be)) return;
            if (!(be.getMetaMachine() instanceof com.gtlsupb.gtlsupb.common.machine.multiblock.CosmicMatterSiphonerMachine machine)) return;
            machine.gtlsupb$setOverclockEnabled(msg.enabled);
        });
        ctx.get().setPacketHandled(true);
    }
}
