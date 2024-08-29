package com.invasion.entity.ai.builder;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.NexusEntity;
import com.invasion.entity.pathfinding.BuilderIMMobNavigation;
import com.invasion.entity.pathfinding.IMLandPathNodeMaker;
import com.invasion.nexus.ai.scaffold.Scaffold;
import com.invasion.util.math.PosUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class TerrainBuilder implements ITerrainBuild {
    private static final float LADDER_COST = 25;
    private static final float PLANKS_COST = 45;
    private static final float COBBLE_COST = 65;

    private final NexusEntity mob;
    private float buildRate;

    public TerrainBuilder(NexusEntity entity, float buildRate) {
        mob = entity;
        this.buildRate = buildRate;
    }

    public void setBuildRate(float buildRate) {
        this.buildRate = buildRate;
    }

    public float getBuildRate() {
        return this.buildRate;
    }

    @Override
    public Stream<ModifyBlockEntry> askBuildScaffoldLayer(BlockPos pos) {
        @Nullable
        Scaffold scaffold = mob.getNexus().getAttackerAI().getScaffolds().getAt(pos);
        if (scaffold == null) {
            return Stream.empty();
        }

        int height = pos.getY() - scaffold.getNode().bottom();
        Direction offset = scaffold.getNode().orientation();
        BlockPos.Mutable mutable = pos.mutableCopy();

        World world = mob.asEntity().getWorld();
        BlockState block = world.getBlockState(mutable.set(pos).move(offset).move(Direction.DOWN));
        Stream.Builder<ModifyBlockEntry> builder = Stream.builder();

        if (height == 1) {
            if (!block.isFullCube(world, mutable)) {
                builder.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
            }
            if (world.isAir(mutable.set(pos).move(Direction.DOWN))) {
                builder.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
            }
        }

        if (!world.getBlockState(mutable.set(pos).move(offset)).isFullCube(world, mutable)) {
            builder.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
        }
        if (!world.getBlockState(pos).isOf(Blocks.LADDER)) {
            builder.add(new ModifyBlockEntry(pos, Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }

        if (scaffold.isPlatformLayer(height)) {
            for (Vec3i i : PosUtils.OFFSET_RING) {
                if (!i.equals(offset.getVector()) && !world.getBlockState(mutable.set(pos).move(i)).isFullCube(world, mutable)) {
                    builder.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
                }
            }
        }

        return builder.build();
    }

    @Override
    public Stream<ModifyBlockEntry> askBuildLadderTower(BlockPos pos, Direction orientation, int layersToBuild) {
        Stream.Builder<ModifyBlockEntry> builder = Stream.builder();

        BlockPos.Mutable mutable = pos.mutableCopy();
        World world = mob.asEntity().getWorld();

        if (!world.getBlockState(mutable.set(pos).move(orientation).move(Direction.DOWN)).isFullCube(world, mutable)) {
            builder.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
        }
        if (world.isAir(mutable.move(Direction.DOWN))) {
            builder.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
        }

        for (int i = 0; i < layersToBuild; i++) {
            if (!world.getBlockState(mutable.set(pos).move(orientation).move(Direction.UP, i)).isFullCube(world, mutable)) {
                builder.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.OAK_PLANKS.getDefaultState(), (int) (PLANKS_COST / buildRate)));
            }
            if (world.getBlockState(mutable.move(Direction.UP, i)).isOf(Blocks.LADDER)) {
                builder.add(new ModifyBlockEntry(mutable.toImmutable(), Blocks.LADDER.getDefaultState(), (int) (LADDER_COST / buildRate)));
            }
        }

        return builder.build();
    }

    @Override
    public Stream<ModifyBlockEntry> askBuildLadder(BlockPos pos, Direction orientation) {
        Stream.Builder<ModifyBlockEntry> builder = Stream.builder();

        World world = mob.asEntity().getWorld();
        BlockState ladderState = Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, orientation.getOpposite());
        BlockPos.Mutable mutable = pos.mutableCopy();

        if (!world.getBlockState(pos).isOf(Blocks.LADDER)) {
            if (!BuilderIMMobNavigation.canPositionSupportLadder(world, mutable, orientation)) {
                return Stream.empty();
            }

            builder.add(new ModifyBlockEntry(pos, ladderState, (int) (LADDER_COST / buildRate)));
        }

        if (world.isAir(mutable.move(Direction.DOWN, 2)) && BuilderIMMobNavigation.canPositionSupportLadder(world, mutable.set(pos).move(Direction.DOWN), orientation)) {
            builder.add(new ModifyBlockEntry(mutable.toImmutable(), ladderState, (int) (LADDER_COST / buildRate)));
        }

        return builder.build();
    }

    @Override
    public Stream<ModifyBlockEntry> askBuildBridge(BlockPos pos) {
        Stream.Builder<ModifyBlockEntry> builder = Stream.builder();

        BlockPos.Mutable mutable = pos.mutableCopy();
        BlockPos posBelow = pos.down();
        World world = mob.asEntity().getWorld();

        boolean isFluid = world.getBlockState(pos).isLiquid();
        boolean isAirBelow = world.isAir(posBelow);

        if (isAirBelow || isFluid) {
            if (isFluid) {
                posBelow = pos;
            }
            boolean needsSupport = IMLandPathNodeMaker.avoidsBlock(mob.asEntity(), mutable.set(pos).move(Direction.DOWN, 2))
                                || IMLandPathNodeMaker.avoidsBlock(mob.asEntity(), mutable.set(pos).move(Direction.DOWN, 3));
            builder.add(new ModifyBlockEntry(posBelow,
                    (needsSupport ? Blocks.COBBLESTONE : Blocks.OAK_PLANKS).getDefaultState(),
                    (int) ((needsSupport ? COBBLE_COST : PLANKS_COST) / buildRate))
            );
        }

        return builder.build();
    }
}