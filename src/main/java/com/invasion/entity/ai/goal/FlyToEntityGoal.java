package com.invasion.entity.ai.goal;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.EntityIMFlying;
import com.invasion.entity.HasAiGoals;
import com.invasion.entity.pathfinding.FlyingNavigation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.MathHelper;

public class FlyToEntityGoal extends Goal {
    private final EntityIMFlying theEntity;

    public FlyToEntityGoal(EntityIMFlying entity) {
        theEntity = entity;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return theEntity.hasGoal(HasAiGoals.Goal.GOTO_ENTITY) && theEntity.getTarget() != null;
    }

    @Override
    public void start() {
        FlyingNavigation nav = (FlyingNavigation)theEntity.getNavigatorNew();
        Entity target = theEntity.getTarget();
        if (target != theEntity.getNavigatorNew().getTargetEntity()) {
            nav.stop();
            nav.setMovementType(FlyingNavigation.MoveType.PREFER_WALKING);
            @Nullable
            Path path = theEntity.getNavigation().findPathTo(target, MathHelper.ceil(2 * theEntity.distanceTo(target)));
            if (path != null && path.getLength() > 2 * theEntity.distanceTo(target)) {
                nav.setMovementType(FlyingNavigation.MoveType.MIXED);
            }
            theEntity.getNavigatorNew().autoPathToEntity(target);
        }
    }
}