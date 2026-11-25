package com.gtlsupb.gtlsupb.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CMachineUiPacket {

    private final BlockPos pos;
    private final CompoundTag payload;

    public S2CMachineUiPacket(BlockPos pos, CompoundTag payload) {
        this.pos = pos;
        this.payload = payload == null ? new CompoundTag() : payload;
    }

    public static void encode(S2CMachineUiPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeNbt(msg.payload);
    }

    public static S2CMachineUiPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        CompoundTag tag = buf.readNbt();
        return new S2CMachineUiPacket(pos, tag);
    }

    public static void handle(S2CMachineUiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.gtlsupb.gtlsupb.client.screen.MachineUiScreen.onSnapshot(msg.pos, msg.payload)));
        ctx.get().setPacketHandled(true);
    }

    public static final String KEY_TITLE = "t";

    public static final String KEY_ITEMS = "i";

    public static final String KEY_FLAGS = "f";

    public static final String KEY_INFO = "n";

    public static final String KEY_ROWS = "r";

    public static ListTag writeItems(java.util.List<net.minecraft.world.item.ItemStack> stacks) {
        ListTag list = new ListTag();
        for (net.minecraft.world.item.ItemStack stack : stacks) {
            list.add(stack.isEmpty() ? new CompoundTag() : stack.save(new CompoundTag()));
        }
        return list;
    }

    public static net.minecraft.world.item.ItemStack readItem(ListTag list, int index) {
        if (list == null || index < 0 || index >= list.size()) return net.minecraft.world.item.ItemStack.EMPTY;
        CompoundTag tag = list.getCompound(index);
        if (tag.isEmpty()) return net.minecraft.world.item.ItemStack.EMPTY;
        return net.minecraft.world.item.ItemStack.of(tag);
    }
}
