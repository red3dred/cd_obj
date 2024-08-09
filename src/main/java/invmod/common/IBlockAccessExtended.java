package invmod.common;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface IBlockAccessExtended extends BlockView {
    int MOB_DENSITY_FLAG = 0x7;

    void setData(BlockPos pos, Integer data);

    int getData(BlockPos pos);
}