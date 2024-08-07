package invmod.common.util;

import net.minecraft.util.math.BlockPos;

public record CoordsInt(int x, int y, int z) implements IPosition {
    public static final int[] offsetAdjX = { 1, -1, 0, 0 };
    public static final int[] offsetAdjZ = { 0, 0, 1, -1 };

    public static final int[] offsetAdj2X = { 2, 2, -1, -1, 1, 0, 0, 1 };
    public static final int[] offsetAdj2Z = { 0, 1, 1, 0, 2, 2, -1, -1 };

    public static final int[] offsetRing1X = { 1, 0, -1, -1, -1, 0, 1, 1 };
    public static final int[] offsetRing1Z = { 1, 1, 1, 0, -1, -1, -1, 0 };

    public CoordsInt(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getXCoord() {
        return this.x;
    }

    @Override
    public int getYCoord() {
        return this.y;
    }

    @Override
    public int getZCoord() {
        return this.z;
    }
}