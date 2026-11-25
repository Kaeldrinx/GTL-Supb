package com.gtlsupb.gtlsupb.integration.ftbquests;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import com.gtlsupb.gtlsupb.GTLSupb;
import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;

import java.util.List;
import java.util.UUID;

public class AEQuestRewardHandler {

    public static void init() {
        ObjectCompletedEvent.QUEST.register(event -> {
            Quest quest = event.getQuest();
            TeamData data = event.getData();
            List<ServerPlayer> onlineMembers = event.getOnlineMembers();
            if (onlineMembers.isEmpty()) return EventResult.pass();

            MinecraftServer server = onlineMembers.get(0).getServer();

            MEStorage storage = null;
            UUID teamId = FTBQuestHatchManager.getTeamId(data.getTeamId());
            for (var hatch : FTBQuestHatchManager.getHatches()) {
                if (!hatch.isRewardEnabled()) continue;
                if (hatch.getOwnerUuid() == null) continue;
                if (!teamId.equals(FTBQuestHatchManager.getTeamId(hatch.getOwnerUuid()))) continue;
                IGrid grid = FTBQuestHatchManager.getGrid(hatch);
                if (grid == null) continue;
                try {
                    var storageService = grid.getStorageService();
                    if (storageService != null) storage = storageService.getInventory();
                } catch (Throwable ignored) {}
                if (storage != null) break;
            }
            if (storage == null) return EventResult.pass();

            ServerPlayer actingPlayer = onlineMembers.get(0);
            IActionSource source = IActionSource.ofPlayer(actingPlayer);

            for (Reward reward : quest.getRewards()) {
                if (!(reward instanceof ItemReward)) continue;
                ItemReward itemReward = (ItemReward) reward;

                if (reward.getAutoClaimType() == RewardAutoClaim.DISABLED) continue;

                if (reward.getExcludeFromClaimAll()) continue;

                boolean alreadyClaimed = false;
                for (ServerPlayer member : onlineMembers) {
                    if (data.isRewardClaimed(member.getUUID(), reward)) {
                        alreadyClaimed = true;
                        break;
                    }
                }
                if (alreadyClaimed) continue;

                ItemStack rewardStack = itemReward.getItem();
                int count = itemReward.getCount();
                if (rewardStack == null || rewardStack.isEmpty()) continue;

                long multipliedCount = gtlsupb$rewardCount(count);
                AEItemKey key = AEItemKey.of(rewardStack);
                long inserted = storage.insert(key, multipliedCount, Actionable.MODULATE, source);
                if (inserted > 0) {

                    long timestamp = System.currentTimeMillis();
                    for (ServerPlayer member : onlineMembers) {
                        if (!data.isRewardClaimed(member.getUUID(), reward)) {
                            data.markRewardAsClaimed(member.getUUID(), reward, timestamp);
                        }
                    }
                    if (AEQuestSubmitHelper.SUPPRESS_REWARD_MESSAGES.get()) {

                        AEQuestSubmitHelper.collectReward(
                                AEQuestSubmitHelper.REWARD_COLLECTOR.get(), rewardStack, inserted);
                    } else {
                        Component msg = Component.translatable("gtlsupb.aequest.reward.to_ae",
                                inserted + "x " + rewardStack.getHoverName().getString());
                        for (ServerPlayer member : onlineMembers) {
                            member.sendSystemMessage(msg);
                        }
                    }
                    GTLSupb.LOGGER.info("[AEQuest] 奖励已放入AE: {}x{} (quest={})",
                            inserted, rewardStack, quest);
                } else {
                    GTLSupb.LOGGER.warn("[AEQuest] 奖励放入AE失败(网络已满?): {}x{} (quest={})",
                            count, rewardStack, quest);
                }
            }
            return EventResult.pass();
        });
    }

    private static long gtlsupb$rewardCount(int base) {
        int multiplier = Math.max(1, AEQuestSubmitHelper.REWARD_MULTIPLIER.get());
        long value = (long) base * multiplier;
        return Math.max(1L, value);
    }

    public static boolean tryClaimRewardToAE(ServerPlayer player, ItemReward reward) {
        var server = player.getServer();
        if (server == null) return false;

        ItemStack rewardStack = reward.getItem();
        int count = reward.getCount();
        if (rewardStack == null || rewardStack.isEmpty()) return false;

        MEStorage storage = null;
        UUID teamId = FTBQuestHatchManager.getTeamId(player.getUUID());
        for (var hatch : FTBQuestHatchManager.getHatches()) {
            if (!hatch.isRewardEnabled()) continue;
            if (hatch.getOwnerUuid() == null) continue;
            if (!teamId.equals(FTBQuestHatchManager.getTeamId(hatch.getOwnerUuid()))) continue;
            IGrid grid = FTBQuestHatchManager.getGrid(hatch);
            if (grid == null) continue;
            try {
                var storageService = grid.getStorageService();
                if (storageService != null) storage = storageService.getInventory();
            } catch (Throwable ignored) {}
            if (storage != null) break;
        }
        if (storage == null) return false;

        IActionSource source = IActionSource.ofPlayer(player);
        AEItemKey key = AEItemKey.of(rewardStack);
        long inserted = storage.insert(key, gtlsupb$rewardCount(count), Actionable.MODULATE, source);

        if (inserted <= 0) return false;

        TeamData teamData = TeamData.get(player);
        if (teamData != null) {
            teamData.markRewardAsClaimed(player.getUUID(), reward, System.currentTimeMillis());
        }

        if (AEQuestSubmitHelper.SUPPRESS_REWARD_MESSAGES.get()) {
            AEQuestSubmitHelper.collectReward(
                    AEQuestSubmitHelper.REWARD_COLLECTOR.get(), rewardStack, inserted);
        } else {
            Component msg = Component.translatable("gtlsupb.aequest.reward.to_ae",
                    inserted + "x " + rewardStack.getHoverName().getString());
            player.sendSystemMessage(msg);
        }

        GTLSupb.LOGGER.info("[AEQuest] 奖励已放入AE(手动领取): {}x{} (player={})",
                inserted, rewardStack, player.getName().getString());
        return true;
    }

    public static void tryAutoClaimRewards(TeamData teamData, Quest quest, ServerPlayer player) {
        for (Reward reward : quest.getRewards()) {
            if (teamData.isRewardClaimed(player.getUUID(), reward)) continue;
            try {
                teamData.claimReward(player, reward, false);
            } catch (Exception ignored) {

            }
        }
    }
}
