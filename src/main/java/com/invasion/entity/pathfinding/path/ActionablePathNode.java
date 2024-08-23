package com.invasion.entity.pathfinding.path;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public interface ActionablePathNode {
    PathAction getAction();

    net.minecraft.entity.ai.pathing.PathNode getWithAction(PathAction action);

    static net.minecraft.entity.ai.pathing.PathNode create(int x, int y, int z, PathAction action) {
        return new PathNode(x, y, z, action);
    }

    static net.minecraft.entity.ai.pathing.PathNode create(BlockPos pos, PathAction action) {
        return new PathNode(pos, action);
    }

    static net.minecraft.entity.ai.pathing.TargetPathNode createTarget(int x, int y, int z, PathAction action) {
        return new TargetPathNode(x, y, z, action);
    }

    static net.minecraft.entity.ai.pathing.TargetPathNode createTarget(net.minecraft.entity.ai.pathing.PathNode node) {
        return new TargetPathNode(node);
    }

    static PathAction getAction(net.minecraft.entity.ai.pathing.PathNode node) {
        return node instanceof ActionablePathNode a ? a.getAction() : PathAction.NONE;
    }

    static net.minecraft.entity.ai.pathing.PathNode setAction(net.minecraft.entity.ai.pathing.PathNode node, PathAction action) {
        if (node instanceof ActionablePathNode a) {
            return a.getWithAction(action);
        }
        if (node instanceof net.minecraft.entity.ai.pathing.TargetPathNode target) {
            return new TargetPathNode(target, action);
        }
        return new PathNode(node, action);
    }

    static int makeHash(BlockPos pos, PathAction action) {
        return makeHash(pos.getX(), pos.getY(), pos.getZ(), action);
    }

    static int makeHash(int x, int y, int z, PathAction action) {
        return y & 0xFF | (x & 0xFF) << 8 | (z & 0xFF) << 16 | (action.ordinal() & 0xFF) << 24;
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

    static boolean compareBlockPositions(@Nullable Vec3d a, @Nullable Vec3d b) {
        return Objects.equal(a, b) || (
                a != null && b != null
                && compareFloored(a.x, b.x)
                && compareFloored(a.y, b.y)
                && compareFloored(a.z, b.z)
        );
    }

    static boolean compareFloored(double a, double b) {
        return MathHelper.floor(a) == MathHelper.floor(b);
    }
}
