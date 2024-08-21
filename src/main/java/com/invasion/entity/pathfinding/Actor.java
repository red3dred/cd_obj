package com.invasion.entity.pathfinding;

import java.util.Optional;

import com.invasion.IBlockAccessExtended;
import com.invasion.block.BlockMetadata;
import com.invasion.block.DestructableType;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;
import com.invasion.util.math.PosUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;

@Deprecated
public class Actor<T extends Entity> implements IMPathNodeMaker {
    protected final T entity;

    private boolean canClimb;
    private boolean canDig;
    private boolean canMin;
    private boolean canSwim;

    private Optional<BlockPos> currentTargetPos = Optional.empty();

    public Actor(T entity) {
        this.entity = entity;
    }

    public Optional<BlockPos> getCurrentTargetPos() {
        return currentTargetPos;
    }

    public void setCurrentTargetPos(BlockPos pos) {
        currentTargetPos = Optional.of(pos);
    }

    public boolean getCanClimb() {
        return canClimb;
    }

    public void setCanClimb(boolean flag) {
        canClimb = flag;
    }

    public boolean getCanDigDown() {
        return canDig;
    }

    public boolean canDestroyBlocks() {
        return canMin && entity.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
    }

    public void setCanDestroyBlocks(boolean flag) {
        canMin = flag;
    }

    public boolean canSwimHorizontal() {
        return canSwim;
    }

    public boolean canSwimVertical() {
        return canSwim;
    }

    public void setCanSwim(boolean flag) {
        canSwim = flag;
    }

    public float getBlockStrength(BlockPos pos) {
        return getBlockStrength(pos, entity.getWorld().getBlockState(pos));
    }

    public float getBlockStrength(BlockPos pos, BlockState state) {
        return BlockMetadata.getStrength(pos, state, entity.getWorld());
    }

    public boolean avoidsBlock(BlockState state) {
        return !entity.isInvulnerable()
                && (
                        (!entity.isFireImmune() && PathNodeMaker.isFireDamaging(state))
                    || state.isOf(Blocks.BEDROCK)
                    || state.isOf(Blocks.CACTUS)
            );
    }

    public boolean isBlockDestructible(BlockView world, BlockPos pos, BlockState state) {
        if (state.getHardness(world, pos) < 0 || state.isOf(Blocks.COMMAND_BLOCK) || state.isOf(Blocks.CHAIN_COMMAND_BLOCK) || state.isOf(Blocks.REPEATING_COMMAND_BLOCK)) {
            return false;
        }
        if (state.isAir() || !canDestroyBlocks() || BlockMetadata.isIndestructible(state) || blockHasLadder(world, pos)) {
            return false;
        }
        return state.isIn(BlockTags.DOORS) || state.isIn(BlockTags.TRAPDOORS) || state.isSolidBlock(world, pos);
    }

    @Override
    public float getPathNodePenalty(PathNode prevNode, PathNode node, BlockView terrainMap) {
        float multiplier = 1 + ((IBlockAccessExtended.getData(terrainMap, node.getBlockPos()) & IBlockAccessExtended.MOB_DENSITY_FLAG) * 3);

        if (node.y > prevNode.y && getNodeDestructability(terrainMap, node.getBlockPos()) == DestructableType.DESTRUCTABLE) {
            multiplier += 2;
        }

        if (blockHasLadder(terrainMap, node.getBlockPos())) {
            multiplier += 5;
        }

        if (ActionablePathNode.getAction(node) == PathAction.SWIM) {
            multiplier *= node.y <= prevNode.y && !terrainMap.getBlockState(node.getBlockPos()).isAir() ? 3 : 1;
            return prevNode.getDistance(node) * 1.3F * multiplier;
        }

        BlockState state = terrainMap.getBlockState(node.getBlockPos());
        return prevNode.getDistance(node) * BlockMetadata.getCost(state).orElse(state.isSolidBlock(terrainMap, node.getBlockPos()) ? 3.2F : 1) * multiplier;
    }

