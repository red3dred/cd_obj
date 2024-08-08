package invmod.common.entity;

import invmod.common.INotifyTask;
import invmod.common.util.CoordsInt;
import invmod.common.util.IPosition;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class TerrainBuilder implements ITerrainBuild {
    private static final float LADDER_COST = 25;
    private static final float PLANKS_COST = 45;
    private static final float COBBLE_COST = 65;

    private EntityIMLiving mob;
    private ITerrainModify modifier;
    private float buildRate;

    public TerrainBuilder(EntityIMLiving entity, ITerrainModify modifier, float buildRate) {
        this.mob = entity;
        this.modifier = modifier;
        this.buildRate = buildRate;
    }

    public void setBuildRate(float buildRate) {
        this.buildRate = buildRate;
    }

    public float getBuildRate() {
        return this.buildRate;
    }

    @Override
    public boolean askBuildScaffoldLayer(IPosition position, INotifyTask asker) {
        if (!modifier.isReadyForTask(asker)) {
            return false;
        }
        BlockPos pos = position.toBlockPos();
        Scaffold scaffold = mob.getNexus().getAttackerAI().getScaffoldAt(pos);
        if (scaffold == null) {
            return false;
        }

        int height = pos.getY() - scaffold.getYCoord();
        int xOffset = CoordsInt.offsetAdjX[scaffold.getOrientation()];
        int zOffset = CoordsInt.offsetAdjZ[scaffold.getOrientation()];
        BlockPos posBelow = pos.add(xOffset, -1, zOffset);
        BlockState block = this.mob.getWorld().getBlockState(posBelow);
        List<ModifyBlockEntry> modList = new ArrayList<>();

        if (height == 1) {
            if (!block.isFullCube(mob.getWorld(), posBelow)) {
                modList.add(new ModifyBlockEntry(posBelow, Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
            }
            if (this.mob.getWorld().isAir(pos.down())) {
                modList.add(new ModifyBlockEntry(pos.down(), Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
            }
        }

        posBelow = pos.add(xOffset, 0, zOffset);
        block = this.mob.getWorld().getBlockState(posBelow);
        if (!block.isFullCube(mob.getWorld(), posBelow)) {
            modList.add(new ModifyBlockEntry(posBelow, Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
        }
        block = this.mob.getWorld().getBlockState(pos);
        if (!block.isOf(Blocks.LADDER)) {
            modList.add(new ModifyBlockEntry(pos, Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }

        if (scaffold.isLayerPlatform(height)) {
            for (int i = 0; i < 8; i++) {
                if (CoordsInt.offsetRing1X[i] != xOffset || CoordsInt.offsetRing1Z[i] != zOffset) {
                    posBelow = pos.add(CoordsInt.offsetRing1X[i], 0, CoordsInt.offsetRing1Z[i]);
                    block = mob.getWorld().getBlockState(posBelow);
                    if (!block.isFullCube(mob.getWorld(), posBelow)) {
                        modList.add(new ModifyBlockEntry(posBelow, Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
                    }
                }
            }
        }

        return !modList.isEmpty() && modifier.requestTask(modList.toArray(ModifyBlockEntry[]::new), asker, null);
    }

    @Override
    public boolean askBuildLadderTower(IPosition position, int orientation, int layersToBuild, INotifyTask asker) {
        if (!modifier.isReadyForTask(asker)) {
            return false;
        }
        int xOffset = orientation == 1 ? -1 : orientation == 0 ? 1 : 0;
        int zOffset = orientation == 3 ? -1 : orientation == 2 ? 1 : 0;
        List<ModifyBlockEntry> modList = new ArrayList<>();
        BlockPos pos = position.toBlockPos();
        BlockPos posBelow = pos.add(xOffset, -1, zOffset);
        BlockState block = mob.getWorld().getBlockState(posBelow);
        if (!block.isFullCube(mob.getWorld(), posBelow)) {
            modList.add(new ModifyBlockEntry(posBelow, Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
        }
        posBelow = pos.down();
        if (this.mob.getWorld().isAir(posBelow)) {
            modList.add(new ModifyBlockEntry(posBelow, Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }
        for (int i = 0; i < layersToBuild; i++) {
            posBelow = pos.add(xOffset, i, zOffset);
            block = mob.getWorld().getBlockState(posBelow);
            if (!block.isFullCube(mob.getWorld(), posBelow)) {
                modList.add(new ModifyBlockEntry(posBelow, Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
            }
            posBelow = pos.up(i);
            block = mob.getWorld().getBlockState(posBelow);
            if (block.isOf(Blocks.LADDER)) {
                modList.add(new ModifyBlockEntry(posBelow, Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
            }
        }

        return !modList.isEmpty() && modifier.requestTask(modList.toArray(new ModifyBlockEntry[modList.size()]), asker, null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean askBuildLadder(IPosition position, INotifyTask asker) {
        if (!modifier.isReadyForTask(asker)) {
            return false;
        }
        List<ModifyBlockEntry> modList = new ArrayList<>();
        BlockPos pos = position.toBlockPos();

        BlockState block = mob.getWorld().getBlockState(pos);
        if (!block.isOf(Blocks.LADDER)) {
            if (!EntityIMPigEngy.canPlaceLadderAt(mob.getWorld(), pos)) {
                return false;
            }

            modList.add(new ModifyBlockEntry(pos, Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }

        block = mob.getWorld().getBlockState(pos.down(2));
        if (!block.isAir() && block.isSolid() && EntityIMPigEngy.canPlaceLadderAt(mob.getWorld(), pos.down())) {
            modList.add(new ModifyBlockEntry(pos.down(), Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }

        return !modList.isEmpty() && modifier.requestTask(modList.toArray(ModifyBlockEntry[]::new), asker, null);
    }

    @Override
    public boolean askBuildBridge(IPosition position, INotifyTask asker) {
        if (!modifier.isReadyForTask(asker)) {
            return false;
        }

        List<ModifyBlockEntry> modList = new ArrayList<>();
        BlockPos pos = position.toBlockPos();
        if (mob.getWorld().isAir(pos.down())) {
            if ((mob.avoidsBlock(mob.getWorld().getBlockState(pos.down(2))))
                || (mob.avoidsBlock(mob.getWorld().getBlockState(pos.down(3))))) {
                modList.add(new ModifyBlockEntry(pos.down(1), Blocks.COBBLESTONE.getDefaultState(), (int) (COBBLE_COST / buildRate)));
            } else {
                modList.add(new ModifyBlockEntry(pos.down(1), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
            }
        }

        return !modList.isEmpty() && modifier.requestTask(modList.toArray(new ModifyBlockEntry[modList.size()]), asker, null);
    }
}