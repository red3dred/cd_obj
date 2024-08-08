package invmod.common.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;

import invmod.common.util.IPosition;

public record ModifyBlockEntry(
        BlockPos pos,
        @Nullable BlockState oldBlock,
        BlockState newBlock,
        int cost
    ) implements IPosition {

    public ModifyBlockEntry(BlockPos pos, BlockState state, int cost) {
        this(pos, null, state, cost);
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
        return oldBlock;
    }

    public ModifyBlockEntry withOldState(BlockState oldState) {
        return new ModifyBlockEntry(pos, oldState, newBlock, cost);
    }
}