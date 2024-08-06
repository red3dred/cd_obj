package invmod.common.nexus;

import invmod.common.util.IPolarAngle;
import invmod.common.util.IPosition;

public record SpawnPoint(int x, int y, int z, int angle, SpawnType type)
        implements IPosition, IPolarAngle, Comparable<IPolarAngle> {
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