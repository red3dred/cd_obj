package com.invasion.entity.pathfinding;

import com.invasion.entity.EntityIMLiving;
import com.invasion.util.math.PosRotate3D;

public abstract class NavigatorParametric extends NavigatorIM {
    protected double minMoveToleranceSq;
    protected int timeParam;

    public NavigatorParametric(EntityIMLiving entity, IPathSource pathSource) {
        super(entity, pathSource);
        this.minMoveToleranceSq = 21.0D;
        this.timeParam = 0;
    }

    public void onUpdateNavigation(int paramElapsed) {
        totalTicks++;
        if (noPath() || waitingForNotify) {
            return;
        }
        if (canNavigate() && nodeActionFinished) {
            int pathIndex = path.getCurrentPathIndex();
            pathFollow(timeParam + paramElapsed);
            doMovementTo(timeParam);

            if (path.getCurrentPathIndex() != pathIndex) {
                ticksStuck = 0;
                if (activeNode.action != PathAction.NONE) {
                    nodeActionFinished = false;
                }
            }
        }
        if (nodeActionFinished) {
            if (!isPositionClear(activeNode.pos, theEntity)) {
                if (theEntity.onPathBlocked(path, this)) {
                    setDoingTaskAndHold();
                } else {
                    clearPath();
                }
            }
        } else {
            handlePathAction();
        }
    }

    @Override
    public void onUpdateNavigation() {
        onUpdateNavigation(1);
    }

    protected void doMovementTo(int param) {
        PosRotate3D movePos = entityPositionAtParam(param);
        this.theEntity.getMoveControl().moveTo(movePos.position().x, movePos.position().y, movePos.position().z,
                theEntity.getMovementSpeed());

        if (Math.abs(theEntity.squaredDistanceTo(movePos.position())) < minMoveToleranceSq) {
            timeParam = param;
            ticksStuck--;
        } else {
            ticksStuck++;
        }
    }

    protected abstract PosRotate3D entityPositionAtParam(int paramInt);

    protected abstract boolean isReadyForNextNode(int paramInt);

    protected void pathFollow(int param) {
        int nextIndex = path.getCurrentPathIndex() + 1;
        if (isReadyForNextNode(param)) {
            if (nextIndex < path.getCurrentPathLength()) {
                timeParam = 0;
                path.setCurrentPathIndex(nextIndex);
                activeNode = path.getPathPointFromIndex(path.getCurrentPathIndex());
            }
        } else {
            timeParam = param;
        }
    }

    @Override
    protected void pathFollow() {
        pathFollow(0);
    }
}