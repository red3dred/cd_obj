package invmod.common;

import invmod.common.entity.PathAction;
import invmod.common.entity.PathNode;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class TerrainDataLayer implements IBlockAccessExtended {
    public static final int EXT_DATA_SCAFFOLD_METAPOSITION = 16384;

    private final Int2IntMap dataLayer = new Int2IntOpenHashMap();
    private final BlockView world;

    public TerrainDataLayer(BlockView world) {
        this.world = world;
    }

    @Deprecated
    public TerrainDataLayer(BlockView world, Int2IntMap dataLayer) {
        this.world = world;
        this.dataLayer.putAll(dataLayer);
    }

    @Override
    public void setData(BlockPos pos, Integer data) {
        dataLayer.put(PathNode.makeHash(pos, PathAction.NONE), data.intValue());
    }

    @Override
    public int getData(BlockPos pos) {
        return dataLayer.get(PathNode.makeHash(pos, PathAction.NONE));
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return world.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return world.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return world.getFluidState(pos);
    }

    @Override
    public int getHeight() {
        return world.getHeight();
    }

    @Override
    public int getBottomY() {
        return world.getBottomY();
    }
}