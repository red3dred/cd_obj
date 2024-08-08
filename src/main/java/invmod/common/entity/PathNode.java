package invmod.common.entity;

import invmod.common.util.IPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class PathNode implements IPosition {
    public BlockPos pos;

    public final PathAction action;

    private final int hash;

    int index;
    float totalPathDistance;
    float distanceToNext;
    float distanceToTarget;

    private PathNode previous;

    public boolean isFirst;

    public PathNode(int i, int j, int k) {
        this(i, j, k, PathAction.NONE);
    }

    public PathNode(int i, int j, int k, PathAction pathAction) {
        this.index = -1;
        this.isFirst = false;
        this.pos = new BlockPos(i, j, k);
        this.action = pathAction;
        this.hash = makeHash(i, j, k, this.action);
    }

    public float distanceTo(PathNode pathpoint) {
        return MathHelper.sqrt((float)pos.getSquaredDistance(pathpoint.pos));
    }

    public float distanceTo(float x, float y, float z) {
        return MathHelper.sqrt((float)pos.getSquaredDistance(x, y, z));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PathNode node && hash == node.hash && isAt(node) && node.action == action;
    }

    public boolean isAt(IPosition position) {
        return pos.equals(position.toBlockPos());
    }

    public boolean equals(int x, int y, int z) {
        return pos.getX() == x && pos.getY() == y && pos.getZ() == z;
    }

    public boolean isAssigned() {
        return this.index >= 0;
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

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public String toString() {
        return pos.toShortString() + ", " + this.action;
    }

    public static int makeHash(IPosition pos, PathAction action) {
        return makeHash(pos.getXCoord(), pos.getYCoord(), pos.getZCoord(), action);
    }

    public static int makeHash(int x, int y, int z, PathAction action) {
        return y & 0xFF | (x & 0xFF) << 8 | (z & 0xFF) << 16 | (action.ordinal() & 0xFF) << 24;
    }
}