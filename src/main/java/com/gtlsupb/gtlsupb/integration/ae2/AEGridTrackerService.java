package com.gtlsupb.gtlsupb.integration.ae2;

import net.minecraft.nbt.CompoundTag;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;

public class AEGridTrackerService implements IGridServiceProvider {

    public AEGridTrackerService() {}

    @Override
    public void addNode(IGridNode node, CompoundTag tag) {
        if (node == null) return;
        try {
            IGrid grid = node.getGrid();

            if (grid != null) {
                AEGridRegistry.register(grid);

                try {
                    AEGridRegistry.recordOwner(grid, node.getOwningPlayerProfileId());
                } catch (Throwable ignored) {

                }
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    public void removeNode(IGridNode node) {
        if (node == null) return;
        try {
            IGrid grid = node.getGrid();

            if (grid != null && grid.isEmpty()) {
                AEGridRegistry.unregister(grid);
            }
        } catch (Exception ignored) {

        }
    }
}
