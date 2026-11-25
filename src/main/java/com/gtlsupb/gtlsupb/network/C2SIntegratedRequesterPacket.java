package com.gtlsupb.gtlsupb.network;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitIntegratedHatchPartMachine;

import java.util.function.Supplier;

public class C2SIntegratedRequesterPacket {

    public static final int ACTION_TOGGLE_AUTO = 0;
    public static final int ACTION_SET_FREQUENCY = 1;
    public static final int ACTION_SET_PER_PULL = 2;
    public static final int ACTION_ADD_COUNT = 3;
    public static final int ACTION_SET_COUNT = 4;
    public static final int ACTION_RUN_ONCE = 5;
    public static final int ACTION_TOGGLE_OUTPUT_ME = 6;
    public static final int ACTION_RETURN_CACHE = 7;

    private final BlockPos pos;
    private final int action;
    private final long value;

    public C2SIntegratedRequesterPacket(BlockPos pos, int action, long value) {
        this.pos = pos;
        this.action = action;
        this.value = value;
    }

    public static void encode(C2SIntegratedRequesterPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.action);
        buf.writeVarLong(msg.value);
    }

    public static C2SIntegratedRequesterPacket decode(FriendlyByteBuf buf) {
        return new C2SIntegratedRequesterPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarLong());
    }

    public static void handle(C2SIntegratedRequesterPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.pos == null) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;
            if (!(level.getBlockEntity(msg.pos) instanceof MetaMachineBlockEntity be)) return;
            if (!(be.getMetaMachine() instanceof OverlimitIntegratedHatchPartMachine part)) return;
            part.gtlsupb$handleRequester(msg.action, msg.value);
        });
        ctx.get().setPacketHandled(true);
    }
}
