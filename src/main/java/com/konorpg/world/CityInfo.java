package com.konorpg.world;

import net.minecraft.core.BlockPos;

/** Cached city bounds so natural monster spawning can avoid the city. */
public final class CityInfo {
    private static volatile BlockPos center;

    public static void set(BlockPos c) { center = c; }

    public static boolean inCity(BlockPos p) {
        BlockPos c = center;
        return c != null && Math.abs(p.getX() - c.getX()) <= 46 && Math.abs(p.getZ() - c.getZ()) <= 46;
    }

    private CityInfo() {}
}
