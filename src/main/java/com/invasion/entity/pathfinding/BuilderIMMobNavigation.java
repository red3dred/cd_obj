package com.invasion.entity.pathfinding;

import java.util.Set;

import com.invasion.block.BlockMetadata;
import com.invasion.block.InvBlocks;
import com.invasion.entity.NexusEntity;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;
import com.invasion.entity.pathfinding.path.PathAction.Type;
import com.invasion.nexus.NexusAccess;
import com.invasion.nexus.ai.scaffold.ScaffoldView;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;

public class BuilderIMMobNavigation extends IMMobNavigation {
    private static final int MAX_LADDER_TOWER_HEIGHT = 4;
    private static final int WORK_FOUND_COOLDOWN = 60;
    private static final int JOBLESS_COOLDOWN = 140;

    private int jobRequestTimer;
    private boolean waitingForJob;

    private final NexusEntity nexusEntity;

    public <T extends MobEntity & NexusEntity> BuilderIMMobNavigation(T entity) {
        super(entity);
        this.nexusEntity = entity;
    }

    @Override
    public PathNodeMaker createNodeMaker() {
        var nodeMaker = new NodeMaker();
        nodeMaker.setCanEnterOpenDoors(true);
        nodeMaker.setCanOpenDoors(true);
        nodeMaker.setCanSwim(true);
        nodeMaker.setCanClimbLadders(true);
        return nodeMaker;
    }

    @Override
    protected Path findPathToAny(Set<BlockPos> positions, int range, boolean useHeadPos, int distance, float followRange) {
        return super.findPathToAny(positions, range, useHeadPos, distance, followRange);
    }

    @Override
    protected void tickObjectives() {
        super.tickObjectives();

        if (!waitingForJob && nexusEntity.hasNexus()) {
            jobRequestTimer = Math.max(0, jobRequestTimer - 1);
            int yDifference = nexusEntity.getNexus().getOrigin().getY() - entity.getBlockPos().getY();
            int weight = yDifference > 1 ? Math.max(6000 / yDifference, 1) : 1;
            if (getAIGoal() == Goal.BREAK_NEXUS && (getLastPathDistanceToTarget() > 2 && jobRequestTimer <= 0 || entity.getRandom().nextInt(weight) == 0)) {
                waitingForJob = true;
                stop();
                nexusEntity.getNexus().getAttackerAI().requestBuildJob(nexusEntity, target -> {
                    waitingForJob = false;
                    jobRequestTimer = target.isPresent() ? WORK_FOUND_COOLDOWN : JOBLESS_COOLDOWN;
                    target.ifPresent(pos -> {
                        startMovingAlong(findPathTo(pos, nexusEntity.asEntity().getBlockPos().getManhattanDistance(pos)), 1);
                    });
                });
            }
        }
    }

    class NodeMaker extends IMLandPathNodeMaker {
        @Override
        protected void populateChunkCacheData(NexusAccess nexus, CollisionView view) {
            super.populateChunkCacheData(nexus, view);
            nexus.getAttackerAI().addScaffoldDataTo(view);
        }

        @Override
        public float getDistancePenalty(PathNode previousNode, PathNode nextNode, CollisionView world) {
            world = context.getWorld();

            BlockState block = world.getBlockState(nextNode.getBlockPos());
            PathAction action = ActionablePathNode.getAction(nextNode);
            float materialMultiplier = !block.isAir() && canBuildOnBlock(world, nextNode.getBlockPos()) ? 3.2F : 1;

            if (action.getType() == PathAction.Type.BRIDGE) {
                return 1.7F * materialMultiplier;
            }

            if (action.getType() == PathAction.Type.SCAFFOLD) {
                return 0.5F;
            }

            if (action.getType() == PathAction.Type.LADDER && action.getOrientation() != Direction.UP) {
                return 1.3F * materialMultiplier;
            }

            if (action.getType() == PathAction.Type.TOWER) {
                return 1.4F;
            }

            float multiplier = 1 + ScaffoldView.of(world).getMobDensity(nextNode.getBlockPos());

            if (block.isAir() || block.isReplaceable()) {
                return multiplier;
            }

            if (block.isOf(Blocks.LADDER)) {
                return 0.7F * multiplier;
            }

            if (!block.isOf(InvBlocks.NEXUS_CORE) && !block.isSolidBlock(world, nextNode.getBlockPos())) {
                return 3.2F;
            }

            return super.getDistancePenalty(previousNode, nextNode, world);
        }

