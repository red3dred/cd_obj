package com.invasion.nexus.ai;

import java.util.List;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.border.WorldBorder;

class TerrainDataLayer extends TerrainDataLayerChunk implements CollisionView {
    private final Long2ObjectMap<BlockView> chunks = new Long2ObjectOpenHashMap<>();
    private final CollisionView world;

    public TerrainDataLayer(CollisionView world) {
        super(world);
        this.world = world;
    }

    public TerrainDataLayer(CollisionView world, Long2ObjectMap<Integer> dataLayer) {
        this(world);
        this.data.putAll(dataLayer);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return world.getWorldBorder();
    }

    @Override
    public BlockView getChunkAsView(int chunkX, int chunkZ) {
        BlockView chunk = world.getChunkAsView(chunkX, chunkZ);
        return chunk == null ? null : chunks.computeIfAbsent(ChunkPos.toLong(chunkX, chunkZ), l -> new TerrainDataLayerChunk(chunk));
    }

    @Override
    public List<VoxelShape> getEntityCollisions(Entity entity, Box box) {
        return world.getEntityCollisions(entity, box);
    }
}