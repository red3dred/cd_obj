package com.invasion.entity.ai.goal;

import java.util.EnumSet;

import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.pathfinding.Path;

import net.minecraft.entity.ai.goal.Goal;

public class EntityAIWanderIM extends Goal {
    private static final int MIN_HORIZONTAL_PATH = 1;
    private static final int MAX_HORIZONTAL_PATH = 6;
    private static final int MAX_VERTICAL_PATH = 4;

    private final EntityIMLiving mob;

    public EntityAIWanderIM(EntityIMLiving mob) {
        this.mob = mob;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (mob.getRandom().nextInt(120) == 0) {
            double x = mob.getX() + mob.getRandom().nextInt(13) - MAX_HORIZONTAL_PATH;
            double z = mob.getZ() + mob.getRandom().nextInt(13) - MAX_HORIZONTAL_PATH;
            Path path = mob.getNavigatorNew().getPathTowardsXZ(x, z, MIN_HORIZONTAL_PATH, MAX_HORIZONTAL_PATH, MAX_VERTICAL_PATH);
            if (path != null) {
                mob.getNavigatorNew().setPath(path, mob.getMovementSpeed());
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        return !mob.getNavigatorNew().noPath() && mob.getNavigatorNew().getStuckTime() < 40;
    }
}