package com.invasion.nexus.wave;

import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.invasion.InvasionMod;
import com.invasion.nexus.EntityConstruct;
import com.invasion.nexus.IEntityIMPattern;
import com.invasion.nexus.spawns.Spawner;
import com.invasion.nexus.spawns.SpawnType;
import com.invasion.util.Select;

public class WaveEntry {
    static final int DEFAULT_NEXT_ALERT_TIME = Integer.MAX_VALUE;
    static final int MAX_ANGLE = 360;
    static final int MAX_VALID_ANGLE = 180;

    static int wrapAngle(int angle) {
        while (angle > MAX_VALID_ANGLE) {
            angle -= MAX_ANGLE;
        }
        return angle;
    }

    static int clampAngle(int angle) {
        while (angle < 0) {
            angle += MAX_ANGLE;
        }
        return angle == 0 ? MAX_ANGLE : angle;
    }

    private final int timeBegin;
    private final int timeEnd;
    private final int amount;
    private final int granularity;

    private int amountQueued;
    private int elapsed;
    private int toNextSpawn;
    private int minAngle;
    private int maxAngle;
    private int minPointsInRange;
    private int nextAlert = DEFAULT_NEXT_ALERT_TIME;

    private final Select<IEntityIMPattern> mobPool;
    private final List<EntityConstruct> spawnList = new ArrayList<>();
    private final Map<Integer, String> alerts = new HashMap<>();

    public WaveEntry(int timeBegin, int timeEnd, int amount, int granularity, Select<IEntityIMPattern> mobPool) {
        this(timeBegin, timeEnd, amount, granularity, mobPool, -MAX_VALID_ANGLE, MAX_VALID_ANGLE, 1);
    }

    public WaveEntry(int timeBegin, int timeEnd, int amount, int granularity, Select<IEntityIMPattern> mobPool, int angleRange, int minPointsInRange) {
        this(timeBegin, timeEnd, amount, granularity, mobPool, 0, 0, minPointsInRange);
        this.minAngle = Random.create().nextInt(MAX_ANGLE) - MAX_VALID_ANGLE;
        this.maxAngle = wrapAngle(minAngle + angleRange);
    }

    public WaveEntry(int timeBegin, int timeEnd, int amount, int granularity, Select<IEntityIMPattern> mobPool, int minAngle, int maxAngle, int minPointsInRange) {
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.amount = amount;
        this.granularity = granularity;
        this.mobPool = mobPool;
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
        this.minPointsInRange = minPointsInRange;
    }

    public WaveEntry addAlert(String message, int timeElapsed) {
        alerts.put(timeElapsed, message);
        if (timeElapsed < nextAlert) {
            nextAlert = timeElapsed;
        }
        return this;
    }

    public int doNextSpawns(int elapsedMillis, Spawner spawner) {
        toNextSpawn -= elapsedMillis;
        if (nextAlert <= elapsed - toNextSpawn) {
            sendNextAlert(spawner);
        }

        if (toNextSpawn <= 0) {
            elapsed += granularity;
            toNextSpawn += granularity;
            if (toNextSpawn < 0) {
                elapsed -= toNextSpawn;
                toNextSpawn = 0;
            }

            int amountToSpawn = Math.round(amount * elapsed / (timeEnd - timeBegin)) - amountQueued;
            if (amountToSpawn > 0) {
                if (amountToSpawn + amountQueued > amount) {
                    amountToSpawn = amount - amountQueued;
                }
                while (amountToSpawn > 0) {
                    IEntityIMPattern pattern = mobPool.selectNext();
                    if (pattern != null) {
                        EntityConstruct mobConstruct = pattern.generateEntityConstruct(minAngle, maxAngle);
                        if (mobConstruct != null) {
                            amountToSpawn--;
                            this.amountQueued += 1;
                            this.spawnList.add(mobConstruct);
                        }
                    } else {
                        InvasionMod.log("A selection pool in wave entry " + toString() + " returned empty. Pool: " + mobPool.toString());
                    }
                }
            }
        }

        if (!spawnList.isEmpty()) {
            int numberOfSpawns = 0;
            if (spawner.getNumberOfPointsInRange(minAngle, maxAngle, SpawnType.HUMANOID) >= minPointsInRange) {
                for (int i = spawnList.size() - 1; i >= 0; i--) {
                    if (spawner.attemptSpawn(spawnList.get(i), minAngle, maxAngle)) {
                        numberOfSpawns++;
                        spawnList.remove(i);
                    }
                }
            } else {
                reviseSpawnAngles(spawner);
            }
            return numberOfSpawns;
        }
        return 0;
    }

    public void resetToBeginning() {
        elapsed = 0;
        amountQueued = 0;
        mobPool.reset();
    }

    public void setToTime(int millis) {
        elapsed = millis;
    }

    public int getTimeBegin() {
        return timeBegin;
    }

    public int getTimeEnd() {
        return timeEnd;
    }

    public int getAmount() {
        return amount;
    }

    public int getGranularity() {
        return granularity;
    }

    private void sendNextAlert(Spawner spawner) {
        @Nullable
        String message = alerts.remove(nextAlert);
        if (message != null) {
            spawner.sendSpawnAlert(message, Formatting.RED);
        }
        nextAlert = DEFAULT_NEXT_ALERT_TIME;
        if (alerts.size() > 0) {
            for (Integer key : alerts.keySet()) {
                if (key.intValue() < nextAlert) {
                    nextAlert = key.intValue();
                }
            }
        }
    }

    private void reviseSpawnAngles(Spawner spawner) {
        int angleRange = clampAngle(maxAngle - minAngle);
        List<Integer> validAngles = getAllowedAngles(spawner, angleRange);
        if (!validAngles.isEmpty()) {
            minAngle = Util.getRandom(validAngles, Random.create());
            maxAngle = wrapAngle(minAngle + angleRange);
        }

        if (minPointsInRange > 1) {
            InvasionMod.LOGGER.warn("Can't find a direction with enough spawn points: " + minPointsInRange + ". Lowering requirement.");
            this.minPointsInRange = 1;
        } else if (maxAngle - minAngle < MAX_ANGLE) {
            InvasionMod.LOGGER.warn("Can't find a direction with enough spawn points: " + minPointsInRange + ". Switching to 360 degree mode for this entry");
            minAngle = -MAX_VALID_ANGLE;
            maxAngle = MAX_VALID_ANGLE;
        } else {
            InvasionMod.log("Wave entry cannot find a single spawn point");
            spawner.noSpawnPointNotice();
        }
    }

    private List<Integer> getAllowedAngles(Spawner spawner, int angleRange) {
        List<Integer> validAngles = new ArrayList<>();
        for (int angle = -MAX_VALID_ANGLE; angle < MAX_VALID_ANGLE; angle += angleRange) {
            int nextAngle = wrapAngle(angle + angleRange);
            if (spawner.getNumberOfPointsInRange(angle, nextAngle, SpawnType.HUMANOID) >= minPointsInRange) {
                validAngles.add(angle);
            }
        }
        return validAngles;
    }

    @Override
    public String toString() {
        return "WaveEntry@" + Integer.toHexString(hashCode()) + "#time=" + timeBegin + "-" + timeEnd + "#amount=" + amount;
    }
}