package com.invasion.entity.pathfinding;

import com.invasion.Notifiable;
import com.invasion.entity.HasAiGoals;
import com.invasion.entity.pathfinding.path.PathAction;

import net.minecraft.entity.Entity;

public interface Navigation extends Notifiable, HasAiGoals {
    @Deprecated
    Actor<?> getActor();

    PathAction getCurrentWorkingAction();

    void autoPathToEntity(Entity target);

    boolean isWaitingForTask();

    Status getLastActionResult();

    boolean isIdle();

    int getStuckTime();

    float getLastPathDistanceToTarget();

    void haltForTick();

    Entity getTargetEntity();
}