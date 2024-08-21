package com.invasion.entity.pathfinding;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.invasion.InvasionMod;
import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathMinHeap;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Version of PathNodeNavigator that takes the PathNodeMaker as a parameter rather than storing internally
 */
@Deprecated
public class IMPathNodeNavigator {
    private BlockView worldMap;
    private IMPathNodeMaker pathNodeMaker;
    private final PathMinHeap minHeap = new PathMinHeap();

    // PathNodeMaker.pathNodeCache
    private final Int2ObjectMap<PathNode> pathNodeCache = new Int2ObjectOpenHashMap<>();
    private final PathNode[] successors = new PathNode[32];
    private PathNode finalTarget;
    private float targetRadius;
    private int pathsIndex;
    private float searchRange;
    private int nodeLimit;
    private int nodesOpened;

    private final IMPathNodeMaker.PathBuilder pathBuilder = new IMPathNodeMaker.PathBuilder() {
        @Override
        public void addNode(BlockPos pos, PathAction action) {
            PathNode node = openPoint(pos, action);
            if (node != null && !node.visited && node.getDistance(finalTarget) < searchRange) {
                successors[pathsIndex++] = node;
            }
        }
    };

    @Nullable
    public Path createPath(IMPathNodeMaker pather, BlockPos from, BlockPos to, float targetRadius, float maxSearchRange, BlockView iblockaccess, int searchDepth, int quickFailDepth) {
        worldMap = iblockaccess;
        pathNodeMaker = pather;
        nodeLimit = searchDepth;
        nodesOpened = 1;
        searchRange = maxSearchRange;
        minHeap.clear();
        pathNodeCache.clear();
        PathNode start = openPoint(from);
        PathNode target = openPoint(to);
        finalTarget = target;
        this.targetRadius = targetRadius;
        Path path = addToPath(start, target);
        if (path != null) {
            InvasionMod.LOGGER.info("Path find success");
        }
        return path;
    }

    /**
     * PathNodeNavigator.findPathToAny
     */
    @Nullable
    private Path addToPath(PathNode start, PathNode target) {
        start.penalizedPathLength = 0;
        start.distanceToNearestTarget = start.getDistance(target);
        start.heapWeight = start.distanceToNearestTarget;

        minHeap.clear();
        minHeap.push(start);
        PathNode previousPoint = start;

        while (!minHeap.isEmpty()) {
            if (nodesOpened > nodeLimit) {
                return createPath(start, previousPoint.getBlockPos(), false);
            }
            PathNode examiningPoint = minHeap.pop();
            float distanceToTarget = examiningPoint.getDistance(target);
            if (distanceToTarget < this.targetRadius + 0.1F) {
                return createPath(start, examiningPoint.getBlockPos(), true);
            }
            if (distanceToTarget < previousPoint.getDistance(target)) {
                previousPoint = examiningPoint;
            }
            examiningPoint.visited = true;

            pathsIndex = 0;
            pathNodeMaker.getSuccessors(worldMap, examiningPoint, pathBuilder);
            int i = pathsIndex;

            for (int j = 0; j < i; j++) {
                PathNode newPoint = successors[j];

                float actualCost = examiningPoint.penalizedPathLength + pathNodeMaker.getPathNodePenalty(examiningPoint, newPoint, this.worldMap);

                if (!newPoint.isInHeap() || actualCost < newPoint.penalizedPathLength) {
                    newPoint.previous = examiningPoint;
                    newPoint.penalizedPathLength = actualCost;
                    newPoint.distanceToNearestTarget = calculateDistance(newPoint, target);

                    if (newPoint.isInHeap()) {
                        minHeap.setNodeWeight(newPoint, newPoint.penalizedPathLength + newPoint.distanceToNearestTarget);
                    } else {
                        newPoint.heapWeight = newPoint.penalizedPathLength + newPoint.distanceToNearestTarget;
                        minHeap.push(newPoint);
                    }
                }
            }
        }

        return previousPoint == start ? null : createPath(previousPoint, finalTarget.getBlockPos(), true);
    }

    private Path createPath(PathNode endNode, BlockPos target, boolean reachesTarget) {
        List<PathNode> list = Lists.<PathNode>newArrayList();
        PathNode pathNode = endNode;
        list.add(0, endNode);

        while(pathNode.previous != null) {
            pathNode = pathNode.previous;
            list.add(0, pathNode);
        }

        return new Path(list, target, reachesTarget);
    }

    private float calculateDistance(PathNode start, PathNode target) {
        return start.getSquaredDistance(target);
    }

    private PathNode openPoint(BlockPos pos) {
        return openPoint(pos, PathAction.NONE);
    }

    private PathNode openPoint(BlockPos pos, PathAction action) {
        int hash = ActionablePathNode.makeHash(pos, action);
        PathNode pathpoint = pathNodeCache.get(hash);
        if (pathpoint == null) {
            pathpoint = ActionablePathNode.create(pos, action);
            pathNodeCache.put(hash, pathpoint);
            nodesOpened++;
        }

        return pathpoint;
    }
}