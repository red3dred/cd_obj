package invmod.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public interface Distance {
    static double distanceBetween(IPosition pos1, IPosition pos2) {
        return Math.sqrt(pos1.squareDistanceTo(pos2));
    }

    static double distanceBetween(IPosition pos1, Vec3d pos2) {
        return Math.sqrt(pos2.squaredDistanceTo(pos1.getXCoord(), pos1.getYCoord(), pos1.getZCoord()));
    }

    static double distanceBetween(IPosition pos1, double x2, double y2, double z2) {
        double dX = x2 - pos1.getXCoord();
        double dY = y2 - pos1.getYCoord();
        double dZ = z2 - pos1.getZCoord();
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    static double distanceBetween(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    static double distanceBetween(Entity entity, Vec3d pos2) {
        return Math.sqrt(entity.squaredDistanceTo(pos2));
    }
}