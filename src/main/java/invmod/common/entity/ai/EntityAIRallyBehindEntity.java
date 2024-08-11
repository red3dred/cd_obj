package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.ILeader;
import net.minecraft.entity.LivingEntity;

public class EntityAIRallyBehindEntity<T extends LivingEntity> extends EntityAIFollowEntity<T> {
    private static final float DEFAULT_FOLLOW_DISTANCE = 5;

    public EntityAIRallyBehindEntity(EntityIMLiving entity, Class<T> leader) {
        this(entity, leader, DEFAULT_FOLLOW_DISTANCE);
    }

    public EntityAIRallyBehindEntity(EntityIMLiving entity, Class<T> leader, float followDistance) {
        super(entity, leader, followDistance);
    }

    @Override
    public boolean canStart() {
        return mob.readyToRally() && (super.canStart());
    }

    @Override
    public boolean shouldContinue() {
        return mob.readyToRally() && super.shouldContinue();
    }

    @Override
    public void tick() {
        super.tick();
        if (mob.readyToRally() && getTarget() instanceof ILeader leader && leader.isMartyr()) {
            mob.rally(leader);
        }
    }
}