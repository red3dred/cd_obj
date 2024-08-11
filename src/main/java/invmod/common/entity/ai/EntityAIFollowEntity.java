package invmod.common.entity.ai;

import java.util.EnumSet;

import invmod.common.entity.EntityIMLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class EntityAIFollowEntity<T extends LivingEntity> extends EntityAIMoveToEntity<T> {
    private float followDistanceSq;

    @SuppressWarnings("unchecked")
    public EntityAIFollowEntity(EntityIMLiving entity, float followDistance) {
        this(entity, (Class<T>)LivingEntity.class, followDistance);
    }

    public EntityAIFollowEntity(EntityIMLiving entity, Class<? extends T> target, float followDistance) {
        super(entity, target);
        this.followDistanceSq = (followDistance * followDistance);
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public void start() {
        mob.onFollowingEntity(getTarget());
        super.start();
    }

    @Override
    public void stop() {
        mob.onFollowingEntity(null);
        super.stop();
    }

    @Override
    public void tick() {
        super.tick();
        Entity target = getTarget();
        if (target != null && mob.squaredDistanceTo(target) < followDistanceSq)
            mob.getNavigatorNew().haltForTick();
    }
}