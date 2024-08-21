package com.invasion.entity.pathfinding;

import com.invasion.IBlockAccessExtended;
import com.invasion.block.DestructableType;
import com.invasion.block.InvBlocks;
import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.PigmanEngineerEntity;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;
import com.invasion.entity.pathfinding.path.PathAction.Type;
import com.invasion.nexus.INexusAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

public class PigmanEngineerNavigator extends IMNavigation {
    private static final int MAX_LADDER_TOWER_HEIGHT = 4;

    private final PigmanEngineerEntity pigEntity;

    public PigmanEngineerNavigator(PigmanEngineerEntity entity, PathSource pathSource) {
        super(entity, pathSource);
        this.pigEntity = entity;
        setNoMaintainPos();
    }

    @Override
    protected <T extends Entity> Actor<T> createActor(T entity) {
        return new Actor<>(entity) {
            {
                setCanDestroyBlocks(true);
                setCanClimb(false);
            }

            private int planks = 15;

            @Override
            public boolean avoidsBlock(BlockState state) {
                return super.avoidsBlock(state) || state.isIn(BlockTags.DOORS);
            }

            @Override
            public float getPathNodePenalty(PathNode prevNode, PathNode node, BlockView terrainMap) {
                // TODO: why?
                if ((node.x == -21) && (node.z == 180)) {
                    planks = 10;
                }
                BlockState block = terrainMap.getBlockState(node.getBlockPos());
                PathAction action = ActionablePathNode.getAction(node);
                float materialMultiplier = !block.isAir() && isBlockDestructible(terrainMap, node.getBlockPos(), block) ? 3.2F : 1;

                if (action.getType() == PathAction.Type.BRIDGE) {
                    return prevNode.getDistance(node) * 1.7F * materialMultiplier;
                }

                if (action.getType() == PathAction.Type.SCAFFOLD) {
                    return prevNode.getDistance(node) * 0.5F;
                }

                if (action.getType() == PathAction.Type.LADDER && action.getBuildDirection() != Direction.UP) {
                    return prevNode.getDistance(node) * 1.3F * materialMultiplier;
                }

                if (action.getType() == PathAction.Type.TOWER) {
                    return prevNode.getDistance(node) * 1.4F;
                }

                float multiplier = 1 + (IBlockAccessExtended.getData(terrainMap, node.getBlockPos()) & IBlockAccessExtended.MOB_DENSITY_FLAG);

                if (block.isAir() || block.isReplaceable()) {
                    return prevNode.getDistance(node) * multiplier;
                }
                if (block.isOf(Blocks.LADDER)) {
                    return prevNode.getDistance(node) * 0.7F * multiplier;
                }
                if (!block.isOf(InvBlocks.NEXUS_CORE) && !block.isSolidBlock(terrainMap, node.getBlockPos())) {
                    return prevNode.getDistance(node) * 3.2F;
                }

                return super.getPathNodePenalty(prevNode, node, terrainMap);
            }

            @Override
            public void getSuccessors(BlockView terrainMap, PathNode currentNode, PathBuilder pathFinder) {
                super.getSuccessors(terrainMap, currentNode, pathFinder);
                if (planks <= 0) {
                    return;
                }

                BlockPos.Mutable mutable = new BlockPos.Mutable();
                for (Direction offset : Direction.Type.HORIZONTAL) {
                    if (getNodeDestructability(terrainMap, mutable.set(currentNode.x, currentNode.y, currentNode.z).move(offset)) > DestructableType.UNBREAKABLE) {
                        for (int yOffset = 0; yOffset > -MAX_LADDER_TOWER_HEIGHT; yOffset--) {
                            BlockState block = terrainMap.getBlockState(mutable.set(currentNode.x, currentNode.y, currentNode.z).move(offset).move(Direction.UP, yOffset - 1));
                            if (!block.isAir()) {
                                break;
                            }
                            pathFinder.addNode(mutable.set(currentNode.x, currentNode.y, currentNode.z).move(offset).move(Direction.UP, yOffset).toImmutable(), PathAction.BRIDGE);
                        }
                    }
                }
            }

            @Override
            protected void calcPathOptionsVertical(BlockView terrainMap, PathNode currentNode, PathBuilder pathFinder) {
                // TODO: why?
                if (currentNode.x == -11 && currentNode.z == 177) {
                    planks = 10;
                }
                super.calcPathOptionsVertical(terrainMap, currentNode, pathFinder);
                if (planks <= 0) {
                    return;
                }
                PathAction action = ActionablePathNode.getAction(currentNode);
                BlockPos.Mutable mutable = currentNode.getBlockPos().mutableCopy().move(Direction.UP);

                if (getNodeDestructability(terrainMap, mutable) > DestructableType.UNBREAKABLE) {
                    if (terrainMap.getBlockState(mutable).isAir()) {
                        if (action == PathAction.NONE) {
                            addAnyLadderPoint(terrainMap, currentNode, pathFinder);
                        } else if (!continueLadder(terrainMap, currentNode, pathFinder)) {
                            addAnyLadderPoint(terrainMap, currentNode, pathFinder);
                        }

                    }

                    if ((action == PathAction.NONE) || (action == PathAction.BRIDGE)) {
                        int maxHeight = 4;
                        int collideHeight = MathHelper.ceil(entity.getHeight());
                        for (int i = collideHeight; i < 4; i++) {
                            BlockState block = terrainMap.getBlockState(mutable.set(currentNode.x, currentNode.y, currentNode.z).move(Direction.UP, i));
                            if (!block.isAir() && !block.canPathfindThrough(NavigationType.LAND)) {
                                maxHeight = i - collideHeight;
                                break;
                            }

                        }

                        for (Direction facing : Direction.Type.HORIZONTAL) {
                            BlockState block = terrainMap.getBlockState(mutable.set(currentNode.x, currentNode.y, currentNode.z).move(facing));
                            if (block.isFullCube(terrainMap, mutable)) {
                                for (int height = 0; height < maxHeight; height++) {
                                    block = terrainMap.getBlockState(mutable.set(currentNode.x, currentNode.y, currentNode.z).move(facing).move(Direction.UP, height));
                                    if (!block.isAir()) {
                                        if (!block.isFullCube(terrainMap, mutable)) {
                                            break;
                                        }
                                        pathFinder.addNode(currentNode.getBlockPos().up(), PathAction.getLadderActionForDirection(facing));
                                        break;
                                    }
                                }
                            }
                        }
                    }

                }

                if (IBlockAccessExtended.getData(terrainMap, currentNode.getBlockPos().up()) == IBlockAccessExtended.EXT_DATA_SCAFFOLD_METAPOSITION) {
                    pathFinder.addNode(currentNode.getBlockPos().up(), PathAction.SCAFFOLD_UP);
                }
            }

            private void addAnyLadderPoint(BlockView terrainMap, PathNode currentNode, PathBuilder pathFinder) {
                BlockPos.Mutable mutable = currentNode.getBlockPos().mutableCopy();
                for (Direction facing : Direction.Type.HORIZONTAL) {
                    if (terrainMap.getBlockState(mutable.set(currentNode.x, currentNode.y, currentNode.z).move(Direction.UP).move(facing)).isFullCube(terrainMap, mutable)) {
                        pathFinder.addNode(currentNode.getBlockPos().up(), PathAction.getLadderActionForDirection(facing));
                    }
                }
            }

            private boolean continueLadder(BlockView terrainMap, PathNode currentNode, PathBuilder pathFinder) {
                PathAction action = ActionablePathNode.getAction(currentNode);
                if (action.getType() != Type.LADDER || action.getBuildDirection() == Direction.UP) {
                    return false;
                }

                BlockPos pos = currentNode.getBlockPos().offset(action.getBuildDirection(), 1).up();
                if (terrainMap.getBlockState(pos).isFullCube(terrainMap, pos)) {
                    pathFinder.addNode(currentNode.getBlockPos().up(), action);
                }
                return true;
            }

        };
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
        return pathSource.createPath(entity, pos, targetRadius, maxSearchRange, terrainCache);
    }

