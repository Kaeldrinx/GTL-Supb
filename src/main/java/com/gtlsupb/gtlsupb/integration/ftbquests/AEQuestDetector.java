package com.gtlsupb.gtlsupb.integration.ftbquests;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import appeng.api.networking.IGrid;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitFTBQuestIntegrationHatchPartMachine;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;

import java.util.List;
import java.util.UUID;

public class AEQuestDetector {

    private static final java.util.Map<OverlimitFTBQuestIntegrationHatchPartMachine, Integer> HATCH_TICKS = new java.util.WeakHashMap<>();

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        ServerQuestFile questFile = ServerQuestFile.INSTANCE;
        if (questFile == null) return;

        List<Task> submitTasks = questFile.getSubmitTasks();
        if (submitTasks.isEmpty()) return;

        List<OverlimitFTBQuestIntegrationHatchPartMachine> hatches = FTBQuestHatchManager.getHatches();
        if (hatches.isEmpty()) return;

        for (OverlimitFTBQuestIntegrationHatchPartMachine hatch : hatches) {
            if (hatch == null || hatch.getLevel() == null || hatch.getLevel().isClientSide) continue;
            UUID ownerUuid = hatch.getOwnerUuid();
            if (ownerUuid == null) continue;
            if (!hatch.isDetectionEnabled()) continue;

            int tick = HATCH_TICKS.merge(hatch, 1, Integer::sum);
            if (tick < hatch.gtlsupb$getInterval()) continue;
            HATCH_TICKS.put(hatch, 0);

            ServerPlayer player = server.getPlayerList().getPlayer(ownerUuid);
            if (player == null) continue;

            TeamData teamData = TeamData.get(player);
            if (teamData == null) continue;

            IGrid grid = FTBQuestHatchManager.getGrid(hatch);
            if (grid == null) continue;

            KeyCounter mergedCounter = null;
            try {
                IStorageService storageService = grid.getStorageService();
                if (storageService != null) {
                    KeyCounter counter = storageService.getCachedInventory();
                    if (counter != null && !counter.isEmpty()) mergedCounter = counter;
                }
            } catch (Throwable ignored) {}

            for (Task task : submitTasks) {
                if (teamData.isCompleted(task)) continue;
                if (!teamData.areDependenciesComplete(task.getQuest())) continue;

                long maxProgress = task.getMaxProgress();
                if (maxProgress <= 0) continue;

                long currentProgress = teamData.getProgress(task);
                if (currentProgress >= maxProgress) continue;

                boolean consumable = task.consumesResources();
                boolean repeatable = task.getQuest().canBeRepeated();

                if (consumable) {
                    if (repeatable) {
                        if (!hatch.isAutoSubmitEnabled()) continue;
                        if (teamData.getMilliSecondsUntilRepeatable(task.getQuest()) > 0) continue;
                    }
                    AEQuestSubmitHelper.autoSubmitFromGrid(teamData, task, player, grid, hatch);
                } else {
                    if (mergedCounter == null) continue;
                    AEKey key = getTaskKey(task);
                    if (key == null) continue;
                    long aeAmount = mergedCounter.get(key);
                    if (aeAmount <= 0) continue;
                    long newProgress = Math.min(maxProgress, aeAmount);
                    if (newProgress > currentProgress) teamData.setProgress(task, newProgress);
                }
            }
        }
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
}
