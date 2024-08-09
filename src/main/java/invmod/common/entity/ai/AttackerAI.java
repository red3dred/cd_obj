package invmod.common.entity.ai;

import invmod.common.IBlockAccessExtended;
import invmod.common.IPathfindable;
import invmod.common.TerrainDataLayer;
import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.IPathSource;
import invmod.common.entity.Path;
import invmod.common.entity.PathAction;
import invmod.common.entity.PathCreator;
import invmod.common.entity.PathNode;
import invmod.common.entity.Scaffold;
import invmod.common.nexus.INexusAccess;
import invmod.common.util.CoordsInt;
import invmod.common.util.IPosition;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkCache;

public class AttackerAI {
    private final INexusAccess nexus;
    private final IPathSource pathSource = new PathCreator();
    private final Int2IntMap entityDensityData = new Int2IntOpenHashMap();
    private List<Scaffold> scaffolds = new ArrayList<>();
    private int scaffoldLimit;
    private int minDistanceBetweenScaffolds;
    private int nextScaffoldCalcTimer;
    private int updateScaffoldTimer;
    private int nextEntityDensityUpdate;

    public AttackerAI(INexusAccess nexus) {
        this.nexus = nexus;
        pathSource.setSearchDepth(8500);
        pathSource.setQuickFailDepth(8500);
    }

    public void update() {
        nextScaffoldCalcTimer -= 1;
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

    @Deprecated
    public IBlockAccessExtended wrapEntityData(BlockView terrainMap) {
        return new TerrainDataLayer(terrainMap, entityDensityData);
    }

    public int getMinDistanceBetweenScaffolds() {
        return minDistanceBetweenScaffolds;
    }

    public List<Scaffold> getScaffolds() {
        return scaffolds;
    }

    public boolean askGenerateScaffolds(EntityIMLiving entity) {
        if ((this.nextScaffoldCalcTimer > 0) || (this.scaffolds.size() > this.scaffoldLimit)) {
            return false;
        }
        this.nextScaffoldCalcTimer = 200;
        List<Scaffold> newScaffolds = findMinScaffolds(entity, entity.getBlockPos());
        if (!newScaffolds.isEmpty()) {
            addNewScaffolds(newScaffolds);
            return true;
        }

        return false;
    }

    public List<Scaffold> findMinScaffolds(IPathfindable pather, BlockPos pos) {
        BlockPos nexusPos = nexus.toBlockPos();
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
                terrainMap.setData(s.toBlockPos(), 200000);
                Path path = createPath(pather, pos, nexusPos, terrainMap);
                if (path.getTotalPathCost() < lowestCost && path.getFinalPathPoint().isAt(nexus)) {
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
                        terrainMap.setData(s.toBlockPos(), 200000);
                    }
                }

                if (!createPath(pather, pos, nexusPos, terrainMap).getFinalPathPoint().isAt(nexus)) {
                    costDif.add(s);
                }

            }

