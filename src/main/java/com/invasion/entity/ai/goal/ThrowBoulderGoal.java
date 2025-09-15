package com.invasion.entity.ai.goal;

import com.invasion.entity.ThrowerEntity;
import com.invasion.nexus.NexusAccess;

import net.minecraft.entity.ai.goal.Goal;

public class ThrowBoulderGoal extends Goal {
    private final ThrowerEntity theEntity;
    private int randomAmmo;
    private int timer = 180;

    public ThrowBoulderGoal(ThrowerEntity entity, int ammo) {
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
        NexusAccess nexus = theEntity.getNexus();
        int d = Math.max(1, (int) (theEntity.findDistanceToNexus() * 0.37D));
        theEntity.throwProjectile(nexus.getOrigin().toBottomCenterPos().add(
                theEntity.getRandom().nextTriangular(0, d),
                theEntity.getRandom().nextTriangular(0, 10),
                theEntity.getRandom().nextTriangular(0, d)
        ), theEntity.createProjectile(0));
    }
}