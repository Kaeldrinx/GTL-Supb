package com.gtlsupb.gtlsupb.common.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class PlayerBindingHelper {

    private PlayerBindingHelper() {}

    public static Player findNearestPlayer(Level level, BlockPos pos, double radius) {
        if (level == null) {
            return null;
        }
        double best = radius * radius;
        Player nearest = null;
        double x = pos.getX() + 0.5d;
        double y = pos.getY() + 0.5d;
        double z = pos.getZ() + 0.5d;
        for (Player player : level.players()) {
            double d = player.distanceToSqr(x, y, z);
            if (d < best) {
                best = d;
                nearest = player;
            }
        }
        return nearest;
    }
}
