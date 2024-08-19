package com.invasion.entity.ai.builder;

import com.invasion.Notifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface ITerrainBuild {
    boolean askBuildScaffoldLayer(BlockPos pos, Notifiable task);

    boolean askBuildLadderTower(BlockPos pos, Direction orientation, int layersToBuild, Notifiable task);

    boolean askBuildLadder(BlockPos pos, Notifiable task);

    boolean askBuildBridge(BlockPos pos, Notifiable task);
}