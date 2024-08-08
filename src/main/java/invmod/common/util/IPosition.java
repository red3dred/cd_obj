package invmod.common.util;

import net.minecraft.util.math.BlockPos;

public interface IPosition {
    int getXCoord();

    int getYCoord();

    int getZCoord();

    default boolean columnEquals(IPosition position) {
        return getXCoord() == position.getXCoord() && getZCoord() == position.getZCoord();
    }

    default BlockPos toBlockPos() {
        return new BlockPos(getXCoord(), getYCoord(), getZCoord());
    }

    default double squareDistanceTo(IPosition other) {
        return Math.pow(getXCoord() - other.getXCoord(), 2)
            + Math.pow(getYCoord() - other.getYCoord(), 2)
            + Math.pow(getZCoord() - other.getZCoord(), 2);
    }

    default double getInclinationTo(BlockPos pos) {
        BlockPos delta = toBlockPos().subtract(pos);
        if (delta.getY() <= 0) {
            return 0;
        }
        int dX = delta.getX();
        int dZ = delta.getZ();
        return (delta.getY() + 8) / (Math.sqrt(dX * dX + dZ * dZ) + 1.E-005D);
    }
}