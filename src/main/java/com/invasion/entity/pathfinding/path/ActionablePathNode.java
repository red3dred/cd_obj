package com.invasion.entity.pathfinding.path;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;

public interface ActionablePathNode {
    PathAction getAction();

    void setAction(PathAction action);

    static PathAction getAction(@Nullable PathNode node) {
        return node instanceof ActionablePathNode a ? a.getAction() : PathAction.NONE;
    }

    static @Nullable PathNode setActionIfNotPresent(@Nullable PathNode node, PathAction action) {
        return getAction(node) == PathAction.NONE ? setAction(node, action) : node;
    }

    static @Nullable PathNode setAction(@Nullable PathNode node, PathAction action) {
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
