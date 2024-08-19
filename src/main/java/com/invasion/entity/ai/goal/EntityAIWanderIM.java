package com.invasion.entity.ai.goal;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import com.invasion.InvasionMod;
import com.invasion.entity.NexusEntity;
import com.invasion.entity.pathfinding.Path;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;

@Deprecated
public class EntityAIWanderIM extends Goal {
    private static final int MIN_HORIZONTAL_PATH = 1;
    private static final int MAX_HORIZONTAL_PATH = 6;
    private static final int MAX_VERTICAL_PATH = 4;

    private final PathAwareEntity mob;
    private final NexusEntity nexusMob;

    public <T extends PathAwareEntity & NexusEntity> EntityAIWanderIM(T mob) {
        this.mob = mob;
        this.nexusMob = mob;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (mob.getWorld().getRandom().nextInt(toGoalTicks(120)) == 0) {
            double x = mob.getX() + mob.getWorld().getRandom().nextInt(13) - MAX_HORIZONTAL_PATH;
            double z = mob.getZ() + mob.getWorld().getRandom().nextInt(13) - MAX_HORIZONTAL_PATH;
            Path path = nexusMob.getNavigatorNew().getPathTowardsXZ(x, z, MIN_HORIZONTAL_PATH, MAX_HORIZONTAL_PATH, MAX_VERTICAL_PATH);
            if (path != null) {
                nexusMob.getNavigatorNew().setPath(path, 1);
                return true;
            } else {
                @Nullable
                Vec3d randomTarget = NoPenaltyTargeting.find(this.mob, 10, 7);
                if (randomTarget != null) {
                    return nexusMob.getNavigatorNew().tryMoveToXYZ(randomTarget, 10, 1);
                }
                InvasionMod.LOGGER.info("Failed random path");
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        return !nexusMob.getNavigatorNew().noPath() && nexusMob.getNavigatorNew().getStuckTime() < 40;
    }
}