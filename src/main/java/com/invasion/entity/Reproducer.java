package com.invasion.entity;

import java.util.List;

import net.minecraft.entity.Entity;

public interface Reproducer {
    List<Entity> getOffspring(Entity paramEntity);
}