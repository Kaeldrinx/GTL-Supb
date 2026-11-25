package com.gtlsupb.gtlsupb.integration.ae2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

import java.math.BigInteger;

public final class MEBigIntegerTransfer {

    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private MEBigIntegerTransfer() {}

    public static void writeBuffer(CompoundTag tag, String key, Object2ObjectMap<AEKey, BigInteger> buffer) {
        ListTag list = new ListTag();
        for (var entry : buffer.entrySet()) {
            CompoundTag compound = entry.getKey().toTagGeneric();
            compound.putString("gtlsupb_big_amount", entry.getValue().toString());
            list.add(compound);
        }
        tag.put(key, list);
    }

    public static void readBuffer(ListTag list, Object2ObjectMap<AEKey, BigInteger> buffer) {
        if (list == null) return;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag compound = list.getCompound(i);
            AEKey key = AEKey.fromTagGeneric(compound);
            if (key == null) continue;
            BigInteger amount;
            if (compound.contains("gtlsupb_big_amount", Tag.TAG_STRING)) {
                try {
                    amount = new BigInteger(compound.getString("gtlsupb_big_amount"));
                } catch (NumberFormatException ignored) {
                    continue;
                }
            } else {
                amount = BigInteger.valueOf(compound.getLong("real"));
            }
            if (amount.signum() <= 0) continue;
            buffer.merge(key, amount, BigInteger::add);
        }
    }

    public static BigInteger insert(IGrid grid, AEKey key, BigInteger amount, IActionSource source) {
        if (grid == null || key == null || amount == null || amount.signum() <= 0) return BigInteger.ZERO;
        MEStorage storage = grid.getStorageService() == null ? null : grid.getStorageService().getInventory();
        if (storage == null) return BigInteger.ZERO;
        BigInteger left = amount;
        int failures = 0;
        while (left.signum() > 0 && failures < 5) {
            long chunk = left.min(LONG_MAX).longValueExact();
            long inserted = StorageHelper.poweredInsert(grid.getEnergyService(), storage, key, chunk, source);
            if (inserted <= 0L) {
                failures++;
                continue;
            }
            left = left.subtract(BigInteger.valueOf(inserted));
        }
        return amount.subtract(left);
    }

    public static BigInteger extract(IGrid grid, AEKey key, BigInteger amount, Actionable actionable,
                                     IActionSource source) {
        if (grid == null || key == null || amount == null || amount.signum() <= 0) return BigInteger.ZERO;
        MEStorage storage = grid.getStorageService() == null ? null : grid.getStorageService().getInventory();
        if (storage == null) return BigInteger.ZERO;
        BigInteger left = amount;
        while (left.signum() > 0) {
            long chunk = left.min(LONG_MAX).longValueExact();
            long extracted = storage.extract(key, chunk, actionable, source);
            if (extracted <= 0L) break;
            if (actionable == Actionable.SIMULATE) {
                left = left.subtract(BigInteger.valueOf(extracted));
                break;
            }
            left = left.subtract(BigInteger.valueOf(extracted));
        }
        return amount.subtract(left);
    }
}
