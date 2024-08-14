package com.invasion.entity;

import org.jetbrains.annotations.Nullable;

import com.invasion.nexus.INexusAccess;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public abstract class EntityIMMob extends EntityIMLiving {
    public EntityIMMob(EntityType<? extends EntityIMMob> type, World world, @Nullable INexusAccess nexus) {
        super(type, world, nexus);
    }
}