package com.gtlsupb.gtlsupb.network;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitIntegratedHatchPartMachine;

import java.util.function.Supplier;

public class C2SIntegratedHatchPacket {

    public static final int ACTION_TOGGLE_CIRCUIT = 0;
    public static final int ACTION_SET_ALL_CIRCUITS = 1;
    public static final int ACTION_CLEAR_CIRCUITS = 2;
    public static final int ACTION_SET_OUTPUT_FREQUENCY = 3;
    public static final int ACTION_SET_TAG_REFRESH = 4;
    public static final int ACTION_SET_STOCK_PAGES = 5;
    public static final int ACTION_SET_TAG_PAGES = 6;
    public static final int ACTION_SET_ITEM_TAG_PULL = 7;
    public static final int ACTION_SET_FLUID_TAG_PULL = 8;

    private final BlockPos pos;
    private final int action;
    private final int value;

    public C2SIntegratedHatchPacket(BlockPos pos, int action, int value) {
        this.pos = pos;
        this.action = action;
        this.value = value;
    }

    public static void encode(C2SIntegratedHatchPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.action);
        buf.writeVarInt(msg.value);
    }

    public static C2SIntegratedHatchPacket decode(FriendlyByteBuf buf) {
        return new C2SIntegratedHatchPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(C2SIntegratedHatchPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.pos == null) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;
            if (!(level.getBlockEntity(msg.pos) instanceof MetaMachineBlockEntity be)) return;
            if (!(be.getMetaMachine() instanceof OverlimitIntegratedHatchPartMachine part)) return;

            switch (msg.action) {
                case ACTION_TOGGLE_CIRCUIT -> part.gtlsupb$toggleCircuitServer(msg.value);
                case ACTION_SET_ALL_CIRCUITS -> part.gtlsupb$setAllCircuitsServer(true);
                case ACTION_CLEAR_CIRCUITS -> part.gtlsupb$setAllCircuitsServer(false);
                case ACTION_SET_OUTPUT_FREQUENCY -> part.gtlsupb$setOutputFrequency(msg.value);
                case ACTION_SET_TAG_REFRESH -> part.gtlsupb$setTagRefreshTicks(msg.value);
                case ACTION_SET_STOCK_PAGES -> part.gtlsupb$setStockPages(msg.value);
                case ACTION_SET_TAG_PAGES -> part.gtlsupb$setTagPages(msg.value);
                case ACTION_SET_ITEM_TAG_PULL -> part.gtlsupb$setItemTagPull(msg.value != 0);
                case ACTION_SET_FLUID_TAG_PULL -> part.gtlsupb$setFluidTagPull(msg.value != 0);
                default -> {}
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
