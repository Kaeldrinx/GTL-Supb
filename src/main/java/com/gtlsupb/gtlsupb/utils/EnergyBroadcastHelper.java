package com.gtlsupb.gtlsupb.utils;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableLaserContainer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EnergyBroadcastHelper {

    public static final EnergyBroadcastHelper INSTANCE = new EnergyBroadcastHelper();

    private static final ConcurrentHashMap<UUID, ConcurrentHashMap<ResourceKey<Level>, List<BlockPos>>> knownReceivers = new ConcurrentHashMap<>();

    private static final long RESCAN_INTERVAL_TICKS = 200L;

    private static final long EMPTY_RESCAN_INTERVAL_TICKS = 20L;

    private static final ConcurrentHashMap<UUID, Long> lastRescanTicks = new ConcurrentHashMap<>();

    private EnergyBroadcastHelper() {}

    public static long broadcastEnergy(MetaMachine machine, long targetEU, UUID ownerUuid) {
        if (targetEU <= 0) return 0L;
        var level = machine.getLevel();
        var server = level != null ? level.getServer() : null;
        if (server == null) return targetEU;
        long remaining = targetEU;

        if (!isFtbChunksAvailable()) {
            return Math.max(remaining, 0L);
        }

        UUID teamId = gtlsupb$teamId(ownerUuid);

        long gameTime = level != null ? level.getGameTime() : 0L;

        ConcurrentHashMap<ResourceKey<Level>, List<BlockPos>> cache = knownReceivers.get(teamId);
        long interval = (cache == null || cache.isEmpty()) ? EMPTY_RESCAN_INTERVAL_TICKS : RESCAN_INTERVAL_TICKS;
        long last = lastRescanTicks.getOrDefault(teamId, Long.MIN_VALUE);
        boolean doRescan = (gameTime - last) >= interval;
        if (doRescan) {
            lastRescanTicks.put(teamId, gameTime);

            remaining = rescanForceLoadedChunks(server, teamId, remaining);

        } else if (cache != null && !cache.isEmpty()) {

            remaining = injectCachedReceivers(server, cache, remaining);
        }

        return Math.max(remaining, 0L);
    }

    private static UUID gtlsupb$teamId(UUID ownerUuid) {
        if (ownerUuid == null) return new UUID(0L, 0L);
        try {
            return com.hepdd.gtmthings.utils.TeamUtil.getTeamUUID(ownerUuid);
        } catch (Throwable ignored) {
            return ownerUuid;
        }
    }

    private static long injectCachedReceivers(MinecraftServer server,
                                              ConcurrentHashMap<ResourceKey<Level>, List<BlockPos>> cache,
                                              long remaining) {
        if (remaining <= 0) return 0L;
        int receiverCount = 0;
        for (List<BlockPos> receivers : cache.values()) {
            if (receivers != null) receiverCount += receivers.size();
        }
        if (receiverCount <= 0) return remaining;
        long perTarget = Math.max(remaining / receiverCount, 1L);
        long rem = remaining;
        for (var entry : cache.entrySet()) {
            if (rem <= 0) break;
            ServerLevel dimLevel = server.getLevel(entry.getKey());
            if (dimLevel == null) continue;
            List<BlockPos> receivers = entry.getValue();
            if (receivers == null || receivers.isEmpty()) continue;
            for (BlockPos pos : receivers) {
                if (rem <= 0) return 0L;
                rem = tryInjectEnergyIntoBlock(dimLevel, pos, rem, perTarget);
            }
        }
        return rem;
    }

    private static long rescanForceLoadedChunks(MinecraftServer server, UUID teamId, long remaining) {
        ConcurrentHashMap<ResourceKey<Level>, List<BlockPos>> newCache = new ConcurrentHashMap<>();
        int receiverCount = 0;

        Map<ResourceKey<Level>, Long2ObjectMap<UUID>> allForceLoaded = getAllForceLoadedChunksFresh();

        for (ServerLevel dimLevel : server.getAllLevels()) {
            ResourceKey<Level> dimKey = dimLevel.dimension();
            Long2ObjectMap<UUID> forceLoaded = allForceLoaded.get(dimKey);
            if (forceLoaded == null || forceLoaded.isEmpty()) continue;

            List<BlockPos> dimReceivers = new ArrayList<>();
            for (long chunkKey : forceLoaded.keySet()) {
                UUID chunkTeam = forceLoaded.get(chunkKey);
                if (chunkTeam == null || !chunkTeam.equals(teamId)) continue;
                int cx = ChunkPos.getX(chunkKey);
                int cz = ChunkPos.getZ(chunkKey);
                if (!dimLevel.hasChunk(cx, cz)) continue;
                var chunkObj = dimLevel.getChunk(cx, cz);
                if (!(chunkObj instanceof LevelChunk)) continue;
                LevelChunk chunk = (LevelChunk) chunkObj;
                for (BlockPos blockPos : chunk.getBlockEntities().keySet()) {

                    if (!hasEnergyReceiver(dimLevel, blockPos)) continue;
                    dimReceivers.add(blockPos.immutable());
                }
            }

            if (!dimReceivers.isEmpty()) {
                newCache.put(dimKey, dimReceivers);
                receiverCount += dimReceivers.size();
            }
        }

        knownReceivers.put(teamId, newCache);
        if (receiverCount <= 0) return Math.max(remaining, 0L);

        long perTarget = Math.max(remaining / receiverCount, 1L);
        long rem = remaining;
        for (var entry : newCache.entrySet()) {
            if (rem <= 0) break;
            ServerLevel dimLevel = server.getLevel(entry.getKey());
            if (dimLevel == null) continue;
            for (BlockPos pos : entry.getValue()) {
                if (rem <= 0) return 0L;
                rem = tryInjectEnergyIntoBlock(dimLevel, pos, rem, perTarget);
            }
        }
        return Math.max(rem, 0L);
    }

    public static long broadcastEnergyLocal(MetaMachine machine, long remaining) {
        var lvl = machine.getLevel();
        if (lvl == null) return remaining;
        long rem = remaining;
        long perTarget = Math.max(rem / 100, 1L);
        BlockPos pos = machine.getPos();
        int centerChunkX = pos.getX() >> 4;
        int centerChunkZ = pos.getZ() >> 4;
        int radius = 2;

        for (int dx = -radius; dx <= radius; dx++) {
            if (rem <= 0) break;
            for (int dz = -radius; dz <= radius; dz++) {
                if (rem <= 0) break;
                int cx = centerChunkX + dx;
                int cz = centerChunkZ + dz;
                if (!lvl.hasChunk(cx, cz)) continue;
                var chunkObj = lvl.getChunk(cx, cz);
                if (!(chunkObj instanceof LevelChunk)) continue;
                LevelChunk chunk = (LevelChunk) chunkObj;
                for (BlockPos blockPos : chunk.getBlockEntities().keySet()) {
                    if (rem <= 0) return rem;

                    int chebyshev = Math.max(
                            Math.abs(blockPos.getX() - pos.getX()),
                            Math.max(
                                    Math.abs(blockPos.getY() - pos.getY()),
                                    Math.abs(blockPos.getZ() - pos.getZ())));
                    if (chebyshev > 16) continue;
                    rem = tryInjectEnergyIntoBlock(lvl, blockPos, rem, perTarget);
                }
            }
        }
        return rem;
    }

    public static long tryInjectEnergyIntoBlock(Level lvl, BlockPos pos, long remaining, long amount) {
        if (amount <= 0) return remaining;

        long safeAmount = Math.min(amount, Long.MAX_VALUE / 8);
        long rem = remaining;

        java.util.IdentityHashMap<Object, Boolean> seen = new java.util.IdentityHashMap<>();
        for (Direction side : Direction.values()) {
            if (rem <= 0) break;
            var container = GTCapabilityHelper.getEnergyContainer(lvl, pos, side);
            if (container == null) continue;

            if (seen.put(container, Boolean.TRUE) != null) continue;
            try {
                if (container instanceof NotifiableLaserContainer) {
                    long accepted = ((NotifiableLaserContainer) container).changeEnergy(safeAmount);
                    if (accepted > 0) rem -= accepted;
                } else if (container.inputsEnergy(side)) {
                    long accepted = container.addEnergy(safeAmount);
                    if (accepted > 0) rem -= accepted;
                }
            } catch (Exception ignored) {

            }
        }
        return rem;
    }

    private static boolean hasEnergyReceiver(Level lvl, BlockPos pos) {
        java.util.IdentityHashMap<Object, Boolean> seen = new java.util.IdentityHashMap<>();
        for (Direction side : Direction.values()) {
            var container = GTCapabilityHelper.getEnergyContainer(lvl, pos, side);
            if (container == null) continue;
            if (seen.put(container, Boolean.TRUE) != null) continue;
            if (container instanceof NotifiableLaserContainer) return true;
            if (container.inputsEnergy(side)) return true;
        }
        return false;
    }

    private static boolean isFtbChunksAvailable() {
        try {
            return FTBChunksAPI.api() != null && FTBChunksAPI.api().isManagerLoaded();
        } catch (Throwable t) {
            return false;
        }
    }

    public static Map<ResourceKey<Level>, Long2ObjectMap<UUID>> getAllForceLoadedChunksFresh() {
        Map<ResourceKey<Level>, Long2ObjectMap<UUID>> result = new HashMap<>();
        try {
            var api = FTBChunksAPI.api();
            if (api == null || !api.isManagerLoaded()) return result;
            var manager = api.getManager();
            if (manager == null) return result;
            for (var chunk : manager.getAllClaimedChunks()) {
                if (!chunk.isForceLoaded()) continue;
                var pos = chunk.getPos();
                ResourceKey<Level> dim = pos.dimension();
                Long2ObjectMap<UUID> dimMap = result.computeIfAbsent(dim, k -> new Long2ObjectOpenHashMap<>());
                long chunkKey = ChunkPos.asLong(pos.x(), pos.z());
                dimMap.put(chunkKey, chunk.getTeamData().getTeam().getId());
            }
        } catch (Throwable t) {

        }
        return result;
    }
}
