package com.invasion.entity.pathfinding;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

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
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
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

    @Nullable
    private static Direction getInitialLadderOrientation(CollisionView world, BlockPos.Mutable mutable) {
        for (Direction facing : Direction.Type.HORIZONTAL) {
            if (canPositionSupportLadder(world, mutable, facing)) {
                return facing;
            }
        }
        return Direction.UP;
    }

    private static Direction getHighLadderOrientation(CollisionView world, int maxHeight, BlockPos.Mutable mutable, PathNode currentNode) {
        for (Direction facing : Direction.Type.HORIZONTAL) {
            if (canPositionSupportLadder(world, mutable.set(currentNode.x, currentNode.y, currentNode.z), facing, maxHeight)) {
                return facing;
            }
        }

        return Direction.UP;
    }

    private static boolean canPositionSupportLadder(BlockView world, BlockPos.Mutable pos, Direction side, int height) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        while (height >= 0) {
            if (!canPositionSupportLadder(world, pos.set(x, y + height--, z), side)) {
                return false;
            }
        }
        return true;
    }

    private static boolean canPositionSupportLadder(BlockView world, BlockPos.Mutable pos, Direction side) {
        return world.getBlockState(pos.move(side)).isSideSolidFullSquare(world, pos, side.getOpposite());
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
            index = super.getSuccessors(index, successors, node, world, cache);
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            PathAction ladderAction = PathAction.NONE;
            if (world.getBlockState(mutable.set(node.x, node.y + 1, node.z)).isAir()) {
                ladderAction = getLadderAction(mutable);
            }

            if (ladderAction == PathAction.NONE && (previousNodeAction == PathAction.NONE || previousNodeAction == PathAction.BRIDGE)) {
                if (world.getBlockState(mutable.set(node.x, node.y - 1, node.z)).isAir()) {
                    ladderAction = PathAction.getLadderActionForDirection(
                            getHighLadderOrientation(world, getMaxLadderHeight(mutable, node), mutable.set(node.x, node.y - 1, node.z), node)
                    );
                }
            }

            if (ladderAction != PathAction.NONE) {
                node = getBuilderNode(node.x, mutable.getY(), node.z, ladderAction);
                if (!node.visited) {
                    successors[index++] = node;
                }
            }

            return index;
        }

        private PathAction getLadderAction(BlockPos.Mutable mutable) {
            if (previousNodeAction == PathAction.NONE) {
                return PathAction.getLadderActionForDirection(getInitialLadderOrientation(world, mutable));
            }

            Direction orientation = previousNodeAction.getOrientation();
            return previousNodeAction.getType() == Type.LADDER
                    && orientation != Direction.UP
                    && canPositionSupportLadder(world, mutable, orientation) ? previousNodeAction : PathAction.NONE;
        }

        @Override
        protected PathNode getPathNode(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType) {
            PathNode node = super.getPathNode(x, y, z, maxYStep, prevFeetY, direction, nodeType);
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            if (node != null && ActionablePathNode.getAction(node) == PathAction.NONE) {
                if (ScaffoldView.of(world).isScaffoldPosition(mutable.set(x, y, z))) {
                    ActionablePathNode.setAction(node, PathAction.SCAFFOLD_UP);
                }
            }

            if (node == null || ActionablePathNode.getAction(node) == PathAction.NONE) {
                if (direction.getAxis().isHorizontal()) {
                    if (isClear(context.getWorld(), mutable.set(x, y - MAX_LADDER_TOWER_HEIGHT, z), MAX_LADDER_TOWER_HEIGHT)) {
                        return getBuilderNode(x, y, z, PathAction.BRIDGE);
                    }
                }
            }

            return node;
        }

        private PathNode getBuilderNode(int x, int y, int z, PathAction action) {
            PathNode node = getNode(x, y, z);
            node.type = PathNodeType.WALKABLE;
            node.penalty = node.type.getDefaultPenalty();
            return ActionablePathNode.setAction(node, action);
        }

        private boolean isClear(CollisionView world, BlockPos.Mutable mutable, int height) {
            int originalY = mutable.getY() + 1;
            try {
                for (int yOffset = 0; yOffset < height; yOffset++) {
                    if (!world.getBlockState(mutable.setY(originalY + yOffset)).isAir()) {
                        return false;
                    }
                }
                return true;
            } finally {
                mutable.setY(originalY);
            }
        }

        private int getMaxLadderHeight(BlockPos.Mutable mutable, PathNode currentNode) {
            int collideHeight = MathHelper.ceil(entity.getHeight());
            for (int i = collideHeight; i < 4; i++) {
                BlockState block = world.getBlockState(mutable.setY(currentNode.y + i));
                if (block.isSolidBlock(world, mutable)) {
                    return i - collideHeight;
                }
            }
            return 4;
        }

        private boolean canBuildOnBlock(CollisionView world, BlockPos pos) {
            return world.getBlockState(pos).isAir() || !BlockMetadata.isIndestructible(world.getBlockState(pos)) || PathingUtil.hasAdjacentLadder(world, pos);
        }
    }
}
