package com.invasion.entity.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PathNavigateAdapter extends EntityNavigation {
    private final INavigation navigator;

    public PathNavigateAdapter(MobEntity entity, World world, INavigation navigator) {
        super(entity, world);
        this.navigator = navigator;
    }

    public INavigation getNewNavigator() {
        return navigator;
    }

    @Override
    public void tick() {
        navigator.onUpdateNavigation();
    }

    @Override
    public void recalculatePath() {

    }

    @Override
    public boolean isIdle() {
        return navigator.noPath();
    }

    @Override
    public void stop() {
        navigator.clearPath();
    }

    @Override
    public void setSpeed(double speed) {
        navigator.setSpeed((float) speed);
    }

    @Override
    public boolean startMovingTo(double x, double y, double z, double movespeed) {
        return navigator.tryMoveToXYZ(new Vec3d(x, y, z), 0.0F, (float) movespeed);
    }

    @Override
    public boolean startMovingTo(Entity entity, double movespeed) {
        return navigator.tryMoveToEntity(entity, 0.0F, (float) movespeed);
    }

    @Override
    public PathNodeMaker getNodeMaker() {
        return null;
    }

    @Override
    public void setCanSwim(boolean par1) {
    }

    @Override
    public boolean canSwim() {
        return false;
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        return null;
    }

    @Override
    protected Vec3d getPos() {
        return entity.getPos();
    }

    @Override
    protected boolean isAtValidPosition() {
        return true;
    }
}