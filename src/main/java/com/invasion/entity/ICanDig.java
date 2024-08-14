package com.invasion.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
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
            toBlockPos().up(getCollideSize().getY()),
            pos
        };
    }

    float getBlockRemovalCost(BlockPos pos);

    boolean canClearBlock(BlockPos pos);

    default void onBlockRemoved(BlockPos pos, BlockState state) {

    }

    BlockView getTerrain();

    Vec3i getCollideSize();
}