    @Override
    public void getSuccessors(BlockView terrainMap, PathNode currentNode, PathBuilder pathFinder) {
        if (entity.getWorld().isOutOfHeightLimit(currentNode.getBlockPos())) {
            return;
        }

        calcPathOptionsVertical(terrainMap, currentNode, pathFinder);

        PathAction action = ActionablePathNode.getAction(currentNode);

        if (action == PathAction.DIG && !canStandAt(terrainMap, currentNode.getBlockPos())) {
            return;
        }

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int height = MathHelper.ceil(entity.getStepHeight());
        for (int i = 1; i <= height; i++) {
            if (getNodeDestructability(terrainMap, mutable.set(currentNode.x, currentNode.y, currentNode.z).move(Direction.UP, i)) == DestructableType.UNBREAKABLE) {
                height = i - 1;
            }
        }

        int maxFall = 8;
        for (Direction facing : Direction.Type.HORIZONTAL) {
            if (action != PathAction.NONE) {
                if (facing == Direction.EAST && action == PathAction.LADDER_UP_NX) {
                    height = 0;
                }
                if (facing == Direction.WEST && action == PathAction.LADDER_UP_PX) {
                    height = 0;
                }
                if (facing == Direction.SOUTH && action == PathAction.LADDER_UP_NZ) {
                    height = 0;
                }
                if (facing == Direction.NORTH && action == PathAction.LADDER_UP_PZ) {
                    height = 0;
                }
            }
            int currentY = currentNode.y + height;
            boolean passedLevel = false;
            do {
                int yOffset = getNextLowestSafeYOffset(terrainMap,
                        mutable.set(currentNode.x, currentY, currentNode.z).move(facing),
                        maxFall + currentY - currentNode.y
                );
                if (yOffset > 0) {
                    break;
                }
                if (yOffset > -maxFall) {
                    pathFinder.addNode(mutable.move(Direction.UP, yOffset).toImmutable(), PathAction.NONE);
                }

                currentY += yOffset - 1;

                if ((!passedLevel) && (currentY <= currentNode.y)) {
                    passedLevel = true;
                    if (currentY != currentNode.y) {
                        addAdjacent(terrainMap, currentNode.getBlockPos().offset(facing), currentNode, pathFinder);
                    }

                }

            } while (currentY >= currentNode.y);
        }

        if (canSwimHorizontal()) {
            for (Direction offset : Direction.Type.HORIZONTAL) {
                if (getNodeDestructability(terrainMap, mutable.set(currentNode.x, currentNode.y, currentNode.z).move(offset)) == DestructableType.FLUID) {
                    pathFinder.addNode(mutable.toImmutable(), PathAction.SWIM);
                }
            }
        }
    }

    protected void calcPathOptionsVertical(BlockView terrainMap, PathNode currentNode, PathBuilder pathFinder) {
        int collideUp = getNodeDestructability(terrainMap, currentNode.getBlockPos().up());
        PathAction pathAction = ActionablePathNode.getAction(currentNode);
        if (collideUp > DestructableType.UNBREAKABLE) {
            BlockState state = terrainMap.getBlockState(currentNode.getBlockPos().up());
            if (state.isIn(BlockTags.CLIMBABLE)) {
                Direction facing = state.getOrEmpty(HorizontalFacingBlock.FACING).orElse(null);
                PathAction action = PathAction.getLadderActionForDirection(facing);

                if (pathAction == PathAction.NONE) {
                    pathFinder.addNode(currentNode.getBlockPos().up(), action);
                } else if (pathAction.getType() == PathAction.Type.LADDER && pathAction.getBuildDirection() != Direction.UP) {
                    if (action == pathAction) {
                        pathFinder.addNode(currentNode.getBlockPos().up(), action);
                    }
                } else {
                    pathFinder.addNode(currentNode.getBlockPos().up(), action);
                }
            } else if (getCanClimb()) {
                if (isAdjacentSolidBlock(terrainMap, currentNode.getBlockPos().up())) {
                    pathFinder.addNode(currentNode.getBlockPos().up(), PathAction.NONE);
                }
            }
        }
        int below = getNodeDestructability(terrainMap, currentNode.getBlockPos().down());
        int above = getNodeDestructability(terrainMap, currentNode.getBlockPos().up());
        if (getCanDigDown()) {
            if (below == DestructableType.DESTRUCTABLE) {
                pathFinder.addNode(currentNode.getBlockPos().down(), PathAction.DIG);
            } else if (below == DestructableType.TERRAIN) {
                int yOffset = getNextLowestSafeYOffset(terrainMap, currentNode.getBlockPos().down(), 5);
                if (yOffset <= 0) {
                    pathFinder.addNode(currentNode.getBlockPos().up(yOffset - 1), PathAction.NONE);
                }
            }
        }

        if (canSwimVertical()) {
            if (below == -1) {
                pathFinder.addNode(currentNode.getBlockPos().down(), PathAction.SWIM);
            }
            if (above == -1) {
                pathFinder.addNode(currentNode.getBlockPos().up(), PathAction.SWIM);
            }
        }
    }

