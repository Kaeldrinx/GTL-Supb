package com.gtlsupb.gtlsupb.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StructureExporterItem extends Item {

    private static final String TAG_POS1 = "gtlsupb_pos1";
    private static final String TAG_POS2 = "gtlsupb_pos2";

    public StructureExporterItem(Properties properties) {
        super(properties);
    }

    @Override
    @Nonnull
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (player.isSpectator()) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        InteractionHand hand = context.getHand();

        if (player.isShiftKeyDown()) {

            setPos(stack, TAG_POS2, pos);
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("gtlsupb.structure_exporter.pos2_set",
                        pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.AQUA));
                checkBothPositions(player, stack);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {

            setPos(stack, TAG_POS1, pos);
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("gtlsupb.structure_exporter.pos1_set",
                        pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.AQUA));
                checkBothPositions(player, stack);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        BlockPos pos1 = getPos(stack, TAG_POS1);
        BlockPos pos2 = getPos(stack, TAG_POS2);

        if (pos1 == null || pos2 == null) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("gtlsupb.structure_exporter.need_positions")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {

            com.gtlsupb.gtlsupb.common.tool.StructureCodeGenerator.ExportResult result = com.gtlsupb.gtlsupb.common.tool.StructureCodeGenerator.export(level, pos1, pos2,
                    serverPlayer.getGameProfile().getName());

            for (Component msg : result.messages) {
                serverPlayer.sendSystemMessage(msg);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level,
                                @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        tooltip.add(Component.translatable("gtlsupb.structure_exporter.tooltip.0")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gtlsupb.structure_exporter.tooltip.1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gtlsupb.structure_exporter.tooltip.2")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gtlsupb.structure_exporter.tooltip.3")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gtlsupb.structure_exporter.tooltip.4")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gtlsupb.structure_exporter.tooltip.5")
                .withStyle(ChatFormatting.GRAY));

        BlockPos pos1 = getPos(stack, TAG_POS1);
        BlockPos pos2 = getPos(stack, TAG_POS2);

        if (pos1 != null) {
            tooltip.add(Component.translatable("gtlsupb.structure_exporter.pos1_display",
                    pos1.getX(), pos1.getY(), pos1.getZ()).withStyle(ChatFormatting.AQUA));
        }
        if (pos2 != null) {
            tooltip.add(Component.translatable("gtlsupb.structure_exporter.pos2_display",
                    pos2.getX(), pos2.getY(), pos2.getZ()).withStyle(ChatFormatting.AQUA));
        }
    }

    private static void setPos(ItemStack stack, String key, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putIntArray(key, new int[] { pos.getX(), pos.getY(), pos.getZ() });
    }

    @Nullable
    private static BlockPos getPos(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(key)) return null;
        int[] arr = tag.getIntArray(key);
        if (arr.length != 3) return null;
        return new BlockPos(arr[0], arr[1], arr[2]);
    }

    private void checkBothPositions(Player player, ItemStack stack) {
        BlockPos pos1 = getPos(stack, TAG_POS1);
        BlockPos pos2 = getPos(stack, TAG_POS2);
        if (pos1 != null && pos2 != null) {
            int sx = Math.abs(pos1.getX() - pos2.getX()) + 1;
            int sy = Math.abs(pos1.getY() - pos2.getY()) + 1;
            int sz = Math.abs(pos1.getZ() - pos2.getZ()) + 1;
            player.sendSystemMessage(Component.translatable("gtlsupb.structure_exporter.both_set",
                    sx, sy, sz).withStyle(ChatFormatting.GREEN));
            player.sendSystemMessage(Component.translatable("gtlsupb.structure_exporter.ready_to_export")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }
}
