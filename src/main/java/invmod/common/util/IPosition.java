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
}