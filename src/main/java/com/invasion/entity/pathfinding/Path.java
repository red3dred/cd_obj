package com.invasion.entity.pathfinding;

import java.util.Arrays;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Path {
    private final PathNode[] points;

    @Nullable
    private PathNode intendedTarget;
    private int length;
    private int index;

    Path(PathNode[] nodes, @Nullable PathNode intendedTarget) {
        points = nodes;
        length = nodes.length;
        this.intendedTarget = intendedTarget;
    }

    public float getTotalPathCost() {
        @Nullable
        PathNode finalNode = getFinalPathPoint();
        return finalNode == null ? 0 : finalNode.totalPathDistance;
    }

    public void incrementPathIndex() {
        index++;
    }

    public boolean isFinished() {
        return index >= points.length;
    }

    @Nullable
    public PathNode getFinalPathPoint() {
        return length > 0 ? points[length - 1] : null;
    }

    public PathNode getPathPointFromIndex(int par1) {
        return points[par1];
    }

    public int getCurrentPathLength() {
        return length;
    }

    public void setCurrentPathLength(int length) {
        this.length = Math.min(points.length, length);
    }

    public int getCurrentPathIndex() {
        return index;
    }

    public void setCurrentPathIndex(int index) {
        this.index = index;
    }

    @Nullable
    public PathNode getIntendedTarget() {
        return intendedTarget;
    }

    public Vec3d getPositionAtIndex(Entity entity, int index) {
        double width = (int) (entity.getWidth() + 1) * 0.5D;
        return Vec3d.of(points[index].pos).add(width, 0, width);
    }

    public Vec3d getCurrentNodeVec3d(Entity entity) {
        return getPositionAtIndex(entity, index);
    }

    @Nullable
    public Vec3d destination() {
        @Nullable
        PathNode finalNode = getFinalPathPoint();
        return finalNode == null ? null : finalNode.pos.toBottomCenterPos();
    }

    public boolean equalsPath(@Nullable Path path) {
        return path != null && Arrays.equals(points, path.points, PathNode.POSITION_COMPARATOR);
    }

    public boolean isDestinationSame(@Nullable Vec3d pos) {
        return compareBlockPositions(destination(), pos);
    }

    public Path combine(Path path2, int lowerBoundP1, int upperBoundP1) {
        int k = upperBoundP1 - lowerBoundP1;
        PathNode[] newPoints = new PathNode[k + path2.getCurrentPathLength()];
        System.arraycopy(points, lowerBoundP1, newPoints, 0, k);
        System.arraycopy(path2.points, 0, newPoints, k, path2.getCurrentPathLength());
        return new Path(newPoints, path2.getIntendedTarget());
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