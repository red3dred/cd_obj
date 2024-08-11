package invmod.common;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface IBlockAccessExtended extends BlockView {
    int MOB_DENSITY_FLAG = 0x7;
    int EXT_DATA_SCAFFOLD_METAPOSITION = 0x4000;

    void setData(BlockPos pos, int data);

    int getData(BlockPos pos);

    static int getData(BlockView view, BlockPos pos) {
        return view instanceof IBlockAccessExtended i ? i.getData(pos) : 0;
    }
}