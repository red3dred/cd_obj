package com.invasion.nexus.ai.scaffold;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record ScaffoldNode(
        BlockPos pos,
        Direction orientation,
        int height
    ) {
    public ScaffoldNode(NbtCompound compound) {
        this(
            NbtHelper.toBlockPos(compound, "pos").orElse(BlockPos.ORIGIN),
            Direction.fromHorizontal(compound.getInt("orientation")),
            compound.getInt("height")
        );
    }

    public void toNbt(NbtCompound compound) {
        compound.put("pos", NbtHelper.fromBlockPos(pos));
        compound.putInt("orientation", orientation.getHorizontal());
        compound.putInt("height", height);
    }

    public int bottom() {
        return pos.getY();
    }

    public int top() {
        return bottom() + height();
    }

    public ScaffoldNode merge(ScaffoldNode newScaffold) {
        BlockPos newPos = newScaffold.pos();

        if (pos.getX() != newPos.getX() || pos.getZ() != newPos.getZ()) {
            return this;
        }

        int yChange = newScaffold.bottom() - bottom();

        if (yChange > 0 && yChange < height()) {
            return new ScaffoldNode(pos, orientation, yChange + newScaffold.height());
        }

        if (newScaffold.top() > bottom()) {
            return new ScaffoldNode(newPos, orientation, height() + newScaffold.height());
        }

        return this;
    }

    public boolean contains(BlockPos pos) {
        return pos().getX() == pos.getX() && pos().getZ() == pos.getZ()
                && bottom() <= pos.getY()
                && top() >= pos.getY();
    }
}
