package com.invasion.util.math;

import java.util.Comparator;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public interface ComparatorDistanceFrom {
    static Comparator<Entity> ofComparisonEntities(IPosition pos) {
        return ofComparisonEntities(new Vec3d(pos.getXCoord(), pos.getYCoord(), pos.getZCoord()));
    }

    static Comparator<Entity> ofComparisonEntities(double x, double y, double z) {
        return ofComparisonEntities(new Vec3d(x, y, z));
    }

    private static Comparator<Entity> ofComparisonEntities(Vec3d origin) {
        return Comparator.comparingDouble(e -> e.squaredDistanceTo(origin));
    }
}