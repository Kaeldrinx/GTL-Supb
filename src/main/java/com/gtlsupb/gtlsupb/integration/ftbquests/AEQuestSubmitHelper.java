package com.gtlsupb.gtlsupb.integration.ftbquests;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import com.gtlsupb.gtlsupb.integration.ae2.AEGridRegistry;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;

import java.util.ArrayList;
import java.util.List;

public class AEQuestSubmitHelper {

    public static final ThreadLocal<Boolean> SUPPRESS_REWARD_MESSAGES = ThreadLocal.withInitial(() -> false);

    public static final ThreadLocal<List<RewardEntry>> REWARD_COLLECTOR = ThreadLocal.withInitial(ArrayList::new);

    public static final ThreadLocal<Integer> REWARD_MULTIPLIER = ThreadLocal.withInitial(() -> 1);

    private static final ThreadLocal<Boolean> SUBMITTING = ThreadLocal.withInitial(() -> false);

    public static final class RewardEntry {

        private final ItemStack stack;
        private long count;

        public RewardEntry(ItemStack stack, long count) {
            this.stack = stack;
            this.count = count;
        }

        public ItemStack getStack() {
            return stack;
        }

        public long getCount() {
            return count;
        }

        public void addCount(long delta) {
            this.count += delta;
        }
    }

    public static void collectReward(List<RewardEntry> list, ItemStack stack, long count) {
        if (stack == null || stack.isEmpty() || count <= 0) return;
        for (RewardEntry e : list) {
            if (ItemStack.isSameItemSameTags(e.stack, stack)) {
                e.addCount(count);
                return;
            }
        }
        list.add(new RewardEntry(stack.copy(), count));
    }

    public static long submitFromAE(TeamData teamData, ServerPlayer player, Task task) {
        if (SUBMITTING.get()) return 0;
        SUBMITTING.set(true);
        try {
            return submitFromAEInner(teamData, player, task);
        } finally {
            SUBMITTING.set(false);
        }
    }

    private static long submitFromAEInner(TeamData teamData, ServerPlayer player, Task task) {
        AEKey key = getTaskKey(task);
        if (key == null) return 0;

        long needed = task.getMaxProgress() - teamData.getProgress(task);
        if (needed <= 0) return 0;

        IActionSource source = sourceOf(player);

        long canExtract = simulateExtractFromAE(key, needed, source, player);
        if (canExtract <= 0) return 0;

        long extracted = extractFromAE(key, canExtract, source, player);
        if (extracted > 0) {
            teamData.addProgress(task, extracted);
        }
        return extracted;
    }

    public static long autoSubmitOnce(TeamData teamData, Task task, ServerPlayer player) {
        if (SUBMITTING.get()) return 0;
        SUBMITTING.set(true);
        try {
            return autoSubmitOnceInner(teamData, task, player);
        } finally {
            SUBMITTING.set(false);
        }
    }

    private static long autoSubmitOnceInner(TeamData teamData, Task task, ServerPlayer player) {
        AEKey key = getTaskKey(task);
        if (key == null) return 0;

        long needed = task.getMaxProgress() - teamData.getProgress(task);
        if (needed <= 0) return 0;

        IActionSource source = sourceOf(player);
        long extracted = extractFromAE(key, needed, source, player);
        if (extracted <= 0) return 0;

        teamData.addProgress(task, extracted);
        return extracted;
    }

    public static long autoSubmitFromGrid(TeamData teamData, Task task, ServerPlayer player, IGrid grid,
                                          com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitFTBQuestIntegrationHatchPartMachine hatch) {
        if (SUBMITTING.get()) return 0;
        SUBMITTING.set(true);
        try {
            return autoSubmitFromGridInner(teamData, task, player, grid, hatch);
        } finally {
            SUBMITTING.set(false);
        }
    }

