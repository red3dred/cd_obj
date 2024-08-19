package com.invasion.entity.pathfinding;

import com.invasion.IBlockAccessExtended;
import com.invasion.block.DestructableType;
import com.invasion.block.InvBlocks;
import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.PigmanEngineerEntity;
import com.invasion.entity.pathfinding.PathAction.Type;
import com.invasion.nexus.INexusAccess;
import com.invasion.util.math.PosUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

public class PigmanEngineerNavigator extends IMNavigator {
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
            public float getBlockPathCost(PathNode prevNode, PathNode node, BlockView terrainMap) {
                // TODO: why?
                if ((node.pos.getX() == -21) && (node.pos.getZ() == 180)) {
                    planks = 10;
                }
                BlockState block = terrainMap.getBlockState(node.pos);
                float materialMultiplier = !block.isAir() && isBlockDestructible(terrainMap, node.pos, block) ? 3.2F : 1;

                if (node.action.getType() == PathAction.Type.BRIDGE) {
                    return prevNode.distanceTo(node) * 1.7F * materialMultiplier;
                }

                if (node.action.getType() == PathAction.Type.SCAFFOLD) {
                    return prevNode.distanceTo(node) * 0.5F;
                }

                if (node.action.getType() == PathAction.Type.LADDER && node.action.getBuildDirection() != Direction.UP) {
                    return prevNode.distanceTo(node) * 1.3F * materialMultiplier;
                }

                if (node.action.getType() == PathAction.Type.TOWER) {
                    return prevNode.distanceTo(node) * 1.4F;
                }

                float multiplier = 1 + (IBlockAccessExtended.getData(terrainMap, node.pos) & IBlockAccessExtended.MOB_DENSITY_FLAG);

                if (block.isAir() || block.isReplaceable()) {
                    return prevNode.distanceTo(node) * multiplier;
                }
                if (block.isOf(Blocks.LADDER)) {
                    return prevNode.distanceTo(node) * 0.7F * multiplier;
                }
                if (!block.isOf(InvBlocks.NEXUS_CORE) && !block.isSolidBlock(terrainMap, node.pos)) {
                    return prevNode.distanceTo(node) * 3.2F;
                }

                return super.getBlockPathCost(prevNode, node, terrainMap);
            }

