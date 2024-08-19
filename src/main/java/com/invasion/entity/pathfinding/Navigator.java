package com.invasion.entity.pathfinding;

import org.jetbrains.annotations.Nullable;

import com.invasion.Notifiable;
import com.invasion.entity.HasAiGoals;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public interface Navigator extends Notifiable, HasAiGoals {

    Actor<?> getActor();

    PathAction getCurrentWorkingAction();

    void setSpeed(float speed);

    Path getPathToXYZ(Vec3d pos, float paramFloat);

    boolean startMovingTo(Vec3d pos, float targetRadius, float speed);

    Path getPathTowardsXZ(double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, int paramInt3);

    boolean tryMoveTowardsXZ(double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, int paramInt3, float paramFloat);

    @Nullable
    Path getPathToEntity(Entity paramEntity, float paramFloat);

    boolean startMovingTo(Entity paramEntity, float paramFloat1, float paramFloat2);

    void autoPathToEntity(Entity paramEntity);

    boolean setPath(Path paramPath, float paramFloat);

    boolean isWaitingForTask();

    Path getPath();

    void tick();

    Status getLastActionResult();

    boolean isIdle();

    Vec3d getPos();

    int getStuckTime();

    float getLastPathDistanceToTarget();

    void stop();

    void haltForTick();

    Entity getTargetEntity();
}