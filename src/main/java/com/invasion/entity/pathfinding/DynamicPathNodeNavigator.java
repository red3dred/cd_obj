package com.invasion.entity.pathfinding;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import com.invasion.entity.pathfinding.path.PathAction;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CollisionView;
import net.minecraft.world.chunk.ChunkCache;

public class DynamicPathNodeNavigator extends PathNodeNavigator {
    private NodeFactory measurer;

    public DynamicPathNodeNavigator(PathNodeMaker pathNodeMaker, int range) {
        super(pathNodeMaker, range);
        measurer = pathNodeMaker instanceof NodeFactory a ? a : NodeFactory.DEFAULT;
    }

    public static <T> T createHeadlessNavigator(PathAwareEntity entity, int range, Function<PathSupplier, T> supplier) {
        EntityNavigation navigation = entity.getNavigation();
        var nav = new DynamicPathNodeNavigator(navigation.getNodeMaker(), range);
        return supplier.apply((context, from, positions, distance, chunkCacheModifier) -> {
            try {
                if (navigation.getNodeMaker() instanceof RootNodeFactory root) {
                    root.setDelegate(context, chunkCacheModifier);
                }
                int i = range + distance;
                ChunkCache chunkCache = new ChunkCache(entity.getWorld(), from.add(-i, -i, -i), from.add(i, i, i));
                return nav.findPathToAny(chunkCache, entity, positions, range, distance, 1);
            } finally {
                if (navigation.getNodeMaker() instanceof RootNodeFactory root) {
                    root.setDelegate(NodeFactory.DEFAULT, a -> {});
                }
            }
        });
    }

    @Override
    protected float getDistance(PathNode previousNode, PathNode nextNode) {
        float distance = previousNode.getDistance(nextNode);
        float distancePenalty = measurer.getDistancePenalty(previousNode, nextNode, null);
        float penalizedDistance = distance * distancePenalty;
        nextNode.penalty += penalizedDistance - distance;
        return distance;
    }

    public interface PathSupplier {
        Path findPathToAny(NodeFactory context, BlockPos from, Set<BlockPos> positions, int distance, Consumer<CollisionView> chunkCacheModifier);

        default Path findPathTo(NodeFactory context, BlockPos from, BlockPos to, int distance, Consumer<CollisionView> chunkCacheModifier) {
            return findPathToAny(context, from, Set.of(to), distance, chunkCacheModifier);
        }

        default Path findPathTo(NodeFactory context, BlockPos from, BlockPos to, int distance) {
            return findPathToAny(context, from, Set.of(to), distance, a -> {});
        }
    }

    public interface RootNodeFactory extends NodeFactory, NodeCache {
        void setDelegate(NodeFactory delegate, Consumer<CollisionView> chunkCacheModifier);
    }

    public interface NodeCache {
        PathNode getNode(int x, int y, int z, PathAction action);
    }

    public interface NodeFactory {
        NodeFactory DEFAULT = new NodeFactory() {
            @Override
            public float getDistancePenalty(PathNode previousNode, PathNode nextNode, CollisionView world) {
                return 1;
            }

            @Override
            public int getSuccessors(int index, PathNode[] successors, PathNode node, CollisionView world, NodeCache cache) {
                return index;
            }
        };

        float getDistancePenalty(PathNode previousNode, PathNode nextNode, CollisionView world);

        int getSuccessors(int index, PathNode[] successors, PathNode node, CollisionView world, NodeCache cache);
    }
}
