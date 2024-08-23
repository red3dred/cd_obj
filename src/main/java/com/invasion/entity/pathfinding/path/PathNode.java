package com.invasion.entity.pathfinding.path;

import java.util.Comparator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * Source: net.minecraft.entity.ai.pathing.PathNode
 */
class PathNode extends net.minecraft.entity.ai.pathing.PathNode implements ActionablePathNode {
    public static final Comparator<PathNode> POSITION_COMPARATOR = Comparator.comparing(a -> a.pos);
    public final BlockPos pos;
    public final PathAction action;

    private final int hashCode;

    public PathNode(BlockPos pos) {
        this(pos, PathAction.NONE);
    }

    public PathNode(BlockPos pos, PathAction pathAction) {
        this(pos.getX(), pos.getY(), pos.getZ(), pathAction);
    }

    public PathNode(int x, int y, int z, PathAction pathAction) {
        super(x, y, z);
        this.heapIndex = -1;
        this.pos = new BlockPos(x, y, z);
        this.action = pathAction;
        this.hashCode = ActionablePathNode.makeHash(x, y, z, action);
    }

    public PathNode(net.minecraft.entity.ai.pathing.PathNode node, PathAction action) {
        this(node.x, node.y, node.z, action);
        heapIndex = node.heapIndex;
        penalizedPathLength = node.penalizedPathLength;
        distanceToNearestTarget = node.distanceToNearestTarget;
        heapWeight = node.heapWeight;
        previous = node.previous;
        visited = node.visited;
        pathLength = node.pathLength;
        penalty = node.penalty;
        type = node.type;
    }

    @Override
    public PathAction getAction() {
        return action;
    }


    @Override
    public net.minecraft.entity.ai.pathing.PathNode getWithAction(PathAction action) {
        return new PathNode(this, action);
    }

    @Override
    public PathNode copyWithNewPosition(int x, int y, int z) {
        PathNode pathNode = new PathNode(x, y, z, action);
        pathNode.heapIndex = heapIndex;
        pathNode.penalizedPathLength = penalizedPathLength;
        pathNode.distanceToNearestTarget = distanceToNearestTarget;
        pathNode.heapWeight = heapWeight;
        pathNode.previous = previous;
        pathNode.visited = visited;
        pathNode.pathLength = pathLength;
        pathNode.penalty = penalty;
        pathNode.type = type;
        return pathNode;
    }

    public float distanceTo(PathNode pathpoint) {
        return MathHelper.sqrt((float)pos.getSquaredDistance(pathpoint.pos));
    }

    public float distanceTo(float x, float y, float z) {
        return MathHelper.sqrt((float)pos.getSquaredDistance(x, y, z));
    }

    public float distanceTo(BlockPos pos) {
        return MathHelper.sqrt((float)this.pos.getSquaredDistance(pos));
    }

    public boolean isAt(BlockPos position) {
        return pos.equals(position);
    }

    public boolean equals(int x, int y, int z) {
        return pos.getX() == x && pos.getY() == y && pos.getZ() == z;
    }

    @Override
    public boolean isInHeap() {
        return heapIndex >= 0;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PathNode node && hashCode == node.hashCode && isAt(node.pos) && node.action == action;
    }

    @Override
    public String toString() {
        return pos.toShortString() + ", " + action;
    }
}