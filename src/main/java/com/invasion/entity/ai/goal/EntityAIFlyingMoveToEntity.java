package com.invasion.entity.ai.goal;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.EntityIMFlying;
import com.invasion.entity.ai.Goal;
import com.invasion.entity.pathfinding.INavigationFlying;
import com.invasion.entity.pathfinding.Path;

import net.minecraft.entity.Entity;

public class EntityAIFlyingMoveToEntity extends net.minecraft.entity.ai.goal.Goal {
    private final EntityIMFlying theEntity;

    public EntityAIFlyingMoveToEntity(EntityIMFlying entity) {
        theEntity = entity;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return theEntity.getAIGoal() == Goal.GOTO_ENTITY && theEntity.getTarget() != null;
    }

    @Override
    public void start() {
        INavigationFlying nav = (INavigationFlying)theEntity.getNavigatorNew();
        Entity target = theEntity.getTarget();
        if (target != nav.getTargetEntity()) {
            nav.clearPath();
            nav.setMovementType(INavigationFlying.MoveType.PREFER_WALKING);
            @Nullable
            Path path = nav.getPathToEntity(target, 0);
            if (path != null && path.getCurrentPathLength() > 2 * theEntity.distanceTo(target)) {
                nav.setMovementType(INavigationFlying.MoveType.MIXED);
            }
            nav.autoPathToEntity(target);
        }
    }
}