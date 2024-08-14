package com.invasion.entity;

import com.invasion.util.math.CoordsInt;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface ICanDig {

    BlockPos toBlockPos();

    default BlockPos[] getBlockRemovalOrder(BlockPos pos) {
        if (toBlockPos().getY() >= pos.getY()) {
            return new BlockPos[] {
                pos,
                pos.up()
            };
        }

        return new BlockPos[] {
            pos.up(),
            toBlockPos().up(getCollideSize().getYCoord()),
            pos
        };
    }

    float getBlockRemovalCost(BlockPos pos);

    boolean canClearBlock(BlockPos pos);

    default void onBlockRemoved(BlockPos pos, BlockState state) {

    }

    BlockView getTerrain();

    CoordsInt getCollideSize();
}