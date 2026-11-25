package com.gtlsupb.gtlsupb.integration.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.CableBusContainer;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitQuantumEntanglementHatchPartMachine;
import com.hepdd.gtmthings.utils.TeamUtil;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class OverlimitQuantumEntanglementWirelessManager {

    private static final ConcurrentHashMap<GlobalPos, HatchState> REGISTRY = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<GlobalPos, Set<GlobalPos>> CANCELLED_CONNECTIONS = new ConcurrentHashMap<>();

    private static final long SCAN_INTERVAL = 40L;
    private static long tickCounter = 0L;

    private OverlimitQuantumEntanglementWirelessManager() {}

    private static final class HatchState {

        final WeakReference<OverlimitQuantumEntanglementHatchPartMachine> ref;

        final Map<GlobalPos, IGridConnection> connections = new ConcurrentHashMap<>();

        HatchState(OverlimitQuantumEntanglementHatchPartMachine hatch) {
            this.ref = new WeakReference<>(hatch);
        }
    }

    public static void onNodeStateChanged(OverlimitQuantumEntanglementHatchPartMachine hatch) {
        Level level = hatch.getLevel();
        if (level == null || level.isClientSide()) return;
        if (hatch.getMainNode().isActive()) {
            register(hatch);
        }
    }

    private static void register(OverlimitQuantumEntanglementHatchPartMachine hatch) {
        GlobalPos gpos = GlobalPos.of(hatch.getLevel().dimension(), hatch.getPos());
        REGISTRY.computeIfAbsent(gpos, k -> new HatchState(hatch));
        Set<GlobalPos> cancelled = CANCELLED_CONNECTIONS.get(gpos);
        if (cancelled != null && !cancelled.isEmpty()) hatch.gtlsupb$writeCancelled(cancelled);
    }

    public static void unregister(OverlimitQuantumEntanglementHatchPartMachine hatch) {
        Level level = hatch.getLevel();
        if (level == null) return;
        GlobalPos gpos = GlobalPos.of(level.dimension(), hatch.getPos());
        HatchState state = REGISTRY.remove(gpos);
        if (state != null) destroyAllConnections(state);
    }

    public static int getConnectionCount(OverlimitQuantumEntanglementHatchPartMachine hatch) {
        Level level = hatch.getLevel();
        if (level == null) return 0;
        HatchState state = REGISTRY.get(GlobalPos.of(level.dimension(), hatch.getPos()));
        return state == null ? 0 : state.connections.size();
    }

    public static boolean isBlacklisted(GlobalPos hatchGpos, GlobalPos targetGpos) {
        Set<GlobalPos> cancelled = CANCELLED_CONNECTIONS.get(hatchGpos);
        return cancelled != null && cancelled.contains(targetGpos);
    }

    public static Set<GlobalPos> getBlacklist(GlobalPos hatchGpos) {
        Set<GlobalPos> cancelled = CANCELLED_CONNECTIONS.get(hatchGpos);
        return cancelled == null ? Set.of() : Set.copyOf(cancelled);
    }

    public static void blacklist(GlobalPos hatchGpos, GlobalPos targetGpos) {
        if (hatchGpos == null || targetGpos == null) return;
        CANCELLED_CONNECTIONS.computeIfAbsent(hatchGpos, k -> ConcurrentHashMap.newKeySet()).add(targetGpos);
        destroyConnection(hatchGpos, targetGpos);
        persist(hatchGpos);
    }

    public static void restoreBlacklist(OverlimitQuantumEntanglementHatchPartMachine hatch,
                                        net.minecraft.nbt.ListTag list) {
        if (hatch.getLevel() == null) return;
        GlobalPos hatchGpos = GlobalPos.of(hatch.getLevel().dimension(), hatch.getPos());
        if (list == null || list.isEmpty()) {
            CANCELLED_CONNECTIONS.remove(hatchGpos);
            return;
        }
        Set<GlobalPos> targets = ConcurrentHashMap.newKeySet();
        for (int i = 0; i < list.size(); i++) {
            net.minecraft.nbt.CompoundTag tag = list.getCompound(i);
            String dim = tag.getString("dim");
            if (dim.isEmpty()) continue;
            targets.add(GlobalPos.of(
                    net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                            new net.minecraft.resources.ResourceLocation(dim)),
                    new net.minecraft.core.BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"))));
        }
        if (targets.isEmpty()) {
            CANCELLED_CONNECTIONS.remove(hatchGpos);
        } else {
            CANCELLED_CONNECTIONS.put(hatchGpos, targets);
        }
    }

    private static void persist(GlobalPos hatchGpos) {
        HatchState state = REGISTRY.get(hatchGpos);
        OverlimitQuantumEntanglementHatchPartMachine hatch = state == null ? null : state.ref.get();
        if (hatch == null) return;
        hatch.gtlsupb$writeCancelled(getBlacklist(hatchGpos));
    }

    public static void unblacklist(GlobalPos hatchGpos, GlobalPos targetGpos) {
        if (hatchGpos == null || targetGpos == null) return;
        Set<GlobalPos> cancelled = CANCELLED_CONNECTIONS.get(hatchGpos);
        if (cancelled != null) {
            cancelled.remove(targetGpos);
            if (cancelled.isEmpty()) CANCELLED_CONNECTIONS.remove(hatchGpos);
        }
        persist(hatchGpos);
    }

    public static boolean destroyConnection(GlobalPos hatchGpos, GlobalPos targetGpos) {
        CANCELLED_CONNECTIONS.computeIfAbsent(hatchGpos, k -> ConcurrentHashMap.newKeySet()).add(targetGpos);
        HatchState state = REGISTRY.get(hatchGpos);
        if (state == null) return false;
        IGridConnection conn = state.connections.remove(targetGpos);
        if (conn != null) {
            safeDestroy(conn);
            return true;
        }
        return false;
    }

    private static Map<ResourceKey<Level>, Long2ObjectMap<UUID>> getAllForceLoadedChunks() {
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
        } catch (Throwable ignored) {

        }
        return result;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        if (tickCounter % SCAN_INTERVAL != 0) return;

        for (Map.Entry<GlobalPos, HatchState> entry : REGISTRY.entrySet()) {
            HatchState state = entry.getValue();
            OverlimitQuantumEntanglementHatchPartMachine hatch = state.ref.get();
            Level lvl = hatch == null ? null : hatch.getLevel();
            if (hatch == null || lvl == null || lvl.isClientSide()) {
                REGISTRY.remove(entry.getKey());
                destroyAllConnections(state);
                continue;
            }
            try {
                scanAndLink(hatch, state);
            } catch (Throwable ignored) {

            }
        }
    }

    private static void scanAndLink(OverlimitQuantumEntanglementHatchPartMachine hatch, HatchState state) {
        Level level = hatch.getLevel();
        if (level == null || level.isClientSide()) return;

        GlobalPos hatchGpos = GlobalPos.of(level.dimension(), hatch.getPos());

        if (!hatch.isWirelessEnabled()) {
            destroyAllConnections(state);
            return;
        }

        final boolean skipCables = hatch.isSkipCables();

        IGridNode selfNode = hatch.getMainNode().getNode();
        if (selfNode == null || !hatch.getMainNode().isActive()) {
            destroyAllConnections(state);
            return;
        }
        IGrid selfGrid = selfNode.getGrid();
        List<IGridConnection> liveConns = selfNode.getConnections();

        MinecraftServer server = level.getServer();
        if (server == null) {
            destroyAllConnections(state);
            return;
        }

        UUID ownerTeam;
        try {
            ownerTeam = TeamUtil.getTeamUUID(hatch.getOwnerUuid());
        } catch (Throwable t) {
            ownerTeam = null;
        }
        if (ownerTeam == null) {
            destroyAllConnections(state);
            return;
        }

        Set<GlobalPos> validTargets = new HashSet<>();
        Map<GlobalPos, IInWorldGridNodeHost> hostCache = new HashMap<>();

        Map<ResourceKey<Level>, Long2ObjectMap<UUID>> allForceLoaded = getAllForceLoadedChunks();

        for (ServerLevel dimLevel : server.getAllLevels()) {
            ResourceKey<Level> dimKey = dimLevel.dimension();
            Long2ObjectMap<UUID> forceLoaded = allForceLoaded.get(dimKey);
            if (forceLoaded == null || forceLoaded.isEmpty()) continue;

            for (long chunkKey : forceLoaded.keySet()) {
                UUID chunkTeam = forceLoaded.get(chunkKey);
                if (chunkTeam == null || !chunkTeam.equals(ownerTeam)) continue;

                int cx = ChunkPos.getX(chunkKey);
                int cz = ChunkPos.getZ(chunkKey);
                if (!dimLevel.hasChunk(cx, cz)) continue;
                LevelChunk chunk = dimLevel.getChunk(cx, cz);
                for (Map.Entry<BlockPos, BlockEntity> beEntry : chunk.getBlockEntities().entrySet()) {
                    BlockPos bePos = beEntry.getKey().immutable();
                    GlobalPos beGpos = GlobalPos.of(dimKey, bePos);
                    if (beGpos.equals(hatchGpos)) continue;
                    if (isBlacklisted(hatchGpos, beGpos)) continue;

                    IInWorldGridNodeHost host = appeng.api.networking.GridHelper.getNodeHost(dimLevel, bePos);
                    if (host == null || shouldSkipTarget(host, skipCables)) continue;

                    hostCache.put(beGpos, host);
                    collectTarget(host, beGpos, selfNode, selfGrid, state, validTargets);
                }
            }
        }

        var iter = state.connections.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<GlobalPos, IGridConnection> connEntry = iter.next();
            GlobalPos targetGpos = connEntry.getKey();
            IGridConnection conn = connEntry.getValue();
            if (!validTargets.contains(targetGpos)) {
                safeDestroy(conn);
                iter.remove();
            } else if (!liveConns.contains(conn)) {
                iter.remove();
            }
        }

        for (GlobalPos targetGpos : validTargets) {
            if (isBlacklisted(hatchGpos, targetGpos)) continue;
            if (state.connections.containsKey(targetGpos)) continue;
            IInWorldGridNodeHost host = hostCache.get(targetGpos);
            if (host == null) continue;
            for (Direction dir : Direction.values()) {
                IGridNode targetNode = host.getGridNode(dir);
                if (targetNode == null || targetNode == selfNode) continue;
                if (targetNode.getGrid() == selfGrid) continue;
                try {
                    IGridConnection conn = appeng.api.networking.GridHelper.createConnection(selfNode, targetNode);
                    state.connections.put(targetGpos, conn);
                } catch (Throwable ignored) {

                }
                break;
            }
        }
    }

    private static boolean shouldSkipTarget(IInWorldGridNodeHost host, boolean skipCables) {
        if (!skipCables) {
            return false;
        }
        if (host instanceof CableBusBlockEntity bus) {
            CableBusContainer cableBus = bus.getCableBus();
            if (cableBus != null) {
                for (Direction dir : Direction.values()) {
                    if (cableBus.getPart(dir) != null) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private static void collectTarget(IInWorldGridNodeHost host, GlobalPos targetGpos, IGridNode selfNode,
                                      IGrid selfGrid, HatchState state, Set<GlobalPos> validTargets) {
        boolean hasCandidate = false;
        boolean hasSameGridNode = false;
        for (Direction dir : Direction.values()) {
            IGridNode targetNode = host.getGridNode(dir);
            if (targetNode == null || targetNode == selfNode) continue;
            if (targetNode.getGrid() == selfGrid) {
                hasSameGridNode = true;
            } else {
                hasCandidate = true;
            }
        }
        if (hasCandidate) {
            validTargets.add(targetGpos);
        } else if (hasSameGridNode && state.connections.containsKey(targetGpos)) {
            validTargets.add(targetGpos);
        }
    }

    private static void destroyAllConnections(HatchState state) {
        List<IGridConnection> snapshot = new ArrayList<>(state.connections.values());
        state.connections.clear();
        for (IGridConnection conn : snapshot) {
            safeDestroy(conn);
        }
    }

    private static void safeDestroy(IGridConnection conn) {
        try {
            conn.destroy();
        } catch (Throwable ignored) {

        }
    }
}
