package com.invasion.nexus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.EntityIMLiving;
import com.invasion.util.math.ComparatorDistanceFrom;

import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;

public class Combatants implements Iterable<EntityIMLiving> {
    private List<EntityIMLiving> mobList = new ArrayList<>();
    private boolean sorted;

    private final Nexus nexus;
    private final Comparator<Entity> sorter;

    public Combatants(Nexus nexus) {
        this.nexus = nexus;
        this.sorter = ComparatorDistanceFrom.ofComparisonEntities(nexus.getOrigin().toCenterPos());
    }

    public void updateMobList(Box arena) {
        mobList = nexus.getWorld().getEntitiesByClass(EntityIMLiving.class, arena, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
        sorted = false;
    }

    @Nullable
    public EntityIMLiving removeNearestCombatant() {
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
    public Iterator<EntityIMLiving> iterator() {
        return mobList.iterator();
    }
}
