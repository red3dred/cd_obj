package com.invasion.entity.pathfinding;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.invasion.Debug;
import com.invasion.InvasionMod;
import com.invasion.block.BlockMetadata;
import com.invasion.entity.pathfinding.DynamicPathNodeNavigator.NodeFactory;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;
import com.invasion.nexus.IHasNexus;
import com.invasion.nexus.NexusAccess;
import com.invasion.nexus.ai.scaffold.ScaffoldView;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.CollisionView;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.ChunkCache;

public class IMLandPathNodeMaker extends LandPathNodeMaker implements DynamicPathNodeNavigator.RootNodeFactory, DynamicPathNodeNavigator.NodeCache {
    private boolean canClimbLadders;
    private boolean canMineBlocks;
    private boolean canDigDown;

    private DynamicPathNodeNavigator.NodeFactory delegate = DynamicPathNodeNavigator.NodeFactory.DEFAULT;
    private Consumer<CollisionView> chunkCacheModifier = a -> {};

    protected PathAction previousNodeAction = PathAction.NONE;
    protected BlockPos previousNodePosition = BlockPos.ORIGIN;

    public boolean canDestroyBlocks() {
        return canMineBlocks && (entity == null || entity.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING));
    }

    public void setCanDestroyBlocks(boolean flag) {
        canMineBlocks = flag;
    }

    public boolean getCanDigDown() {
        return canDigDown;
    }

    public void setCanDigDown(boolean flag) {
        canDigDown = flag;
    }

    public boolean getCanClimbLadders() {
        return canClimbLadders;
    }

    public void setCanClimbLadders(boolean flag) {
        canClimbLadders = flag;
    }

    @Override
    public void init(ChunkCache cachedWorld, MobEntity entity) {
        super.init(cachedWorld, entity);
        if (entity instanceof IHasNexus nexusHolder && nexusHolder.hasNexus()) {
            this.context = new PathContext(nexusHolder.getNexus().getAttackerAI().wrapEntityData(cachedWorld), entity);
            populateChunkCacheData(nexusHolder.getNexus(), context.getWorld());
        }
    }

    protected void populateChunkCacheData(NexusAccess nexus, CollisionView view) {
        this.chunkCacheModifier.accept(view);
    }

    @Override
    public void setDelegate(NodeFactory delegate, Consumer<CollisionView> chunkCacheModifier) {
        this.delegate = delegate;
    }

    @Override
    public PathNode getNode(int x, int y, int z, PathAction action) {
        return ActionablePathNode.setAction(getNode(x, y, z), action);
    }

    @Override
    public float getDistancePenalty(PathNode previousNode, PathNode nextNode, CollisionView world) {
        return delegate.getDistancePenalty(previousNode, nextNode, context.getWorld());
    }

    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        previousNodePosition = node.getBlockPos();
        previousNodeAction = ActionablePathNode.getAction(node);
        int index = getSuccessors(super.getSuccessors(successors, node), successors, node, context.getWorld(), this);
        if (Debug.DEBUG_PATHFINDING) {
            for (int i = 0; i < index; i++) {
                /*if (ActionablePathNode.getAction(successors[i]) != PathAction.NONE) {
                    successors[i].type = PathNodeType.WALKABLE;
                }*/
                if (successors[i].visited) {
                    InvasionMod.LOGGER.warn("{} Looping path detected at ({}) {} was returned in a previous iteration", entity, i, successors[i]);
                } else {
                    for (int j = 0; j < index; j++) {
                        if (i != j && successors[i].hashCode() == successors[j].hashCode()) {
                            InvasionMod.LOGGER.warn("{} Looping path detected at ({}) {} was repeated in this iteration and collides with ({}) {}", entity, i, successors[i], j, successors[j]);
                        }
                    }
                }
            }
        }
        return index;
    }

    @Override
    public int getSuccessors(int index, PathNode[] successors, PathNode node, CollisionView world, DynamicPathNodeNavigator.NodeCache cache) {

        PathNodeType currentNodeType = getNodeType(node.x, node.y, node.z);
        double prevY = getFeetY(node.getBlockPos());

        int stepHeight = MathHelper.floor(Math.max(1, entity.getStepHeight()));

        for (Direction direction : Direction.Type.VERTICAL) {
            PathNode n = getPathNode(node.x, node.y + direction.getOffsetY(), node.z, stepHeight, prevY, direction, currentNodeType);
            if (isValidVerticalSuccessor(n, node, direction)) {
                successors[index++] = n;
            }
        }

        return delegate.getSuccessors(index, successors, node, world, cache);
    }

    protected boolean isValidVerticalSuccessor(@Nullable PathNode node, PathNode successor, Direction direction) {
        return node != null && !node.visited && node.penalty != 0 && ActionablePathNode.getAction(node) != PathAction.NONE;
    }

    @Override
    public PathNodeType getDefaultNodeType(PathContext context, int x, int y, int z) {
        BlockPos.Mutable pos = new BlockPos.Mutable(x, y, z);
        PathNodeType type = getLandNodeType(context, pos);
        if (getCanClimbLadders() && PathingUtil.isLadder(context.getBlockState(pos))) {
            return PathNodeType.WALKABLE;
        }
        if (canDestroyBlocks() && type == PathNodeType.BLOCKED && !context.getBlockState(pos.move(Direction.UP)).canPathfindThrough(NavigationType.LAND)) {
            return PathNodeType.WALKABLE;
        }
        return type;
    }

    @Override
    protected boolean isValidDiagonalSuccessor(PathNode xNode, @Nullable PathNode zNode, @Nullable PathNode xDiagNode) {
        return super.isValidDiagonalSuccessor(xNode, zNode, xDiagNode)
                && (ActionablePathNode.getAction(xNode) != PathAction.DIG || ActionablePathNode.getAction(zNode) != PathAction.DIG);
    }

    protected boolean isNoActionNode(PathNode node) {
        return node != null && ActionablePathNode.getAction(node) == PathAction.NONE;
    }

    @Nullable
    @Override
    protected PathNode getPathNode(int x, int y, int z, int maxYStep, double feetY, Direction direction, PathNodeType nodeType) {
        @Nullable
        PathNode node = super.getPathNode(x, y, z, maxYStep, feetY, direction, nodeType);
        if (canDestroyBlocks() && node != null && getLandNodeType(context, new BlockPos.Mutable(node.x, node.y, node.z)) == PathNodeType.BLOCKED) {
            BlockPos pos = node.getBlockPos();
            BlockState state = context.getBlockState(pos);
            if (canMineBlock(context.getWorld(), pos, state) && !context.getBlockState(pos.up()).canPathfindThrough(NavigationType.LAND)) {
                node.type = PathNodeType.WALKABLE;
                node.penalty = getBlockStrength(pos, state);
                return ActionablePathNode.setAction(node, PathAction.DIG);
            }
        }

        if (node != null && node.type == PathNodeType.WALKABLE) {
            BlockPos pos = node.getBlockPos();
            float mobDensityMultiplier = 1 + (ScaffoldView.of(context.getWorld()).getMobDensity(pos) * 3);
            node.penalty += getWalkableNodePathingPenalty(context.getWorld(), pos, mobDensityMultiplier);
        }

        if (node != null) {
            boolean canClimb = getCanClimbLadders();
            boolean canDigDown = getCanDigDown();
            BlockPos pos = new BlockPos(x, y, z);

            if (canClimb && PathingUtil.isLadder(entity.getWorld().getBlockState(pos))) {
                ActionablePathNode.setAction(node, previousNodeAction.getType() == PathAction.Type.CLIMB ? previousNodeAction
                            : PathAction.getClimbing(direction.getAxis().isVertical() ? direction
                            : node.y < (int)feetY ? Direction.DOWN
                            : Direction.UP
                ));
            }

            if (direction == Direction.DOWN && canDigDown && ActionablePathNode.getAction(node) == PathAction.NONE) {
                BlockState nextState = entity.getWorld().getBlockState(pos);
                boolean isDiggable = canDigDown && direction == Direction.DOWN && canMineBlock(entity.getWorld(), pos, nextState);

                if (isDiggable) {
                    int gapHeight = 0;
                    int magDropHeight = entity.getSafeFallDistance();
                    for (int i = 0; i <= magDropHeight; i++) {
                        if (PathingUtil.isAirOrReplaceable(entity.getWorld().getBlockState(pos.down(1 + i)))) {
                            gapHeight++;
                        }
                    }
                    if (gapHeight > 0 && gapHeight <= magDropHeight) {
                        node.penalty = getBlockStrength(pos, nextState);
                        ActionablePathNode.setAction(node, PathAction.DIG);
                    }
                }
            }
        }

        return node;
    }

    protected float getWalkableNodePathingPenalty(CollisionView world, BlockPos pos, float mobDensityMultiplier) {
        BlockState state = context.getBlockState(pos);
        return BlockMetadata.getCost(state).orElse(state.isSolidBlock(world, pos) ? 3.2F : 1) * mobDensityMultiplier;
    }

    @Override
    protected boolean canPathThrough(BlockPos pos) {
        return super.canPathThrough(pos)
                || (canDestroyBlocks()
                        && canMineBlock(context.getWorld(), pos, context.getBlockState(pos))
                        && !context.getBlockState(pos.up()).canPathfindThrough(NavigationType.LAND));
    }

    public float getBlockStrength(BlockPos pos, BlockState state) {
        return BlockMetadata.getStrength(pos, state, context.getWorld());
    }

    public boolean canMineBlock(CollisionView world, BlockPos pos, BlockState state) {
        if (state.isAir() || !canDestroyBlocks() || BlockMetadata.isIndestructible(state) || PathingUtil.hasAdjacentLadder(world, pos)) {
            return false;
        }
        return state.isIn(BlockTags.DOORS) || state.isIn(BlockTags.TRAPDOORS) || state.isSolidBlock(world, pos);
    }

    public boolean avoidsBlock(MobEntity entity, CollisionView world, BlockPos pos, BlockState state) {
        return PathingUtil.shouldAvoidBlock(entity, pos);
    }

    @Override
    public final boolean avoidsBlock(CollisionView world, BlockPos pos, BlockState state) {
        return avoidsBlock(entity, world, pos, state);
    }

    protected boolean canWalkOn(PathNodeType type) {
        return type != PathNodeType.DAMAGE_FIRE && type != PathNodeType.DANGER_FIRE && type != PathNodeType.LAVA && type != PathNodeType.STICKY_HONEY;
    }

    public static boolean canMineBlock(PathAwareEntity entity, BlockPos pos) {
        return entity.getNavigation().getNodeMaker() instanceof IMLandPathNodeMaker maker
                && maker.canMineBlock(entity.getWorld(), pos, entity.getWorld().getBlockState(pos));
    }

    public static boolean avoidsBlock(PathAwareEntity entity, BlockPos pos) {
        if (entity.getNavigation().getNodeMaker() instanceof IMLandPathNodeMaker maker) {
            return maker.avoidsBlock(entity, entity.getWorld(), pos, entity.getWorld().getBlockState(pos));
        }
        return PathingUtil.shouldAvoidBlock(entity, pos);
    }
}
