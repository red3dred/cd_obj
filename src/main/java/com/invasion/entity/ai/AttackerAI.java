package com.invasion.entity.ai;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.invasion.IBlockAccessExtended;
import com.invasion.TerrainDataLayer;
import com.invasion.entity.NexusEntity;
import com.invasion.entity.ai.builder.Scaffold;
import com.invasion.entity.pathfinding.PathSource;
import com.invasion.entity.pathfinding.IMPathNodeMaker;
import com.invasion.entity.pathfinding.Path;
import com.invasion.entity.pathfinding.PathAction;
import com.invasion.entity.pathfinding.PathCreator;
import com.invasion.entity.pathfinding.PathNode;
import com.invasion.nexus.Nexus;
import com.invasion.util.math.PosUtils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkCache;

public class AttackerAI {
    private final Nexus nexus;
    private final PathSource pathSource = new PathCreator();

    private final Long2ObjectMap<Integer> entityDensityData = new Long2ObjectOpenHashMap<>();

    private List<Scaffold> scaffolds = new ArrayList<>();
    private int scaffoldLimit;
    private int minDistanceBetweenScaffolds;

    private int nextScaffoldCalcTimer;
    private int updateScaffoldTimer;
    private int nextEntityDensityUpdate;

    public AttackerAI(Nexus nexus) {
        this.nexus = nexus;
        pathSource.setSearchDepth(8500);
        pathSource.setQuickFailDepth(8500);
    }

    public void onResume() {
        scaffolds.forEach(Scaffold::forceStatusUpdate);
    }

    public void tick() {
        nextScaffoldCalcTimer--;
        if (--updateScaffoldTimer <= 0) {
            updateScaffoldTimer = 40;
            updateScaffolds();

            scaffoldLimit = (2 + nexus.getCurrentWave() / 2);
            minDistanceBetweenScaffolds = (90 / (nexus.getCurrentWave() + 10));
        }

        if (--nextEntityDensityUpdate <= 0) {
            nextEntityDensityUpdate = 20;
            updateDensityData();
        }
    }

    public IBlockAccessExtended wrapEntityData(BlockView terrainMap) {
        return new TerrainDataLayer(terrainMap, entityDensityData);
    }

    public int getMinDistanceBetweenScaffolds() {
        return minDistanceBetweenScaffolds;
    }

    public List<Scaffold> getScaffolds() {
        return scaffolds;
    }

    public boolean askGenerateScaffolds(NexusEntity entity) {
        if (nextScaffoldCalcTimer > 0 || scaffolds.size() > scaffoldLimit) {
            return false;
        }
        nextScaffoldCalcTimer = 200;
        List<Scaffold> newScaffolds = findMinScaffolds(entity.getNavigatorNew().getNodeMaker(), entity.getBlockPos());
        if (!newScaffolds.isEmpty()) {
            addNewScaffolds(newScaffolds);
            return true;
        }

        return false;
    }

    public List<Scaffold> findMinScaffolds(IMPathNodeMaker pather, BlockPos pos) {
        BlockPos nexusPos = nexus.getOrigin();
        Scaffold scaffold = new Scaffold(nexus);
        scaffold.setPathfindBase(pather);
        Path basePath = createPath(scaffold, pos, nexusPos, 12);
        if (basePath == null) {
            return List.of();
        }
        List<Scaffold> scaffoldPositions = extractScaffolds(basePath);
        if (scaffoldPositions.size() > 1) {
            float lowestCost = 1;
            int lowestCostIndex = -1;
            for (int i = 0; i < scaffoldPositions.size(); i++) {
                IBlockAccessExtended terrainMap = getChunkCache(pos, nexusPos, 12);
                Scaffold s = scaffoldPositions.get(i);
                terrainMap.setData(s.getPos(), 200000);
                Path path = createPath(pather, pos, nexusPos, terrainMap);
                if (path.getTotalPathCost() < lowestCost && path.getFinalPathPoint().pos.equals(nexusPos)) {
                    lowestCostIndex = i;
                }
            }

            if (lowestCostIndex >= 0) {
                return List.of(scaffoldPositions.get(lowestCostIndex));
            }

            List<Scaffold> costDif = new ArrayList<>(scaffoldPositions.size());
            for (int i = 0; i < scaffoldPositions.size(); i++) {
                IBlockAccessExtended terrainMap = getChunkCache(pos, nexusPos, 12);
                Scaffold s = scaffoldPositions.get(i);
                for (int j = 0; j < scaffoldPositions.size(); j++) {
                    if (j != i) {
                        terrainMap.setData(s.getPos(), 200000);
                    }
                }

                if (!createPath(pather, pos, nexusPos, terrainMap).getFinalPathPoint().pos.equals(nexus.getOrigin())) {
                    costDif.add(s);
                }

            }

            return costDif;
        }

        return scaffoldPositions.size() == 1 ? scaffoldPositions : List.of();
    }

    public void addScaffoldDataTo(IBlockAccessExtended terrainMap) {
        for (Scaffold scaffold : scaffolds) {
            BlockPos pos = scaffold.getPos();
            for (int i = 0; i < scaffold.getTargetHeight(); i++) {
                terrainMap.setData(pos, terrainMap.getData(pos) | TerrainDataLayer.EXT_DATA_SCAFFOLD_METAPOSITION);
            }
        }
    }

    public Scaffold getScaffoldAt(BlockPos pos) {
        for (Scaffold scaffold : scaffolds) {
            if (scaffold.getPos().getX() == pos.getX()
                    && scaffold.getPos().getZ() == pos.getZ()
                    && scaffold.getPos().getY() <= pos.getY()
                    && scaffold.getPos().getY() + scaffold.getTargetHeight() >= pos.getY()) {
                return scaffold;
            }
        }
        return null;
    }