    @Override
    protected boolean handlePathAction(PathAction action) {
        if (!actionCleared) {
            resetStatus();
            return getLastActionResult() == Status.SUCCESS;
        }

        if (action.getType() == PathAction.Type.LADDER && action.getBuildDirection() != Direction.UP) {
            if (pigEntity.getTerrainBuildEngy().askBuildLadder(activeNode.getBlockPos(), this)) {
                return setDoingTaskAndHold();
            }
        } else if (action.getType() == PathAction.Type.BRIDGE) {
            if (pigEntity.getTerrainBuildEngy().askBuildBridge(activeNode.getBlockPos(), this)) {
                return setDoingTaskAndHold();
            }
        } else if (action.getType() == PathAction.Type.SCAFFOLD) {
            if (pigEntity.getTerrainBuildEngy().askBuildScaffoldLayer(activeNode.getBlockPos(), this)) {
                return setDoingTaskAndHoldOnPoint();
            }
        } else if (action.getType() == PathAction.Type.LADDER) {
            Direction direction = action.getBuildDirection();
            if (pigEntity.getTerrainBuildEngy().askBuildLadderTower(activeNode.getBlockPos(), direction, direction.getVector().getZ(), this)) {
                return setDoingTaskAndHold();
            }
        }
        nodeActionFinished = true;
        return true;
    }
}