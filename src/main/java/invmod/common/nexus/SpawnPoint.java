package invmod.common.nexus;

import invmod.common.util.IPolarAngle;
import invmod.common.util.IPosition;
import net.minecraft.util.math.BlockPos;

public record SpawnPoint(BlockPos pos, int angle, SpawnType type)
        implements IPosition, IPolarAngle, Comparable<IPolarAngle> {
    @Override
    public int getXCoord() {
        return pos.getX();
    }

    @Override
    public int getYCoord() {
        return pos.getY();
    }

    @Override
    public int getZCoord() {
        return pos.getZ();
    }

    @Override
    public int getAngle() {
        return this.angle;
    }

    @Override
    public int compareTo(IPolarAngle polarAngle) {
        if (this.angle < polarAngle.getAngle()) {
            return -1;
        }
        if (this.angle > polarAngle.getAngle()) {
            return 1;
        }

        return 0;
    }
}