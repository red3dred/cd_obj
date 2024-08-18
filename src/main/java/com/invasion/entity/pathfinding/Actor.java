package com.invasion.entity.pathfinding;

import java.util.Optional;

import com.invasion.IBlockAccessExtended;
import com.invasion.block.BlockMetadata;
import com.invasion.block.DestructableType;
import com.invasion.util.math.CoordsInt;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;

public class Actor<T extends Entity> implements IPathfindable {
    protected final T entity;

    private boolean canClimb;
    private boolean canDig;
    private boolean canMin;

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
        return true;
    }

    public boolean canSwimVertical() {
        return true;
    }

    public float getBlockStrength(BlockPos pos) {
        return getBlockStrength(pos, entity.getWorld().getBlockState(pos));
    }

    public float getBlockStrength(BlockPos pos, BlockState state) {
        return BlockMetadata.getStrength(pos, state, entity.getWorld());
    }

    public boolean avoidsBlock(BlockState state) {
        return !entity.isInvulnerable()
                && (!entity.isFireImmune() && (state.isIn(BlockTags.FIRE)
                        || state.isIn(BlockTags.CAMPFIRES)
                        || state.getFluidState().isIn(FluidTags.LAVA))
                || state.isOf(Blocks.BEDROCK)
                || state.isOf(Blocks.CACTUS));
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
    public float getBlockPathCost(PathNode prevNode, PathNode node, BlockView terrainMap) {
        float multiplier = 1 + ((IBlockAccessExtended.getData(terrainMap, node.pos) & IBlockAccessExtended.MOB_DENSITY_FLAG) * 3);

        if (node.pos.getY() > prevNode.pos.getY() && getCollide(terrainMap, node.pos) == DestructableType.DESTRUCTABLE) {
            multiplier += 2;
        }

        if (blockHasLadder(terrainMap, node.pos)) {
            multiplier += 5;
        }

        if (node.action == PathAction.SWIM) {
            multiplier *= node.pos.getY() <= prevNode.pos.getY() && !terrainMap.getBlockState(node.pos).isAir() ? 3 : 1;
            return prevNode.distanceTo(node) * 1.3F * multiplier;
        }

        BlockState state = terrainMap.getBlockState(node.pos);
        return prevNode.distanceTo(node) * BlockMetadata.getCost(state).orElse(state.isSolidBlock(terrainMap, node.pos) ? 3.2F : 1) * multiplier;
    }

    @Override
    public void getPathOptionsFromNode(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        if (entity.getWorld().isOutOfHeightLimit(currentNode.pos)) {
            return;
        }

        calcPathOptionsVertical(terrainMap, currentNode, pathFinder);

        if (currentNode.action == PathAction.DIG && !canStandAt(terrainMap, currentNode.pos)) {
            return;
        }

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int height = MathHelper.ceil(entity.getStepHeight());
        for (int i = 1; i <= height; i++) {
            if (getCollide(terrainMap, mutable.set(currentNode.pos).move(Direction.UP, i)) == DestructableType.UNBREAKABLE) {
                height = i - 1;
            }
        }

        int maxFall = 8;
        for (Direction facing : CoordsInt.CARDINAL_DIRECTIONS) {
            if (currentNode.action != PathAction.NONE) {
                if (facing == Direction.EAST && currentNode.action == PathAction.LADDER_UP_NX) {
                    height = 0;
                }
                if (facing == Direction.WEST && currentNode.action == PathAction.LADDER_UP_PX) {
                    height = 0;
                }
                if (facing == Direction.SOUTH && currentNode.action == PathAction.LADDER_UP_NZ) {
                    height = 0;
                }
                if (facing == Direction.NORTH && currentNode.action == PathAction.LADDER_UP_PZ) {
                    height = 0;
                }
            }
            int currentY = currentNode.pos.getY() + height;
            boolean passedLevel = false;
            do {
                int yOffset = getNextLowestSafeYOffset(terrainMap,
                        mutable.set(currentNode.pos).setY(currentY).move(facing),
                        maxFall + currentY - currentNode.pos.getY()
                );
                if (yOffset > 0) {
                    break;
                }
                if (yOffset > -maxFall) {
                    pathFinder.addNode(mutable.move(Direction.UP, yOffset).toImmutable(), PathAction.NONE);
                }

                currentY += yOffset - 1;

                if ((!passedLevel) && (currentY <= currentNode.pos.getY())) {
                    passedLevel = true;
                    if (currentY != currentNode.pos.getY()) {
                        addAdjacent(terrainMap, currentNode.pos.offset(facing), currentNode, pathFinder);
                    }

                }

            } while (currentY >= currentNode.pos.getY());
        }

        if (canSwimHorizontal()) {
            for (Direction offset : CoordsInt.CARDINAL_DIRECTIONS) {
                if (getCollide(terrainMap, mutable.set(currentNode.pos).move(offset)) == -1) {
                    pathFinder.addNode(mutable.toImmutable(), PathAction.SWIM);
                }
            }
        }
    }

    protected void calcPathOptionsVertical(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        int collideUp = getCollide(terrainMap, currentNode.pos.up());
        if (collideUp > DestructableType.UNBREAKABLE) {
            BlockState state = terrainMap.getBlockState(currentNode.pos.up());
            if (state.isIn(BlockTags.CLIMBABLE)) {
                Direction facing = state.getOrEmpty(HorizontalFacingBlock.FACING).orElse(null);
                PathAction action = PathAction.getLadderActionForDirection(facing);

                if (currentNode.action == PathAction.NONE) {
                    pathFinder.addNode(currentNode.pos.up(), action);
                } else if (currentNode.action.getType() == PathAction.Type.LADDER && currentNode.action.getBuildDirection() != Direction.UP) {
                    if (action == currentNode.action) {
                        pathFinder.addNode(currentNode.pos.up(), action);
                    }
                } else {
                    pathFinder.addNode(currentNode.pos.up(), action);
                }
            } else if (getCanClimb()) {
                if (isAdjacentSolidBlock(terrainMap, currentNode.pos.up())) {
                    pathFinder.addNode(currentNode.pos.up(), PathAction.NONE);
                }
            }
        }
        int below = getCollide(terrainMap, currentNode.pos.down());
        int above = getCollide(terrainMap, currentNode.pos.up());
        if (getCanDigDown()) {
            if (below == DestructableType.DESTRUCTABLE) {
                pathFinder.addNode(currentNode.pos.down(), PathAction.DIG);
            } else if (below == DestructableType.TERRAIN) {
                int yOffset = getNextLowestSafeYOffset(terrainMap, currentNode.pos.down(), 5);
                if (yOffset <= 0) {
                    pathFinder.addNode(currentNode.pos.up(yOffset - 1), PathAction.NONE);
                }
            }
        }

        if (canSwimVertical()) {
            if (below == -1) {
                pathFinder.addNode(currentNode.pos.down(), PathAction.SWIM);
            }
            if (above == -1) {
                pathFinder.addNode(currentNode.pos.up(), PathAction.SWIM);
            }
        }
    }

    protected final void addAdjacent(BlockView terrainMap, BlockPos pos, PathNode currentNode, PathfinderIM pathFinder) {
        if (getCollide(terrainMap, pos) <= DestructableType.UNBREAKABLE) {
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
                : entity.getWidth() == 1 ? CoordsInt.OFFSET_ADJACENT
                : entity.getWidth() == 2 ? CoordsInt.OFFSET_ADJACENT_2
                : CoordsInt.ZERO) {
            BlockState state = terrainMap.getBlockState(mutable.set(pos).add(offset));
            if (!state.isAir() && state.isSolidBlock(terrainMap, mutable)) {
                return true;
            }
        }
        return false;
    }

    protected final int getNextLowestSafeYOffset(BlockView world, BlockPos pos, int maxOffsetMagnitude) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int i = 0; i + pos.getY() > world.getBottomY() && i < maxOffsetMagnitude; i--) {
            mutable.set(pos).move(Direction.UP, i);
            if (canStandAtAndIsValid(world, mutable) || (canSwimHorizontal() && getCollide(world, mutable) == -1)) {
                return i;
            }
        }
        return 1;
    }

    @SuppressWarnings("deprecation")
    public final boolean canStandAt(BlockView world, BlockPos pos) {
        for (BlockPos p : BlockPos.stream(entity.getDimensions(entity.getPose()).getBoxAt(pos.toBottomCenterPos())).toList()) {
            BlockState state = world.getBlockState(p);
            if (!state.isAir() && state.blocksMovement() || avoidsBlock(state)) {
                return false;
            }
        }
        return canStandOnBlock(world, pos.down());
    }

    public boolean canStandAtAndIsValid(BlockView world, BlockPos pos) {
        return getCollide(world, pos) > DestructableType.UNBREAKABLE && canStandAt(world, pos);
    }

    protected boolean canStandOnBlock(BlockView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.hasSolidTopSurface(world, pos, entity) && !avoidsBlock(state);
    }

    protected boolean blockHasLadder(BlockView world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Direction offset : CoordsInt.CARDINAL_DIRECTIONS) {
            if (world.getBlockState(mutable.set(pos).move(offset)).isIn(BlockTags.CLIMBABLE)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    protected final int getCollide(BlockView terrainMap, BlockPos pos) {
        boolean destructibleFlag = false;
        boolean liquidFlag = false;

        for (BlockPos p : BlockPos.stream(entity.getDimensions(entity.getPose()).getBoxAt(pos.toBottomCenterPos())).toList()) {
            BlockState state = terrainMap.getBlockState(p);
            if (!state.isAir()) {
                if (state.isLiquid()) {
                    liquidFlag = true;
                } else if (!state.blocksMovement()) {
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
