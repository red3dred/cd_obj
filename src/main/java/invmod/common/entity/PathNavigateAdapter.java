package invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
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