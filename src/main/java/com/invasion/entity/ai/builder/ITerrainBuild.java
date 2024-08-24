package com.invasion.entity.ai.builder;

import java.util.stream.Stream;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface ITerrainBuild {
    Stream<ModifyBlockEntry> askBuildScaffoldLayer(BlockPos pos);

    Stream<ModifyBlockEntry> askBuildLadderTower(BlockPos pos, Direction orientation, int layersToBuild);

    Stream<ModifyBlockEntry> askBuildLadder(BlockPos pos);

    Stream<ModifyBlockEntry> askBuildBridge(BlockPos pos);
}