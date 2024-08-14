package invmod.common.entity.ai;

import invmod.common.entity.EntityIMThrower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class EntityAIThrowerKillEntity<T extends LivingEntity> extends EntityAIKillEntity<T> {
    private boolean melee;
    private final EntityIMThrower theEntity;
    private int maxBoulderAmount = 3;

    public EntityAIThrowerKillEntity(EntityIMThrower entity, Class<? extends T> targetClass, int attackDelay, float throwRange, float launchSpeed) {
        super(entity, targetClass, attackDelay);
        this.theEntity = entity;
    }

    @Override
    protected void attackEntity(Entity target) {
        if (melee) {
            setAttackTime(getAttackDelay());
            super.attackEntity(target);
        } else {
            setAttackTime(getAttackDelay() * 2);
            int distance = Math.round(theEntity.distanceTo(target));
            int missDistance = Math.round((float) Math.ceil(distance / 10));

            for (int i = 1; i <= theEntity.getRandom().nextInt(maxBoulderAmount); i++) {
                double x = (target.getX() - missDistance) + theEntity.getRandom().nextInt((missDistance + 1) * 2);
                double y = (target.getY() - missDistance + 1) + theEntity.getRandom().nextInt((missDistance + 1) * 2);
                double z = (target.getZ() - missDistance) + theEntity.getRandom().nextInt((missDistance + 1) * 2);

                theEntity.throwProjectile(new Vec3d(x, y, z));
            }
        }
    }

    @Override
    protected boolean canAttackEntity(Entity target) {
        melee = super.canAttackEntity(target);
        if (melee) {
            return true;
        }
        if (!theEntity.canThrow()) {
            return false;
        }

        double dXY = theEntity.getPos().subtract(target.getPos()).horizontalLength();
        return getAttackTime() <= 0 && theEntity.getVisibilityCache().canSee(target) && theEntity.getThrowPower(dXY) <= 1;
    }
}