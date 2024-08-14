package com.invasion.entity.ai.builder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.atomic.AtomicReference;

import com.invasion.util.math.IPosition;

public record ModifyBlockEntry(
        BlockPos pos,
        AtomicReference<BlockState> oldBlock,
        BlockState newBlock,
        int cost
    ) implements IPosition {

    public static ModifyBlockEntry ofDeletion(BlockPos pos, int cost) {
        return new ModifyBlockEntry(pos, Blocks.AIR.getDefaultState(), cost);
    }

    public ModifyBlockEntry(BlockPos pos, BlockState state, int cost) {
        this(pos, new AtomicReference<>(null), state, cost);
    }

    @Override
    public int getXCoord() {
        return pos.getX();
    }

    @Override
    public int getYCoord() {
        return pos.getY();
    }

    @Override
    public int getZCoord() {
        return pos.getZ();
    }

    public int getCost() {
        return cost;
    }

    public BlockState getOldBlock() {
        return oldBlock.get();
    }

    public void setOldBlock(BlockState state) {
        oldBlock.set(state);
    }
}