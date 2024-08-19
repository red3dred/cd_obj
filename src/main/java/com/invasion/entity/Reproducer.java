package com.invasion.entity;

import net.minecraft.entity.Entity;

public interface Reproducer {
    Entity[] getOffspring(Entity paramEntity);
}