    protected final void addAdjacent(BlockView terrainMap, BlockPos pos, PathNode currentNode, PathBuilder pathFinder) {
        if (getNodeDestructability(terrainMap, pos) <= DestructableType.UNBREAKABLE) {
            return;
        }
        if (getCanClimb()) {
            if (isAdjacentSolidBlock(terrainMap, pos)) {
                pathFinder.addNode(pos, PathAction.NONE);
            }
        } else if (terrainMap.getBlockState(pos).isIn(BlockTags.CLIMBABLE)) {
            pathFinder.addNode(pos, PathAction.NONE);
        }
    }

    protected final boolean isAdjacentSolidBlock(BlockView terrainMap, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Vec3i offset
                : entity.getWidth() == 1 ? PosUtils.OFFSET_ADJACENT
                : entity.getWidth() == 2 ? PosUtils.OFFSET_ADJACENT_2
                : PosUtils.ZERO) {
            BlockState state = terrainMap.getBlockState(mutable.set(pos).add(offset));
            if (!state.isAir() && !state.canPathfindThrough(NavigationType.LAND)) {
                return true;
            }
        }
        return false;
    }

    protected final int getNextLowestSafeYOffset(BlockView world, BlockPos pos, int maxOffsetMagnitude) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int i = 0; !world.isOutOfHeightLimit(i + pos.getY()) && i < maxOffsetMagnitude; i--) {
            mutable.set(pos).move(Direction.UP, i);
            if (canStandAtAndIsValid(world, mutable) || (canSwimHorizontal() && getNodeDestructability(world, mutable) == DestructableType.FLUID)) {
                return i;
            }
        }
        return 1;
    }

    public final boolean canStandAt(BlockView world, BlockPos pos) {
        for (BlockPos p : BlockPos.stream(entity.getDimensions(entity.getPose()).getBoxAt(pos.toBottomCenterPos())).toList()) {
            BlockState state = world.getBlockState(p);
            if ((!state.isAir() && !state.canPathfindThrough(NavigationType.LAND)) || avoidsBlock(state)) {
                return false;
            }
        }
        return canStandOnBlock(world, pos.down());
    }

    public boolean canStandAtAndIsValid(BlockView world, BlockPos pos) {
        return getNodeDestructability(world, pos) > DestructableType.UNBREAKABLE && canStandAt(world, pos);
    }

    protected final boolean canStandOnBlock(BlockView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.hasSolidTopSurface(world, pos, entity) && !avoidsBlock(state);
    }

    protected static boolean blockHasLadder(BlockView world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Direction offset : Direction.Type.HORIZONTAL) {
            if (world.getBlockState(mutable.set(pos).move(offset)).isIn(BlockTags.CLIMBABLE)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    protected final int getNodeDestructability(BlockView terrainMap, BlockPos pos) {
        boolean destructibleFlag = false;
        boolean liquidFlag = false;

        for (BlockPos p : BlockPos.stream(entity.getDimensions(entity.getPose()).getBoxAt(pos.toBottomCenterPos())).toList()) {
            BlockState state = terrainMap.getBlockState(p);
            if (!state.isAir()) {
                if (state.isLiquid()) {
                    liquidFlag = true;
                } else if (!state.canPathfindThrough(NavigationType.LAND)) {
                    if (!isBlockDestructible(terrainMap, p, state)) {
                        return DestructableType.UNBREAKABLE;
                    }
                    destructibleFlag = true;
                } else {
                    state = terrainMap.getBlockState(p.down());
                    if (state.isIn(BlockTags.WOODEN_FENCES)) {
                        return isBlockDestructible(terrainMap, pos, state) ? DestructableType.BREAKABLE_BARRIER : DestructableType.UNBREAKABLE;
                    }
                }

                if (avoidsBlock(state)) {
                    return DestructableType.REPELLANT;
                }
            }
        }
        return destructibleFlag ? DestructableType.DESTRUCTABLE : liquidFlag ? DestructableType.FLUID : DestructableType.TERRAIN;
    }
}
