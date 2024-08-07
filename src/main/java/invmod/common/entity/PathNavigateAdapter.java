package invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.util.math.Vec3d;

public class PathNavigateAdapter extends EntityNavigation {
    private NavigatorIM navigator;

    public PathNavigateAdapter(NavigatorIM navigator) {
        super(navigator.getEntity(), navigator.getEntity().getWorld());
        this.navigator = navigator;
    }

    public void onUpdateNavigation() {
        this.navigator.onUpdateNavigation();
    }

    public boolean noPath() {
        return this.navigator.noPath();
    }

    public void clearPathEntity() {
        this.navigator.clearPath();
    }

    @Override
    public void setSpeed(double speed) {
        this.navigator.setSpeed((float) speed);
    }

    public boolean tryMoveToXYZ(double x, double y, double z, double movespeed) {
        return this.navigator.tryMoveToXYZ(x, y, z, 0.0F, (float) movespeed);
    }

    public boolean tryMoveToEntityLiving(Entity entity, double movespeed) {
        return this.navigator.tryMoveToEntity(entity, 0.0F, (float) movespeed);
    }

    public boolean setPath(Path entity, float movespeed) {
        return this.navigator.setPath(entity, movespeed);
    }

    public boolean setPath(Path entity, double movespeed) {
        return false;
    }

    public Path findPathTo(double x, double y, double z) {
        return null;
    }

    public void setAvoidsWater(boolean par1) {
    }

    public boolean getAvoidsWater() {
        return false;
    }

    public void setBreakDoors(boolean par1) {
    }

    public void setEnterDoors(boolean par1) {
    }

    public boolean getCanBreakDoors() {
        return false;
    }

    public void setAvoidSun(boolean par1) {
    }

    @Override
    public void setCanSwim(boolean par1) {
    }

    public Path findPathTo(Entity par1EntityLiving) {
        return null;
    }

    public PathEntity getPath() {
        return null;
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Vec3d getPos() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean isAtValidPosition() {
        // TODO Auto-generated method stub
        return false;
    }
}