package com.gtlsupb.gtlsupb.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.common.data.GTLSupbItems;
import com.gtlsupb.gtlsupb.common.item.OverlimitQuantumEntanglementTool;

import java.util.function.Supplier;

public class C2SQuantumTeleportPacket {

    private final InteractionHand hand;
    private final ResourceLocation dimension;
    private final BlockPos pos;

    public C2SQuantumTeleportPacket(InteractionHand hand, ResourceLocation dimension, BlockPos pos) {
        this.hand = hand;
        this.dimension = dimension;
        this.pos = pos;
    }

    public static void encode(C2SQuantumTeleportPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.hand);
        buf.writeResourceLocation(msg.dimension);
        buf.writeBlockPos(msg.pos);
    }

    public static C2SQuantumTeleportPacket decode(FriendlyByteBuf buf) {
        return new C2SQuantumTeleportPacket(buf.readEnum(InteractionHand.class), buf.readResourceLocation(),
                buf.readBlockPos());
    }

    public static void handle(C2SQuantumTeleportPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ItemStack stack = player.getItemInHand(msg.hand);
            if (stack.isEmpty() || stack.getItem() != GTLSupbItems.QUANTUM_ENTANGLEMENT_TOOL.get()) return;
            ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, msg.dimension);
            GlobalPos target = GlobalPos.of(targetDimension, msg.pos);
            if (!OverlimitQuantumEntanglementTool.readFilters(stack).contains(target)) return;
            ServerLevel level = player.getServer() == null ? null : player.getServer().getLevel(targetDimension);
            BlockPos destination = msg.pos;
            if (level == null || !level.hasChunk(destination.getX() >> 4, destination.getZ() >> 4)) return;
            player.teleportTo(level, destination.getX() + 0.5D, destination.getY() + 1.0D,
                    destination.getZ() + 0.5D, player.getYRot(), player.getXRot());
        });
        ctx.get().setPacketHandled(true);
    }
}
