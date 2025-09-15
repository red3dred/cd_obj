package com.invasion.nexus.spawns;

import com.invasion.nexus.EntityConstruct;

import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;

public interface Spawner {
    Random getRandom();

    boolean attemptSpawn(EntityConstruct mobConstruct, IntRange angle);

    int getNumberOfPointsInRange(IntRange angle, SpawnType type);

    void sendSpawnAlert(String message, Formatting color);

    void noSpawnPointNotice();
}