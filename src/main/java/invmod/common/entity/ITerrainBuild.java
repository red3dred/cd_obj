package invmod.common.entity;

import invmod.common.INotifyTask;
import invmod.common.util.IPosition;

public interface ITerrainBuild {
    boolean askBuildScaffoldLayer(IPosition pos, INotifyTask task);

    boolean askBuildLadderTower(IPosition pos, int paramInt1, int paramInt2, INotifyTask task);

    boolean askBuildLadder(IPosition pos, INotifyTask task);

    boolean askBuildBridge(IPosition pos, INotifyTask task);
}