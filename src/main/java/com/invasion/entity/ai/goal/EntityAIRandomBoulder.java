package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMThrower;
import com.invasion.nexus.INexusAccess;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

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
        int d = Math.max(1, (int) (theEntity.findDistanceToNexus() * 0.37D));
        theEntity.throwProjectile(new Vec3d(
                nexus.getXCoord() - d + theEntity.getRandom().nextInt(2 * d),
                nexus.getYCoord() - 5 + theEntity.getRandom().nextInt(10),
                nexus.getZCoord() - d + theEntity.getRandom().nextInt(2 * d)
        ), theEntity.createProjectile(0));
    }
}