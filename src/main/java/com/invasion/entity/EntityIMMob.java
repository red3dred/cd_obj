package com.invasion.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public abstract class EntityIMMob extends EntityIMLiving {
    public EntityIMMob(EntityType<? extends EntityIMMob> type, World world) {
        super(type, world);
    }
}