package com.invasion.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

// TODO: What is this for?
@Deprecated(since = "unused")
public class SfxEntity extends Entity {
    private int lifespan;

    public SfxEntity(EntityType<SfxEntity> type, World world) {
        super(type, world);
        this.lifespan = 200;
    }

    public SfxEntity(EntityType<SfxEntity> type, World world, double x, double y, double z) {
        this(type, world);
        setPosition(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (lifespan-- <= 0) {
            discard();
        }
    }

    @Override
    public void handleStatus(byte byte0) {
    }

    @Override
    protected void initDataTracker(Builder builder) {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }
}