            return costDif;
        }

        return scaffoldPositions.size() == 1 ? scaffoldPositions : List.of();
    }

    public void addScaffoldDataTo(IBlockAccessExtended terrainMap) {
        for (Scaffold scaffold : scaffolds) {
            BlockPos pos = scaffold.toBlockPos();
            for (int i = 0; i < scaffold.getTargetHeight(); i++) {
                terrainMap.setData(pos, terrainMap.getData(pos) | 0x4000);
            }
        }
    }

    public Scaffold getScaffoldAt(IPosition pos) {
        return getScaffoldAt(pos.toBlockPos());
    }

    public Scaffold getScaffoldAt(BlockPos pos) {
        for (Scaffold scaffold : scaffolds) {
            if (scaffold.getXCoord() == pos.getX()
                    && scaffold.getZCoord() == pos.getZ()
                    && scaffold.getYCoord() <= pos.getY()
                    && scaffold.getYCoord() + scaffold.getTargetHeight() >= pos.getY()) {
                return scaffold;
            }
        }
        return null;
    }

    public void onResume() {
        scaffolds.forEach(Scaffold::forceStatusUpdate);
    }

    public void readFromNBT(NbtCompound compound) {
        scaffolds = compound.getList("scaffolds", NbtElement.COMPOUND_TYPE).stream().map(element -> {
            Scaffold scaffold = new Scaffold(nexus);
            scaffold.readFromNBT((NbtCompound) element);
            return scaffold;
        }).collect(Collectors.toList());
    }

    public void writeToNBT(NbtCompound compound) {
        NbtList nbttaglist = new NbtList();
        for (Scaffold scaffold : scaffolds) {
            NbtCompound nbtscaffold = new NbtCompound();
            scaffold.writeToNBT(nbtscaffold);
            nbttaglist.add(nbtscaffold);
        }
        compound.put("scaffolds", nbttaglist);
    }

    private Path createPath(IPathfindable pather, BlockPos p1, BlockPos p2, BlockView terrainMap) {
        return pathSource.createPath(pather, p1, p2, 1.1F, 12 + MathHelper.sqrt((float) p1.getSquaredDistance(p2)), terrainMap);
    }

    private Path createPath(IPathfindable pather, BlockPos p1, BlockPos p2, float axisExpand) {
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
                    startHeight = node.getYCoord() - 1;
                }

            } else if (node.action != PathAction.SCAFFOLD_UP) {
                Scaffold scaffold = new Scaffold(node.getPrevious().getXCoord(), startHeight, node.getPrevious().getZCoord(), node.getYCoord() - startHeight, this.nexus);
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
        int highestDirectionIndex = 0;
        for (int i = 0; i < 4; i++) {
            int blockCount = 0;
            for (int height = 0; height < scaffold.getYCoord(); height++) {
                BlockPos pos = scaffold.toBlockPos().add(CoordsInt.offsetAdjX[i], height, CoordsInt.offsetAdjZ[i]);
                if (terrainMap.getBlockState(pos).isFullCube(terrainMap, pos)) {
                    blockCount++;
                }
                pos = scaffold.toBlockPos().add(CoordsInt.offsetAdjX[i] * 2, height, CoordsInt.offsetAdjZ[i] * 2);
                if (terrainMap.getBlockState(pos).isFullCube(terrainMap, pos)) {
                    blockCount++;
                }
            }
            if (blockCount > mostBlocks) {
                highestDirectionIndex = i;
            }
        }
        scaffold.setOrientation(highestDirectionIndex);
    }

    private void addNewScaffolds(List<Scaffold> newScaffolds) {
        for (Scaffold newScaffold : newScaffolds) {
            for (Scaffold existingScaffold : scaffolds) {
                if (existingScaffold.getXCoord() == newScaffold.getXCoord() && existingScaffold.getZCoord() == newScaffold.getZCoord()) {
                    if (newScaffold.getYCoord() > existingScaffold.getYCoord()) {
                        if (newScaffold.getYCoord() < existingScaffold.getYCoord() + existingScaffold.getTargetHeight()) {
                            existingScaffold.setHeight(newScaffold.getYCoord() + newScaffold.getTargetHeight() - existingScaffold.getYCoord());
                            break;
                        }
                    } else if (newScaffold.getYCoord() + newScaffold.getTargetHeight() > existingScaffold.getYCoord()) {
                        existingScaffold.setPosition(newScaffold.getXCoord(), newScaffold.getYCoord(), newScaffold.getZCoord());
                        existingScaffold.setHeight(existingScaffold.getYCoord() + existingScaffold.getTargetHeight() - newScaffold.getYCoord());
                        break;
                    }
                }
            }

            this.scaffolds.add(newScaffold);
        }
    }

    private void updateScaffolds() {
        scaffolds.removeIf(scaffold -> {
            nexus.getWorld().addParticle(ParticleTypes.HEART,
                    scaffold.getXCoord() + 0.2D,
                    scaffold.getYCoord() + 0.2D,
                    scaffold.getZCoord() + 0.2D,
                    0.5D, 0.5D, 0.5D
            );

            scaffold.forceStatusUpdate();
            return scaffold.getPercentIntactCached() + 0.05F < 0.4F * scaffold.getPercentCompletedCached();
        });
    }

    private void updateDensityData() {
        entityDensityData.clear();
        for (EntityIMLiving mob : nexus.getMobList()) {
            entityDensityData.compute(PathNode.makeHash(mob, PathAction.NONE), (key, old) -> old == null ? 1 : old + 1);
        }
    }
}