package com.invasion.entity.ai.goal;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.EntityIMMob;
import com.invasion.entity.ai.Goal;
import com.invasion.entity.pathfinding.INavigation;
import com.invasion.nexus.INexusAccess;
import com.invasion.util.math.CoordsInt;
import com.invasion.util.math.Distance;
import com.invasion.util.math.IPosition;

public class EntityAIGoToNexus extends net.minecraft.entity.ai.goal.Goal {
    private EntityIMMob mob;
    private IPosition lastPathRequestPos = new CoordsInt(0, -128, 0);
    private final INavigation navigation;
    private int pathRequestTimer;
    private int pathFailedCount;

    public EntityAIGoToNexus(EntityIMMob entity) {
        this.mob = entity;
        this.navigation = mob.getNavigatorNew();
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return mob.getAIGoal() == Goal.BREAK_NEXUS;
    }

    @Override
    public void start() {
        @Nullable
        INexusAccess nexus = mob.getNexus();

        if (nexus != null && pathRequestTimer-- <= 0) {
            boolean pathSet = false;
            double distance = mob.findDistanceToNexus();

            if (distance > 2000) {
                pathSet = navigation.tryMoveTowardsXZ(nexus.getXCoord(), nexus.getZCoord(), 1, 6, 4, mob.getMovementSpeed());
            } else if (distance > 1.5) {
                pathSet = navigation.tryMoveToXYZ(nexus.toBlockPos().toBottomCenterPos(), 1, mob.getMovementSpeed());
            }

            if (!pathSet || (navigation.getLastPathDistanceToTarget() > 3 && Distance.distanceBetween(lastPathRequestPos, mob) < 3.5)) {
                pathFailedCount++;
                pathRequestTimer = 40 * pathFailedCount + mob.getRandom().nextInt(10);
            } else {
                pathFailedCount = 0;
                pathRequestTimer = 20;
            }

            lastPathRequestPos = new CoordsInt(mob.getBlockPos());
        }
    }

    @Override
    public void tick() {
        if (pathFailedCount > 1) {
            @Nullable
            INexusAccess nexus = mob.getNexus();
            if (nexus != null) {
                mob.getMoveControl().moveTo(nexus.getXCoord() + 0.5D, nexus.getYCoord(), nexus.getZCoord() + 0.5D, mob.getMovementSpeed());
            }
        }
        if (mob.getNavigatorNew().noPath() || mob.getNavigatorNew().getStuckTime() > 40) {
            start();
        }
    }
}