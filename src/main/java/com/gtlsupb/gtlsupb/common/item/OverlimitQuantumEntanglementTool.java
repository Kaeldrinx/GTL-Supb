package com.gtlsupb.gtlsupb.common.item;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitQuantumEntanglementHatchPartMachine;
import com.gtlsupb.gtlsupb.integration.ae2.OverlimitQuantumEntanglementWirelessManager;
import com.gtlsupb.gtlsupb.network.C2SQuantumFilterPacket;
import com.gtlsupb.gtlsupb.network.C2SQuantumTeleportPacket;
import com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OverlimitQuantumEntanglementTool implements IItemUIFactory {

    public static final OverlimitQuantumEntanglementTool INSTANCE = new OverlimitQuantumEntanglementTool();

    private static final String NBT_BOUND = "gqe_tool";
    private static final String NBT_FILTERS = "gqe_filters";

    private OverlimitQuantumEntanglementTool() {}

    public static GlobalPos readBound(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_BOUND)) return null;
        return readPos(tag.getCompound(NBT_BOUND));
    }

    private static void writeBound(ItemStack stack, GlobalPos gpos) {
        CompoundTag tag = stack.getOrCreateTag();
        if (gpos == null) {
            tag.remove(NBT_BOUND);
            tag.remove(NBT_FILTERS);
            return;
        }
        tag.put(NBT_BOUND, writePos(gpos));
    }

    public static List<GlobalPos> readFilters(ItemStack stack) {
        List<GlobalPos> result = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_FILTERS)) return result;
        ListTag list = tag.getList(NBT_FILTERS, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            GlobalPos pos = readPos(list.getCompound(i));
            if (pos != null) result.add(pos);
        }
        return result;
    }

    private static void writeFilters(ItemStack stack, List<GlobalPos> filters) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = new ListTag();
        for (GlobalPos pos : filters) {
            list.add(writePos(pos));
        }
        tag.put(NBT_FILTERS, list);
    }

    public static boolean removeFilterEntry(ItemStack stack, GlobalPos target) {
        List<GlobalPos> filters = readFilters(stack);
        if (!filters.remove(target)) return false;
        writeFilters(stack, filters);
        return true;
    }

    public static boolean removeFilter(ItemStack stack, GlobalPos target) {
        GlobalPos bound = readBound(stack);
        if (bound != null) {
            OverlimitQuantumEntanglementWirelessManager.unblacklist(bound, target);
        }
        return removeFilterEntry(stack, target);
    }

    private static GlobalPos readPos(CompoundTag tag) {
        if (tag == null) return null;
        String dim = tag.getString("dim");
        if (dim.isEmpty()) return null;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dim));
        return GlobalPos.of(key, new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
    }

    private static CompoundTag writePos(GlobalPos gpos) {
        CompoundTag tag = new CompoundTag();
        tag.putString("dim", gpos.dimension().location().toString());
        tag.putInt("x", gpos.pos().getX());
        tag.putInt("y", gpos.pos().getY());
        tag.putInt("z", gpos.pos().getZ());
        return tag;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;

        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity tile = level.getBlockEntity(pos);

        if (isQuantumEntanglementHatch(tile)) {
            handleBind(serverPlayer, stack, level, pos);
            return InteractionResult.SUCCESS;
        }

        if (isAEGridNodeHost(level, pos)) {
            handleToggleFilter(serverPlayer, stack, level, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void handleBind(ServerPlayer player, ItemStack stack, Level level, BlockPos pos) {
        GlobalPos target = GlobalPos.of(level.dimension(), pos);
        GlobalPos bound = readBound(stack);
        if (bound != null && bound.equals(target)) {
            writeBound(stack, null);
            player.displayClientMessage(Component.translatable("gtlsupb.qe_tool.unbound",
                    pos.getX(), pos.getY(), pos.getZ()), true);
        } else {
            writeBound(stack, target);
            player.displayClientMessage(Component.translatable("gtlsupb.qe_tool.bound",
                    pos.getX(), pos.getY(), pos.getZ()), true);
        }
    }

    private void handleToggleFilter(ServerPlayer player, ItemStack stack, Level level, BlockPos pos) {
        GlobalPos bound = readBound(stack);
        if (bound == null) {
            player.displayClientMessage(Component.translatable("gtlsupb.qe_tool.not_bound"), true);
            return;
        }
        GlobalPos target = GlobalPos.of(level.dimension(), pos);
        boolean blacklisted = OverlimitQuantumEntanglementWirelessManager.isBlacklisted(bound, target);
        List<GlobalPos> filters = readFilters(stack);
        if (blacklisted || filters.contains(target)) {
            OverlimitQuantumEntanglementWirelessManager.unblacklist(bound, target);
            filters.remove(target);
            writeFilters(stack, filters);
            player.displayClientMessage(Component.translatable("gtlsupb.qe_tool.unfiltered",
                    pos.getX(), pos.getY(), pos.getZ()), true);
        } else {
            OverlimitQuantumEntanglementWirelessManager.blacklist(bound, target);
            filters.add(target);
            writeFilters(stack, filters);
            player.displayClientMessage(Component.translatable("gtlsupb.qe_tool.filtered",
                    pos.getX(), pos.getY(), pos.getZ()), true);
        }
    }

    private boolean isQuantumEntanglementHatch(BlockEntity tile) {
        return tile instanceof MetaMachineBlockEntity metaBean &&
                metaBean.getMetaMachine() instanceof OverlimitQuantumEntanglementHatchPartMachine;
    }

    private boolean isAEGridNodeHost(Level level, BlockPos pos) {
        try {
            return appeng.api.networking.GridHelper.getNodeHost(level, pos) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        ItemStack stack = holder.getHeld();
        GlobalPos bound = readBound(stack);
        InteractionHand hand = holder.getHand();

        int width = 250;
        int height = 180;
        WidgetGroup root = new WidgetGroup(0, 0, width, height);
        root.addWidget(new LabelWidget(8, 6, Component.translatable("gtlsupb.qe_tool.title").getString()));

        if (bound == null) {
            root.addWidget(new LabelWidget(8, 22, Component.translatable("gtlsupb.qe_tool.info_unbound").getString()));
        } else {
            root.addWidget(new LabelWidget(8, 22, Component.translatable("gtlsupb.qe_tool.info_bound",
                    bound.dimension().location().toString(),
                    bound.pos().getX(), bound.pos().getY(), bound.pos().getZ()).getString()));
        }
        root.addWidget(new LabelWidget(8, 40, Component.translatable("gtlsupb.qe_tool.hint").getString()));

        List<GlobalPos> filters = readFilters(stack);
        filters.sort(Comparator.comparingInt((GlobalPos g) -> g.pos().getY())
                .thenComparingInt(g -> g.pos().getX()));

        DraggableScrollableWidgetGroup list = new DraggableScrollableWidgetGroup(8, 58, width - 40, height - 66);
        list.setYScrollBarWidth(2);
        list.setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F));
        list.setBackground(GuiTextures.DISPLAY);

        if (filters.isEmpty()) {
            list.addWidget(new LabelWidget(4, 4, Component.translatable("gtlsupb.qe_tool.empty").getString()));
        } else {
            int y = 0;
            for (GlobalPos target : filters) {
                WidgetGroup entry = new WidgetGroup(4, 4 + y, 190, 18);
                entry.addWidget(new LabelWidget(0, 2, Component.translatable("gtlsupb.qe_tool.entry_block",
                        Component.literal(resolveBlockName(player, target)),
                        target.pos().getX(), target.pos().getY(), target.pos().getZ()).getString()));

                ButtonWidget teleportBtn = new ButtonWidget(154, 0, 16, 16, clickData -> {
                    GTLSupbNetworkHandler.CHANNEL.sendToServer(
                            new C2SQuantumTeleportPacket(hand, target.dimension().location(), target.pos()));
                });
                teleportBtn.setButtonTexture(GuiTextures.BUTTON, new TextTexture("→"));
                teleportBtn.setHoverTooltips(Component.translatable("gtlsupb.qe_tool.teleport_hint"));
                entry.addWidget(teleportBtn);

                ButtonWidget deleteBtn = new ButtonWidget(174, 0, 16, 16, clickData -> {
                    if (player.level().isClientSide) {
                        list.removeWidget(entry);
                        removeFilterEntry(stack, target);
                        GTLSupbNetworkHandler.CHANNEL.sendToServer(
                                new C2SQuantumFilterPacket(hand, target.dimension().location(), target.pos()));
                    }
                });
                deleteBtn.setButtonTexture(GuiTextures.BUTTON, new TextTexture("✕"));
                deleteBtn.setHoverBorderTexture(1, 0xFFCC0000);
                deleteBtn.setHoverTooltips(Component.translatable("gtlsupb.qe_tool.delete_hint"));
                entry.addWidget(deleteBtn);

                list.addWidget(entry);
                y += 18;
            }
        }
        root.addWidget(list);

        return new ModularUI(width, height, holder, player)
                .widget(root)
                .background(GuiTextures.BACKGROUND);
    }

    private static String resolveBlockName(Player player, GlobalPos target) {
        try {
            Level level = player.level();
            if (level.dimension().equals(target.dimension()) &&
                    level.hasChunk(target.pos().getX() >> 4, target.pos().getZ() >> 4)) {
                return level.getBlockState(target.pos()).getBlock().getName().getString();
            }
        } catch (Throwable ignored) {

        }
        return "??";
    }
}
