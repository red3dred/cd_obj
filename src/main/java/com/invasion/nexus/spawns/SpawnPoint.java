package com.invasion.nexus.spawns;

import com.invasion.util.math.IPolarAngle;
import com.invasion.util.math.IPosition;

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