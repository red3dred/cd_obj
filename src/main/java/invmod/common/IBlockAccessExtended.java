package invmod.common;

import net.minecraft.world.BlockView;

public interface IBlockAccessExtended extends BlockView {
    int getLayeredData(int paramInt1, int paramInt2, int paramInt3);

    void setData(int paramInt1, int paramInt2, int paramInt3, Integer paramInteger);
}