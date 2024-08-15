package com.invasion.nexus;

public interface ControllableNexusAccess extends INexusAccess {

    boolean start(int wave);

    void stop(boolean killEnemies);

    boolean setSpawnRadius(int radius);
}
