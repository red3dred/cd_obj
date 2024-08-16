package com.invasion.nexus.spawns;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.invasion.InvasionMod;
import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.EntityIMZombie;
import com.invasion.entity.InvEntities;
import com.invasion.nexus.Combatant;
import com.invasion.nexus.EntityConstruct;
import com.invasion.nexus.INexusAccess;
import com.invasion.nexus.wave.IMWaveBuilder;
import com.invasion.nexus.wave.Wave;
import com.invasion.nexus.wave.WaveSpawnerException;

public class IMWaveSpawner implements ISpawnerAccess {
	private static final int MAX_SPAWN_TRIES = 20;
	public static final int MIN_SPAWN_RADIUS = 8;
	private static final int NORMAL_SPAWN_HEIGHT = 30;
	private static final int MIN_SPAWN_POINTS_TO_KEEP = 15;
	private static final int MIN_SPAWN_POINTS_TO_KEEP_BELOW_HEIGHT_CUTOFF = 20;
	private static final int HEIGHT_CUTOFF = 35;
	private static final float SPAWN_POINT_CULL_RATE = 0.3F;

	private SpawnPointContainer spawnPointContainer = new SpawnPointContainer();

	private final INexusAccess nexus;

	@Nullable
	private Wave currentWave;

	private boolean active;
	private boolean waveComplete;
	private boolean permitSpawns = true;
	private boolean debugMode;

	private int spawnRadius;
	private int successfulSpawns;
	private long elapsed;

	public IMWaveSpawner(INexusAccess nexus, int radius) {
		this.nexus = nexus;
		this.spawnRadius = radius;
	}

	public long getElapsedTime() {
		return elapsed;
	}

	public boolean setRadius(int radius) {
	    radius = Math.max(8, radius);
	    spawnRadius = radius;
	    return spawnRadius != radius;
	}

	public int getRadius() {
	    return spawnRadius;
	}

	public void beginNextWave(int waveNumber) throws WaveSpawnerException {
		beginNextWave(IMWaveBuilder.generateMainInvasionWave(waveNumber));
	}

	public void beginNextWave(Wave wave) throws WaveSpawnerException {
		if (!active) {
			generateSpawnPoints();
		} else if (debugMode) {
		    InvasionMod.log("Successful spawns of last wave: " + successfulSpawns);
		}

		wave.resetWave();
		waveComplete = false;
		active = true;
		currentWave = wave;
		elapsed = 0L;
		successfulSpawns = 0;

		if (debugMode) {
		    InvasionMod.log("Defined mobs this wave: " + getTotalDefinedMobsThisWave());
		}
	}

