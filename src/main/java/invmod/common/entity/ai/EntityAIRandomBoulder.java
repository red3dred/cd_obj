package invmod.common.entity.ai;

import invmod.common.entity.EntityIMThrower;
import invmod.common.nexus.INexusAccess;
import net.minecraft.entity.ai.goal.Goal;

public class EntityAIRandomBoulder extends Goal {
    private final EntityIMThrower theEntity;
    private int randomAmmo;
    private int timer = 180;

    public EntityAIRandomBoulder(EntityIMThrower entity, int ammo) {
        theEntity = entity;
        randomAmmo = ammo;
    }

    @Override
    public boolean canStart() {
        return theEntity.getNexus() != null && randomAmmo > 0 && theEntity.canThrow() && --timer <= 0;
    }

    @Override
    public boolean canStop() {
        return false;
    }

    @Override
    public void start() {
        randomAmmo--;
        timer = 240;
        INexusAccess nexus = theEntity.getNexus();
        int d = (int) (theEntity.findDistanceToNexus() * 0.37D);
        if (d == 0) {
            d = 1;
        }
        theEntity.throwProjectile(
                nexus.getXCoord() - d + theEntity.getRandom().nextInt(2 * d),
                nexus.getYCoord() - 5 + theEntity.getRandom().nextInt(10),
                nexus.getZCoord() - d + theEntity.getRandom().nextInt(2 * d),
                theEntity.createProjectile(0)
        );
    }
}