    private static long autoSubmitFromGridInner(TeamData teamData, Task task, ServerPlayer player, IGrid grid,
                                                com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitFTBQuestIntegrationHatchPartMachine hatch) {
        AEKey key = getTaskKey(task);
        if (key == null || grid == null) return 0;

        long needed = task.getMaxProgress() - teamData.getProgress(task);
        if (needed <= 0) return 0;

        try {
            var storageService = grid.getStorageService();
            if (storageService == null) return 0;
            MEStorage storage = storageService.getInventory();
            if (storage == null) return 0;

            IActionSource source = sourceOf(player);
            long available = storage.extract(key, needed, Actionable.SIMULATE, source);
            if (available <= 0) return 0;
            long extracted = storage.extract(key, available, Actionable.MODULATE, source);
            if (extracted > 0) {
                teamData.addProgress(task, extracted);
                return extracted;
            }
            return 0;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static long submitCycleLoop(TeamData teamData, Task task, ServerPlayer player, int cycles) {
        if (SUBMITTING.get()) return 0;
        SUBMITTING.set(true);
        try {
            return submitCycleLoopInner(teamData, task, player, cycles);
        } finally {
            SUBMITTING.set(false);
        }
    }

    private static long submitCycleLoopInner(TeamData teamData, Task task, ServerPlayer player, int cycles) {
        if (cycles <= 0 || teamData == null || task == null) return 0;

        AEKey key = getTaskKey(task);
        if (key == null) return 0;

        long maxProgress = task.getMaxProgress();
        if (maxProgress <= 0 || teamData.getMilliSecondsUntilRepeatable(task.getQuest()) > 0) return 0;

        IActionSource source = sourceOf(player);
        List<MEStorage> storages = getPlayerStorages(player);
        if (storages.isEmpty()) return 0;

        long currentProgress = teamData.getProgress(task);
        long firstNeeded = Math.max(0L, maxProgress - currentProgress);
        long available = simulateFromStorages(key, source, storages);
        if (available <= 0L) return 0;

        long possibleCycles;
        if (firstNeeded > 0L) {
            if (available < firstNeeded) {
                possibleCycles = 0L;
            } else {
                possibleCycles = 1L + (available - firstNeeded) / maxProgress;
            }
        } else {
            possibleCycles = available / maxProgress;
        }
        if (possibleCycles <= 0L) return 0;
        long batchCycles = Math.min((long) cycles, possibleCycles);
        if (batchCycles > Integer.MAX_VALUE) batchCycles = Integer.MAX_VALUE;

        long totalCost = firstNeeded + (batchCycles - 1L) * maxProgress;
        long extracted = extractFromStorages(key, totalCost, source, storages);
        if (extracted <= 0L) return 0;

        long completedCycles = extracted >= totalCost ? batchCycles : 0L;
        if (completedCycles <= 0L) {
            teamData.addProgress(task, extracted);
            return extracted;
        }

        SUPPRESS_REWARD_MESSAGES.set(true);
        REWARD_MULTIPLIER.set((int) completedCycles);
        REWARD_COLLECTOR.get().clear();
        try {
            teamData.setProgress(task, maxProgress);
            AEQuestRewardHandler.tryAutoClaimRewards(teamData, task.getQuest(), player);
            teamData.setProgress(task, 0L);
            gtlsupb$bumpCompletionCount(teamData, task, (int) completedCycles);
        } finally {
            SUPPRESS_REWARD_MESSAGES.set(false);
            REWARD_MULTIPLIER.set(1);
            List<RewardEntry> collected = new ArrayList<>(REWARD_COLLECTOR.get());
            REWARD_COLLECTOR.get().clear();
            if (!collected.isEmpty() && player != null) {
                com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                        new com.gtlsupb.gtlsupb.network.S2CRewardListPacket(collected));
            }
        }
        return totalCost;
    }

    private static java.lang.reflect.Field gtlsupb$completionCountField;

    private static void gtlsupb$bumpCompletionCount(TeamData teamData, Task task, int amount) {
        try {
            if (gtlsupb$completionCountField == null) {
                gtlsupb$completionCountField = TeamData.class.getDeclaredField("completionCount");
                gtlsupb$completionCountField.setAccessible(true);
            }
            Object map = gtlsupb$completionCountField.get(teamData);
            if (map instanceof it.unimi.dsi.fastutil.longs.Long2IntMap intMap) {
                intMap.merge(task.getQuest().getId(), amount, Integer::sum);
                teamData.markDirty();
            }
        } catch (Throwable ignored) {}
    }

    private static long simulateFromStorages(AEKey key, IActionSource source, List<MEStorage> storages) {
        long total = 0L;
        for (MEStorage storage : storages) {
            try {
                total += storage.extract(key, Long.MAX_VALUE - total, Actionable.SIMULATE, source);
                if (total == Long.MAX_VALUE) break;
            } catch (Exception ignored) {}
        }
        return total;
    }

    private static List<MEStorage> getPlayerStorages(ServerPlayer player) {
        List<MEStorage> storages = new ArrayList<>();
        for (IGrid grid : AEGridRegistry.getActiveGridsForPlayer(player == null ? null : player.getUUID())) {
            try {
                var storageService = grid.getStorageService();
                if (storageService == null) continue;
                MEStorage storage = storageService.getInventory();
                if (storage != null) storages.add(storage);
            } catch (Exception ignored) {}
        }
        return storages;
    }

    private static long extractFromStorages(AEKey key, long amount, IActionSource source, List<MEStorage> storages) {
        long remaining = amount;
        long extractedTotal = 0L;
        for (MEStorage storage : storages) {
            if (remaining <= 0L) break;
            try {
                long extracted = storage.extract(key, remaining, Actionable.MODULATE, source);
                if (extracted <= 0L) continue;
                extractedTotal += extracted;
                remaining -= extracted;
            } catch (Exception ignored) {}
        }
        return extractedTotal;
    }

    private static AEKey getTaskKey(Task task) {
        if (task instanceof ItemTask itemTask) {
            ItemStack stack = itemTask.getItemStack();
            if (stack == null || stack.isEmpty()) return null;
            return AEItemKey.of(stack);
        } else if (task instanceof FluidTask fluidTask) {
            if (fluidTask.getFluid() == null) return null;
            return AEFluidKey.of(fluidTask.getFluid());
        }
        return null;
    }

    private static IActionSource sourceOf(ServerPlayer player) {
        return player != null ? IActionSource.ofPlayer(player) : IActionSource.empty();
    }

    private static long simulateExtractFromAE(AEKey key, long amount, IActionSource source, ServerPlayer player) {
        if (amount <= 0) return 0;
        long total = 0;
        for (IGrid grid : AEGridRegistry.getActiveGridsForPlayer(player == null ? null : player.getUUID())) {
            if (amount - total <= 0) break;
            try {
                var storageService = grid.getStorageService();
                if (storageService == null) continue;
                MEStorage storage = storageService.getInventory();
                if (storage == null) continue;
                total += storage.extract(key, amount - total, Actionable.SIMULATE, source);
            } catch (Exception ignored) {

            }
        }
        return total;
    }

    private static long extractFromAE(AEKey key, long amount, IActionSource source, ServerPlayer player) {
        if (amount <= 0) return 0;
        long totalExtracted = 0;
        long remaining = amount;
        for (IGrid grid : AEGridRegistry.getActiveGridsForPlayer(player == null ? null : player.getUUID())) {
            if (remaining <= 0) break;
            try {
                var storageService = grid.getStorageService();
                if (storageService == null) continue;
                MEStorage storage = storageService.getInventory();
                if (storage == null) continue;
                long extracted = storage.extract(key, remaining, Actionable.MODULATE, source);
                totalExtracted += extracted;
                remaining -= extracted;
            } catch (Exception ignored) {

            }
        }
        return totalExtracted;
    }
}
