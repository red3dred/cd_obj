package com.invasion.entity;

import java.util.Arrays;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.invasion.InvSounds;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ElectricityBoltEntity extends Entity {
    private static final int VERTEX_COUNT = 60;

    private int ticksToRender;
    private final long timeCreated = System.currentTimeMillis();
    private final Vector3f[] vertices = Util.make(new Vector3f[VERTEX_COUNT], v -> Arrays.fill(v, new Vector3f()));
    private long lastVertexUpdate = timeCreated;

    private double distance;
    private float widthVariance = 6;
    private Vec3d ray;
    private boolean soundMade;

    public ElectricityBoltEntity(EntityType<ElectricityBoltEntity> type, World world) {
        super(type, world);
        ignoreCameraFrustum = true;
    }

    public ElectricityBoltEntity(World world, double x, double y, double z) {
        this(InvEntities.BOLT, world);
        setPosition(x, y, z);
    }

    public ElectricityBoltEntity(World world, Vec3d pos, Vec3d targetPos, int ticksToRender, boolean soundMade) {
        this(InvEntities.BOLT, world);
        setPosition(pos);
        ray = targetPos.subtract(pos);
        this.ticksToRender = ticksToRender;
        this.soundMade = soundMade;
        setHeading((float)ray.x, (float)ray.y, (float)ray.z);
        doVertexUpdate();
    }

    @Override
    protected void initDataTracker(Builder builder) {
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (++age == 1 && soundMade) {
            playSound(InvSounds.ENTITY_LIGHTNING_ZAP, 1, 1);
        }
        if (age > ticksToRender) {
            discard();
        }
    }

    @Nullable
    public Vector3f[] getVertices() {
        long time = System.currentTimeMillis();
        if (time - timeCreated > ticksToRender * 50) {
            return null;
        }
        if (time - lastVertexUpdate >= 75L) {
            doVertexUpdate();
            while (lastVertexUpdate + 50L <= time) {
                lastVertexUpdate += 50L;
            }
        }
        return vertices;
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 0) {
            playSound(InvSounds.ENTITY_LIGHTNING_ZAP, 1, 1);
        }
    }

    private void setHeading(float x, float y, float z) {
        float xzSq = MathHelper.square(x) + MathHelper.square(z);
        setYaw((float)MathHelper.atan2(x, z) * MathHelper.DEGREES_PER_RADIAN + 90);
        setPitch((float)MathHelper.atan2(MathHelper.sqrt(xzSq), y) * MathHelper.DEGREES_PER_RADIAN);
        distance = Math.sqrt(xzSq + MathHelper.square(y));
    }

    private void doVertexUpdate() {
        getWorld().getProfiler().push("IMBolt");
        widthVariance = (10F / (float) Math.log10(distance + 1));
        for (int vertex = 0; vertex < vertices.length; vertex++) {
            vertices[vertex].y = (vertex * (float)distance / (vertices.length - 1));
        }

        createSegment(0, vertices.length - 1);
        getWorld().getProfiler().pop();
    }

    private void createSegment(int begin, int end) {
        int points = end + 1 - begin;
        if (points <= 4) {
            createVertex(begin, begin + 1, end);
            if (points != 3) {
                createVertex(begin, begin + 2, end);
            }
            return;
        }
        int midPoint = begin + points / 2;
        createVertex(begin, midPoint, end);
        createSegment(begin, midPoint);
        createSegment(midPoint, end);
    }

    private void createVertex(int begin, int mid, int end) {
        float xDiff = vertices[end].x - vertices[begin].x;
        float zDiff = vertices[end].z - vertices[begin].z;

        float yDiffToMid = vertices[mid].y - vertices[begin].y;

        float yRatio = yDiffToMid / (vertices[end].y() - vertices[begin].y);

        vertices[mid].x = vertices[begin].x + xDiff * yRatio + (getRandom().nextFloat() - 0.5F) * yDiffToMid * widthVariance;
        vertices[mid].z = vertices[begin].z + zDiff * yRatio + (getRandom().nextFloat() - 0.5F) * yDiffToMid * widthVariance;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }
}