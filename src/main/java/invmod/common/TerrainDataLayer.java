package invmod.common;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class TerrainDataLayer implements IBlockAccessExtended {
    private final Long2ObjectMap<Integer> dataLayer = new Long2ObjectOpenHashMap<>();
    private final BlockView world;

    public TerrainDataLayer(BlockView world) {
        this.world = world;
    }

    public TerrainDataLayer(BlockView world, Long2ObjectMap<Integer> dataLayer) {
        this.world = world;
        this.dataLayer.putAll(dataLayer);
    }

    @Override
    public void setData(BlockPos pos, int data) {
        dataLayer.put(pos.asLong(), Integer.valueOf(data));
    }

    @Override
    public int getData(BlockPos pos) {
        return dataLayer.getOrDefault(pos.asLong(), Integer.valueOf(0));
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