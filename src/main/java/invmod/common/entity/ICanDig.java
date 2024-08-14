package invmod.common.entity;

import invmod.common.util.CoordsInt;
import invmod.common.util.IPosition;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface ICanDig extends IPosition {
    default IPosition[] getBlockRemovalOrder(BlockPos pos) {
        if (toBlockPos().getY() >= pos.getY()) {
            return new IPosition[] {
                new CoordsInt(pos),
                new CoordsInt(pos.up())
            };
        }

        return new IPosition[] {
            new CoordsInt(pos.up()),
            new CoordsInt(toBlockPos().up(getCollideSize().getYCoord())),
            new CoordsInt(pos)
        };
    }

    float getBlockRemovalCost(BlockPos pos);

    boolean canClearBlock(BlockPos pos);

    default void onBlockRemoved(BlockPos pos, BlockState state) {

    }

    BlockView getTerrain();

    CoordsInt getCollideSize();
}