package com.gtlsupb.gtlsupb.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;

public final class SuperTerminalCellTransfer {

    private SuperTerminalCellTransfer() {}

    public static boolean toME(Player player, ItemStack terminal, ItemStack cell) {
        MEStorage storage = resolveStorage(player);
        if (storage == null) return false;

        IActionSource source = IActionSource.ofPlayer(player);

        MEStorage cellInventory = cellInventoryOf(cell);
        if (cellInventory != null) {
            var counter = new appeng.api.stacks.KeyCounter();
            cellInventory.getAvailableStacks(counter);
            for (AEKey key : counter.keySet()) {
                long amount = counter.get(key);
                if (amount <= 0L) continue;
                long moved = storage.insert(key, amount, Actionable.MODULATE, source);
                if (moved > 0L) {
                    cellInventory.extract(key, moved, Actionable.MODULATE, source);
                }
                if (moved < amount) return false;
            }
        }

        for (ItemStack stack : SuperTerminalCellItem.readItems(cell)) {
            if (stack.isEmpty()) continue;
            AEItemKey key = AEItemKey.of(stack);
            if (key == null) return false;
            long inserted = storage.insert(key, stack.getCount(), Actionable.MODULATE, source);
            if (inserted < stack.getCount()) return false;
        }

        for (FluidStack fluid : SuperTerminalCellItem.readFluids(cell)) {
            if (fluid.isEmpty()) continue;
            AEFluidKey key = AEFluidKey.of(fluid);
            if (key == null) return false;
            long inserted = storage.insert(key, fluid.getAmount(), Actionable.MODULATE, source);
            if (inserted < fluid.getAmount()) return false;
        }

        return true;
    }

    private static MEStorage resolveStorage(Player player) {
        ItemStack bound = findBoundAeOwner(player);
        if (!bound.isEmpty()) {
            MEStorage fromBinding = storageFromBoundAe(player, bound);
            if (fromBinding != null) return fromBinding;
        }

        try {
            return com.gtlsupb.gtlsupb.integration.ae2.AEGridRegistry.getFirstAvailableStorage();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static ItemStack findBoundAeOwner(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isSuperTerminal(stack) && hasBoundAe(stack)) return stack;
        }
        ItemStack offhand = player.getOffhandItem();
        if (isSuperTerminal(offhand) && hasBoundAe(offhand)) return offhand;
        return ItemStack.EMPTY;
    }

    private static boolean isSuperTerminal(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && "gtlsupb".equals(id.getNamespace()) && "super_terminal".equals(id.getPath());
    }

    private static boolean hasBoundAe(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("BoundAE") && tag.getCompound("BoundAE").contains("dim");
    }

    private static MEStorage storageFromBoundAe(Player player, ItemStack terminal) {
        CompoundTag aePos = terminal.getTag().getCompound("BoundAE");
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION,
                new ResourceLocation(aePos.getString("dim")));
        if (player.getServer() == null) return null;
        ServerLevel targetLevel = player.getServer().getLevel(dimension);
        if (targetLevel == null) return null;
        BlockPos pos = new BlockPos(aePos.getInt("X"), aePos.getInt("Y"), aePos.getInt("Z"));

        IGridNode node = com.gtlsupb.gtlsupb.api.pattern.AdvancedBlockPattern
                .getGridNode(targetLevel.getBlockEntity(pos));
        if (node == null) return null;
        IGrid grid = node.getGrid();
        if (grid == null || grid.getStorageService() == null) return null;
        return grid.getStorageService().getInventory();
    }

    private static MEStorage cellInventoryOf(ItemStack cell) {
        try {
            return appeng.api.storage.StorageCells.getCellInventory(cell, null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static String describe(ItemStack cell) {
        long itemCount = SuperTerminalCellItem.readItems(cell).stream().mapToLong(ItemStack::getCount).sum();
        long fluidCount = SuperTerminalCellItem.readFluids(cell).stream().mapToLong(FluidStack::getAmount).sum();
        return "items=" + itemCount + " fluids=" + fluidCount + "mB";
    }

    public static GlobalPos boundAeOf(ItemStack terminal) {
        if (!isSuperTerminal(terminal) || !hasBoundAe(terminal)) return null;
        CompoundTag tag = terminal.getTag().getCompound("BoundAE");
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION,
                new ResourceLocation(tag.getString("dim")));
        return GlobalPos.of(dimension, new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z")));
    }
}
