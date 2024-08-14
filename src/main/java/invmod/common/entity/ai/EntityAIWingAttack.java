package invmod.common.entity.ai;

import org.jetbrains.annotations.Nullable;

import invmod.common.entity.EntityIMBird;
import net.minecraft.entity.LivingEntity;

public class EntityAIWingAttack<T extends LivingEntity> extends EntityAIMeleeAttack<T> {
    private EntityIMBird mob;

    public EntityAIWingAttack(EntityIMBird entity, Class<? extends T> targetClass, int attackDelay) {
        super(entity, targetClass, attackDelay);
        mob = entity;
    }

    @Override
    public void tick() {
        if (getAttackTime() == 0) {
            mob.setAttackingWithWings(isInStartMeleeRange());
        }
        super.tick();
    }

    @Override
    public void stop() {
        mob.setAttackingWithWings(false);
    }

    protected boolean isInStartMeleeRange() {
        @Nullable
        LivingEntity target = mob.getTarget();
        return target != null && mob.isInRange(target, mob.getWidth() + 3);
    }
}