package invmod.common.entity;

import invmod.common.INotifyTask;
import invmod.common.util.IPosition;
import net.minecraft.util.math.Direction;

public interface ITerrainBuild {
    boolean askBuildScaffoldLayer(IPosition pos, INotifyTask task);

    boolean askBuildLadderTower(IPosition pos, Direction orientation, int layersToBuild, INotifyTask task);

    boolean askBuildLadder(IPosition pos, INotifyTask task);

    boolean askBuildBridge(IPosition pos, INotifyTask task);
}