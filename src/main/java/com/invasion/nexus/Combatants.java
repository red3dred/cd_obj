package com.invasion.nexus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.NexusEntity;
import com.invasion.util.math.DistanceComparators;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Box;

public class Combatants<T extends PathAwareEntity & NexusEntity> implements Iterable<T> {
    private List<T> mobList = new ArrayList<>();
    private boolean sorted;

    private final Nexus nexus;
    private final Comparator<Entity> sorter;

    public Combatants(Nexus nexus) {
        this.nexus = nexus;
        this.sorter = DistanceComparators.ofComparisonEntities(nexus.getOrigin().toCenterPos());
    }

    @SuppressWarnings("unchecked")
    public void updateMobList(Box arena) {
        mobList = (List<T>)nexus.getWorld().getEntitiesByClass(PathAwareEntity.class, arena, NexusEntity.PREDICATE);
        sorted = false;
    }

    @Nullable
    public T removeNearestCombatant() {
        if (mobList.isEmpty()) {
            return null;
        }

        if (!sorted) {
            sorted = true;
            Collections.sort(mobList, sorter);
        }
        return mobList.removeLast();
    }

    @Override
    public Iterator<T> iterator() {
        return mobList.iterator();
    }
}