	public void spawn(int elapsedMillis) throws WaveSpawnerException {
		elapsed += elapsedMillis;
		if (waveComplete || !active) {
			return;
		}

		if (spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID) < 10) {
			generateSpawnPoints();
			if (spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID) < 10) {
				throw new WaveSpawnerException("Not enough spawn points for type " + SpawnType.HUMANOID);
			}
		}
		currentWave.doNextSpawns(elapsedMillis, this);
		if (currentWave.isComplete()) {
			waveComplete = true;
		}
	}

	public int resumeFromState(Wave wave) throws WaveSpawnerException {
		stop();
		beginNextWave(wave);
		setPermitSpawns(false);
		int numberOfSpawns = 0;
		for (long i = 0; i < elapsed; i += 100L) {
			numberOfSpawns += currentWave.doNextSpawns(100, this);
		}
		setPermitSpawns(true);
		return numberOfSpawns;
	}

	public int resumeFromState(int waveNumber) throws WaveSpawnerException {
		stop();
		beginNextWave(waveNumber);
		setPermitSpawns(false);
		int numberOfSpawns = 0;
		for (long i = 0; i < elapsed; i += 100L) {
		    numberOfSpawns += currentWave.doNextSpawns(100, this);
		}
		setPermitSpawns(true);
		return numberOfSpawns;
	}

	public void stop() {
		active = false;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isReady() {
		return !active && nexus != null && nexus.getWorld() != null;
	}

	public boolean isWaveComplete() {
		return waveComplete;
	}

	public int getWaveDuration() {
		return currentWave.getWaveTotalTime();
	}

	public int getWaveRestTime() {
		return currentWave.getWaveBreakTime();
	}

	public int getSuccessfulSpawnsThisWave() {
		return successfulSpawns;
	}

	public int getTotalDefinedMobsThisWave() {
		return currentWave.getTotalMobAmount();
	}

	public void askForRespawn(Combatant<?> entity) {
		if (spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID) > 10) {
			SpawnPoint spawnPoint = spawnPointContainer.getRandomSpawnPoint(SpawnType.HUMANOID);
			if (spawnPoint != null) {
			    final byte statusAddDeathParticles = (byte)60;
			    spawnPoint.applyTo(entity.asEntity());
			    entity.resetHealth();
			    entity.asEntity().getWorld().sendEntityStatus(entity.asEntity(), statusAddDeathParticles);
			}
		}
	}

	@Override
    public void sendSpawnAlert(String message, Formatting color) {
		if (debugMode) {
		    InvasionMod.log(message);
		}
		nexus.getParticipants().sendMessage(color, message);
	}

	@Override
    public void noSpawnPointNotice() {
	}

	public void debugMode(boolean isOn) {
		debugMode = isOn;
	}

	@Override
    public int getNumberOfPointsInRange(int minAngle, int maxAngle, SpawnType type) {
		return spawnPointContainer.getNumberOfSpawnPoints(type, minAngle, maxAngle);
	}

	public void setPermitSpawns(boolean flag) {
		permitSpawns = flag;
	}

	public void giveSpawnPoints(SpawnPointContainer spawnPointContainer) {
		this.spawnPointContainer = spawnPointContainer;
	}

	@Override
	public boolean attemptSpawn(EntityConstruct mobConstruct, int minAngle, int maxAngle) {
		if (nexus.getWorld() == null && permitSpawns) {
			return false;
		}

		EntityIMLiving mob = mobConstruct.createMob(nexus);
		int spawnTries = Math.min(getNumberOfPointsInRange(minAngle, maxAngle, SpawnType.HUMANOID), MAX_SPAWN_TRIES);

		for (int j = 0; j < spawnTries; j++) {
		    @Nullable
			final SpawnPoint spawnPoint = maxAngle - minAngle >= 360
				        ? spawnPointContainer.getRandomSpawnPoint(SpawnType.HUMANOID)
		                : spawnPointContainer.getRandomSpawnPoint(SpawnType.HUMANOID, minAngle, maxAngle);

			if (spawnPoint == null) {
				return false;
			}
			if (!permitSpawns) {
				successfulSpawns++;
				if (debugMode) {
				    InvasionMod.log("[Spawn] Time: " + currentWave.getTimeInWave() / 1000 + "  Type: " + mob + "  Coords: " + spawnPoint + "  Specified: " + minAngle + "," + maxAngle);
				}

				return true;
			}

			if (spawnPoint.trySpawnEntity(nexus.getWorld(), mob)) {
				successfulSpawns++;
				if (debugMode) {
				    InvasionMod.log("[Spawn] Time: " + currentWave.getTimeInWave() / 1000 + "  Type: " + mob + "  Coords: " + mob.getX() + ", " + mob.getY() + ", " + mob.getZ() + "  θ" + spawnPoint.getAngle() + "  Specified: " + minAngle + "," + maxAngle);
				}

				return true;
			}
		}
		InvasionMod.log("Could not find valid spawn for '" + mob.getName().getString() + "' after " + spawnTries + " tries");
		return false;
	}

	private void generateSpawnPoints() {
		if (nexus.getWorld() == null) {
			return;
		}
		EntityIMZombie zombie = InvEntities.ZOMBIE.create(nexus.getWorld());
		zombie.setNexus(nexus);
		List<SpawnPoint> spawnPoints = new ArrayList<>();
		BlockPos origin = nexus.getOrigin();
		BlockPos.Mutable mutable = origin.mutableCopy();
		for (int vertical = nexus.getWorld().getBottomY();
		         vertical < nexus.getWorld().getTopY() - 2;
		         vertical = vertical > 0 ? vertical * -1 : vertical * -1 + 1) {
			for (int i = 0; i <= spawnRadius * 0.7D + 1.0D; i++) {
				int j = (int) Math.round(spawnRadius * Math.cos(Math.asin(i / spawnRadius)));

				addValidSpawn(zombie, spawnPoints, mutable.set(origin).move( i, vertical, j));
				addValidSpawn(zombie, spawnPoints, mutable.set(origin).move( i, vertical,-j));
				addValidSpawn(zombie, spawnPoints, mutable.set(origin).move(-i, vertical, j));
				addValidSpawn(zombie, spawnPoints, mutable.set(origin).move(-i, vertical,-j));

                addValidSpawn(zombie, spawnPoints, mutable.set(origin).move( j, vertical, i));
                addValidSpawn(zombie, spawnPoints, mutable.set(origin).move( j, vertical,-i));
                addValidSpawn(zombie, spawnPoints, mutable.set(origin).move(-j, vertical, i));
                addValidSpawn(zombie, spawnPoints, mutable.set(origin).move(-j, vertical,-i));
			}
		}

		if (spawnPoints.size() > MIN_SPAWN_POINTS_TO_KEEP) {
			int i;
			int amountToRemove = (int) ((spawnPoints.size() - MIN_SPAWN_POINTS_TO_KEEP) * SPAWN_POINT_CULL_RATE);
			for (i = spawnPoints.size() - 1; i >= spawnPoints.size() - amountToRemove; i--) {
				if (Math.abs(spawnPoints.get(i).pos().getY() - origin.getY()) < NORMAL_SPAWN_HEIGHT) {
					break;
				}
			}
			for (; i >= MIN_SPAWN_POINTS_TO_KEEP_BELOW_HEIGHT_CUTOFF; i--) {
				SpawnPoint spawnPoint = spawnPoints.get(i);
				if (spawnPoint.pos().getY() - origin.getY() <= HEIGHT_CUTOFF) {
					spawnPointContainer.addSpawnPointXZ(spawnPoint);
				}

			}
			for (; i >= 0; i--) {
				spawnPointContainer.addSpawnPointXZ(spawnPoints.get(i));
			}

		}

		InvasionMod.log("Num. Spawn Points: " + Integer.toString(spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID)));
	}

	private void addValidSpawn(EntityIMLiving entity, List<SpawnPoint> spawnPoints, BlockPos pos) {
		entity.updatePositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
		if (entity.canSpawn(nexus.getWorld())) {
			int angle = (int) (Math.atan2(nexus.getOrigin().getZ() - pos.getZ(), nexus.getOrigin().getX() - pos.getX()) * MathHelper.DEGREES_PER_RADIAN);
			spawnPoints.add(new SpawnPoint(pos.toImmutable(), angle, SpawnType.HUMANOID));
		}
	}

    public void readNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
        setRadius(compound.getInt("spawnRadius"));
        elapsed = compound.getLong("elapsed");
    }

    public NbtCompound writeNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
        compound.putInt("spawnRadius", spawnRadius);
        compound.putLong("elapsed", elapsed);
        return compound;
    }
}