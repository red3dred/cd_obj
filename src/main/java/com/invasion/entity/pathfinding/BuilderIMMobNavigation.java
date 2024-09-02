package com.invasion.entity.pathfinding;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.invasion.block.BlockMetadata;
import com.invasion.block.InvBlocks;
import com.invasion.entity.NexusEntity;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;
import com.invasion.nexus.NexusAccess;
import com.invasion.nexus.ai.scaffold.ScaffoldView;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;

public class BuilderIMMobNavigation extends IMMobNavigation {
    private static final int MAX_LADDER_TOWER_HEIGHT = 4;
    private static final int MAX_LADDERABLE_WALL_HEIGHT = 16;
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

    public static boolean canPositionSupportLadder(BlockView world, BlockPos.Mutable pos, Direction side, int height) {
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

    public static boolean canPositionSupportLadder(BlockView world, BlockPos.Mutable pos, Direction side) {
        return world.getBlockState(pos.move(side)).isSideSolidFullSquare(world, pos, side);
    }

    class NodeMaker extends IMLandPathNodeMaker {

        private final Long2ObjectMap<List<Direction>> ladderOrientationPossibilities = new Long2ObjectOpenHashMap<>();
        private final Object2ObjectMap<Direction, Long2ObjectMap<Long>> climbableObstacleHeights = new Object2ObjectOpenHashMap<>();

        @Override
        public void clear() {
            super.clear();
            ladderOrientationPossibilities.clear();
            climbableObstacleHeights.clear();
        }

        protected final List<Direction> getPossibleLadderOrientations(BlockPos pos) {
            return ladderOrientationPossibilities.computeIfAbsent(pos.asLong(), l -> ClimberUtil.getPossibleLadderOrientations(world, pos.mutableCopy()).toList());
        }

        protected final long getWallHeightPermittingGaps(Direction orientation, BlockPos pos) {
            return climbableObstacleHeights.computeIfAbsent(orientation, o -> new Long2ObjectOpenHashMap<Long>())
                    .computeIfAbsent(pos.asLong(), l -> (long)ClimberUtil.getWallHeightPermittingGaps(world, pos.mutableCopy(), orientation, MAX_LADDER_TOWER_HEIGHT, MAX_LADDER_TOWER_HEIGHT));
        }

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
        protected PathNode getPathNode(int x, int y, int z, int maxYStep, double feetY, Direction direction, PathNodeType nodeType) {
            PathNode node = super.getPathNode(x, y, z, maxYStep, feetY, direction, nodeType);
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            if (node != null && ActionablePathNode.getAction(node) == PathAction.NONE && ScaffoldView.of(world).isScaffoldPosition(mutable.set(x, y, z))) {
                ActionablePathNode.setAction(node, PathAction.SCAFFOLD_UP);
            }

            if (node == null || ActionablePathNode.getAction(node) == PathAction.NONE) {
                if (direction.getAxis().isHorizontal()) {
                    if (isBridgableGap(context.getWorld(), mutable.set(x, y - MAX_LADDER_TOWER_HEIGHT, z), MAX_LADDER_TOWER_HEIGHT)) {
                        return getBuilderNode(x, y, z, PathAction.BRIDGE);
                    }
                }
            }

            if (direction.getAxis().isHorizontal() && previousNodeAction.getType() != PathAction.Type.LADDER) {
                // check can begin ladder
                if (node == null || node.type != PathNodeType.WALKABLE) {
                    return node;
                }

                // Get possible sides that can hold a ladder
                List<Direction> possibleOrientations = getPossibleLadderOrientations(mutable.set(x, y, z));

                if (!possibleOrientations.isEmpty()) {
                    // Get the initial orientation from vertical neighbours
                    Direction ladderOrientation = ClimberUtil.getOrientationFromNeighbors(world, mutable.set(x, y, z), possibleOrientations);
                    long ladderHeight = getWallHeightPermittingGaps(ladderOrientation, mutable.set(x, feetY, z));
                    Direction optimalOrientation = ladderOrientation;

                    // Measure wall heights and pick the side that gets us the farthest
                    for (Direction alternate : possibleOrientations) {
                        if (alternate != ladderOrientation) {
                            long alternateHeight = getWallHeightPermittingGaps(alternate, mutable.set(x, feetY, z));
                            if (alternateHeight > 0 && alternateHeight < MAX_LADDER_TOWER_HEIGHT && alternateHeight > ladderHeight) {
                                optimalOrientation = alternate;
                                ladderHeight = alternateHeight;
                            }
                        }
                    }

                    // Only add if it's valid and above the jump height
                    if (ladderOrientation.getAxis() != Direction.Axis.Y && ladderHeight > maxYStep) {
                        return getBuilderNode(x, y, z, PathAction.getLadderActionForDirection(optimalOrientation));
                    }
                }
            } else if (direction.getAxis().isVertical()) {
                BlockState stateAtNode = world.getBlockState(previousNodePosition);
                Direction ladderOrientation = Direction.UP;
                if (PathingUtil.isLadder(stateAtNode)) {
                    ladderOrientation = stateAtNode.get(LadderBlock.FACING);
                }
                if (previousNodeAction.getType() == PathAction.Type.LADDER && previousNodeAction.getOrientation().getAxis() != Direction.Axis.Y) {
                    ladderOrientation = previousNodeAction.getOrientation();
                }

                if (ladderOrientation.getAxis() != Direction.Axis.Y) {
                    // Check if we can continue laddering
                    int ascentionHeight = ClimberUtil.getGapHeight(world, mutable.set(x, feetY, z).move(ladderOrientation.getOpposite()), MAX_LADDERABLE_WALL_HEIGHT);

                    if (ascentionHeight > 0 && ascentionHeight < MAX_LADDER_TOWER_HEIGHT) {
                        if (ClimberUtil.canPositionSupportLadder(world, mutable.set(x, y, z), ladderOrientation)) {
                            node = getBuilderNode(x, y, z, PathAction.getLadderActionForDirection(ladderOrientation));
                            node.penalty /= 4;
                            return node;
                        }

                        if (PathingUtil.isAirOrReplaceable(world.getBlockState(mutable.set(x, y, z)))
                            && PathingUtil.isAirOrReplaceable(world.getBlockState(mutable.set(x, y, z).move(ladderOrientation.getOpposite())))) {
                            // If there is no supporting block, but we're below the required height, build our own
                            node = getBuilderNode(x, y, z, PathAction.getTowerActionForDirection(ladderOrientation));
                            node.penalty /= 2;
                            return node;
                        }
                    }
                }
            }

            return node;
        }

        private PathNode getBuilderNode(int x, int y, int z, PathAction action) {
            PathNode node = getNode(x, y, z);
            node.type = PathNodeType.WALKABLE;
            node.penalty = 0.5F;
            return ActionablePathNode.setAction(node, action);
        }

        private boolean isBridgableGap(CollisionView world, BlockPos.Mutable mutable, int height) {
            int originalY = mutable.getY() + 1;
            try {
                for (int yOffset = 0; yOffset < height; yOffset++) {
                    PathNodeType type = getLandNodeType(entity, mutable.setY(originalY + yOffset));
                    if (type != PathNodeType.OPEN && type != PathNodeType.WATER && type != PathNodeType.LAVA) {
                        return false;
                    }
                }
                return true;
            } finally {
                mutable.setY(originalY);
            }
        }

        private boolean canBuildOnBlock(CollisionView world, BlockPos pos) {
            return world.getBlockState(pos).isAir() || !BlockMetadata.isIndestructible(world.getBlockState(pos)) || PathingUtil.hasAdjacentLadder(world, pos);
        }
    }
}
