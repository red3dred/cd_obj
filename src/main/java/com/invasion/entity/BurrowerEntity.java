package com.invasion.entity;

import java.util.Arrays;

import org.joml.Vector3f;

import com.invasion.Notifiable;
import com.invasion.entity.ai.builder.TerrainDigger;
import com.invasion.entity.ai.builder.TerrainModifier;
import com.invasion.entity.pathfinding.BurrowerNavigation;
import com.invasion.entity.pathfinding.Navigation;
import com.invasion.entity.pathfinding.PathCreator;
import com.invasion.util.math.PosRotate3D;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;


public class BurrowerEntity extends IMMobEntity implements Miner {
    public static final int NUMBER_OF_SEGMENTS = 16;
    private TerrainModifier terrainModifier = new TerrainModifier(this, 2);
    private TerrainDigger terrainDigger = new TerrainDigger(this, terrainModifier, 1);

    private final PosRotate3D[] segments3D = new PosRotate3D[NUMBER_OF_SEGMENTS];
    private final PosRotate3D[] segments3DLastTick = new PosRotate3D[NUMBER_OF_SEGMENTS];

    protected final Vector3f rot = new Vector3f();
    protected final Vector3f prevRot = new Vector3f();

    public BurrowerEntity(EntityType<BurrowerEntity> type, World world) {
        super(type, world);
        Arrays.fill(segments3D, PosRotate3D.ZERO);
        Arrays.fill(segments3DLastTick, PosRotate3D.ZERO);
        getNavigatorNew().setCanDestroyBlocks(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 0);
    }

    @Override
    protected Navigation createIMNavigation() {
        return new BurrowerNavigation(this, new PathCreator(800, 400), 16, -4);
    }

    @Override
    public boolean onPathBlocked(Path path, Notifiable notifee) {
        return terrainDigger.askClearPosition(path.getCurrentNodePos(), notifee, 1);
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
    public String getLegacyName() {
        return "EntityIMBurrower#u-u-u";
    }
}