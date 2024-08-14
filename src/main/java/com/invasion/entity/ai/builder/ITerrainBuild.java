package com.invasion.entity.ai.builder;

import com.invasion.INotifyTask;
import com.invasion.util.math.IPosition;

import net.minecraft.util.math.Direction;

public interface ITerrainBuild {
    boolean askBuildScaffoldLayer(IPosition pos, INotifyTask task);

    boolean askBuildLadderTower(IPosition pos, Direction orientation, int layersToBuild, INotifyTask task);

    boolean askBuildLadder(IPosition pos, INotifyTask task);

    boolean askBuildBridge(IPosition pos, INotifyTask task);
}