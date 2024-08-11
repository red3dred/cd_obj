package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.EntityIMPigEngy;

public class EntityAIWaitForEngy extends EntityAIFollowEntity<EntityIMPigEngy> {
    private final boolean canHelp;

    public EntityAIWaitForEngy(EntityIMLiving entity, float followDistance, boolean canHelp) {
        super(entity, EntityIMPigEngy.class, followDistance);
        this.canHelp = canHelp;
    }

    @Override
    public void tick() {
        super.tick();
        if (canHelp && getTarget() != null) {
            getTarget().supportForTick(mob, 1);
        }
    }
}