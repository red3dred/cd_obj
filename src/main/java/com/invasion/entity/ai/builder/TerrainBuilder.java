package com.invasion.entity.ai.builder;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.invasion.Notifiable;
import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.PigmanEngineerEntity;
import com.invasion.util.math.PosUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public class TerrainBuilder implements ITerrainBuild {
    private static final float LADDER_COST = 25;
    private static final float PLANKS_COST = 45;
    private static final float COBBLE_COST = 65;

    private final EntityIMLiving mob;
    private final ITerrainModify modifier;
    private float buildRate;

    public TerrainBuilder(EntityIMLiving entity, ITerrainModify modifier, float buildRate) {
        mob = entity;
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
    public boolean askBuildScaffoldLayer(BlockPos pos, Notifiable asker) {
        if (!modifier.isReadyForTask(asker)) {
            return false;
        }
        @Nullable
        Scaffold scaffold = mob.getNexus().getAttackerAI().getScaffoldAt(pos);
        if (scaffold == null) {
            return false;
        }

        int height = pos.getY() - scaffold.getPos().getY();
        Direction offset = scaffold.getOrientation();
        BlockPos.Mutable mutable = pos.mutableCopy();

        BlockState block = mob.getWorld().getBlockState(mutable.set(pos).move(offset).move(Direction.DOWN));
        List<ModifyBlockEntry> modList = new ArrayList<>();

        if (height == 1) {
            if (!block.isFullCube(mob.getWorld(), mutable)) {
                modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
            }
            if (mob.getWorld().isAir(mutable.set(pos).move(Direction.DOWN))) {
                modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
            }
        }

        if (!mob.getWorld().getBlockState(mutable.set(pos).move(offset)).isFullCube(mob.getWorld(), mutable)) {
            modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
        }
        if (!mob.getWorld().getBlockState(pos).isOf(Blocks.LADDER)) {
            modList.add(new ModifyBlockEntry(pos, Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }

        if (scaffold.isLayerPlatform(height)) {
            for (Vec3i i : PosUtils.OFFSET_RING) {
                if (!i.equals(offset.getVector()) && !mob.getWorld().getBlockState(mutable.set(pos).move(i)).isFullCube(mob.getWorld(), mutable)) {
                    modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
                }
            }
        }

        return !modList.isEmpty() && modifier.requestTask(modList, asker, null);
    }

    @Override
    public boolean askBuildLadderTower(BlockPos pos, Direction orientation, int layersToBuild, Notifiable asker) {
        if (!modifier.isReadyForTask(asker)) {
            return false;
        }
        List<ModifyBlockEntry> modList = new ArrayList<>();

        BlockPos.Mutable mutable = pos.mutableCopy();

        if (!mob.getWorld().getBlockState(mutable.set(pos).move(orientation).move(Direction.DOWN)).isFullCube(mob.getWorld(), mutable)) {
            modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
        }
        if (mob.getWorld().isAir(mutable.move(Direction.DOWN))) {
            modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }
        for (int i = 0; i < layersToBuild; i++) {
            if (!mob.getWorld().getBlockState(mutable.set(pos).move(orientation).move(Direction.UP, i)).isFullCube(mob.getWorld(), mutable)) {
                modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
            }
            if (mob.getWorld().getBlockState(mutable.move(Direction.UP, i)).isOf(Blocks.LADDER)) {
                modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
            }
        }

        return !modList.isEmpty() && modifier.requestTask(modList, asker, null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean askBuildLadder(BlockPos pos, Notifiable asker) {
        if (!modifier.isReadyForTask(asker)) {
            return false;
        }
        List<ModifyBlockEntry> modList = new ArrayList<>();

        if (!mob.getWorld().getBlockState(pos).isOf(Blocks.LADDER)) {
            if (!PigmanEngineerEntity.canPlaceLadderAt(mob.getWorld(), pos)) {
                return false;
            }

            modList.add(new ModifyBlockEntry(pos, Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }

        BlockPos.Mutable mutable = pos.mutableCopy();

        BlockState block = mob.getWorld().getBlockState(mutable.move(Direction.DOWN, 2));
        if (!block.isAir() && block.isSolid() && PigmanEngineerEntity.canPlaceLadderAt(mob.getWorld(), mutable.set(pos).move(Direction.DOWN))) {
            modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }

        return !modList.isEmpty() && modifier.requestTask(modList, asker, null);
    }

    @Override
    public boolean askBuildBridge(BlockPos pos, Notifiable asker) {
        if (!modifier.isReadyForTask(asker)) {
            return false;
        }

        List<ModifyBlockEntry> modList = new ArrayList<>();
        BlockPos.Mutable mutable = pos.mutableCopy();

        if (mob.getWorld().isAir(mutable.move(Direction.DOWN))) {
            boolean needsSupport = mob.getNavigatorNew().getNodeMaker().avoidsBlock(mob.getWorld().getBlockState(mutable.set(pos).move(Direction.DOWN, 2)))
                                || mob.getNavigatorNew().getNodeMaker().avoidsBlock(mob.getWorld().getBlockState(mutable.set(pos).move(Direction.DOWN, 3)));
            modList.add(new ModifyBlockEntry(pos.down(),
                    (needsSupport ? Blocks.COBBLESTONE : Blocks.OAK_PLANKS).getDefaultState(),
                    (int) ((needsSupport ? COBBLE_COST : PLANKS_COST) / buildRate))
            );
        }

        return !modList.isEmpty() && modifier.requestTask(modList, asker, Notifiable.NONE);
    }
}