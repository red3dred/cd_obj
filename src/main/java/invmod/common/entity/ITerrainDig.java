package invmod.common.entity;

import invmod.common.INotifyTask;
import net.minecraft.util.math.BlockPos;

public interface ITerrainDig {
    boolean askRemoveBlock(BlockPos pos, INotifyTask paramINotifyTask, float paramFloat);

    boolean askClearPosition(BlockPos pos, INotifyTask paramINotifyTask, float paramFloat);
}