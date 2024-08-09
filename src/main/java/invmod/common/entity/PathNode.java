package invmod.common.entity;

import invmod.common.util.IPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class PathNode implements IPosition {
    public final BlockPos pos;
    public final PathAction action;

    private final int hash;

    int index;
    float totalPathDistance;
    float distanceToNext;
    float distanceToTarget;

    private PathNode previous;

    public boolean isFirst;

    public PathNode(BlockPos pos) {
        this(pos, PathAction.NONE);
    }

    public PathNode(BlockPos pos, PathAction pathAction) {
        this.index = -1;
        this.isFirst = false;
        this.pos = pos;
        this.action = pathAction;
        this.hash = makeHash(pos, action);
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PathNode node && hash == node.hash && isAt(node) && node.action == action;
    }

    public boolean isAt(IPosition position) {
        return equals(position.getXCoord(), position.getYCoord(), position.getZCoord());
    }

    public boolean equals(int x, int y, int z) {
        return pos.getX() == x && pos.getY() == y && pos.getZ() == z;
    }

    public boolean isAssigned() {
        return index >= 0;
    }

    @Override
    public int getXCoord() {
        return pos.getX();
    }

    @Override
    public int getYCoord() {
        return pos.getY();
    }

    @Override
    public int getZCoord() {
        return pos.getZ();
    }

    @Override
    public BlockPos toBlockPos() {
        return pos;
    }

    public PathNode getPrevious() {
        return previous;
    }

    public void setPrevious(PathNode previous) {
        this.previous = previous;
    }

    public PathNode[] getPath() {
        int i = 1;
        for (PathNode node = this;
                node.getPrevious() != null;
                node = node.getPrevious()) {
            i++;
        }
        PathNode[] nodes = new PathNode[i];
        PathNode node = this;
        for (nodes[(--i)] = node; node.getPrevious() != null; nodes[(--i)] = node) {
            node = node.getPrevious();
        }

        return nodes;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return pos.toShortString() + ", " + action;
    }

    public static int makeHash(IPosition pos, PathAction action) {
        return makeHash(pos.getXCoord(), pos.getYCoord(), pos.getZCoord(), action);
    }

    public static int makeHash(BlockPos pos, PathAction action) {
        return makeHash(pos.getX(), pos.getY(), pos.getZ(), action);
    }

    @Deprecated
    public static int makeHash(int x, int y, int z, PathAction action) {
        return y & 0xFF | (x & 0xFF) << 8 | (z & 0xFF) << 16 | (action.ordinal() & 0xFF) << 24;
    }
}