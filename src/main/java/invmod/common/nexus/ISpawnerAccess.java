package invmod.common.nexus;

import net.minecraft.util.Formatting;

public interface ISpawnerAccess {
    boolean attemptSpawn(EntityConstruct mobConstruct, int minAngle, int maxAngle);

    int getNumberOfPointsInRange(int minAngle, int maxAngle, SpawnType type);

    void sendSpawnAlert(String message, Formatting color);

    void noSpawnPointNotice();
}