    public void readNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
        scaffolds = compound.getList("scaffolds", NbtElement.COMPOUND_TYPE).stream().map(element -> {
            Scaffold scaffold = new Scaffold(nexus);
            scaffold.readFromNBT((NbtCompound) element);
            return scaffold;
        }).collect(Collectors.toList());
    }

    public NbtCompound writeNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
        NbtList nbttaglist = new NbtList();
        for (Scaffold scaffold : scaffolds) {
            NbtCompound nbtscaffold = new NbtCompound();
            scaffold.writeToNBT(nbtscaffold);
            nbttaglist.add(nbtscaffold);
        }
        compound.put("scaffolds", nbttaglist);
        return compound;
    }

    private Path createPath(IMPathNodeMaker pather, BlockPos p1, BlockPos p2, BlockView terrainMap) {
        return pathSource.createPath(pather, p1, p2, 1.1F, 12 + MathHelper.sqrt((float) p1.getSquaredDistance(p2)), terrainMap);
    }

    private Path createPath(IMPathNodeMaker pather, BlockPos p1, BlockPos p2, float axisExpand) {
        IBlockAccessExtended terrainMap = getChunkCache(p1, p2, axisExpand);
        addScaffoldDataTo(terrainMap);
        return createPath(pather, p1, p2, terrainMap);
    }

    private IBlockAccessExtended getChunkCache(BlockPos p1, BlockPos p2, float axisExpand) {
        BlockBox box = BlockBox.create(p1, p2).expand((int) axisExpand);
        return new TerrainDataLayer(new ChunkCache(this.nexus.getWorld(),
                new BlockPos(box.getMinX(), box.getMinY(), box.getMinZ()),
                new BlockPos(box.getMaxX(), box.getMaxY(), box.getMaxZ())
        ));
    }

    private List<Scaffold> extractScaffolds(Path path) {
        List<Scaffold> scaffoldPositions = new ArrayList<>();
        boolean flag = false;
        int startHeight = 0;
        for (int i = 0; i < path.getCurrentPathLength(); i++) {
            PathNode node = path.getPathPointFromIndex(i);
            if (!flag) {
                if (node.action == PathAction.SCAFFOLD_UP) {
                    flag = true;
                    startHeight = node.pos.getY() - 1;
                }

            } else if (node.action != PathAction.SCAFFOLD_UP) {
                Scaffold scaffold = new Scaffold(node.getPrevious().pos.getX(), startHeight, node.getPrevious().pos.getZ(), node.pos.getY() - startHeight, this.nexus);
                orientScaffold(scaffold, nexus.getWorld());
                scaffold.setInitialIntegrity();
                scaffoldPositions.add(scaffold);
                flag = false;
            }
        }

        return scaffoldPositions;
    }

    private void orientScaffold(Scaffold scaffold, BlockView terrainMap) {
        int mostBlocks = 0;
        Direction highestDirection = PosUtils.CARDINAL_DIRECTIONS[0];
        BlockPos.Mutable mutable = scaffold.getPos().mutableCopy();
        for (Direction offset : PosUtils.CARDINAL_DIRECTIONS) {
            int blockCount = 0;
            for (int height = 0; height < scaffold.getPos().getY(); height++) {
                if (terrainMap.getBlockState(mutable.set(scaffold.getPos()).move(Direction.UP, height).move(offset)).isFullCube(terrainMap, mutable)) {
                    blockCount++;
                }
                if (terrainMap.getBlockState(mutable.set(scaffold.getPos()).move(Direction.UP, height).move(offset, 2)).isFullCube(terrainMap, mutable)) {
                    blockCount++;
                }
            }
            if (blockCount > mostBlocks) {
                highestDirection = offset;
            }
        }
        scaffold.setOrientation(highestDirection);
    }

    private void addNewScaffolds(List<Scaffold> newScaffolds) {
        for (Scaffold newScaffold : newScaffolds) {
            for (Scaffold existingScaffold : scaffolds) {
                if (existingScaffold.getPos().getX() == newScaffold.getPos().getX() && existingScaffold.getPos().getZ() == newScaffold.getPos().getZ()) {
                    if (newScaffold.getPos().getY() > existingScaffold.getPos().getY()) {
                        if (newScaffold.getPos().getY() < existingScaffold.getPos().getY() + existingScaffold.getTargetHeight()) {
                            existingScaffold.setHeight(newScaffold.getPos().getY() + newScaffold.getTargetHeight() - existingScaffold.getPos().getY());
                            break;
                        }
                    } else if (newScaffold.getPos().getY() + newScaffold.getTargetHeight() > existingScaffold.getPos().getY()) {
                        existingScaffold.setPosition(newScaffold.getPos().getX(), newScaffold.getPos().getY(), newScaffold.getPos().getZ());
                        existingScaffold.setHeight(existingScaffold.getPos().getY() + existingScaffold.getTargetHeight() - newScaffold.getPos().getY());
                        break;
                    }
                }
            }

            this.scaffolds.add(newScaffold);
        }
    }

    private void updateScaffolds() {
        scaffolds.removeIf(scaffold -> {
            Vec3d pos = scaffold.getPos().toCenterPos();
            nexus.getWorld().addParticle(ParticleTypes.HEART, pos.x, pos.y, pos.z, 0.5D, 0.5D, 0.5D);

            scaffold.forceStatusUpdate();
            return scaffold.getPercentIntactCached() + 0.05F < 0.4F * scaffold.getPercentCompletedCached();
        });
    }

    private void updateDensityData() {
        entityDensityData.clear();
        for (NexusEntity mob : nexus.getCombatants()) {
            entityDensityData.compute(mob.getBlockPos().asLong(), (key, old) -> old == null ? 1 : old + 1);
        }
    }
}