package com.invasion.entity.pathfinding;

import java.util.Set;

import com.invasion.nexus.IHasNexus;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkCache;

public class EntityDensityAwarePathNodeNavigator extends PathNodeNavigator {
    public EntityDensityAwarePathNodeNavigator(PathNodeMaker pathNodeMaker, int range) {
        super(pathNodeMaker, range);
    }

    @Override
    public Path findPathToAny(ChunkCache world, MobEntity mob, Set<BlockPos> positions, float followRange, int distance, float rangeMultiplier) {
        if (mob instanceof IHasNexus nexusHolder && nexusHolder.hasNexus()) {
            // TODO:
           // world = nexusHolder.getNexus().getAttackerAI().wrapEntityData(world);
        }
        return super.findPathToAny(world, mob, positions, followRange, distance, rangeMultiplier);
    }
}
