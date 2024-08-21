package com.invasion.entity.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Deprecated
public class PathNavigateAdapter extends EntityNavigation {
    private final Navigation navigator;

    public PathNavigateAdapter(MobEntity entity, World world, Navigation navigator) {
        super(entity, world);
        this.navigator = navigator;
    }

    public Navigation getNewNavigator() {
        return navigator;
    }

    @Override
    public void tick() {
        ((IMNavigation)navigator).tick();
    }

    @Override
    public void recalculatePath() {

    }

    @Override
    public boolean isIdle() {
        return navigator.isIdle();
    }

    @Override
    public void stop() {
        ((IMNavigation)navigator).stop();
    }

    @Override
    public void setSpeed(double speed) {
        ((IMNavigation)navigator).setSpeed(speed);
    }

    @Override
    public boolean startMovingTo(double x, double y, double z, double movespeed) {
        return ((IMNavigation)navigator).startMovingTo(x, y, z, (float) movespeed);
    }

    @Override
    public boolean startMovingTo(Entity entity, double movespeed) {
        return ((IMNavigation)navigator).startMovingTo(entity, (float) movespeed);
    }

    @Override
    public PathNodeMaker getNodeMaker() {
        return null;
    }

    @Override
    public void setCanSwim(boolean canSwim) {
        navigator.getActor().setCanSwim(canSwim);
    }

    @Override
    public boolean canSwim() {
        return true;
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        return null;
    }

    @Override
    protected Vec3d getPos() {
        return ((IMNavigation)navigator).getPos();
    }

    @Override
    protected boolean isAtValidPosition() {
        return true;
    }
}