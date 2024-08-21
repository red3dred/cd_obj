package com.invasion.entity.pathfinding.path;

import java.util.Comparator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * Source: net.minecraft.entity.ai.pathing.PathNode
 */
class TargetPathNode extends net.minecraft.entity.ai.pathing.TargetPathNode implements ActionablePathNode {
    public static final Comparator<TargetPathNode> POSITION_COMPARATOR = Comparator.comparing(a -> a.pos);
    public final BlockPos pos;
    public final PathAction action;

    private final int hashCode;

    public TargetPathNode(net.minecraft.entity.ai.pathing.PathNode node) {
        this(node.x, node.y, node.z, ActionablePathNode.getAction(node));
    }

    public TargetPathNode(int x, int y, int z, PathAction pathAction) {
        super(x, y, z);
        this.pos = new BlockPos(x, y, z);
        this.action = pathAction;
        this.hashCode = ActionablePathNode.makeHash(pos, action);
    }

    @Override
    public PathAction getAction() {
        return action;
    }

    @Override
    public TargetPathNode copyWithNewPosition(int x, int y, int z) {
        TargetPathNode pathNode = new TargetPathNode(x, y, z, action);
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

    public float distanceTo(TargetPathNode pathpoint) {
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
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TargetPathNode node && hashCode == node.hashCode && isAt(node.pos) && node.action == action;
    }

    @Override
    public String toString() {
        return pos.toShortString() + ", " + action;
    }
}