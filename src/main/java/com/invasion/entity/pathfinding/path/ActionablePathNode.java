package com.invasion.entity.pathfinding.path;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.util.math.BlockPos;

public interface ActionablePathNode {
    PathAction getAction();

    void setAction(PathAction action);

    static net.minecraft.entity.ai.pathing.PathNode create(int x, int y, int z, PathAction action) {
        return setAction(new PathNode(x, y, z), action);
    }

    static net.minecraft.entity.ai.pathing.PathNode create(BlockPos pos, PathAction action) {
        return setAction(new PathNode(pos.getX(), pos.getY(), pos.getZ()), action);
    }

    static TargetPathNode createTarget(int x, int y, int z, PathAction action) {
        return (TargetPathNode)setAction(new TargetPathNode(x, y, z), action);
    }

    static net.minecraft.entity.ai.pathing.TargetPathNode createTarget(PathNode node) {
        return new TargetPathNode(node);
    }

    static PathAction getAction(PathNode node) {
        return node instanceof ActionablePathNode a ? a.getAction() : PathAction.NONE;
    }

    static PathNode setAction(PathNode node, PathAction action) {
        if (node instanceof ActionablePathNode a) {
            a.setAction(action);
        }
        return node;
    }

    static Path combine(Path path1, Path path2, int lowerBoundP1, int upperBoundP1) {
        List<net.minecraft.entity.ai.pathing.PathNode> newNodes = new ArrayList<>();
        for (int i = lowerBoundP1; i < upperBoundP1; i++) {
            newNodes.add(path1.getNode(i));
        }
        for (int i = 0; i < path2.getLength(); i++) {
            newNodes.add(path2.getNode(i));
        }
        return new Path(newNodes, path2.getTarget(), path2.reachesTarget());
    }
}
