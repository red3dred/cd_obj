package invmod.common.entity;

import invmod.common.IPathfindable;
import invmod.common.nexus.INexusAccess;
import invmod.common.util.CoordsInt;
import invmod.common.util.IPosition;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class Scaffold implements IPathfindable, IPosition {
    private static final int MIN_SCAFFOLD_HEIGHT = 4;
    private BlockPos pos = BlockPos.ORIGIN;
    private int targetHeight;
    private int orientation;
    private int[] platforms;
    private IPathfindable pathfindBase;
    private INexusAccess nexus;
    private float latestPercentCompleted;
    private float latestPercentIntact;
    private float initialCompletion = 0.01F;

    public Scaffold(INexusAccess nexus) {
        this(0, 0, 0, 0, nexus);
    }

    public Scaffold(int x, int y, int z, int height, INexusAccess nexus) {
        this.targetHeight = height;
        this.nexus = nexus;
        setPosition(x, y, z);
        calcPlatforms();
    }

    public void setPosition(int x, int y, int z) {
        pos = new BlockPos(x, y, z);
    }

    public void setInitialIntegrity() {
        initialCompletion = evaluateIntegrity();
        if (initialCompletion == 0) {
            initialCompletion = 0.01F;
        }
    }

    public void setOrientation(int i) {
        this.orientation = i;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setHeight(int height) {
        targetHeight = height;
        calcPlatforms();
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public void forceStatusUpdate() {
        latestPercentIntact = (evaluateIntegrity() - initialCompletion) / (1 - initialCompletion);
        if (latestPercentIntact > latestPercentCompleted) {
            latestPercentCompleted = latestPercentIntact;
        }
    }

    public float getPercentIntactCached() {
        return this.latestPercentIntact;
    }

    public float getPercentCompletedCached() {
        return this.latestPercentCompleted;
    }

    @Override
    public int getXCoord() {
        return pos.getX();
    }

    @Override
    public int getYCoord() {
        return pos.getY();
    }

    @Override
    public int getZCoord() {
        return pos.getZ();
    }

    public INexusAccess getNexus() {
        return this.nexus;
    }

    public void setPathfindBase(IPathfindable base) {
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
        orientation = compound.getInt("orientation");
        initialCompletion = compound.getFloat("initialCompletion");
        latestPercentCompleted = compound.getFloat("latestPercentCompleted");
        calcPlatforms();
    }

    public void writeToNBT(NbtCompound compound) {
        compound.putInt("xCoord", pos.getX());
        compound.putInt("yCoord", pos.getY());
        compound.putInt("zCoord", pos.getZ());
        compound.putInt("targetHeight", targetHeight);
        compound.putInt("orientation", orientation);
        compound.putFloat("initialCompletion", initialCompletion);
        compound.putFloat("latestPercentCompleted", latestPercentCompleted);
    }

    private void calcPlatforms() {
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
            if (world.getBlockState(mutable.set(pos).move(CoordsInt.offsetAdjX[orientation], i, CoordsInt.offsetAdjZ[orientation])).isFullCube(world, mutable)) {
                existingMainSectionBlocks++;
            }
            if (world.getBlockState(mutable.set(pos).move(0, 1, 0)).isIn(BlockTags.CLIMBABLE)) {
                existingMainLadderBlocks++;
            }
            if (isLayerPlatform(i)) {
                for (int j = 0; j < 8; j++) {
                    if (world.getBlockState(mutable.set(pos).add(CoordsInt.offsetRing1X[j], i, CoordsInt.offsetRing1Z[j])).isFullCube(world, mutable)) {
                        existingPlatformBlocks++;
                    }
                }
            }
        }

        float mainSectionPercent = targetHeight > 0 ? existingMainSectionBlocks / targetHeight : 0;
        float ladderPercent = targetHeight > 0 ? existingMainLadderBlocks / targetHeight : 0;

        return 0.7F * (0.7F * mainSectionPercent + 0.3F * ladderPercent) + 0.3F * (existingPlatformBlocks / (platforms.length + 1) * 8);
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getBlockPathCost(PathNode prevNode, PathNode node, BlockView terrainMap) {
        float materialMultiplier = terrainMap.getBlockState(node.pos).isSolid() ? 2.2F : 1.0F;
        if (node.action == PathAction.SCAFFOLD_UP) {
            if (prevNode.action != PathAction.SCAFFOLD_UP) {
                materialMultiplier *= 3.4F;
            }
            return prevNode.distanceTo(node) * 0.85F * materialMultiplier;
        }
        if (node.action == PathAction.BRIDGE) {
            if (prevNode.action == PathAction.SCAFFOLD_UP) {
                materialMultiplier = 0;
            }
            return prevNode.distanceTo(node) * 1.1F * materialMultiplier;
        }
        if (node.action == PathAction.LADDER_UP_NX
                || node.action == PathAction.LADDER_UP_NZ
                || node.action == PathAction.LADDER_UP_PX
                || node.action == PathAction.LADDER_UP_PZ) {
            return prevNode.distanceTo(node) * 1.5F * materialMultiplier;
        }

        if (pathfindBase != null) {
            return pathfindBase.getBlockPathCost(prevNode, node, terrainMap);
        }
        return prevNode.distanceTo(node);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void getPathOptionsFromNode(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        if (pathfindBase != null) {
            pathfindBase.getPathOptionsFromNode(terrainMap, currentNode, pathFinder);
        }
        BlockPos positionAbove = currentNode.pos.up();
        BlockState block = terrainMap.getBlockState(positionAbove);
        if (currentNode.getPrevious() != null && currentNode.getPrevious().action == PathAction.SCAFFOLD_UP && !avoidsBlock(block)) {
            pathFinder.addNode(positionAbove, PathAction.SCAFFOLD_UP);
            return;
        }
        int minDistance;
        if (nexus != null) {
            List<Scaffold> scaffolds = nexus.getAttackerAI().getScaffolds();
            minDistance = nexus.getAttackerAI().getMinDistanceBetweenScaffolds();
            for (int sl = scaffolds.size() - 1; sl >= 0; sl--) {
                Scaffold scaffold = scaffolds.get(sl);
                if (scaffold.toBlockPos().isWithinDistance(currentNode.pos, minDistance)) {
                    return;
                }
            }
        }

        if (block.isAir() && terrainMap.getBlockState(currentNode.pos.down(2)).isSolid()) {
            boolean flag = false;
            for (int i = 1; i < MIN_SCAFFOLD_HEIGHT; i++) {
                if (terrainMap.getBlockState(currentNode.pos.up(i)).isAir()) {
                    flag = true;
                    break;
                }
            }

            if (!flag) {
                pathFinder.addNode(positionAbove, PathAction.SCAFFOLD_UP);
            }
        }
    }

    private boolean avoidsBlock(BlockState state) {
        return state.isIn(BlockTags.FIRE) || state.isOf(Blocks.BEDROCK) || state.isIn(BlockTags.DOORS) || state.getBlock() instanceof FluidBlock;
    }
}
