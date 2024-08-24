package com.invasion.entity.ai.builder;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.invasion.Notifiable;
import com.invasion.block.BlockMetadata;
import com.invasion.entity.NexusEntity;
import com.invasion.entity.pathfinding.IMLandPathNodeMaker;
import com.invasion.nexus.ai.scaffold.Scaffold;
import com.invasion.util.math.PosUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TerrainBuilder implements ITerrainBuild {
    private static final float LADDER_COST = 25;
    private static final float PLANKS_COST = 45;
    private static final float COBBLE_COST = 65;

    private final NexusEntity mob;
    private final ITerrainModify modifier;
    private float buildRate;

    public TerrainBuilder(NexusEntity entity, ITerrainModify modifier, float buildRate) {
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
        Scaffold scaffold = mob.getNexus().getAttackerAI().getScaffolds().getAt(pos);
        if (scaffold == null) {
            return false;
        }

        int height = pos.getY() - scaffold.getNode().bottom();
        Direction offset = scaffold.getNode().orientation();
        BlockPos.Mutable mutable = pos.mutableCopy();

        World world = mob.asEntity().getWorld();
        BlockState block = world.getBlockState(mutable.set(pos).move(offset).move(Direction.DOWN));
        List<ModifyBlockEntry> modList = new ArrayList<>();

        if (height == 1) {
            if (!block.isFullCube(world, mutable)) {
                modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
            }
            if (world.isAir(mutable.set(pos).move(Direction.DOWN))) {
                modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
            }
        }

        if (!world.getBlockState(mutable.set(pos).move(offset)).isFullCube(world, mutable)) {
            modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
        }
        if (!world.getBlockState(pos).isOf(Blocks.LADDER)) {
            modList.add(new ModifyBlockEntry(pos, Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }

        if (scaffold.isPlatformLayer(height)) {
            for (Vec3i i : PosUtils.OFFSET_RING) {
                if (!i.equals(offset.getVector()) && !world.getBlockState(mutable.set(pos).move(i)).isFullCube(world, mutable)) {
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
        World world = mob.asEntity().getWorld();

        if (!world.getBlockState(mutable.set(pos).move(orientation).move(Direction.DOWN)).isFullCube(world, mutable)) {
            modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
        }
        if (world.isAir(mutable.move(Direction.DOWN))) {
            modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }

        for (int i = 0; i < layersToBuild; i++) {
            if (!world.getBlockState(mutable.set(pos).move(orientation).move(Direction.UP, i)).isFullCube(world, mutable)) {
                modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
            }
            if (world.getBlockState(mutable.move(Direction.UP, i)).isOf(Blocks.LADDER)) {
                modList.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
            }
        }

        return !modList.isEmpty() && modifier.requestTask(modList, asker, null);
    }

    @Override
    public boolean askBuildLadder(BlockPos pos, Notifiable asker) {
        if (!modifier.isReadyForTask(asker)) {
            return false;
        }
        List<ModifyBlockEntry> modList = new ArrayList<>();
        World world = mob.asEntity().getWorld();

        if (!world.getBlockState(pos).isOf(Blocks.LADDER)) {
            if (!canPlaceLadderAt(world, pos)) {
                return false;
            }

            modList.add(new ModifyBlockEntry(pos, Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }

        BlockPos.Mutable mutable = pos.mutableCopy();

        BlockState block = world.getBlockState(mutable.move(Direction.DOWN, 2));
        if (!block.isAir() && block.isSolidBlock(world, pos) && canPlaceLadderAt(world, mutable.set(pos).move(Direction.DOWN))) {
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
        World world = mob.asEntity().getWorld();

        if (world.isAir(mutable.move(Direction.DOWN))) {
            boolean needsSupport = IMLandPathNodeMaker.avoidsBlock(mob.asEntity(), mutable.set(pos).move(Direction.DOWN, 2))
                                || IMLandPathNodeMaker.avoidsBlock(mob.asEntity(), mutable.set(pos).move(Direction.DOWN, 3));
            modList.add(new ModifyBlockEntry(pos.down(),
                    (needsSupport ? Blocks.COBBLESTONE : Blocks.OAK_PLANKS).getDefaultState(),
                    (int) ((needsSupport ? COBBLE_COST : PLANKS_COST) / buildRate))
            );
        }

        return !modList.isEmpty() && modifier.requestTask(modList, asker, Notifiable.NONE);
    }

    public static boolean canPlaceLadderAt(BlockView map, BlockPos pos) {
        if (BlockMetadata.isIndestructible(map.getBlockState(pos))) {
            BlockPos.Mutable mutable = pos.mutableCopy();
            if (map.getBlockState(mutable.set(pos).move(1, 0, 0)).isFullCube(map, mutable)
                    || map.getBlockState(mutable.set(pos).move(-1, 0, 0)).isFullCube(map, mutable)
                    || map.getBlockState(mutable.set(pos).move(0, 0, 1)).isFullCube(map, mutable)
                    || map.getBlockState(mutable.set(pos).move(0, 0, -1)).isFullCube(map, mutable)) {
                return true;
            }
        }
        return false;
    }

}