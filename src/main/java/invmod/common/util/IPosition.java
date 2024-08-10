package invmod.common.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

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
        return MathHelper.square(getXCoord() - other.getXCoord())
            + MathHelper.square(getYCoord() - other.getYCoord())
            + MathHelper.square(getZCoord() - other.getZCoord());
    }

    default double getInclinationTo(BlockPos pos) {
        BlockPos delta = toBlockPos().subtract(pos);
        if (delta.getY() <= 0) {
            return 0;
        }
        return (delta.getY() + 8) / (Math.sqrt(MathHelper.square(delta.getX()) + MathHelper.square(delta.getZ())) + MathHelper.EPSILON);
    }
}