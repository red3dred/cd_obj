package com.invasion.nexus.ai.scaffold;

import com.invasion.nexus.NexusAccess;
import com.invasion.util.math.PosUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class Scaffold {
    private static final int MIN_SCAFFOLD_HEIGHT = 4;

    private final NexusAccess nexus;

    private ScaffoldNode node;

    private int[] platforms;
    private float latestPercentCompleted;
    private float latestPercentIntact;
    private float initialCompletion = 0.01F;

    public Scaffold(ScaffoldNode node, NexusAccess nexus) {
        this.nexus = nexus;
        this.node = node;
        this.platforms = createPlatforms(node);
    }

    public Scaffold(NbtCompound compound, NexusAccess nexus) {
        this.nexus = nexus;
        node = new ScaffoldNode(compound);
        initialCompletion = compound.getFloat("initialCompletion");
        latestPercentCompleted = compound.getFloat("latestPercentCompleted");
        platforms = createPlatforms(node);
    }

    public ScaffoldNode getNode() {
        return node;
    }

    public boolean merge(ScaffoldNode newScaffold) {
        newScaffold = node.merge(newScaffold);
        if (newScaffold != node) {
            node = newScaffold;
            platforms = createPlatforms(node);
            return true;
        }
        return false;
    }

    public boolean isPlatformLayer(int height) {
        if (height == node.height() - 1) {
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

    public boolean updateStatus() {
        latestPercentIntact = (calculateIntactPercentage() - initialCompletion) / (1 - initialCompletion);
        latestPercentCompleted = Math.max(latestPercentCompleted, latestPercentIntact);

        return latestPercentIntact + 0.05F < 0.4F * latestPercentCompleted;
    }

    private float calculateIntactPercentage() {
        int existingMainSectionBlocks = 0;
        int existingMainLadderBlocks = 0;
        int existingPlatformBlocks = 0;
        World world = nexus.getWorld();
        BlockPos.Mutable mutable = node.pos().mutableCopy();
        for (int i = 0; i < node.height(); i++) {
            if (world.getBlockState(mutable.set(node.pos()).move(node.orientation()).move(Direction.UP, i)).isFullCube(world, mutable)) {
                existingMainSectionBlocks++;
            }
            if (world.getBlockState(mutable.set(node.pos()).move(Direction.UP)).isIn(BlockTags.CLIMBABLE)) {
                existingMainLadderBlocks++;
            }
            if (isPlatformLayer(i)) {
                for (Vec3i offset : PosUtils.OFFSET_RING) {
                    if (world.getBlockState(mutable.set(node.pos()).move(Direction.UP, i).move(offset)).isFullCube(world, mutable)) {
                        existingPlatformBlocks++;
                    }
                }
            }
        }

        float mainSectionPercent = node.height() > 0 ? existingMainSectionBlocks / node.height() : 0;
        float ladderPercent = node.height() > 0 ? existingMainLadderBlocks / node.height() : 0;

        return 0.7F * (0.7F * mainSectionPercent + 0.3F * ladderPercent) + 0.3F * (existingPlatformBlocks / (platforms.length + 1) * 8);
    }

    public NbtCompound toNBT(NbtCompound compound) {
        node.toNbt(compound);
        compound.putFloat("initialCompletion", initialCompletion);
        compound.putFloat("latestPercentCompleted", latestPercentCompleted);
        return compound;
    }

    private static int[] createPlatforms(ScaffoldNode node) {
        final int spanningPlatforms = node.height() < 16
                ? node.height() / MIN_SCAFFOLD_HEIGHT - 1
                : node.height() / 5 - 1;
        if (spanningPlatforms <= 0) {
            return new int[0];
        }

        int avgSpace = node.height() / (spanningPlatforms + 1);
        int remainder = node.height() % (spanningPlatforms + 1) - 1;

        final int[] platforms = new int[spanningPlatforms];
        for (int i = 0; i < spanningPlatforms; i++) {
            platforms[i] = (avgSpace * (i + 1) - 1);
        }

        int i = spanningPlatforms - 1;
        while (remainder > 0) {
            platforms[i--]++;
            if (i < 0) {
                i = spanningPlatforms - 1;
                remainder--;
            }
            remainder--;
        }

        return platforms;
    }
}
