package com.invasion.entity.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PathNavigateAdapter extends EntityNavigation {
    private final INavigation navigator;
    private MobNavigation mobNavigation;
    private boolean useOldNavigation;

    public PathNavigateAdapter(MobEntity entity, World world, INavigation navigator) {
        super(entity, world);
        this.navigator = navigator;
    }

    public INavigation getNewNavigator() {
        return navigator;
    }

    protected MobNavigation getMobNavigation() {
        if (mobNavigation == null) {
            mobNavigation = new MobNavigation(entity, world);
        }
        return mobNavigation;
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
        return navigator.tryMoveToXYZ(new Vec3d(x, y, z), 0, (float) movespeed);
    }

    @Override
    public boolean startMovingTo(Entity entity, double movespeed) {
        return navigator.tryMoveToEntity(entity, 0.0F, (float) movespeed);
    }

    @Override
    public PathNodeMaker getNodeMaker() {
        return getMobNavigation().getNodeMaker();
    }

    @Override
    public void setCanSwim(boolean canSwim) {
        navigator.getActor().setCanSwim(canSwim);
        getMobNavigation().setCanSwim(canSwim);
    }

    @Override
    public boolean canSwim() {
        return getMobNavigation().canSwim();
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        return null;
    }

    @Override
    protected Vec3d getPos() {
        return navigator.getEntityPosition();
    }

    @Override
    protected boolean isAtValidPosition() {
        return true;
    }
}