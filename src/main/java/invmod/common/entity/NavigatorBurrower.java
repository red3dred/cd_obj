package invmod.common.entity;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import invmod.common.util.PosRotate3D;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class NavigatorBurrower extends NavigatorParametric {
    protected PathNode nextNode;
    protected PathNode prevNode;

    protected PathNode[] prevSegmentNodes;
    protected PathNode[] activeSegmentNodes;
    protected PathNode[] nextSegmentNodes;

    protected int[] segmentPathIndices;
    protected int[] segmentTime;
    protected int[] segmentOffsets;
    protected float timePerTick = 0.05F;
    protected Path lastPath;
    protected boolean nodeChanged;

    public NavigatorBurrower(EntityIMBurrower entity, IPathSource pathSource, int segments, int offset) {
        super(entity, pathSource);
        prevSegmentNodes = new PathNode[segments];
        activeSegmentNodes = new PathNode[segments];
        nextSegmentNodes = new PathNode[segments];
        segmentPathIndices = new int[segments];
        segmentTime = new int[segments];
        segmentOffsets = new int[segments];

        for (int i = 0; i < segmentOffsets.length; i++) {
            segmentOffsets[i] = ((i + 1) * offset);
        }
    }

    @Override
    protected PosRotate3D entityPositionAtParam(int time) {
        return calcAbsolutePositionAndRotation(time * timePerTick, prevNode, activeNode, nextNode);
    }

    @Override
    protected boolean isReadyForNextNode(int ticks) {
        return ticks * timePerTick >= 1;
    }

    @Override
    protected void pathFollow(int time) {
        int nextFrontIndex = path.getCurrentPathIndex() + 2;
        if (isReadyForNextNode(time)) {
            if (nextFrontIndex < path.getCurrentPathLength()) {
                timeParam = 0;
                path.setCurrentPathIndex(nextFrontIndex - 1);
                prevNode = activeNode;
                activeNode = nextNode;
                nextNode = path.getPathPointFromIndex(nextFrontIndex);
                nodeChanged = true;
            }
        } else {
            timeParam = time;
        }
    }

    protected void doSegmentFollowTo(int ticks, int segmentIndex) {
        ticks += segmentOffsets[segmentIndex];
        // TODO: Tick rate can be changed
        while (ticks <= 0) {
            ticks += 20;
        }

        int nextFrontIndex = segmentPathIndices[segmentIndex] + 2;
        if (isReadyForNextNode(ticks)) {
            if (nextFrontIndex < path.getCurrentPathLength()) {
                segmentPathIndices[segmentIndex] = (nextFrontIndex - 1);
                prevSegmentNodes[segmentIndex] = activeSegmentNodes[segmentIndex];
                activeSegmentNodes[segmentIndex] = nextSegmentNodes[segmentIndex];
                nextSegmentNodes[segmentIndex] = path.getPathPointFromIndex(segmentPathIndices[segmentIndex] >= 0 ? nextFrontIndex : 0);
                segmentTime[segmentIndex] = 0;
            }
        } else {
            segmentTime[segmentIndex] = ticks;
        }

        if (segmentPathIndices[segmentIndex] >= 0) {
            PosRotate3D pos = positionAtTime(segmentTime[segmentIndex], prevSegmentNodes[segmentIndex], activeSegmentNodes[segmentIndex], nextSegmentNodes[segmentIndex]);
            ((EntityIMBurrower) theEntity).setSegment(segmentIndex, pos);
            if (segmentTime[segmentIndex] == 0) {
                ((EntityIMBurrower) theEntity).setSegment(segmentIndex, pos);
            }
        }
    }

    @Override
    protected void doMovementTo(int time) {
        PosRotate3D movePos = entityPositionAtParam(time);
        theEntity.move(MovementType.SELF, movePos.position().subtract(theEntity.getPos()));
        ((EntityIMBurrower) theEntity).setHeadRotation(movePos);

        if (nodeChanged) {
            ((EntityIMBurrower) theEntity).setHeadRotation(movePos);
            nodeChanged = false;
        }

        if (theEntity.squaredDistanceTo(movePos.position()) < minMoveToleranceSq) {
            for (int segmentIndex = 0; segmentIndex < segmentPathIndices.length; segmentIndex++) {
                doSegmentFollowTo(time, segmentIndex);
            }
            timeParam = time;
            ticksStuck--;
        } else {
            ticksStuck++;
        }
    }

    @Override
    public boolean noPath() {
        return path == null || path.getCurrentPathIndex() >= path.getCurrentPathLength() - 2;
    }

    @Override
    public boolean setPath(Path newPath, float speed) {
        if (newPath == null || newPath.getCurrentPathLength() < 2) {
            path = null;
            return false;
        }

        if (path == null) {
            path = newPath;
            activeNode = path.getPathPointFromIndex(0);
            prevNode = activeNode;
            nextNode = path.getPathPointFromIndex(1);
            if (activeNode.action != PathAction.NONE) {
                nodeActionFinished = false;
            }
            for (int i = 0; i < segmentPathIndices.length; i++) {
                if (activeSegmentNodes[i] == null) {
                    activeSegmentNodes[i] = activeNode;
                    nextSegmentNodes[i] = activeNode;
                    segmentPathIndices[i] = 0;
                    segmentTime[i] = segmentOffsets[i];
                    while (segmentTime[i] < 0) {
                        // TODO: tick rate can change now
                        segmentTime[i] += 20;
                        segmentPathIndices[i]--;
                    }
                }
            }
        }

        int mainIndex = path.getCurrentPathIndex();
        if (newPath.getPathPointFromIndex(0).equals(activeNode)) {
            if (segmentPathIndices.length > 0) {
                int lowestIndex = Math.max(0, segmentPathIndices[segmentPathIndices.length - 1]);
                path = extendPath(path, newPath, lowestIndex, mainIndex);
                mainIndex -= lowestIndex;
                path.setCurrentPathIndex(mainIndex);
                nextNode = path.getPathPointFromIndex(mainIndex + 1);
                for (int i = 0; i < segmentPathIndices.length; i++) {
                    segmentPathIndices[i] -= lowestIndex;
                    if (segmentPathIndices[i] == mainIndex) {
                        nextSegmentNodes[i] = nextNode;
                    }
                }
            } else {
                path = newPath;
                path.setCurrentPathIndex(0);
                nextNode = path.getPathPointFromIndex(1);
            }
        } else {
            path = newPath;
            activeNode = path.getPathPointFromIndex(0);
            prevNode = activeNode;
            nextNode = path.getPathPointFromIndex(1);
            if (activeNode.action != PathAction.NONE) {
                nodeActionFinished = false;
            }
            for (int i = 0; i < segmentPathIndices.length; i++) {
                if (activeSegmentNodes[i] == null) {
                    activeSegmentNodes[i] = activeNode;
                    nextSegmentNodes[i] = activeNode;
                    segmentPathIndices[i] = 0;
                    segmentTime[i] = segmentOffsets[i];
                    while (segmentTime[i] < 0) {
                        // TODO: tick rate can change now
                        segmentTime[i] += 20;
                        segmentPathIndices[i]--;
                    }
                }
            }
        }

        ticksStuck = 0;

        if (noSunPathfind) {
            removeSunnyPath();
        }

        return true;
    }

    protected PosRotate3D positionAtTime(int tick, PathNode start, PathNode middle, PathNode end) {
        return calcAbsolutePositionAndRotation(tick * timePerTick, start, middle, end);
    }

    private PosRotate3D calcAbsolutePositionAndRotation(float time, PathNode start, PathNode middle, PathNode end) {
        PosRotate3D pos = calcPositionAndRotation(time, start, middle, end);
        return new PosRotate3D(pos.position().add(Vec3d.of(middle.pos)), pos.rotation());
    }

    private PosRotate3D calcPositionAndRotation(float time, PathNode start, PathNode middle, PathNode end) {
        Vec3i v = end.pos.subtract(start.pos);
        Vector3dc vd = new Vector3d(v.getX(), v.getY(), v.getZ());
        Vector3dc h = new Vector3d(
                middle.pos.getX() != start.pos.getZ() ? 1 : -1,
                middle.pos.getY() != start.pos.getY() ? 1 : -1,
                middle.pos.getZ() != start.pos.getZ() ? 1 : -1
        );
        Vector3dc g = new Vector3d(
                middle.pos.getX() != end.pos.getX() ? 1 : -1,
                middle.pos.getY() != end.pos.getY() ? 1 : -1,
                middle.pos.getZ() != end.pos.getZ() ? 1 : -1
        );
        Vector3dc offset = vd.mul(-0.5D, new Vector3d()).mul(h);

        if (h.x() == 1 && g.x() == 1) {
            return new PosRotate3D(new Vec3d(time * v.getX() * 0.5D + (v.getX() > 0 ? 0 : 1), 0.5D, 0.5D), new Vector3f(0, v.getX() >= 1 ? 0 : MathHelper.PI, 0));
        }
        if (h.y() == 1 && g.y() == 1) {
            return new PosRotate3D(new Vec3d(0.5D, time * v.getY() * 0.5D + (v.getY() > 0 ? 0 : 1), 0.5D), new Vector3f());
        }
        if (h.z() == 1 && g.z() == 1) {
            return new PosRotate3D(new Vec3d(time * v.getZ() * 0.5D + (v.getZ() > 0 ? 0 : 1), 0.5, 0.5), new Vector3f(0, v.getZ() * MathHelper.PI / 4F, 0));
        }

        double sin = Math.sin(time * 0.5D * Math.PI) * 0.5D;
        double cos = Math.cos(time * 0.5D * Math.PI) * 0.5D;

        Vector3d pos = vd.mul(h, new Vector3d()).mul(
                h.x() == 1 ? sin : cos,
                h.y() == 1 ? sin : cos,
                h.z() == 1 ? sin : cos
        ).add(offset);

        Vector3f rot = new Vector3f();

        if (h.x() == 1) {
            rot.set(0, vd.x() == 1 ? 0 : 180, 0);
            if (g.z() == 1)
                rot.add(0F, (float)(time * vd.z() * vd.x() * 90F), 0F);
            else if (g.y() == 1)
                rot.add(0F, 0F, (float)(time * vd.y() * 90F));
        } else if (h.y() == 1) {
            if (g.x() == 1) {
                rot.set(vd.x() == 1 ? 0 : 180, 0, 90 * vd.y() + time * vd.x() * -90);
            } else if (g.z() == 1) {
                rot.set(90,  vd.z() * (-90 * time * vd.y()), -90);
            }
        } else if (h.z() == 1) {
            if (g.x() == 1) {
                rot.set(0,   vd.y() * ( 90 + time * vd.x() * -90), 0);
            } else if (g.y() == 1) {
                rot.set(90, -vd.z() * (-90 + time * vd.y() * -90), -90);
            }
        }

        pos = pos.add(0.5, 0.5, 0.5);
        return new PosRotate3D(new Vec3d(pos.x(), pos.y(), pos.z()), rot.mul(MathHelper.RADIANS_PER_DEGREE));
    }

    private Path extendPath(Path path1, Path path2, int lowerBoundP1, int upperBoundP1) {
        return path1.combine(path2, lowerBoundP1, upperBoundP1);
    }
}