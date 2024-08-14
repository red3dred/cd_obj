package com.invasion.entity.pathfinding;

import com.invasion.IBlockAccessExtended;
import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.EntityIMPigEngy;
import com.invasion.nexus.INexusAccess;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class NavigatorEngy extends NavigatorIM {
    private final EntityIMPigEngy pigEntity;

    public NavigatorEngy(EntityIMPigEngy entity, IPathSource pathSource) {
        super(entity, pathSource);
        this.pigEntity = entity;
        setNoMaintainPos();
    }

    @Override
    protected Path createPath(EntityIMLiving entity, BlockPos pos, float targetRadius) {
        BlockView terrainCache = getChunkCache(entity.getBlockPos(), pos, 16);
        INexusAccess nexus = pigEntity.getNexus();
        if (nexus != null) {
            IBlockAccessExtended terrainCacheExt = nexus.getAttackerAI().wrapEntityData(terrainCache);

            nexus.getAttackerAI().addScaffoldDataTo(terrainCacheExt);
            terrainCache = terrainCacheExt;
        }
        float maxSearchRange = 12 + (float) entity.getPos().distanceTo(pos.toBottomCenterPos());
        if (!pathSource.canPathfindNice(IPathSource.PathPriority.HIGH, maxSearchRange, pathSource.getSearchDepth(), pathSource.getQuickFailDepth())) {
            return null;
        }
        return pathSource.createPath(entity, pos, targetRadius, maxSearchRange, terrainCache);
    }

    @Override
    protected boolean handlePathAction() {
        if (!actionCleared) {
            resetStatus();
            return getLastActionResult() == Status.SUCCESS;
        }

        if (activeNode.action.getType() == PathAction.Type.LADDER && activeNode.action.getBuildDirection() != Direction.UP) {
            if (pigEntity.getTerrainBuildEngy().askBuildLadder(activeNode, this)) {
                return setDoingTaskAndHold();
            }
        } else if (activeNode.action.getType() == PathAction.Type.BRIDGE) {
            if (pigEntity.getTerrainBuildEngy().askBuildBridge(activeNode, this)) {
                return setDoingTaskAndHold();
            }
        } else if (activeNode.action.getType() == PathAction.Type.SCAFFOLD) {
            if (pigEntity.getTerrainBuildEngy().askBuildScaffoldLayer(activeNode, this)) {
                return setDoingTaskAndHoldOnPoint();
            }
        } else if (activeNode.action.getType() == PathAction.Type.LADDER) {
            Direction direction = activeNode.action.getBuildDirection();
            if (pigEntity.getTerrainBuildEngy().askBuildLadderTower(activeNode, direction, direction.getVector().getZ(), this)) {
                return setDoingTaskAndHold();
            }
        }
        nodeActionFinished = true;
        return true;
    }
}