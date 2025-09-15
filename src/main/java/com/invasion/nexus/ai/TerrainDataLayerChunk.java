package com.invasion.nexus.ai;

import com.invasion.nexus.ai.scaffold.ScaffoldView;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

class TerrainDataLayerChunk implements ScaffoldView, BlockView {
    protected final Long2ObjectMap<Integer> data = new Long2ObjectOpenHashMap<>();
    private final BlockView world;

    public TerrainDataLayerChunk(BlockView world) {
        this.world = world;
    }

    public TerrainDataLayerChunk(BlockView world, Long2ObjectMap<Integer> dataLayer) {
        this.world = world;
        this.data.putAll(dataLayer);
    }

    @Override
    public void setData(BlockPos pos, int data) {
        this.data.put(pos.asLong(), Integer.valueOf(data));
    }

    @Override
    public int getData(BlockPos pos) {
        return data.getOrDefault(pos.asLong(), Integer.valueOf(0));
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