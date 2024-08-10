package invmod.common.nexus;

import invmod.common.util.PolarAngle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpawnPointContainer {
    private final Map<SpawnType, List<SpawnPoint>> spawnPoints = new EnumMap<>(SpawnType.class);
    private boolean sorted;
    private Random random = new Random();

    public void addSpawnPointXZ(SpawnPoint spawnPoint) {
        boolean foundMatch = false;
        List<SpawnPoint> spawnList = spawnPoints.computeIfAbsent(spawnPoint.type(), i -> new ArrayList<>());

        for (int i = 0; i < spawnList.size(); i++) {
            SpawnPoint oldPoint = spawnList.get(i);
            if (oldPoint.columnEquals(spawnPoint)) {
                if (oldPoint.pos().getY() > spawnPoint.getYCoord()) {
                    spawnList.set(i, spawnPoint);
                }
                foundMatch = true;
                break;
            }
        }

        if (!foundMatch) {
            spawnList.add(spawnPoint);
        }
        this.sorted = false;
    }

    @Nullable
    public SpawnPoint getRandomSpawnPoint(SpawnType spawnType) {
        List<SpawnPoint> spawnList = spawnPoints.getOrDefault(spawnType, List.of());
        return spawnList.isEmpty() ? null : spawnList.get(random.nextInt(spawnList.size()));
    }

    public SpawnPoint getRandomSpawnPoint(SpawnType spawnType, int minAngle, int maxAngle) {
        List<SpawnPoint> spawnList = spawnPoints.get(spawnType);
        if (spawnList.isEmpty()) {
            return null;
        }

        if (!this.sorted) {
            Collections.sort(spawnList);
            this.sorted = true;
        }

        int start = Collections.binarySearch(spawnList, new PolarAngle(minAngle));
        if (start < 0) {
            start = -start - 1;
        }
        int end = Collections.binarySearch(spawnList, new PolarAngle(maxAngle));
        if (end < 0) {
            end = -end - 1;
        }
        if (end > start) {
            return spawnList.get(start + this.random.nextInt(end - start));
        }
        if ((start > end) && (end > 0)) {
            int r = start + this.random.nextInt(spawnList.size() + end - start);
            if (r >= spawnList.size()) {
                r -= spawnList.size();
            }
            return spawnList.get(r);
        }
        return null;
    }

    public int getNumberOfSpawnPoints(SpawnType type) {
        return spawnPoints.getOrDefault(SpawnType.HUMANOID, List.of()).size();
    }

    public int getNumberOfSpawnPoints(SpawnType spawnType, int minAngle, int maxAngle) {
        List<SpawnPoint> spawnList = this.spawnPoints.get(spawnType);
        if (spawnList.isEmpty() || (maxAngle - minAngle) >= 360) {
            return spawnList.size();
        }

        if (!this.sorted) {
            Collections.sort(spawnList);
            this.sorted = true;
        }

        int start = Collections.binarySearch(spawnList, new PolarAngle(minAngle));
        if (start < 0) {
            start = -start - 1;
        }
        int end = Collections.binarySearch(spawnList, new PolarAngle(maxAngle));
        if (end < 0) {
            end = -end - 1;
        }
        if (end > start) {
            return end - start;
        }
        if ((start > end) && (end > 0)) {
            return end + spawnList.size() - start;
        }
        return 0;
    }

    public void pointDisplayTest(Block block, World world) {
        List<SpawnPoint> points = this.spawnPoints.get(SpawnType.HUMANOID);
        for (int i = 0; i < points.size(); i++) {
            SpawnPoint point = points.get(i);
            world.setBlockState(new BlockPos(point.getXCoord(), point.getYCoord(), point.getZCoord()),
                    block.getDefaultState());
        }
    }
}