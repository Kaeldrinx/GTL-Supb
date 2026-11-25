package com.gtlsupb.gtlsupb.api.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import com.hepdd.gtmthings.utils.TeamUtil;

import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SteamEnergyManager {

    private SteamEnergyManager() {}

    public static final ConcurrentHashMap<UUID, BigInteger> globalSteamEnergy = new ConcurrentHashMap<>();

    public static volatile boolean isDirty = false;

    private static final ConcurrentHashMap<UUID, UUID> userToTeamCache = new ConcurrentHashMap<>();

    private static UUID getTeamUUIDCached(UUID userUuid) {
        return userToTeamCache.computeIfAbsent(userUuid, k -> TeamUtil.getTeamUUID(k));
    }

    public static final ConcurrentHashMap<UUID, java.util.Set<BlockPos>> factoryConnections = new ConcurrentHashMap<>();

    public static void registerFactoryConnection(UUID userUuid, BlockPos pos) {
        UUID teamUUID = getTeamUUIDCached(userUuid);
        factoryConnections.computeIfAbsent(teamUUID, k -> ConcurrentHashMap.newKeySet()).add(pos);
    }

    public static void unregisterFactoryConnection(UUID userUuid, BlockPos pos) {
        UUID teamUUID = getTeamUUIDCached(userUuid);
        java.util.Set<BlockPos> set = factoryConnections.get(teamUUID);
        if (set != null) {
            set.remove(pos);

            if (set.isEmpty()) {
                factoryConnections.remove(teamUUID);
            }
        }
    }

    public static long getFactoryConnectionCount(UUID userUuid) {
        UUID teamUUID = getTeamUUIDCached(userUuid);
        java.util.Set<BlockPos> set = factoryConnections.get(teamUUID);
        return set != null ? (long) set.size() : 0L;
    }

    public static boolean addSteamToGlobalMap(UUID userUuid, BigInteger steam) {
        isDirty = true;
        UUID teamUUID = getTeamUUIDCached(userUuid);
        boolean[] success = { true };
        globalSteamEnergy.compute(teamUUID, (key, current) -> {
            BigInteger total = current != null ? current : BigInteger.ZERO;
            BigInteger newTotal = total.add(steam);
            if (newTotal.signum() < 0) {

                success[0] = false;
                return total.equals(BigInteger.ZERO) ? null : total;
            } else {
                return newTotal;
            }
        });
        return success[0];
    }

    public static boolean addSteamToGlobalMap(UUID userUuid, long steam) {
        if (steam == 0L) return true;
        return addSteamToGlobalMap(userUuid, BigInteger.valueOf(steam));
    }

    public static BigInteger getUserSteam(UUID userUuid) {
        UUID teamUUID = getTeamUUIDCached(userUuid);
        return globalSteamEnergy.getOrDefault(teamUUID, BigInteger.ZERO);
    }

    public static long getUserSteamLong(UUID userUuid) {
        UUID teamUUID = getTeamUUIDCached(userUuid);
        BigInteger steam = globalSteamEnergy.getOrDefault(teamUUID, BigInteger.ZERO);
        if (steam == null) return 0L;

        return steam.bitLength() <= 63 ? steam.longValue() : Long.MAX_VALUE;
    }

    public static boolean tryConsumeSteam(UUID userUuid, long steam) {
        return addSteamToGlobalMap(userUuid, -steam);
    }

    public static CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (java.util.Map.Entry<UUID, BigInteger> entry : globalSteamEnergy.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("uuid", entry.getKey());
            entryTag.putString("steam", entry.getValue().toString());
            list.add(entryTag);
        }
        tag.put("entries", list);
        return tag;
    }

    public static void loadFromNBT(CompoundTag tag) {
        globalSteamEnergy.clear();
        ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID uuid = entry.getUUID("uuid");
            BigInteger steam = new BigInteger(entry.getString("steam"));
            globalSteamEnergy.put(uuid, steam);
        }
    }

    public static SteamEnergySavedData getSavedData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                SteamEnergySavedData::load,
                SteamEnergySavedData::new,
                "gtlsupb_steam_energy");
    }
}
