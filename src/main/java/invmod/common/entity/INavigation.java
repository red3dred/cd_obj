package invmod.common.entity;

import org.jetbrains.annotations.Nullable;

import invmod.common.INotifyTask;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public interface INavigation extends INotifyTask {
    PathAction getCurrentWorkingAction();

    void setSpeed(float paramFloat);

    Path getPathToXYZ(Vec3d pos, float paramFloat);

    boolean tryMoveToXYZ(Vec3d pos, float paramFloat1, float paramFloat2);

    Path getPathTowardsXZ(double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, int paramInt3);

    boolean tryMoveTowardsXZ(double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, int paramInt3, float paramFloat);

    @Nullable
    Path getPathToEntity(Entity paramEntity, float paramFloat);

    boolean tryMoveToEntity(Entity paramEntity, float paramFloat1, float paramFloat2);

    void autoPathToEntity(Entity paramEntity);

    boolean setPath(Path paramPath, float paramFloat);

    boolean isWaitingForTask();

    Path getPath();

    void onUpdateNavigation();

    int getLastActionResult();

    boolean noPath();

    int getStuckTime();

    float getLastPathDistanceToTarget();

    void clearPath();

    void haltForTick();

    Entity getTargetEntity();

    String getStatus();
}