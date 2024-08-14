package com.invasion;

import com.invasion.entity.pathfinding.PathNode;
import com.invasion.entity.pathfinding.PathfinderIM;

import net.minecraft.world.BlockView;

public interface IPathfindable {
    float getBlockPathCost(PathNode startNode, PathNode endNode, BlockView world);

    void getPathOptionsFromNode(BlockView world, PathNode node, PathfinderIM finder);
}