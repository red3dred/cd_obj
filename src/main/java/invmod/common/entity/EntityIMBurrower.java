package invmod.common.entity;

import java.util.Arrays;

import org.joml.Vector3f;

import invmod.common.INotifyTask;
import invmod.common.nexus.INexusAccess;
import invmod.common.util.CoordsInt;
import invmod.common.util.PosRotate3D;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;


public class EntityIMBurrower extends EntityIMMob implements ICanDig {
    public static final int NUMBER_OF_SEGMENTS = 16;
    private TerrainModifier terrainModifier = new TerrainModifier(this, 2);
    private TerrainDigger terrainDigger = new TerrainDigger(this, terrainModifier, 1);

    private final PosRotate3D[] segments3D = new PosRotate3D[NUMBER_OF_SEGMENTS];
    private final PosRotate3D[] segments3DLastTick = new PosRotate3D[NUMBER_OF_SEGMENTS];

    protected final Vector3f rot = new Vector3f();
    protected final Vector3f prevRot = new Vector3f();

    public EntityIMBurrower(EntityType<EntityIMBurrower> type, World world) {
        this(type, world, null);
    }

    public EntityIMBurrower(EntityType<EntityIMBurrower> type, World world, INexusAccess nexus) {
        super(type, world, nexus);

        setGender(0);
        // this.setSize(0.5F, 0.5F);
        setJumpHeight(0);
        setCanClimb(true);
        setDestructiveness(2);
        maxDestructiveness = 2;
        blockRemoveSpeed = 0.5F;

        Arrays.fill(segments3D, PosRotate3D.ZERO);
        Arrays.fill(segments3DLastTick, PosRotate3D.ZERO);
    }

    @Override
    protected IPathSource createPathSource() {
        return new PathCreator(800, 400);
    }

    @Override
    protected INavigation createIMNavigation(IPathSource pathSource) {
        return new NavigatorBurrower(this, pathSource, 16, -4);
    }

    @Override
    public BlockView getTerrain() {
        return getWorld();
    }

    @Override
    public float getBlockRemovalCost(BlockPos pos) {
        return getBlockStrength(pos) * 20;
    }

    @Override
    public boolean canClearBlock(BlockPos pos) {
        BlockState block = getWorld().getBlockState(pos);
        return block.isAir() || isBlockDestructible(getWorld(), pos, block);
    }

    @Override
    public int getTier() {
        return 3;
    }

    @Override
    protected boolean onPathBlocked(Path path, INotifyTask notifee) {
        return terrainDigger.askClearPosition(path.getPathPointFromIndex(path.getCurrentPathIndex()).pos, notifee, 1);
    }

    public Vector3f getRotation() {
        return rot;
    }

    public Vector3f getPrevRotation() {
        return prevRot;
    }

    public PosRotate3D[] getSegments3D() {
        return segments3D;
    }

    public PosRotate3D[] getSegments3DLastTick() {
        return this.segments3DLastTick;
    }

    public void setSegment(int index, PosRotate3D pos) {
        if (index < segments3D.length) {
            segments3DLastTick[index] = segments3D[index];
            segments3D[index] = pos;
        }
    }

    public void setHeadRotation(PosRotate3D pos) {
        prevRot.set(rot);
        rot.set(pos.rotation());
    }

    @Override
    public void mobTick() {
        super.mobTick();
        terrainModifier.onUpdate();
    }

    @Override
    public float getBlockPathCost(PathNode prevNode, PathNode node, BlockView worldMap) {
        BlockState block = worldMap.getBlockState(node.pos);

        float penalty = 0.0F;
        int enclosedLevelSide = 0;

        BlockPos.Mutable mutable = node.pos.mutableCopy();
        if (!getWorld().getBlockState(mutable.move(Direction.DOWN)).isFullCube(getWorld(), mutable)) {
            penalty += 0.3F;
        }
        if (!getWorld().getBlockState(mutable.set(node.pos).move(Direction.UP)).isFullCube(getWorld(), mutable)) {
            penalty += 2;
        }

        for (Direction offset : CoordsInt.CARDINAL_DIRECTIONS) {
            if (!getWorld().getBlockState(mutable.set(node.pos).move(offset)).isFullCube(getWorld(), mutable)) {
                enclosedLevelSide++;
            }
        }

        if (enclosedLevelSide > 2) {
            enclosedLevelSide = 2;
        }
        penalty += enclosedLevelSide * 0.5F;

        float factor = !block.isAir() && (block.isSolidBlock(worldMap, node.pos) || EntityIMLiving.getBlockCost(block).isPresent()) ? 1.3F : 1;

        return prevNode.distanceTo(node) * factor * penalty;
    }

    @Override
    public void onBlockRemoved(BlockPos pos, BlockState state) {
    }

    @Override
    public String getLegacyName() {
        return "EntityIMBurrower#u-u-u";
    }
}