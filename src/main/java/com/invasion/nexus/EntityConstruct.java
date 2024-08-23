package com.invasion.nexus;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public record EntityConstruct (
        EntityType<? extends MobEntity> entityType,
        int texture,
        int tier,
        int flavour,
        float scaling,
        int minAngle,
        int maxAngle
    ) {

    public MobEntity createMob(NexusAccess nexus) {
        return createMob(nexus.getWorld(), nexus);
    }

    public MobEntity createMob(World world, @Nullable NexusAccess nexus) {
        MobEntity entity = entityType().create(world);
        if (entity instanceof BuildableMob b) {
            b.onSpawned(nexus, this);
        }
        return entity;
    }

    public interface BuildableMob {
        void onSpawned(NexusAccess nexus, EntityConstruct spawnConditions);
    }
}