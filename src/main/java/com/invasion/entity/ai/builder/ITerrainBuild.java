package com.invasion.entity.ai.builder;

import com.invasion.INotifyTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface ITerrainBuild {
    boolean askBuildScaffoldLayer(BlockPos pos, INotifyTask task);

    boolean askBuildLadderTower(BlockPos pos, Direction orientation, int layersToBuild, INotifyTask task);

    boolean askBuildLadder(BlockPos pos, INotifyTask task);

    boolean askBuildBridge(BlockPos pos, INotifyTask task);
}