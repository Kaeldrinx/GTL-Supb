package com.gtlsupb.gtlsupb.integration.ae2;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.MEStorage;
import com.hepdd.gtmthings.utils.TeamUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AEGridRegistry {

    private static final ConcurrentHashMap<IGrid, Boolean> ACTIVE_GRIDS = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<IGrid, Set<UUID>> GRID_OWNERS = new ConcurrentHashMap<>();

    public static void register(IGrid grid) {
        if (grid != null) {
            ACTIVE_GRIDS.put(grid, true);
        }
    }

    public static void recordOwner(IGrid grid, UUID owner) {
        if (grid == null || owner == null) return;
        GRID_OWNERS.computeIfAbsent(grid, k -> ConcurrentHashMap.newKeySet()).add(owner);
    }

    public static void unregister(IGrid grid) {
        if (grid != null) {
            ACTIVE_GRIDS.remove(grid);
            GRID_OWNERS.remove(grid);
        }
    }

    public static List<IGrid> getActiveGrids() {
        List<IGrid> valid = new ArrayList<>();
        var iterator = ACTIVE_GRIDS.keySet().iterator();
        while (iterator.hasNext()) {
            IGrid grid = iterator.next();
            try {
                if (grid.isEmpty()) {
                    iterator.remove();
                    GRID_OWNERS.remove(grid);
                    continue;
                }
                valid.add(grid);
            } catch (Exception e) {
                iterator.remove();
                GRID_OWNERS.remove(grid);
            }
        }
        return valid;
    }

    private static Set<UUID> recordOwnersFromNodes(IGrid grid) {
        try {
            for (IGridNode node : grid.getNodes()) {
                if (node == null) continue;
                try {
                    UUID owner = node.getOwningPlayerProfileId();
                    if (owner != null) {
                        recordOwner(grid, owner);
                    }
                } catch (Throwable ignored) {

                }
            }
        } catch (Throwable ignored) {

        }
        return GRID_OWNERS.get(grid);
    }

    public static List<IGrid> getActiveGridsForPlayer(UUID userUuid) {
        if (userUuid == null) return List.of();
        UUID teamUuid;
        try {
            teamUuid = TeamUtil.getTeamUUID(userUuid);
        } catch (Throwable t) {
            teamUuid = userUuid;
        }
        List<IGrid> result = new ArrayList<>();
        for (IGrid grid : getActiveGrids()) {
            Set<UUID> owners = GRID_OWNERS.get(grid);
            if (owners == null || owners.isEmpty()) {
                owners = recordOwnersFromNodes(grid);
            }
            if (owners == null || owners.isEmpty()) continue;
            boolean sameTeam = false;
            for (UUID owner : owners) {
                UUID ownerTeam;
                try {
                    ownerTeam = TeamUtil.getTeamUUID(owner);
                } catch (Throwable t) {
                    ownerTeam = owner;
                }
                if (teamUuid.equals(ownerTeam)) {
                    sameTeam = true;
                    break;
                }
            }
            if (sameTeam) {
                result.add(grid);
            }
        }
        return result;
    }

    public static MEStorage getFirstAvailableStorageForPlayer(UUID userUuid) {
        for (IGrid grid : getActiveGridsForPlayer(userUuid)) {
            try {
                IStorageService storageService = grid.getStorageService();
                if (storageService != null) {
                    MEStorage storage = storageService.getInventory();
                    if (storage != null) {
                        return storage;
                    }
                }
            } catch (Exception ignored) {

            }
        }
        return null;
    }

    public static MEStorage getFirstAvailableStorage() {
        var iterator = ACTIVE_GRIDS.keySet().iterator();
        while (iterator.hasNext()) {
            IGrid grid = iterator.next();
            try {
                if (grid.isEmpty()) {
                    iterator.remove();
                    continue;
                }
                IStorageService storageService = grid.getStorageService();
                if (storageService != null) {
                    MEStorage storage = storageService.getInventory();
                    if (storage != null) {
                        return storage;
                    }
                }
            } catch (Exception e) {
                iterator.remove();
            }
        }
        return null;
    }

    public static boolean hasActiveGrid() {
        var iterator = ACTIVE_GRIDS.keySet().iterator();
        while (iterator.hasNext()) {
            IGrid grid = iterator.next();
            try {
                if (grid.isEmpty()) {
                    iterator.remove();
                } else {
                    return true;
                }
            } catch (Exception e) {
                iterator.remove();
            }
        }
        return false;
    }

    public static void clear() {
        ACTIVE_GRIDS.clear();
        GRID_OWNERS.clear();
    }
}
