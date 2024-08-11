package invmod.common.entity;

import invmod.common.IBlockAccessExtended;
import invmod.common.nexus.INexusAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
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
        if (!this.actionCleared) {
            resetStatus();
            if (getLastActionResult() != 0) {
                return false;
            }
            return true;
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
            Vec3i direction = activeNode.action.getBuildDirection().getVector();
            if (pigEntity.getTerrainBuildEngy().askBuildLadderTower(activeNode, direction.getX(), direction.getZ(), this)) {
                return setDoingTaskAndHold();
            }
        }
        nodeActionFinished = true;
        return true;
    }
}