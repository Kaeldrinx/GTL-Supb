package com.gtlsupb.gtlsupb.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.common.machine.multiblock.part.PatternAssemblyPartMachine;

import java.util.function.Supplier;

public class C2SPatternAssemblyConfigPacket {

    public static final int ACTION_SET_PACK_MODE = 0;
    public static final int ACTION_DETECT_MULTIPLIER = 1;

    private final BlockPos pos;
    private final int action;
    private final String value;

    public C2SPatternAssemblyConfigPacket(BlockPos pos, int action, String value) {
        this.pos = pos;
        this.action = action;
        this.value = value == null ? "" : value;
    }

    public static void encode(C2SPatternAssemblyConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.action);
        buf.writeUtf(msg.value, 32767);
    }

    public static C2SPatternAssemblyConfigPacket decode(FriendlyByteBuf buf) {
        return new C2SPatternAssemblyConfigPacket(buf.readBlockPos(), buf.readVarInt(), buf.readUtf(32767));
    }

    public static void handle(C2SPatternAssemblyConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.pos == null) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;
            if (!(level.getBlockEntity(msg.pos) instanceof com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity be)) return;
            if (!(be.getMetaMachine() instanceof PatternAssemblyPartMachine part)) return;

            if (msg.action == ACTION_SET_PACK_MODE) {
                part.gtlsupb$setPackRecipeType(msg.value);
            } else if (msg.action == ACTION_DETECT_MULTIPLIER) {
                part.gtlsupb$detectHostMultiplierOnce();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
