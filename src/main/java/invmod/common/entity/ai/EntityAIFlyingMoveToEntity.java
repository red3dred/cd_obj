package invmod.common.entity.ai;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import invmod.common.entity.EntityIMFlying;
import invmod.common.entity.Goal;
import invmod.common.entity.INavigationFlying;
import invmod.common.entity.Path;
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
        INavigationFlying nav = theEntity.getNavigatorNew();
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