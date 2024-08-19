package com.invasion.entity.pathfinding;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface IMPathNodeMaker {
    float getPathNodePenalty(PathNode startNode, PathNode endNode, BlockView world);

    void getSuccessors(BlockView world, PathNode node, PathBuilder pathBuilder);

    interface PathBuilder {
        void addNode(BlockPos pos, PathAction action);
    }
}