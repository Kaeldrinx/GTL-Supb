package com.gtlsupb.gtlsupb.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.common.machine.generator.VoidEnergyAbsorberMachine;

import java.util.function.Supplier;

public class C2SVoidEnergyAbsorberTogglePacket {

    public static final int TOGGLE_BROADCAST = 0;
    public static final int TOGGLE_GRID_PULL = 1;

    private final BlockPos pos;
    private final int toggle;
    private final boolean value;

    public C2SVoidEnergyAbsorberTogglePacket(BlockPos pos, int toggle, boolean value) {
        this.pos = pos;
        this.toggle = toggle;
        this.value = value;
    }

    public static void encode(C2SVoidEnergyAbsorberTogglePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.toggle);
        buf.writeBoolean(msg.value);
    }

    public static C2SVoidEnergyAbsorberTogglePacket decode(FriendlyByteBuf buf) {
        return new C2SVoidEnergyAbsorberTogglePacket(buf.readBlockPos(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(C2SVoidEnergyAbsorberTogglePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.pos == null) return;
            if (!(player.level() instanceof ServerLevel level)) return;

            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;

            if (level.getBlockEntity(msg.pos) instanceof com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity be && be.getMetaMachine() instanceof VoidEnergyAbsorberMachine machine) {
                machine.applyToggleFromClient(msg.toggle, msg.value);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
