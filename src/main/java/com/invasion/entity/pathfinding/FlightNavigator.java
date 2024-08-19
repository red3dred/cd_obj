package com.invasion.entity.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public interface FlightNavigator extends Navigator {
    void setMovementType(MoveType paramMoveType);

    void setLandingPath();

    default void setCirclingPath(Entity target, float preferredHeight, float preferredRadius) {
        setCirclingPath(target.getPos(), preferredHeight, preferredRadius);
    }

    void setCirclingPath(Vec3d position, float paramFloat1, float paramFloat2);

    float getDistanceToCirclingRadius();

    boolean isCircling();

    void setFlySpeed(float paramFloat);

    void setPitchBias(float paramFloat1, float paramFloat2);

    void enableDirectTarget(boolean paramBoolean);

    public enum MoveType {
        PREFER_WALKING, MIXED, PREFER_FLYING;
    }
}