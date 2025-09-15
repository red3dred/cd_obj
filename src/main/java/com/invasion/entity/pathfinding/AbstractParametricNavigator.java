package com.invasion.entity.pathfinding;

import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.pathfinding.path.PathAction;
import com.invasion.util.math.PosRotate3D;

@Deprecated
public abstract class AbstractParametricNavigator extends IMNavigation {
    protected double minMoveToleranceSq = 21;
    protected int timeParam;

    public AbstractParametricNavigator(EntityIMLiving entity, PathSource pathSource) {
        super(entity, pathSource);
    }

    @Override
    public void tick() {
        totalTicks++;
        if (isIdle() || waitingForNotify) {
            return;
        }
        if (nodeActionFinished) {
            int pathIndex = path.getCurrentNodeIndex();
            pathFollow(timeParam + 1);
            doMovementTo(timeParam);

            if (path.getCurrentNodeIndex() != pathIndex) {
                ticksStuck = 0;
                if (getCurrentWorkingAction() != PathAction.NONE) {
                    nodeActionFinished = false;
                }
            }
        }
        if (nodeActionFinished) {
            if (!isPositionClear(activeNode.getBlockPos(), theEntity)) {
                if (theEntity.onPathBlocked(path, this)) {
                    setDoingTaskAndHold();
                } else {
                    stop();
                }
            }
        } else {
            handlePathAction(getCurrentWorkingAction());
        }
    }

    protected void doMovementTo(int time) {
        PosRotate3D movePos = entityPositionAtParam(time);
        theEntity.getMoveControl().moveTo(movePos.position().x, movePos.position().y, movePos.position().z, 1);

        if (Math.abs(theEntity.squaredDistanceTo(movePos.position())) < minMoveToleranceSq) {
            timeParam = time;
            ticksStuck--;
        } else {
            ticksStuck++;
        }
    }

    protected abstract PosRotate3D entityPositionAtParam(int time);

    protected abstract boolean isReadyForNextNode(int time);

    protected void pathFollow(int time) {
        int nextIndex = path.getCurrentNodeIndex() + 1;
        if (isReadyForNextNode(time)) {
            if (nextIndex < path.getLength()) {
                timeParam = 0;
                path.setCurrentNodeIndex(nextIndex);
                activeNode = path.getNode(path.getCurrentNodeIndex());
            }
        } else {
            timeParam = time;
        }
    }

    @Override
    protected void pathFollow() {
        pathFollow(0);
    }
}