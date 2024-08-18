package com.invasion.entity.ai.goal;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import com.invasion.InvasionMod;
import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.pathfinding.Path;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

public class EntityAIWanderIM extends Goal {
    private static final int MIN_HORIZONTAL_PATH = 1;
    private static final int MAX_HORIZONTAL_PATH = 6;
    private static final int MAX_VERTICAL_PATH = 4;

    private final EntityIMLiving mob;

    public EntityAIWanderIM(EntityIMLiving mob) {
        this.mob = mob;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (mob.getRandom().nextInt(toGoalTicks(120)) == 0) {
            double x = mob.getX() + mob.getRandom().nextInt(13) - MAX_HORIZONTAL_PATH;
            double z = mob.getZ() + mob.getRandom().nextInt(13) - MAX_HORIZONTAL_PATH;
            Path path = mob.getNavigatorNew().getPathTowardsXZ(x, z, MIN_HORIZONTAL_PATH, MAX_HORIZONTAL_PATH, MAX_VERTICAL_PATH);
            if (path != null) {
                mob.getNavigatorNew().setPath(path, 1);
                return true;
            } else {
                @Nullable
                Vec3d randomTarget = NoPenaltyTargeting.find(this.mob, 10, 7);
                if (randomTarget != null) {
                    return mob.getNavigatorNew().tryMoveToXYZ(randomTarget, 10, 1);
                }
                InvasionMod.LOGGER.info("Failed random path");
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        return !mob.getNavigatorNew().noPath() && mob.getNavigatorNew().getStuckTime() < 40;
    }
}