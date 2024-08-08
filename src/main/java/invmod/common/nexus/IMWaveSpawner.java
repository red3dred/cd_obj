package invmod.common.nexus;

import invmod.common.mod_Invasion;
import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.InvEntities;
import net.minecraft.util.Formatting;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class IMWaveSpawner implements ISpawnerAccess {
	private static final int MAX_SPAWN_TRIES = 20;
	private static final int NORMAL_SPAWN_HEIGHT = 30;
	private static final int MIN_SPAWN_POINTS_TO_KEEP = 15;
	private static final int MIN_SPAWN_POINTS_TO_KEEP_BELOW_HEIGHT_CUTOFF = 20;
	private static final int HEIGHT_CUTOFF = 35;
	private static final float SPAWN_POINT_CULL_RATE = 0.3F;

	private SpawnPointContainer spawnPointContainer = new SpawnPointContainer();

	@Nullable
	private INexusAccess nexus;
	private final MobBuilder mobBuilder = new MobBuilder();
	//private final Random rand = new Random();
	@Nullable
	private Wave currentWave;

	private boolean active;
	private boolean waveComplete;
	private boolean spawnMode = true;
	private boolean debugMode;

	private int spawnRadius;
	//private int currentWaveNumber = 1;
	private int successfulSpawns;
	private long elapsed;

	public IMWaveSpawner(INexusAccess tileEntityNexus, int radius) {
		this.nexus = tileEntityNexus;
		this.spawnRadius = radius;
	}

	public long getElapsedTime() {
		return this.elapsed;
	}

	public void setRadius(int radius) {
	    // TODO: Why does it not update for radius less than 8? This is going to cause syncing issues
		if (radius > 8) {
			this.spawnRadius = radius;
		}
	}

	public void beginNextWave(int waveNumber) throws WaveSpawnerException {
		beginNextWave(IMWaveBuilder.generateMainInvasionWave(waveNumber));
	}

	public void beginNextWave(Wave wave) throws WaveSpawnerException {
		if (!this.active) {
			generateSpawnPoints();
		} else if (this.debugMode) {
			mod_Invasion.log("Successful spawns of last wave: " + this.successfulSpawns);
		}

		wave.resetWave();
		this.waveComplete = false;
		this.active = true;
		this.currentWave = wave;
		this.elapsed = 0L;
		this.successfulSpawns = 0;

		if (this.debugMode)
			mod_Invasion.log("Defined mobs this wave: " + getTotalDefinedMobsThisWave());
	}

	public void spawn(int elapsedMillis) throws WaveSpawnerException {
		this.elapsed += elapsedMillis;
		if ((this.waveComplete) || (!this.active)) {
			return;
		}

		if (this.spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID) < 10) {
			generateSpawnPoints();
			if (this.spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID) < 10) {
				throw new WaveSpawnerException("Not enough spawn points for type " + SpawnType.HUMANOID);
			}
		}
		this.currentWave.doNextSpawns(elapsedMillis, this);
		if (this.currentWave.isComplete())
			this.waveComplete = true;
	}

	public int resumeFromState(Wave wave, long elapsedTime, int radius) throws WaveSpawnerException {
		this.spawnRadius = radius;
		stop();
		beginNextWave(wave);

		setSpawnMode(false);
		int numberOfSpawns = 0;
		for (; this.elapsed < elapsedTime; this.elapsed += 100L) {
			numberOfSpawns += this.currentWave.doNextSpawns(100, this);
		}
		setSpawnMode(true);
		return numberOfSpawns;
	}

	public void resumeFromState(int waveNumber, long elapsedTime, int radius) throws WaveSpawnerException {
		this.spawnRadius = radius;
		stop();
		beginNextWave(waveNumber);

		setSpawnMode(false);
		for (; this.elapsed < elapsedTime; this.elapsed += 100L) {
			this.currentWave.doNextSpawns(100, this);
		}
		setSpawnMode(true);
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
		return this.waveComplete;
	}

	public int getWaveDuration() {
		return this.currentWave.getWaveTotalTime();
	}

	public int getWaveRestTime() {
		return this.currentWave.getWaveBreakTime();
	}

	public int getSuccessfulSpawnsThisWave() {
		return this.successfulSpawns;
	}

	public int getTotalDefinedMobsThisWave() {
		return this.currentWave.getTotalMobAmount();
	}

	public void askForRespawn(EntityIMLiving entity) {
		if (spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID) > 10) {
			SpawnPoint spawnPoint = spawnPointContainer.getRandomSpawnPoint(SpawnType.HUMANOID);
			entity.updatePositionAndAngles(spawnPoint.getXCoord() + 0.5D, spawnPoint.getYCoord() + 0.5D, spawnPoint.getZCoord() + 0.5D, 0, 0);
		}
	}

	@Override
    public void sendSpawnAlert(String message, Formatting color) {
		if (this.debugMode) {
			mod_Invasion.log(message);
		}
		mod_Invasion.sendMessageToPlayers(this.nexus.getBoundPlayers(), color, message);
	}

	@Override
    public void noSpawnPointNotice() {
	}

	public void debugMode(boolean isOn) {
		this.debugMode = isOn;
	}

	@Override
    public int getNumberOfPointsInRange(int minAngle, int maxAngle, SpawnType type) {
		return this.spawnPointContainer.getNumberOfSpawnPoints(type, minAngle, maxAngle);
	}

	public void setSpawnMode(boolean flag) {
		this.spawnMode = flag;
	}

	public void giveSpawnPoints(SpawnPointContainer spawnPointContainer) {
		this.spawnPointContainer = spawnPointContainer;
	}

	@Override
	public boolean attemptSpawn(EntityConstruct mobConstruct, int minAngle, int maxAngle) {
		if (this.nexus.getWorld() == null) {
			if (this.spawnMode) {
				return false;
			}
		}
		EntityIMLiving mob = this.mobBuilder.createMobFromConstruct(mobConstruct, this.nexus.getWorld(), this.nexus);
		if (mob == null) {
			mod_Invasion.log("Invalid entity construct");
			return false;
		}

		int spawnTries = getNumberOfPointsInRange(minAngle, maxAngle, SpawnType.HUMANOID);
		if (spawnTries > MAX_SPAWN_TRIES) {
			spawnTries = MAX_SPAWN_TRIES;
		}
		for (int j = 0; j < spawnTries; j++) {
			SpawnPoint spawnPoint;
			if (maxAngle - minAngle >= 360)
				spawnPoint = this.spawnPointContainer.getRandomSpawnPoint(SpawnType.HUMANOID);
			else {
				spawnPoint = this.spawnPointContainer.getRandomSpawnPoint(SpawnType.HUMANOID, minAngle, maxAngle);
			}
			if (spawnPoint == null) {
				return false;
			}
			if (!this.spawnMode) {
				this.successfulSpawns += 1;
				if (this.debugMode) {
					mod_Invasion.log("[Spawn] Time: " + this.currentWave.getTimeInWave() / 1000 + "  Type: " + mob.toString() + "  Coords: " + spawnPoint.getXCoord() + ", " + spawnPoint.getYCoord() + ", " + spawnPoint.getZCoord() + "  θ" + spawnPoint.getAngle() + "  Specified: " + minAngle + "," + maxAngle);
				}

				return true;
			}

			mob.updatePositionAndAngles(spawnPoint.getXCoord() + 0.5, spawnPoint.getYCoord() + 0.5, spawnPoint.getZCoord() + 0.5, 0, 0);
			if (mob.canSpawn(nexus.getWorld())) {
				this.successfulSpawns += 1;
				this.nexus.getWorld().spawnEntity(mob);
				if (this.debugMode) {
					mod_Invasion.log("[Spawn] Time: " + this.currentWave.getTimeInWave() / 1000 + "  Type: " + mob.toString() + "  Coords: " + mob.getX() + ", " + mob.getY() + ", " + mob.getZ() + "  θ" + spawnPoint.getAngle() + "  Specified: " + minAngle + "," + maxAngle);
				}

				return true;
			}
		}
		mod_Invasion.log("Could not find valid spawn for '" + mob.getName().getString() + "' after " + spawnTries + " tries");
		return false;
	}

	private void generateSpawnPoints() {
		if (this.nexus.getWorld() == null) {
			return;
		}
		EntityIMZombie zombie = InvEntities.ZOMBIE.create(nexus.getWorld());
		zombie.setNexus(nexus);
		List<SpawnPoint> spawnPoints = new ArrayList<>();
		int x = this.nexus.getXCoord();
		int y = this.nexus.getYCoord();
		int z = this.nexus.getZCoord();
		for (int vertical = 0; vertical < 128; vertical = vertical > 0 ? vertical * -1 : vertical * -1 + 1) {
			if (y + vertical <= 252) {
				for (int i = 0; i <= this.spawnRadius * 0.7D + 1.0D; i++) {
					int j = (int) Math.round(this.spawnRadius * Math.cos(Math.asin(i / this.spawnRadius)));

					addValidSpawn(zombie, spawnPoints, x + i, y + vertical, z + j);
					addValidSpawn(zombie, spawnPoints, x + j, y + vertical, z + i);

					addValidSpawn(zombie, spawnPoints, x + i, y + vertical, z - j);
					addValidSpawn(zombie, spawnPoints, x + j, y + vertical, z - i);

					addValidSpawn(zombie, spawnPoints, x - i, y + vertical, z + j);
					addValidSpawn(zombie, spawnPoints, x - j, y + vertical, z + i);

					addValidSpawn(zombie, spawnPoints, x - i, y + vertical, z - j);
					addValidSpawn(zombie, spawnPoints, x - j, y + vertical, z - i);
				}

			}

		}

		if (spawnPoints.size() > MIN_SPAWN_POINTS_TO_KEEP) {
			int i;
			int amountToRemove = (int) ((spawnPoints.size() - MIN_SPAWN_POINTS_TO_KEEP) * SPAWN_POINT_CULL_RATE);
			for (i = spawnPoints.size() - 1; i >= spawnPoints.size() - amountToRemove; i--) {
				if (Math.abs(spawnPoints.get(i).getYCoord() - y) < NORMAL_SPAWN_HEIGHT) {
					break;
				}
			}
			for (; i >= MIN_SPAWN_POINTS_TO_KEEP_BELOW_HEIGHT_CUTOFF; i--) {
				SpawnPoint spawnPoint = spawnPoints.get(i);
				if (spawnPoint.getYCoord() - y <= HEIGHT_CUTOFF) {
					this.spawnPointContainer.addSpawnPointXZ(spawnPoint);
				}

			}
			for (; i >= 0; i--) {
				this.spawnPointContainer.addSpawnPointXZ(spawnPoints.get(i));
			}

		}

		mod_Invasion.log("Num. Spawn Points: " + Integer.toString(this.spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID)));
	}

	private void addValidSpawn(EntityIMLiving entity, List<SpawnPoint> spawnPoints, int x, int y, int z) {
		entity.updatePositionAndAngles(x + 0.5, y + 0.5, z + 0.5, 0, 0);
		if (entity.canSpawn(nexus.getWorld())) {
			int angle = (int) (Math.atan2(this.nexus.getZCoord() - z, this.nexus.getXCoord() - x) * 180.0D / 3.141592653589793D);
			spawnPoints.add(new SpawnPoint(x, y, z, angle, SpawnType.HUMANOID));
		}
	}

	/*private void checkAddSpawn(EntityIMLiving entity, int x, int y, int z) {
	    entity.updatePositionAndAngles(x + 0.5, y + 0.5, z + 0.5, 0, 0);
		if (entity.getCanSpawnHere()) {
			int angle = (int) (Math.atan2(this.nexus.getZCoord() - z, this.nexus.getXCoord() - x) * 180.0D / 3.141592653589793D);
			this.spawnPointContainer.addSpawnPointXZ(new SpawnPoint(x, y, z, angle, SpawnType.HUMANOID));
		}
	}*/
}