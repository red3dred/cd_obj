package com.invasion.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public abstract class IMMobEntity extends EntityIMLiving {
    public IMMobEntity(EntityType<? extends IMMobEntity> type, World world) {
        super(type, world);
    }
}