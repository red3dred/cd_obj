package com.invasion.entity.pathfinding;

import com.invasion.block.BlockMetadata;
import com.invasion.block.DestructableType;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;

public class IMLandPathNodeMaker extends LandPathNodeMaker {
    private boolean canClimbLadders;
    private boolean canMineBlocks;
    private boolean canDigDown;

    public boolean canDestroyBlocks() {
        return canMineBlocks && entity.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
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
        // TODO: Port existing logic to account for block density
        return super.getSuccessors(successors, node);
    }

    @Override
    public PathNodeType getDefaultNodeType(PathContext context, int x, int y, int z) {
        return getLandNodeType(context, new BlockPos.Mutable(x, y, z));
    }

    @Override
    protected boolean canPathThrough(BlockPos pos) {
        // TODO: Pathfinding through obstacles
        return super.canPathThrough(pos);
    }


    public float getBlockStrength(PathContext context, BlockPos pos) {
        return BlockMetadata.getStrength(pos, context.getBlockState(pos), context.getWorld());
    }

    public float getBlockStrength(BlockPos pos, BlockState state) {
        return BlockMetadata.getStrength(pos, state, entity.getWorld());
    }

    public boolean isBlockDestructible(BlockView world, BlockPos pos, BlockState state) {
        if (state.getHardness(world, pos) < 0 || state.isOf(Blocks.COMMAND_BLOCK) || state.isOf(Blocks.CHAIN_COMMAND_BLOCK) || state.isOf(Blocks.REPEATING_COMMAND_BLOCK)) {
            return false;
        }
        if (state.isAir() || !canDestroyBlocks() || BlockMetadata.isIndestructible(state) || isLadderAdjacent(world, pos)) {
            return false;
        }
        return state.isIn(BlockTags.DOORS) || state.isIn(BlockTags.TRAPDOORS) || state.isSolidBlock(world, pos);
    }

    protected static boolean isLadderAdjacent(BlockView world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Direction offset : Direction.Type.HORIZONTAL) {
            if (world.getBlockState(mutable.set(pos).move(offset)).isIn(BlockTags.CLIMBABLE)) {
                return true;
            }
        }
        return false;
    }

    protected boolean canWalkOn(PathNodeType type) {
        return type != PathNodeType.DAMAGE_FIRE && type != PathNodeType.DANGER_FIRE && type != PathNodeType.LAVA && type != PathNodeType.STICKY_HONEY;
    }

    @SuppressWarnings("deprecation")
    protected final int getNodeDestructability(PathContext context, BlockPos pos) {
        boolean destructibleFlag = false;
        boolean liquidFlag = false;

        BlockPos.Mutable p = new BlockPos.Mutable();
        for(int i = 0; i < entityBlockXSize; ++i) {
            for(int j = 0; j < entityBlockYSize; ++j) {
                for(int k = 0; k < entityBlockZSize; ++k) {
                    BlockState state = context.getBlockState(p.set(pos).move(i, j, k));
                    if (!state.isAir()) {
                        if (state.isLiquid()) {
                            liquidFlag = true;
                        } else if (!state.canPathfindThrough(NavigationType.LAND)) {
                            if (!isBlockDestructible(context.getWorld(), p, state)) {
                                return DestructableType.UNBREAKABLE;
                            }
                            destructibleFlag = true;
                        } else {
                            state = context.getBlockState(p.down());
                            if (state.isIn(BlockTags.WOODEN_FENCES)) {
                                return isBlockDestructible(context.getWorld(), pos, state) ? DestructableType.BREAKABLE_BARRIER : DestructableType.UNBREAKABLE;
                            }
                        }

                        if (!canWalkOn(context.getNodeType(p.getX(), p.getY(), p.getZ()))) {
                            return DestructableType.REPELLANT;
                        }
                    }
                }
            }
        }
        return destructibleFlag ? DestructableType.DESTRUCTABLE : liquidFlag ? DestructableType.FLUID : DestructableType.TERRAIN;
    }
}
