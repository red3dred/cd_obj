package com.invasion.entity.pathfinding;

import com.invasion.entity.pathfinding.path.PathAction;

import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Deprecated
public interface IMPathNodeMaker {
    float getPathNodePenalty(PathNode startNode, PathNode endNode, BlockView world);

    void getSuccessors(BlockView world, PathNode node, PathBuilder pathBuilder);

    interface PathBuilder {
        void addNode(BlockPos pos, PathAction action);
    }
}