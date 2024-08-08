package invmod.common.entity;

import invmod.common.util.IPosition;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface ICanDig {
    IPosition[] getBlockRemovalOrder(BlockPos pos);

    float getBlockRemovalCost(BlockPos pos);

    boolean canClearBlock(BlockPos pos);

    void onBlockRemoved(BlockPos pos, BlockState state);

    BlockView getTerrain();
}