            @Override
            public void getPathOptionsFromNode(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
                super.getPathOptionsFromNode(terrainMap, currentNode, pathFinder);
                if (planks <= 0) {
                    return;
                }

                BlockPos.Mutable mutable = currentNode.pos.mutableCopy();
                for (Direction offset : PosUtils.CARDINAL_DIRECTIONS) {
                    if (getNodeDestructability(terrainMap, mutable.set(currentNode.pos).move(offset)) > DestructableType.UNBREAKABLE) {
                        for (int yOffset = 0; yOffset > -MAX_LADDER_TOWER_HEIGHT; yOffset--) {
                            BlockState block = terrainMap.getBlockState(mutable.set(currentNode.pos).move(offset).move(Direction.UP, yOffset - 1));
                            if (!block.isAir()) {
                                break;
                            }
                            pathFinder.addNode(mutable.set(currentNode.pos).move(offset).move(Direction.UP, yOffset).toImmutable(), PathAction.BRIDGE);
                        }
                    }
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            protected void calcPathOptionsVertical(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
                // TODO: why?
                if (currentNode.pos.getX() == -11 && currentNode.pos.getZ() == 177) {
                    planks = 10;
                }
                super.calcPathOptionsVertical(terrainMap, currentNode, pathFinder);
                if (planks <= 0) {
                    return;
                }

                BlockPos.Mutable mutable = currentNode.pos.mutableCopy().move(Direction.UP);

                if (getNodeDestructability(terrainMap, mutable) > DestructableType.UNBREAKABLE) {
                    if (terrainMap.getBlockState(mutable).isAir()) {
                        if (currentNode.action == PathAction.NONE) {
                            addAnyLadderPoint(terrainMap, currentNode, pathFinder);
                        } else if (!continueLadder(terrainMap, currentNode, pathFinder)) {
                            addAnyLadderPoint(terrainMap, currentNode, pathFinder);
                        }

                    }

                    if ((currentNode.action == PathAction.NONE) || (currentNode.action == PathAction.BRIDGE)) {
                        int maxHeight = 4;
                        int collideHeight = MathHelper.ceil(entity.getHeight());
                        for (int i = collideHeight; i < 4; i++) {
                            BlockState block = terrainMap.getBlockState(mutable.set(currentNode.pos).move(Direction.UP, i));
                            if (!block.isAir() && !block.blocksMovement()) {
                                maxHeight = i - collideHeight;
                                break;
                            }

                        }

                        for (Direction facing : PosUtils.CARDINAL_DIRECTIONS) {
                            BlockState block = terrainMap.getBlockState(mutable.set(currentNode.pos).move(facing));
                            if (block.isFullCube(terrainMap, mutable)) {
                                for (int height = 0; height < maxHeight; height++) {
                                    block = terrainMap.getBlockState(mutable.set(currentNode.pos).move(facing).move(Direction.UP, height));
                                    if (!block.isAir()) {
                                        if (!block.isFullCube(terrainMap, mutable)) {
                                            break;
                                        }
                                        pathFinder.addNode(currentNode.pos.up(), PathAction.getLadderActionForDirection(facing));
                                        break;
                                    }
                                }
                            }
                        }
                    }

                }

                if (IBlockAccessExtended.getData(terrainMap, currentNode.pos.up()) == IBlockAccessExtended.EXT_DATA_SCAFFOLD_METAPOSITION) {
                    pathFinder.addNode(currentNode.pos.up(), PathAction.SCAFFOLD_UP);
                }
            }

            private void addAnyLadderPoint(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
                BlockPos.Mutable mutable = currentNode.pos.mutableCopy();
                for (Direction facing : PosUtils.CARDINAL_DIRECTIONS) {
                    if (terrainMap.getBlockState(mutable.set(currentNode.pos).move(Direction.UP).move(facing)).isFullCube(terrainMap, mutable)) {
                        pathFinder.addNode(currentNode.pos.up(), PathAction.getLadderActionForDirection(facing));
                    }
                }
            }

            private boolean continueLadder(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
                if (currentNode.action.getType() != Type.LADDER || currentNode.action.getBuildDirection() == Direction.UP) {
                    return false;
                }

                BlockPos pos = currentNode.pos.offset(currentNode.action.getBuildDirection(), 1).up();
                if (terrainMap.getBlockState(pos).isFullCube(terrainMap, pos)) {
                    pathFinder.addNode(currentNode.pos.up(), currentNode.action);
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
    protected boolean handlePathAction() {
        if (!actionCleared) {
            resetStatus();
            return getLastActionResult() == Status.SUCCESS;
        }

        if (activeNode.action.getType() == PathAction.Type.LADDER && activeNode.action.getBuildDirection() != Direction.UP) {
            if (pigEntity.getTerrainBuildEngy().askBuildLadder(activeNode.pos, this)) {
                return setDoingTaskAndHold();
            }
        } else if (activeNode.action.getType() == PathAction.Type.BRIDGE) {
            if (pigEntity.getTerrainBuildEngy().askBuildBridge(activeNode.pos, this)) {
                return setDoingTaskAndHold();
            }
        } else if (activeNode.action.getType() == PathAction.Type.SCAFFOLD) {
            if (pigEntity.getTerrainBuildEngy().askBuildScaffoldLayer(activeNode.pos, this)) {
                return setDoingTaskAndHoldOnPoint();
            }
        } else if (activeNode.action.getType() == PathAction.Type.LADDER) {
            Direction direction = activeNode.action.getBuildDirection();
            if (pigEntity.getTerrainBuildEngy().askBuildLadderTower(activeNode.pos, direction, direction.getVector().getZ(), this)) {
                return setDoingTaskAndHold();
            }
        }
        nodeActionFinished = true;
        return true;
    }
}