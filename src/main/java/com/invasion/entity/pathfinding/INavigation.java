package com.invasion.entity.pathfinding;

import org.jetbrains.annotations.Nullable;

import com.invasion.INotifyTask;
import com.invasion.entity.IHasAiGoals;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public interface INavigation extends INotifyTask, IHasAiGoals {

    Actor<?> getActor();

    PathAction getCurrentWorkingAction();

    void setSpeed(float speed);

    Path getPathToXYZ(Vec3d pos, float paramFloat);

    boolean tryMoveToXYZ(Vec3d pos, float targetRadius, float speed);

    Path getPathTowardsXZ(double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, int paramInt3);

    boolean tryMoveTowardsXZ(double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, int paramInt3, float paramFloat);

    @Nullable
    Path getPathToEntity(Entity paramEntity, float paramFloat);

    boolean tryMoveToEntity(Entity paramEntity, float paramFloat1, float paramFloat2);

    void autoPathToEntity(Entity paramEntity);

    boolean setPath(Path paramPath, float paramFloat);

    boolean isWaitingForTask();

    Path getPath();

    void onUpdateNavigation();

    Status getLastActionResult();

    boolean noPath();

    int getStuckTime();

    float getLastPathDistanceToTarget();

    void clearPath();

    void haltForTick();

    Entity getTargetEntity();

    String getStatus();
}