package com.invasion.entity.ai.goal;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.EntityIMFlying;
import com.invasion.entity.HasAiGoals;
import com.invasion.entity.pathfinding.FlightNavigator;
import com.invasion.entity.pathfinding.Path;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;

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
        FlightNavigator nav = (FlightNavigator)theEntity.getNavigatorNew();
        Entity target = theEntity.getTarget();
        if (target != nav.getTargetEntity()) {
            nav.stop();
            nav.setMovementType(FlightNavigator.MoveType.PREFER_WALKING);
            @Nullable
            Path path = nav.getPathToEntity(target, 0);
            if (path != null && path.getCurrentPathLength() > 2 * theEntity.distanceTo(target)) {
                nav.setMovementType(FlightNavigator.MoveType.MIXED);
            }
            nav.autoPathToEntity(target);
        }
    }
}