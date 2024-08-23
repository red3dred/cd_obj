package com.invasion.entity.ai.builder;

import java.util.List;

import com.invasion.entity.pathfinding.IMPathNodeMaker;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;
import com.invasion.nexus.NexusAccess;
import com.invasion.util.math.PosUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class Scaffold implements IMPathNodeMaker {
    private static final int MIN_SCAFFOLD_HEIGHT = 4;
    private BlockPos pos = BlockPos.ORIGIN;
    private int targetHeight;
    private Direction orientation = Direction.EAST;
    private int[] platforms;
    private IMPathNodeMaker pathfindBase;
    private NexusAccess nexus;
    private float latestPercentCompleted;
    private float latestPercentIntact;
    private float initialCompletion = 0.01F;

    public Scaffold(NexusAccess nexus) {
        this(0, 0, 0, 0, nexus);
    }

    public Scaffold(int x, int y, int z, int height, NexusAccess nexus) {
        this.targetHeight = height;
        this.nexus = nexus;
        setPosition(x, y, z);
        createPlatforms();
    }

    public void setPosition(int x, int y, int z) {
        pos = new BlockPos(x, y, z);
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setInitialIntegrity() {
        initialCompletion = evaluateIntegrity();
        if (initialCompletion == 0) {
            initialCompletion = 0.01F;
        }
    }

    public void setOrientation(Direction orientation) {
        this.orientation = orientation;
    }

    public Direction getOrientation() {
        return orientation;
    }

    public void setHeight(int height) {
        targetHeight = height;
        createPlatforms();
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public float getPercentIntactCached() {
        return this.latestPercentIntact;
    }

    public float getPercentCompletedCached() {
        return this.latestPercentCompleted;
    }

    public NexusAccess getNexus() {
        return this.nexus;
    }

    public void setPathfindBase(IMPathNodeMaker base) {
        this.pathfindBase = base;
    }

    public boolean isLayerPlatform(int height) {
        if (height == targetHeight - 1) {
            return true;
        }
        if (platforms != null) {
            for (int i : platforms) {
                if (i == height) {
                    return true;
                }
            }
        }
        return false;
    }

    public void readFromNBT(NbtCompound compound) {
        setPosition(compound.getInt("xCoord"), compound.getInt("yCoord"), compound.getInt("zCoord"));
        targetHeight = compound.getInt("targetHeight");
        orientation = Direction.fromHorizontal(compound.getInt("orientation"));
        initialCompletion = compound.getFloat("initialCompletion");
        latestPercentCompleted = compound.getFloat("latestPercentCompleted");
        createPlatforms();
    }

    public void writeToNBT(NbtCompound compound) {
        compound.putInt("xCoord", pos.getX());
        compound.putInt("yCoord", pos.getY());
        compound.putInt("zCoord", pos.getZ());
        compound.putInt("targetHeight", targetHeight);
        compound.putInt("orientation", orientation.getHorizontal());
        compound.putFloat("initialCompletion", initialCompletion);
        compound.putFloat("latestPercentCompleted", latestPercentCompleted);
    }

    public void forceStatusUpdate() {
        latestPercentIntact = (evaluateIntegrity() - initialCompletion) / (1 - initialCompletion);
        if (latestPercentIntact > latestPercentCompleted) {
            latestPercentCompleted = latestPercentIntact;
        }
    }

    private void createPlatforms() {
        int spanningPlatforms = this.targetHeight < 16 ? this.targetHeight / MIN_SCAFFOLD_HEIGHT - 1 : this.targetHeight / 5 - 1;
        if (spanningPlatforms > 0) {
            int avgSpace = this.targetHeight / (spanningPlatforms + 1);
            int remainder = this.targetHeight % (spanningPlatforms + 1) - 1;
            this.platforms = new int[spanningPlatforms];
            for (int i = 0; i < spanningPlatforms; i++) {
                this.platforms[i] = (avgSpace * (i + 1) - 1);
            }

            int i = spanningPlatforms - 1;
            while (remainder > 0) {
                this.platforms[i] += 1;
                i--;
                if (i < 0) {
                    i = spanningPlatforms - 1;
                    remainder--;
                }
                remainder--;
            }
        } else {
            this.platforms = new int[0];
        }
    }

    private float evaluateIntegrity() {
        if (this.nexus == null) {
            return 0;
        }

        int existingMainSectionBlocks = 0;
        int existingMainLadderBlocks = 0;
        int existingPlatformBlocks = 0;
        World world = nexus.getWorld();
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int i = 0; i < targetHeight; i++) {
            if (world.getBlockState(mutable.set(pos).move(orientation).move(Direction.UP, i)).isFullCube(world, mutable)) {
                existingMainSectionBlocks++;
            }
            if (world.getBlockState(mutable.set(pos).move(Direction.UP)).isIn(BlockTags.CLIMBABLE)) {
                existingMainLadderBlocks++;
            }
            if (isLayerPlatform(i)) {
                for (Vec3i offset : PosUtils.OFFSET_RING) {
                    if (world.getBlockState(mutable.set(pos).move(Direction.UP, i).move(offset)).isFullCube(world, mutable)) {
                        existingPlatformBlocks++;
                    }
                }
            }
        }

        float mainSectionPercent = targetHeight > 0 ? existingMainSectionBlocks / targetHeight : 0;
        float ladderPercent = targetHeight > 0 ? existingMainLadderBlocks / targetHeight : 0;

        return 0.7F * (0.7F * mainSectionPercent + 0.3F * ladderPercent) + 0.3F * (existingPlatformBlocks / (platforms.length + 1) * 8);
    }

    @Override
    public float getPathNodePenalty(PathNode prevNode, PathNode node, BlockView terrainMap) {
        PathAction action = ActionablePathNode.getAction(node);
        PathAction prevAction = ActionablePathNode.getAction(prevNode);
        BlockState state = terrainMap.getBlockState(node.getBlockPos());
        float materialMultiplier = state.isSolidBlock(terrainMap, node.getBlockPos()) ? 2.2F : 1.0F;
        if (action == PathAction.SCAFFOLD_UP) {
            if (prevAction != PathAction.SCAFFOLD_UP) {
                materialMultiplier *= 3.4F;
            }
            return prevNode.getDistance(node) * 0.85F * materialMultiplier;
        }
        if (action == PathAction.BRIDGE) {
            if (prevAction == PathAction.SCAFFOLD_UP) {
                materialMultiplier = 0;
            }
            return prevNode.getDistance(node) * 1.1F * materialMultiplier;
        }
        if (action.getType() == PathAction.Type.LADDER && action.isHorizontal()) {
            return prevNode.getDistance(node) * 1.5F * materialMultiplier;
        }

        if (pathfindBase != null) {
            return pathfindBase.getPathNodePenalty(prevNode, node, terrainMap);
        }
        return prevNode.getDistance(node);
    }

    @Override
    public void getSuccessors(BlockView terrainMap, PathNode currentNode, PathBuilder pathFinder) {
        if (pathfindBase != null) {
            pathfindBase.getSuccessors(terrainMap, currentNode, pathFinder);
        }
        BlockPos positionAbove = currentNode.getBlockPos().up();
        BlockState stateAbove = terrainMap.getBlockState(positionAbove);
        if (ActionablePathNode.getAction(currentNode.previous) == PathAction.SCAFFOLD_UP && !avoidsBlock(stateAbove)) {
            pathFinder.addNode(positionAbove, PathAction.SCAFFOLD_UP);
            return;
        }
        int minDistance;
        if (nexus != null) {
            List<Scaffold> scaffolds = nexus.getAttackerAI().getScaffolds();
            minDistance = nexus.getAttackerAI().getMinDistanceBetweenScaffolds();
            for (int sl = scaffolds.size() - 1; sl >= 0; sl--) {
                if (scaffolds.get(sl).pos.isWithinDistance(currentNode.getBlockPos(), minDistance)) {
                    return;
                }
            }
        }

        if (stateAbove.isAir()) {
            BlockPos.Mutable mutable = currentNode.getBlockPos().mutableCopy();
            if (terrainMap.getBlockState(mutable.move(Direction.DOWN, 2)).isSolidBlock(terrainMap, mutable)) {
                for (int i = 1; i < MIN_SCAFFOLD_HEIGHT; i++) {
                    if (terrainMap.getBlockState(mutable.set(currentNode.x, currentNode.y, currentNode.z).move(Direction.UP, i)).isAir()) {
                        return;
                    }
                }

                pathFinder.addNode(positionAbove, PathAction.SCAFFOLD_UP);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private boolean avoidsBlock(BlockState state) {
        return state.isIn(BlockTags.FIRE)
                || state.isOf(Blocks.BEDROCK)
                || state.isIn(BlockTags.DOORS)
                || state.isLiquid();
    }
}
