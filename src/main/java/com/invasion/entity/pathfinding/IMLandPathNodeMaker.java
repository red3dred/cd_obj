package com.invasion.entity.pathfinding;

import com.invasion.block.BlockMetadata;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.CollisionView;
import net.minecraft.world.GameRules;

public class IMLandPathNodeMaker extends LandPathNodeMaker {
    private boolean canClimbLadders;
    private boolean canMineBlocks;
    private boolean canDigDown;

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

    public boolean getCanClimb() {
        return canClimbLadders;
    }

    public void setCanClimb(boolean flag) {
        canClimbLadders = flag;
    }


    @Override
    protected PathNode getNode(int x, int y, int z) {
        return getNode(x, y, z, PathAction.NONE);
    }

    protected PathNode getNode(int x, int y, int z, PathAction action) {
        return pathNodeCache.computeIfAbsent(ActionablePathNode.makeHash(x, y, z, action), l -> ActionablePathNode.create(x, y, z, action));
    }

    @Override
    protected TargetPathNode createNode(double x, double y, double z) {
        return ActionablePathNode.createTarget(getNode(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z)));
    }

    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        this.setCanDestroyBlocks(true);
        this.setCanDigDown(true);
        int index = super.getSuccessors(successors, node);

        boolean canClimb = getCanClimb();
        boolean canDigDown = getCanDigDown();

        if (canClimb || canDigDown) {
            PathNodeType currentNodeType = getNodeType(node.x, node.y, node.z);
            BlockPos pos = new BlockPos(node.x, node.y, node.z);
            double prevY = getFeetY(pos);
            if (PathingUtil.isLadder(entity.getWorld().getBlockState(pos))) {
                for (Direction direction : Direction.Type.VERTICAL) {
                    BlockPos p = pos.offset(direction);
                    BlockState state = entity.getWorld().getBlockState(p);
                    boolean isClimbable = canClimb && PathingUtil.isLadder(state);
                    boolean isDiggable = canDigDown && direction == Direction.DOWN && canMineBlock(entity.getWorld(), p, state);

                    if (isClimbable || isDiggable) {
                        PathNode n = getPathNode(node.x, node.y + direction.getOffsetY(), node.z, 1, prevY, direction, currentNodeType);
                        n.penalty = isClimbable ? 0 : getBlockStrength(pos, state);
                        successors[index++] = ActionablePathNode.setAction(n, isClimbable ? PathAction.getClimbing(direction) : PathAction.DIG);
                    }
                }
            }
        }

        return index;
    }

    @Override
    public PathNodeType getDefaultNodeType(PathContext context, int x, int y, int z) {
        BlockPos.Mutable pos = new BlockPos.Mutable(x, y, z);
        PathNodeType type = getLandNodeType(context, pos);
        if (getCanClimb() && PathingUtil.isLadder(context.getBlockState(pos))) {
            return PathNodeType.WALKABLE;
        }
        if (canDestroyBlocks() && type == PathNodeType.BLOCKED) {
            return PathNodeType.WALKABLE;
        }
        return type;
    }

    @Override
    protected PathNode getPathNode(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType) {
        PathNode node = super.getPathNode(x, y, z, maxYStep, prevFeetY, direction, nodeType);
        if (canDestroyBlocks() && node != null && getLandNodeType(entity, node.getBlockPos()) == PathNodeType.BLOCKED) {
            BlockPos pos = new BlockPos(node.x, node.y, node.z);
            BlockState state = context.getBlockState(pos);
            if (canMineBlock(context.getWorld(), pos, state)) {
                node.type = PathNodeType.WALKABLE;
                node.penalty = getBlockStrength(pos, state);
                return ActionablePathNode.setAction(node, PathAction.DIG);
            }

        }
        return node;
    }

    @Override
    protected boolean canPathThrough(BlockPos pos) {
        return super.canPathThrough(pos) || (canDestroyBlocks() && canMineBlock(entity.getWorld(), pos, entity.getWorld().getBlockState(pos)));
    }


    public float getBlockStrength(PathContext context, BlockPos pos) {
        return BlockMetadata.getStrength(pos, context.getBlockState(pos), context.getWorld());
    }

    public float getBlockStrength(BlockPos pos, BlockState state) {
        return BlockMetadata.getStrength(pos, state, entity.getWorld());
    }

    public boolean canMineBlock(CollisionView world, BlockPos pos, BlockState state) {
        if (state.isAir() || !canDestroyBlocks() || BlockMetadata.isIndestructible(state) || PathingUtil.hasAdjacentLadder(world, pos)) {
            return false;
        }
        return state.isIn(BlockTags.DOORS) || state.isIn(BlockTags.TRAPDOORS) || state.isSolidBlock(world, pos);
    }

    public boolean avoidsBlock(CollisionView world, BlockPos pos, BlockState state) {
        return PathingUtil.shouldAvoidBlock(entity, pos);
    }

    protected boolean canWalkOn(PathNodeType type) {
        return type != PathNodeType.DAMAGE_FIRE && type != PathNodeType.DANGER_FIRE && type != PathNodeType.LAVA && type != PathNodeType.STICKY_HONEY;
    }

    public static boolean canMineBlock(PathAwareEntity entity, BlockPos pos) {
        return entity.getNavigation().getNodeMaker() instanceof IMLandPathNodeMaker maker
                && maker.canMineBlock(entity.getWorld(), pos, entity.getWorld().getBlockState(pos));
    }

    public static boolean avoidsBlock(PathAwareEntity entity, BlockPos pos) {
        return entity.getNavigation().getNodeMaker() instanceof IMLandPathNodeMaker maker
                && maker.avoidsBlock(entity.getWorld(), pos, entity.getWorld().getBlockState(pos));
    }
}
