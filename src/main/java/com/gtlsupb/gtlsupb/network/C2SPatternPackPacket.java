package com.gtlsupb.gtlsupb.network;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import com.gtlsupb.gtlsupb.common.data.GTLSupbItems;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.PatternAssemblyPartMachine;

import java.util.function.Supplier;

public class C2SPatternPackPacket {

    private final BlockPos pos;
    private final String recipeType;

    public C2SPatternPackPacket(BlockPos pos) {
        this(pos, "");
    }

    public C2SPatternPackPacket(BlockPos pos, String recipeType) {
        this.pos = pos;
        this.recipeType = recipeType == null ? "" : recipeType;
    }

    public static void encode(C2SPatternPackPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.recipeType, 32767);
    }

    public static C2SPatternPackPacket decode(FriendlyByteBuf buf) {
        return new C2SPatternPackPacket(buf.readBlockPos(), buf.readUtf(32767));
    }

    public static void handle(C2SPatternPackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.pos == null) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;
            if (!(level.getBlockEntity(msg.pos) instanceof com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity be)) {
                return;
            }
            if (!(be.getMetaMachine() instanceof PatternAssemblyPartMachine part)) return;

            if (!gtlsupb$hasPattern(part, msg.recipeType)) {
                player.displayClientMessage(Component.translatable("gtlsupb.pattern_assembly.pack.empty"), true);
                return;
            }

            ItemStack toolStack = new ItemStack(GTLSupbItems.PATTERN_CUT_TOOL.get());
            CompoundTag behaviours = toolStack.getOrCreateTagElement(ToolHelper.BEHAVIOURS_TAG_KEY);

            part.gtlsupb$cutToTagFiltered(behaviours, msg.recipeType);
            if (!behaviours.contains("cut")) {
                player.displayClientMessage(Component.translatable("gtlsupb.pattern_assembly.pack.empty"), true);
                return;
            }

            ItemStack moving = toolStack.copy();
            player.getInventory().add(moving);
            if (!moving.isEmpty()) {
                player.drop(moving, false);
            }
            player.containerMenu.broadcastChanges();
            player.displayClientMessage(Component.translatable("gtlsupb.pattern_assembly.pack.done"), true);
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean gtlsupb$hasPattern(PatternAssemblyPartMachine part, String recipeType) {
        var inventory = part.getPatternInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && matches(stack, recipeType)) return true;
        }
        return false;
    }

    private static boolean matches(ItemStack stack, String recipeType) {
        if (recipeType == null || recipeType.isEmpty()) return true;
        if (stack.getTag() == null) return false;
        ListTag types = stack.getTag().getCompound("gtlcore").getList("patternQuickUploadRecipeTypes", Tag.TAG_STRING);
        for (int i = 0; i < types.size(); i++) {
            if (recipeType.equals(types.getString(i))) return true;
        }
        return false;
    }
}
