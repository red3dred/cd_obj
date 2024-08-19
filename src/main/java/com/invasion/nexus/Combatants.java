package com.invasion.nexus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.invasion.util.math.DistanceComparators;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Box;

public class Combatants implements Iterable<Combatant<?>> {
    private List<Combatant<?>> mobList = new ArrayList<>();
    private boolean sorted;

    private final Nexus nexus;
    private final Comparator<Combatant<?>> sorter;

    public Combatants(Nexus nexus) {
        this.nexus = nexus;
        this.sorter = Comparator.comparing(Combatant::asEntity, DistanceComparators.ofComparisonEntities(nexus.getOrigin().toCenterPos()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void updateMobList(Box arena) {
        mobList = (List)nexus.getWorld().getEntitiesByClass(PathAwareEntity.class, arena, Combatant.PREDICATE);
        sorted = false;
    }

    @Nullable
    public Combatant<?> removeNearestCombatant() {
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
    public Iterator<Combatant<?>> iterator() {
        return mobList.iterator();
    }
}
