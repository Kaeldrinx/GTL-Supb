package com.gtlsupb.gtlsupb.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.stacks.AEKeyType;
import appeng.items.storage.StorageTier;
import appeng.items.tools.powered.PortableCellItem;
import appeng.menu.me.common.MEStorageMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SuperTerminalCellItem extends PortableCellItem {

    public static final String TAG_ITEMS = "GtlsupbCellItems";
    public static final String TAG_FLUIDS = "GtlsupbCellFluids";
    public static final String TAG_COLLECT = "GtlsupbCellCollect";

    public static final int MAX_ITEM_STACKS = 128;

    public static final long MAX_FLUID_MB = 1_000_000L;

    private static final double POWER_BUFFER = 1_000_000_000D;

    private static final StorageTier TIER = new StorageTier(100, "super", Integer.MAX_VALUE, 100,
            () -> net.minecraft.world.item.Items.NETHERITE_INGOT);

    private static final String WIRELESS_TERMINAL_CLASS = "appeng.items.tools.powered.WirelessTerminalItem";

    public SuperTerminalCellItem(Properties properties) {
        super(AEKeyType.items(), 64, MEStorageMenu.PORTABLE_ITEM_CELL_TYPE, TIER, properties.stacksTo(1), 0xDDDDDD);
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return TIER.bytes();
    }

    @Override
    public double getAEMaxPower(ItemStack stack) {
        return POWER_BUFFER;
    }

    @Override
    public double getAECurrentPower(ItemStack stack) {
        return POWER_BUFFER;
    }

    @Override
    public double extractAEPower(ItemStack stack, double amount, appeng.api.config.Actionable mode) {
        return amount;
    }

    @Override
    public double injectAEPower(ItemStack stack, double amount, appeng.api.config.Actionable mode) {
        return 0D;
    }

    public static boolean isCollectEnabled(ItemStack cell) {
        CompoundTag tag = cell.getTag();
        return tag == null || !tag.contains(TAG_COLLECT) || tag.getBoolean(TAG_COLLECT);
    }

    public static void setCollectEnabled(ItemStack cell, boolean enabled) {
        cell.getOrCreateTag().putBoolean(TAG_COLLECT, enabled);
    }

    public static List<ItemStack> insertItems(ItemStack cell, List<ItemStack> stacks) {
        List<ItemStack> leftover = new ArrayList<>();
        if (stacks == null || stacks.isEmpty()) return leftover;

        try {
            var cellInventory = appeng.api.storage.StorageCells.getCellInventory(cell, null);
            if (cellInventory != null) {
                boolean anyMoved = false;
                for (ItemStack incoming : stacks) {
                    if (incoming == null || incoming.isEmpty()) continue;
                    var key = appeng.api.stacks.AEItemKey.of(incoming);
                    long moved = key == null ? 0L : cellInventory.insert(key, incoming.getCount(),
                            appeng.api.config.Actionable.MODULATE, null);
                    if (moved > 0L) anyMoved = true;
                    if (moved < incoming.getCount()) {
                        leftover.add(incoming.copyWithCount((int) (incoming.getCount() - moved)));
                    }
                }

                if (anyMoved) return leftover;
                leftover.clear();
            }
        } catch (Throwable ignored) {

        }
        ListTag list = list(cell, TAG_ITEMS);
        for (ItemStack incoming : stacks) {
            if (incoming == null || incoming.isEmpty()) continue;
            ItemStack remain = incoming.copy();
            for (int i = 0; i < list.size() && !remain.isEmpty(); i++) {
                ItemStack stored = ItemStack.of(list.getCompound(i));
                if (stored.isEmpty() || !ItemStack.isSameItemSameTags(stored, remain)) continue;
                int room = stored.getMaxStackSize() - stored.getCount();
                if (room <= 0) continue;
                int move = Math.min(room, remain.getCount());
                stored.grow(move);
                remain.shrink(move);
                list.set(i, stored.save(new CompoundTag()));
            }
            while (!remain.isEmpty() && list.size() < MAX_ITEM_STACKS) {
                int move = Math.min(remain.getMaxStackSize(), remain.getCount());
                list.add(remain.copyWithCount(move).save(new CompoundTag()));
                remain.shrink(move);
            }
            if (!remain.isEmpty()) leftover.add(remain);
        }
        cell.getOrCreateTag().put(TAG_ITEMS, list);
        return leftover;
    }

    public static FluidStack insertFluid(ItemStack cell, FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) return FluidStack.EMPTY;
        ListTag list = list(cell, TAG_FLUIDS);
        FluidStack remain = fluid.copy();
        for (int i = 0; i < list.size() && !remain.isEmpty(); i++) {
            FluidStack stored = FluidStack.loadFluidStackFromNBT(list.getCompound(i));
            if (stored.isEmpty() || !stored.isFluidEqual(remain)) continue;
            long room = MAX_FLUID_MB - stored.getAmount();
            if (room <= 0L) continue;
            int move = (int) Math.min(room, remain.getAmount());
            stored.grow(move);
            remain.shrink(move);
            list.set(i, stored.writeToNBT(new CompoundTag()));
        }
        if (!remain.isEmpty()) list.add(remain.writeToNBT(new CompoundTag()));
        cell.getOrCreateTag().put(TAG_FLUIDS, list);
        return FluidStack.EMPTY;
    }

    public static List<ItemStack> readItems(ItemStack cell) {
        List<ItemStack> out = new ArrayList<>();
        for (Tag tag : list(cell, TAG_ITEMS)) {
            ItemStack stack = ItemStack.of((CompoundTag) tag);
            if (!stack.isEmpty()) out.add(stack);
        }
        return out;
    }

    public static List<FluidStack> readFluids(ItemStack cell) {
        List<FluidStack> out = new ArrayList<>();
        for (Tag tag : list(cell, TAG_FLUIDS)) {
            FluidStack stack = FluidStack.loadFluidStackFromNBT((CompoundTag) tag);
            if (!stack.isEmpty()) out.add(stack);
        }
        return out;
    }

    public static void clearContent(ItemStack cell) {
        CompoundTag tag = cell.getOrCreateTag();
        tag.remove(TAG_ITEMS);
        tag.remove(TAG_FLUIDS);
    }

    public static boolean isEmpty(ItemStack cell) {
        return list(cell, TAG_ITEMS).isEmpty() && list(cell, TAG_FLUIDS).isEmpty();
    }

    private static ListTag list(ItemStack cell, String key) {
        CompoundTag tag = cell.getTag();
        if (tag == null || !tag.contains(key, Tag.TAG_LIST)) return new ListTag();
        return tag.getList(key, Tag.TAG_COMPOUND);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack cell = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        if (level.isClientSide()) return InteractionResultHolder.sidedSuccess(cell, true);

        ItemStack terminal = findWirelessTerminal(player);

        boolean transferred = SuperTerminalCellTransfer.toME(player, terminal, cell);
        if (!transferred) {
            player.displayClientMessage(
                    Component.translatable("gtlsupb.item.super_terminal_cell.no_wireless"), true);
            return InteractionResultHolder.fail(cell);
        }

        clearContent(cell);
        cell.shrink(1);
        return InteractionResultHolder.success(cell);
    }

    private static ItemStack findWirelessTerminal(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            for (Class<?> c = stack.getItem().getClass(); c != null; c = c.getSuperclass()) {
                if (WIRELESS_TERMINAL_CLASS.equals(c.getName())) return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (int i = 0;; i++) {
            String key = "gtlsupb.item.super_terminal_cell.tooltip." + i;
            Component line = Component.translatable(key);
            if (line.getString().equals(key)) break;
            tooltip.add(line.copy().withStyle(ChatFormatting.GRAY));
        }
    }

    public static boolean isCell(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && "gtlsupb".equals(id.getNamespace()) && "super_terminal_cell".equals(id.getPath());
    }
}
