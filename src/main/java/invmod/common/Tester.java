package invmod.common;

import invmod.common.nexus.DummyNexus;
import invmod.common.nexus.IMWaveBuilder;
import invmod.common.nexus.IMWaveSpawner;
import invmod.common.nexus.SpawnPoint;
import invmod.common.nexus.SpawnPointContainer;
import invmod.common.nexus.SpawnType;
import invmod.common.nexus.Wave;
import invmod.common.nexus.WaveSpawnerException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tester {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tester.class);

    private final Random rand = new Random();

    public void doWaveBuilderTest(float difficulty, float tierLevel, int lengthSeconds) {
        LOGGER.info("Doing wave builder test. Difficulty: " + difficulty + ", tier: " + tierLevel + ", length: " + lengthSeconds + " seconds");
        LOGGER.info("Generating dummy Nexus and fake spawn points...");
        DummyNexus nexus = new DummyNexus();
        SpawnPointContainer spawnPoints = new SpawnPointContainer();
        for (int i = -170; i < -100; i += 3) {
            spawnPoints.addSpawnPointXZ(new SpawnPoint(i, 0, 0, i, SpawnType.HUMANOID));
        }
        for (int i = 90; i < 180; i += 3) {
            spawnPoints.addSpawnPointXZ(new SpawnPoint(i, 0, 0, i, SpawnType.HUMANOID));
        }
        LOGGER.info("Setting radius to 45");
        IMWaveSpawner spawner = new IMWaveSpawner(nexus, 45);
        spawner.giveSpawnPoints(spawnPoints);
        spawner.debugMode(true);
        spawner.setSpawnMode(false);

        IMWaveBuilder waveBuilder = new IMWaveBuilder();
        Wave wave = waveBuilder.generateWave(difficulty, tierLevel, lengthSeconds);

        int successfulSpawns = 0;
        int definedSpawns = 0;
        try {
            spawner.beginNextWave(wave);
            LOGGER.info("Starting wave.Wave duration: " + spawner.getWaveDuration());
            while (!spawner.isWaveComplete()) {
                spawner.spawn(100);
            }
            LOGGER.info("Wave finished spawning. Wave rest time: " + spawner.getWaveRestTime());
            successfulSpawns += spawner.getSuccessfulSpawnsThisWave();
            definedSpawns += spawner.getTotalDefinedMobsThisWave();
        } catch (WaveSpawnerException e) {
            LOGGER.info(e.getMessage());
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
        }

        LOGGER.info("Successful spawns for wave: " + spawner.getSuccessfulSpawnsThisWave());
        LOGGER.info("Test finished. Total successful spawns: " + successfulSpawns + "  Total defined spawns: " + definedSpawns);
    }

    public void doWaveSpawnerTest(int startWave, int endWave) {
        LOGGER.info("Doing wave spawner test. Start wave: " + startWave + "  End wave: " + endWave);
        LOGGER.info("Generating dummy Nexus and fake spawn points...");
        DummyNexus nexus = new DummyNexus();
        SpawnPointContainer spawnPoints = new SpawnPointContainer();
        for (int i = -170; i < -100; i += 3) {
            spawnPoints.addSpawnPointXZ(new SpawnPoint(i, 0, 0, i, SpawnType.HUMANOID));
        }
        for (int i = 90; i < 180; i += 3) {
            spawnPoints.addSpawnPointXZ(new SpawnPoint(i, 0, 0, i, SpawnType.HUMANOID));
        }
        LOGGER.info("Setting radius to 45");
        IMWaveSpawner spawner = new IMWaveSpawner(nexus, 45);
        spawner.giveSpawnPoints(spawnPoints);
        spawner.debugMode(true);
        spawner.setSpawnMode(false);

        int successfulSpawns = 0;
        int definedSpawns = 0;
        for (; startWave <= endWave; startWave++) {
            try {
                spawner.beginNextWave(startWave);
                LOGGER.info("Starting wave " + startWave + ". Wave duration: " + spawner.getWaveDuration());
                while (!spawner.isWaveComplete()) {
                    spawner.spawn(100);
                }
                LOGGER.info("Wave finished spawning. Wave rest time: " + spawner.getWaveRestTime());
                successfulSpawns += spawner.getSuccessfulSpawnsThisWave();
                definedSpawns += spawner.getTotalDefinedMobsThisWave();
            } catch (WaveSpawnerException e) {
                LOGGER.info(e.getMessage());
            }
        }

        LOGGER.info("Successful spawns last wave: " + spawner.getSuccessfulSpawnsThisWave());
        LOGGER.info("Test finished. Total successful spawns: " + successfulSpawns + "  Total defined spawns: "
                + definedSpawns);
    }

    public void doSpawnPointSelectionTest() {
        LOGGER.info("Doing SpawnPointContainer test");
        LOGGER.info("Filling with spawn points...");
        SpawnPointContainer spawnPoints = new SpawnPointContainer();
        for (int i = -180; i < 180; i += this.rand.nextInt(3)) {
            spawnPoints.addSpawnPointXZ(new SpawnPoint(i, 0, 0, i, SpawnType.HUMANOID));
        }
        LOGGER.info(spawnPoints.getNumberOfSpawnPoints(SpawnType.HUMANOID) + " random points in container");

        LOGGER.info("Cycling through ranges... format: min <= x < max");
        for (int i = -180; i < 180; i += 25) {
            int i2 = i + 40;
            if (i2 >= 180)
                i2 -= 360;
            LOGGER.info(i + " to " + i2);
            for (int j = 0; j < 4; j++) {
                SpawnPoint point = spawnPoints.getRandomSpawnPoint(SpawnType.HUMANOID, i, i2);
                if (point != null) {
                    LOGGER.info(point.toString());
                }
            }
        }
        LOGGER.info("Beginning random stress test");

        int count = 0;
        int count2 = 0;
        for (int i = 0; i < 1105000; i++) {
            int r = this.rand.nextInt(361) - 180;
            int r2 = this.rand.nextInt(361) - 180;
            for (int j = 0; j < 17; j++) {
                count++;
                SpawnPoint point = spawnPoints.getRandomSpawnPoint(SpawnType.HUMANOID, r, r2);
                if (point != null) {
                    if (r < r2) {
                        if ((point.getAngle() < r) || (point.getAngle() >= r2)) {
                            count2++;
                            LOGGER.info(point.toString() + " with specified: " + r + ", " + r2);
                        }

                    } else if ((point.getAngle() >= r) && (point.getAngle() < r2)) {
                        count2++;
                        LOGGER.info(point.toString() + " with specified: " + r + ", " + r2);
                    }
                }
            }

        }

        LOGGER.info("Tested " + count + " random spawn point retrievals. " + count2 + " results out of bounds.");

        LOGGER.info("Finished test.");
    }
}