        @Override
        public int getSuccessors(int index, PathNode[] successors, PathNode node, CollisionView world, DynamicPathNodeNavigator.NodeCache cache) {
            index = getVerticalSuccessors(index, successors, node, world);
            index = super.getSuccessors(index, successors, node, world, cache);
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            for (Direction offset : Direction.Type.HORIZONTAL) {
                if (canBuildOnBlock(world, mutable.set(node.x, node.y, node.z).move(offset))) {
                    for (int yOffset = 0; yOffset > -MAX_LADDER_TOWER_HEIGHT; yOffset--) {
                        BlockState block = world.getBlockState(mutable.set(node.x, node.y + yOffset - 1, node.z).move(offset));
                        if (!block.isAir()) {
                            break;
                        }
                        successors[index++] = getNode(node.x + offset.getOffsetX(), node.y + 1, node.z + offset.getOffsetZ(), PathAction.BRIDGE);
                    }
                }
            }

            return index;
        }

        protected int getVerticalSuccessors(int index, PathNode[] successors, PathNode currentNode, CollisionView world) {
            world = context.getWorld();
            PathAction action = ActionablePathNode.getAction(currentNode);
            BlockPos.Mutable mutable = currentNode.getBlockPos().mutableCopy().move(Direction.UP);

            if (canBuildOnBlock(world, mutable)) {
                if (world.getBlockState(mutable).isAir()) {
                    index = getNextLadderSuccessor(index, successors, currentNode, world);
                }

                if (action == PathAction.NONE || action == PathAction.BRIDGE) {
                    int maxHeight = 4;
                    int collideHeight = MathHelper.ceil(entity.getHeight());
                    for (int i = collideHeight; i < 4; i++) {
                        BlockState block = world.getBlockState(mutable.set(currentNode.x, currentNode.y, currentNode.z).move(Direction.UP, i));
                        if (!block.isAir() && !block.canPathfindThrough(NavigationType.LAND)) {
                            maxHeight = i - collideHeight;
                            break;
                        }
                    }

                    for (Direction facing : Direction.Type.HORIZONTAL) {
                        BlockState block = world.getBlockState(mutable.set(currentNode.x, currentNode.y, currentNode.z).move(facing));
                        if (block.isFullCube(world, mutable)) {
                            for (int height = 0; height < maxHeight; height++) {
                                block = world.getBlockState(mutable.set(currentNode.x, currentNode.y, currentNode.z).move(facing).move(Direction.UP, height));
                                if (!block.isAir()) {
                                    if (!block.isFullCube(world, mutable)) {
                                        break;
                                    }
                                    successors[index++] = getNode(currentNode.x, currentNode.y + 1, currentNode.z, PathAction.getLadderActionForDirection(facing));
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (ScaffoldView.of(world).isScaffoldPosition(currentNode.getBlockPos().up())) {
                successors[index++] = getNode(currentNode.x, currentNode.y + 1, currentNode.z, PathAction.SCAFFOLD_UP);
            }

            return index;
        }

        private boolean canBuildOnBlock(CollisionView world, BlockPos pos) {
            return world.getBlockState(pos).isAir() || !BlockMetadata.isIndestructible(world.getBlockState(pos)) || PathingUtil.hasAdjacentLadder(world, pos);
        }

        private int getNextLadderSuccessor(int index, PathNode[] successors, PathNode currentNode, BlockView terrainMap) {
            PathAction action = ActionablePathNode.getAction(currentNode);
            BlockPos.Mutable mutable = currentNode.getBlockPos().mutableCopy();

            if (action == PathAction.NONE) {
                for (Direction facing : Direction.Type.HORIZONTAL) {
                    if (canPositionSupportLadder(terrainMap, mutable.set(currentNode.x, currentNode.y + 1, currentNode.z), facing)) {
                        successors[index++] = getNode(currentNode.x, currentNode.y + 1, currentNode.z, PathAction.getLadderActionForDirection(facing));
                    }
                }

                return index;
            }

            Direction orientation = action.getOrientation();

            if (action.getType() == Type.LADDER && orientation != Direction.UP
                    && canPositionSupportLadder(terrainMap, mutable.set(currentNode.x, currentNode.y + 1, currentNode.z), orientation)) {
                successors[index++] = getNode(currentNode.x, currentNode.y + 1, currentNode.z, action);
            }

            return index;
        }

        private boolean canPositionSupportLadder(BlockView world, BlockPos.Mutable pos, Direction side) {
            return world.getBlockState(pos.move(side)).isSideSolidFullSquare(world, pos, side.getOpposite());
        }
    }
}
