package invmod.common.entity.ai;

import invmod.common.entity.EntityIMSpider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

public class EntityAIPounce extends Goal {
    private final EntityIMSpider theEntity;
    private final float minPower;
    private final float maxPower;

    private boolean isPouncing;
    private int pounceTimer;
    private int cooldown;

    public EntityAIPounce(EntityIMSpider entity, float minPower, float maxPower, int cooldown) {
        this.theEntity = entity;
        this.minPower = minPower;
        this.maxPower = maxPower;
        this.cooldown = cooldown;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = theEntity.getTarget();
        return --pounceTimer <= 0 && target != null && theEntity.getVisibilityCache().canSee(target) && theEntity.isOnGround();
    }

    public boolean continueExecuting() {
        return this.isPouncing;
    }

    public void startExecuting() {
        if (pounce(theEntity.getTarget().getPos())) {
            theEntity.setAirborneTime(0);
            isPouncing = true;
            theEntity.getNavigatorNew().haltForTick();
        } else {
            isPouncing = false;
        }
    }

    public void updateTask() {
        theEntity.getNavigatorNew().haltForTick();
        int airborneTime = theEntity.getAirborneTime();
        if (airborneTime > 20 && theEntity.isOnGround()) {
            isPouncing = false;
            pounceTimer = cooldown;
            theEntity.setAirborneTime(0);
            theEntity.getNavigatorNew().clearPath();
        } else {
            theEntity.setAirborneTime(airborneTime + 1);
        }
    }

    protected boolean pounce(Vec3d pos) {
        Vec3d delta = pos.subtract(theEntity.getPos());
        double dXZ = delta.length();
        double a = Math.atan(delta.y / dXZ);
        if (Math.abs(a) > 0.7853981633974483D) {
            double r = dXZ / ((1 - Math.tan(a)) * (1D / Math.cos(a)));
            double v = 1D / Math.sqrt(1F / theEntity.getGravity() / r);
            if (v > minPower && v < maxPower) {
                double distance = Math.sqrt(2 * (dXZ * dXZ));
                theEntity.addVelocity(
                        (v * delta.x / distance),
                        (v * dXZ / distance),
                        (v * delta.z / distance)
                );
                return true;
            }
        }
        return false;
    }
}