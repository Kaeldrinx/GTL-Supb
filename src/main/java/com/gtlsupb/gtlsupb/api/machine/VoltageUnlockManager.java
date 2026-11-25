package com.gtlsupb.gtlsupb.api.machine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import com.hepdd.gtmthings.utils.TeamUtil;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VoltageUnlockManager {

    private VoltageUnlockManager() {}

    public static final ConcurrentHashMap<UUID, Integer> globalVoltageTier = new ConcurrentHashMap<>();

    public static volatile boolean isDirty = false;

    private static final ConcurrentHashMap<UUID, UUID> userToTeamCache = new ConcurrentHashMap<>();

    private static UUID getTeamUUIDCached(UUID userUuid) {
        return userToTeamCache.computeIfAbsent(userUuid, k -> TeamUtil.getTeamUUID(k));
    }

    public static void setTier(UUID userUuid, int tier) {
        isDirty = true;
        UUID teamUUID = getTeamUUIDCached(userUuid);
        int current = globalVoltageTier.getOrDefault(teamUUID, 0);
        if (tier > current) {
            globalVoltageTier.put(teamUUID, tier);
        }
    }

    public static void setTierDirect(UUID userUuid, int tier) {
        isDirty = true;
        UUID teamUUID = getTeamUUIDCached(userUuid);
        globalVoltageTier.put(teamUUID, tier);
    }

    public static int getTier(UUID userUuid) {
        UUID teamUUID = getTeamUUIDCached(userUuid);
        return globalVoltageTier.getOrDefault(teamUUID, -1);
    }

    public static void removeTier(UUID userUuid) {
        isDirty = true;
        UUID teamUUID = getTeamUUIDCached(userUuid);
        globalVoltageTier.remove(teamUUID);
    }

    public static CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag tierList = new ListTag();
        for (java.util.Map.Entry<UUID, Integer> entry : globalVoltageTier.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("uuid", entry.getKey());
            entryTag.putInt("tier", entry.getValue());
            tierList.add(entryTag);
        }
        tag.put("entries", tierList);
        return tag;
    }

    public static void loadFromNBT(CompoundTag tag) {
        globalVoltageTier.clear();

        ListTag tierList = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < tierList.size(); i++) {
            CompoundTag entry = tierList.getCompound(i);
            UUID uuid = entry.getUUID("uuid");
            int tier = entry.getInt("tier");
            globalVoltageTier.put(uuid, tier);
        }
    }

    public static VoltageUnlockSavedData getSavedData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                VoltageUnlockSavedData::load,
                VoltageUnlockSavedData::new,
                "gtlsupb_voltage_unlock");
    }
}
