package com.gtlsupb.gtlsupb.integration.ftbquests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AEQuestSettings extends SavedData {

    public static final String DATA_NAME = "gtlsupb_aequest_settings";

    public static final int INTERVAL_MIN = 10;
    public static final int INTERVAL_MAX = 200;
    public static final int INTERVAL_DEFAULT = 40;
    public static final int INTERVAL_STEP = 10;

    public final Map<String, Boolean> detectionEnabled = new HashMap<>();

    public final Map<String, Boolean> rewardEnabled = new HashMap<>();

    public final Map<String, Boolean> autoSubmitEnabled = new HashMap<>();

    public final Map<String, Integer> detectionInterval = new HashMap<>();

    public AEQuestSettings() {}

    public static AEQuestSettings load(CompoundTag tag) {
        AEQuestSettings data = new AEQuestSettings();
        loadBoolMap(tag, "DetectionEnabled", data.detectionEnabled);
        loadBoolMap(tag, "RewardEnabled", data.rewardEnabled);
        loadBoolMap(tag, "AutoSubmitEnabled", data.autoSubmitEnabled);
        loadIntMap(tag, "DetectionInterval", data.detectionInterval);
        return data;
    }

    private static void loadBoolMap(CompoundTag root, String key, Map<String, Boolean> out) {
        if (!root.contains(key, Tag.TAG_COMPOUND)) return;
        CompoundTag map = root.getCompound(key);
        for (String k : map.getAllKeys()) {
            if (map.getTagType(k) == Tag.TAG_BYTE) out.put(k, map.getBoolean(k));
        }
    }

    private static void loadIntMap(CompoundTag root, String key, Map<String, Integer> out) {
        if (!root.contains(key, Tag.TAG_COMPOUND)) return;
        CompoundTag map = root.getCompound(key);
        for (String k : map.getAllKeys()) {
            if (map.getTagType(k) == Tag.TAG_INT) out.put(k, map.getInt(k));
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag det = new CompoundTag();
        for (Map.Entry<String, Boolean> e : detectionEnabled.entrySet()) det.putBoolean(e.getKey(), e.getValue());
        tag.put("DetectionEnabled", det);

        CompoundTag rew = new CompoundTag();
        for (Map.Entry<String, Boolean> e : rewardEnabled.entrySet()) rew.putBoolean(e.getKey(), e.getValue());
        tag.put("RewardEnabled", rew);

        CompoundTag auto = new CompoundTag();
        for (Map.Entry<String, Boolean> e : autoSubmitEnabled.entrySet()) auto.putBoolean(e.getKey(), e.getValue());
        tag.put("AutoSubmitEnabled", auto);

        CompoundTag inv = new CompoundTag();
        for (Map.Entry<String, Integer> e : detectionInterval.entrySet()) inv.putInt(e.getKey(), e.getValue());
        tag.put("DetectionInterval", inv);
        return tag;
    }

    public static AEQuestSettings get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                AEQuestSettings::load,
                AEQuestSettings::new,
                DATA_NAME);
    }

    public static boolean isDetectionEnabled(MinecraftServer server, UUID playerUUID) {
        if (server == null) return true;
        return get(server).detectionEnabled.getOrDefault(playerUUID.toString(), Boolean.TRUE);
    }

    public static boolean toggleDetection(MinecraftServer server, UUID playerUUID) {
        AEQuestSettings data = get(server);
        boolean next = !data.detectionEnabled.getOrDefault(playerUUID.toString(), Boolean.TRUE);
        data.detectionEnabled.put(playerUUID.toString(), next);
        data.setDirty();
        return next;
    }

    public static boolean isRewardEnabled(MinecraftServer server, UUID playerUUID) {
        if (server == null) return true;
        return get(server).rewardEnabled.getOrDefault(playerUUID.toString(), Boolean.TRUE);
    }

    public static boolean toggleReward(MinecraftServer server, UUID playerUUID) {
        AEQuestSettings data = get(server);
        boolean next = !data.rewardEnabled.getOrDefault(playerUUID.toString(), Boolean.TRUE);
        data.rewardEnabled.put(playerUUID.toString(), next);
        data.setDirty();
        return next;
    }

    public static boolean isAutoSubmitEnabled(MinecraftServer server, UUID playerUUID) {
        if (server == null) return false;
        return get(server).autoSubmitEnabled.getOrDefault(playerUUID.toString(), Boolean.FALSE);
    }

    public static boolean toggleAutoSubmit(MinecraftServer server, UUID playerUUID) {
        AEQuestSettings data = get(server);
        boolean next = !data.autoSubmitEnabled.getOrDefault(playerUUID.toString(), Boolean.FALSE);
        data.autoSubmitEnabled.put(playerUUID.toString(), next);
        data.setDirty();
        return next;
    }

    public static int getInterval(MinecraftServer server, UUID playerUUID) {
        if (server == null) return INTERVAL_DEFAULT;
        return get(server).detectionInterval.getOrDefault(playerUUID.toString(), INTERVAL_DEFAULT);
    }

    public static int setInterval(MinecraftServer server, UUID playerUUID, int value) {
        AEQuestSettings data = get(server);
        int clamped = Math.max(INTERVAL_MIN, Math.min(INTERVAL_MAX, value));
        data.detectionInterval.put(playerUUID.toString(), clamped);
        data.setDirty();
        return clamped;
    }

    public static int stepInterval(MinecraftServer server, UUID playerUUID, int delta) {
        int current = getInterval(server, playerUUID);
        return setInterval(server, playerUUID, current + delta);
    }
}
