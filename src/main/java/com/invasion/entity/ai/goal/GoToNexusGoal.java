package com.invasion.entity.ai.goal;

import java.util.EnumSet;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.IMMobEntity;
import com.invasion.entity.HasAiGoals;
import com.invasion.entity.pathfinding.Navigation;
import com.invasion.nexus.NexusAccess;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class GoToNexusGoal extends Goal {
    private IMMobEntity mob;
    private Optional<BlockPos> lastPathRequestPos = Optional.empty();
    private final Navigation navigation;
    private int pathRequestTimer;
    private int pathFailedCount;

    public GoToNexusGoal(IMMobEntity entity) {
        this.mob = entity;
        this.navigation = mob.getNavigatorNew();
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public boolean canStart() {
        return mob.hasGoal(HasAiGoals.Goal.BREAK_NEXUS) && mob.hasNexus();
    }

    @Override
    public void start() {
        boolean pathSet = false;
        double distance = mob.findDistanceToNexus();

        if (--pathRequestTimer <= 0) {
            if (distance > 1.5) {
                NexusAccess nexus = mob.getNexus();
                BlockPos target = nexus.getOrigin();

                for (Direction i : Direction.Type.HORIZONTAL) {
                    if (mob.getWorld().getBlockState(nexus.getOrigin().offset(i)).canPathfindThrough(NavigationType.LAND)) {
                        target = target.add(i.getOffsetX(), 0, i.getOffsetZ());
                    }
                }

                @Nullable
                Path path = mob.getNavigation().findPathTo(target, (int)distance);
                if (path != null) {
                    mob.setTarget(null);
                    mob.getNavigation().startMovingAlong(path, distance > 2000 ? 2 : 1);
                    pathSet = true;
                }

                mob.setPositionTarget(nexus.getOrigin(), (int)distance);
            }

            if (!pathSet || (navigation.getLastPathDistanceToTarget() > 3 && lastPathRequestPos.isPresent() && mob.getBlockPos().isWithinDistance(lastPathRequestPos.get(), 3.5))) {
                pathFailedCount++;
                pathRequestTimer = 40 * pathFailedCount + mob.getRandom().nextInt(10);
            } else {
                pathFailedCount = 0;
                pathRequestTimer = 20;
            }


            lastPathRequestPos = Optional.of(mob.getBlockPos());
        }
    }

    @Override
    public void tick() {
        if (pathFailedCount > 1) {
            @Nullable
            NexusAccess nexus = mob.getNexus();
            if (nexus != null) {
                Vec3d target = nexus.getOrigin().toCenterPos();
                mob.getMoveControl().moveTo(target.x, target.y, target.z, 1);
                mob.setPositionTarget(nexus.getOrigin(), (int)mob.findDistanceToNexus());
                mob.setTarget(null);
            }
        }
        if (mob.getNavigation().isIdle() || mob.getNavigatorNew().getStuckTime() > 40) {
            start();
        }
    }
}