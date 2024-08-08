package invmod.common.entity;

import invmod.common.IPathfindable;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface IPathSource {
    Path createPath(IPathfindable paramIPathfindable, BlockPos from, BlockPos to, float paramFloat1, float paramFloat2, BlockView world);

    Path createPath(EntityIMLiving paramEntityIMLiving, Entity paramEntity, float paramFloat1, float paramFloat2, BlockView world);

    Path createPath(EntityIMLiving paramEntityIMLiving, int paramInt1, int paramInt2, int paramInt3, float paramFloat1, float paramFloat2, BlockView world);

    void createPath(IPathResult paramIPathResult, IPathfindable paramIPathfindable, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, float paramFloat, BlockView paramIBlockAccess);

    void createPath(IPathResult paramIPathResult, EntityIMLiving paramEntityIMLiving, Entity paramEntity, float paramFloat, BlockView world);

    void createPath(IPathResult paramIPathResult, EntityIMLiving paramEntityIMLiving, int paramInt1, int paramInt2, int paramInt3, float paramFloat, BlockView world);

    int getSearchDepth();

    int getQuickFailDepth();

    void setSearchDepth(int paramInt);

    void setQuickFailDepth(int paramInt);

    boolean canPathfindNice(PathPriority paramPathPriority, float paramFloat, int paramInt1, int paramInt2);

    public enum PathPriority {
        LOW, MEDIUM, HIGH
    }
}