package com.invasion.entity.ai.goal;

import java.util.EnumSet;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.IMMobEntity;
import com.invasion.entity.HasAiGoals;
import com.invasion.entity.pathfinding.Navigation;
import com.invasion.nexus.INexusAccess;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
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
        return mob.hasGoal(HasAiGoals.Goal.BREAK_NEXUS);
    }

    @Override
    public void start() {
        @Nullable
        INexusAccess nexus = mob.getNexus();

        if (nexus != null && pathRequestTimer-- <= 0) {
            boolean pathSet = false;
            double distance = mob.findDistanceToNexus();

            if (distance > 2000) {
                Vec3d target = nexus.getOrigin().toBottomCenterPos();
                pathSet = navigation.tryMoveTowardsXZ(target.x, target.z, 1, 6, 4, 1);
            } else if (distance > 1.5) {
                pathSet = navigation.startMovingTo(nexus.getOrigin().toBottomCenterPos(), 1, 1);
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
            INexusAccess nexus = mob.getNexus();
            if (nexus != null) {
                Vec3d target = nexus.getOrigin().toCenterPos();
                mob.getMoveControl().moveTo(target.x, target.y, target.z, 1);
            }
        }
        if (mob.getNavigation().isIdle() || mob.getNavigatorNew().getStuckTime() > 40) {
            start();
        }
    }
}