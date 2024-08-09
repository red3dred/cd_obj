package invmod.common.entity;

import org.jetbrains.annotations.Nullable;

import invmod.common.IPathfindable;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class PathfinderIM {
    public static final PathfinderIM INSTANCE = new PathfinderIM();
    private BlockView worldMap;
    private final NodeContainer path = new NodeContainer();
    private final Int2ObjectMap<PathNode> pointMap = new Int2ObjectOpenHashMap<>();
    private final PathNode[] pathOptions = new PathNode[32];
    private PathNode finalTarget;
    private float targetRadius;
    private int pathsIndex;
    private float searchRange;
    private int nodeLimit;
    private int nodesOpened;

    @Nullable
    public synchronized Path createPath(IPathfindable entity, BlockPos from, BlockPos to, float targetRadius, float maxSearchRange, BlockView iblockaccess, int searchDepth, int quickFailDepth) {
        worldMap = iblockaccess;
        nodeLimit = searchDepth;
        nodesOpened = 1;
        searchRange = maxSearchRange;
        path.clearPath();
        pointMap.clear();
        PathNode start = openPoint(from);
        PathNode target = openPoint(to);
        finalTarget = target;
        this.targetRadius = targetRadius;
        return addToPath(entity, start, target);
    }

    @Nullable
    private Path addToPath(IPathfindable entity, PathNode start, PathNode target) {
        start.totalPathDistance = 0;
        start.distanceToNext = start.distanceTo(target);
        start.distanceToTarget = start.distanceToNext;

        path.clearPath();
        path.addPoint(start);
        PathNode previousPoint = start;

        while (!path.isPathEmpty()) {
            if (nodesOpened > nodeLimit) {
                return new Path(start.getPath(), previousPoint);
            }
            PathNode examiningPoint = path.dequeue();
            float distanceToTarget = examiningPoint.distanceTo(target);
            if (distanceToTarget < this.targetRadius + 0.1F) {
                return new Path(start.getPath(), examiningPoint);
            }
            if (distanceToTarget < previousPoint.distanceTo(target)) {
                previousPoint = examiningPoint;
            }
            examiningPoint.isFirst = true;

            int i = findPathOptions(entity, examiningPoint, target);

            int j = 0;
            while (j < i) {
                PathNode newPoint = pathOptions[j];

                float actualCost = examiningPoint.totalPathDistance + entity.getBlockPathCost(examiningPoint, newPoint, this.worldMap);

                if (!newPoint.isAssigned() || actualCost < newPoint.totalPathDistance) {
                    newPoint.setPrevious(examiningPoint);
                    newPoint.totalPathDistance = actualCost;
                    newPoint.distanceToNext = estimateDistance(newPoint, target);

                    if (newPoint.isAssigned()) {
                        path.changeDistance(newPoint, newPoint.totalPathDistance + newPoint.distanceToNext);
                    } else {
                        newPoint.distanceToTarget = newPoint.totalPathDistance + newPoint.distanceToNext;
                        path.addPoint(newPoint);
                    }
                }
                j++;
            }
        }

        return previousPoint == start ? null : new Path(previousPoint.getPath(), finalTarget);
    }

    public void addNode(BlockPos pos, PathAction action) {
        PathNode node = openPoint(pos, action);
        if (node != null && !node.isFirst && node.distanceTo(finalTarget) < searchRange) {
            pathOptions[pathsIndex++] = node;
        }
    }

    private float estimateDistance(PathNode start, PathNode target) {
        return (float) start.pos.getSquaredDistance(target.pos);
    }

    protected PathNode openPoint(BlockPos pos) {
        return openPoint(pos, PathAction.NONE);
    }

    protected PathNode openPoint(BlockPos pos, PathAction action) {
        int hash = PathNode.makeHash(pos, action);
        PathNode pathpoint = pointMap.get(hash);
        if (pathpoint == null) {
            pathpoint = new PathNode(pos, action);
            pointMap.put(hash, pathpoint);
            nodesOpened++;
        }

        return pathpoint;
    }

    private int findPathOptions(IPathfindable entity, PathNode pathpoint, PathNode target) {
        pathsIndex = 0;
        entity.getPathOptionsFromNode(worldMap, pathpoint, this);
        return pathsIndex;
    }
}