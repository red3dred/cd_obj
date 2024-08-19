package com.invasion.nexus.spawns;

import com.invasion.nexus.EntityConstruct;

import net.minecraft.util.Formatting;

public interface Spawner {
    boolean attemptSpawn(EntityConstruct mobConstruct, int minAngle, int maxAngle);

    int getNumberOfPointsInRange(int minAngle, int maxAngle, SpawnType type);

    void sendSpawnAlert(String message, Formatting color);

    void noSpawnPointNotice();
}