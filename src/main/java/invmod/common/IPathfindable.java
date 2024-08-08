package invmod.common;

import invmod.common.entity.PathNode;
import invmod.common.entity.PathfinderIM;
import net.minecraft.world.WorldAccess;

public interface IPathfindable {
    float getBlockPathCost(PathNode startNode, PathNode endNode, WorldAccess world);

    void getPathOptionsFromNode(WorldAccess world, PathNode node, PathfinderIM finder);
}