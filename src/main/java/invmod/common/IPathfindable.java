package invmod.common;

import invmod.common.entity.PathNode;
import invmod.common.entity.PathfinderIM;
import net.minecraft.world.WorldAccess;

public interface IPathfindable {
    float getBlockPathCost(PathNode paramPathNode1, PathNode paramPathNode2, WorldAccess paramIBlockAccess);

    void getPathOptionsFromNode(WorldAccess paramIBlockAccess, PathNode paramPathNode, PathfinderIM paramPathfinderIM);
}