package invmod.common.nexus;

import invmod.common.mod_Invasion;
import invmod.common.block.InvBlockEntities;
import invmod.common.block.InvBlocks;
import invmod.common.entity.EntityIMBolt;
import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.EntityIMWolf;
import invmod.common.entity.ai.AttackerAI;
import invmod.common.item.InvItems;
import invmod.common.util.ComparatorDistanceFrom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TileEntityNexus extends BlockEntity implements INexusAccess, SidedInventory {
    private static final int[] SLOTS = {0, 1};
    private static final long BIND_EXPIRE_TIME = 300000L;

    private int activationTimer;
    private int currentWave;
    private int spawnRadius = 52;
    private int nexusLevel = 1;
    private int nexusKills;
    private int generation;
    private int cookTime;
    private int maxHp = 100;
    private int hp = 100;
    private int lastHp = 100;
    private int mode;
    private int powerLevel;
    private int lastPowerLevel;
    private int powerLevelTimer;
    private int mobsLeftInWave;
    private int lastMobsLeftInWave;
    private int mobsToKillInWave;
    private int nextAttackTime;
    private int daysToAttack;
    private long lastWorldTime;
    private int zapTimer;
    private int errorState;
    private int tickCount;
    private int cleanupTimer;
    private long spawnerElapsedRestore;
    private long timer;
    private long waveDelayTimer;
    private long waveDelay;
    private boolean continuousAttack;
    private boolean mobsSorted;
    private boolean resumedFromNBT;
    private boolean activated;

    private IMWaveSpawner waveSpawner = new IMWaveSpawner(this, this.spawnRadius);
    private IMWaveBuilder waveBuilder = new IMWaveBuilder();
    private SimpleInventory nexusItemStacks = new SimpleInventory(2);
    private Box boundingBoxToRadius;
    private Map<UUID, Long> boundPlayers = new HashMap<>();
    private List<EntityIMLiving> mobList = new ArrayList<>();
    private AttackerAI attackerAI = new AttackerAI(this);

    public TileEntityNexus(BlockPos pos, BlockState state) {
        super(InvBlockEntities.NEXUS, pos, state);
        boundingBoxToRadius = computeSpawnArea();
        nexusItemStacks.addListener(l -> markDirty());
    }

    private Box computeSpawnArea() {
        return new Box(pos).expand(spawnRadius + 10, spawnRadius + 40, spawnRadius + 10);
    }

    private Box getChunkBox(World world) {
        return new Box(pos).expand(spawnRadius + 10, spawnRadius + 40, spawnRadius + 10).withMinY(world.getBottomY()).withMaxY(world.getTopY());
    }

    @Override
    public Map<UUID, Long> getBoundPlayers() {
        return boundPlayers;
    }

    public boolean isActive() {
        return this.activated;
    }

    @Override
    public boolean isActivating() {
        return (activationTimer > 0) && (activationTimer < 400);
    }

    public int getHp() {
        return hp;
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public int getActivationTimer() {
        return activationTimer;
    }

    @Override
    public int getSpawnRadius() {
        return spawnRadius;
    }

    @Override
    public int getNexusKills() {
        return nexusKills;
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    @Override
    public int getNexusLevel() {
        return nexusLevel;
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public int getCookTime() {
        return cookTime;
    }

    @Override
    public int getXCoord() {
        return getPos().getX();
    }

    @Override
    public int getYCoord() {
        return getPos().getY();
    }

    @Override
    public int getZCoord() {
        return getPos().getZ();
    }

    @Override
    public List<EntityIMLiving> getMobList() {
        return mobList;
    }

    public int getActivationProgressScaled(int i) {
        return activationTimer * i / 400;
    }

    public int getGenerationProgressScaled(int i) {
        return generation * i / 3000;
    }

    public int getCookProgressScaled(int i) {
        return cookTime * i / 1200;
    }

    public int getNexusPowerLevel() {
        return powerLevel;
    }

    @Override
    public int getCurrentWave() {
        return currentWave;
    }

    @Override
    public int size() {
        return nexusItemStacks.size();
    }

    @Override
    public void setStack(int i, ItemStack stack) {
        this.nexusItemStacks.setStack(i, stack);
    }

    @Override
    public ItemStack getStack(int i) {
        return nexusItemStacks.getStack(i);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return nexusItemStacks.removeStack(slot, amount);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity entityplayer) {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return nexusItemStacks.isEmpty();
    }

    @Override
    public ItemStack removeStack(int slot) {
        return nexusItemStacks.removeStack(slot);
    }

    @Override
    public void clear() {
        nexusItemStacks.clear();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    public void tick(ServerWorld world) {
        updateStatus();
        this.attackerAI.update();

        if (mode == 1 || mode == 2 || mode == 3) {
            if (this.resumedFromNBT) {
                boundingBoxToRadius = getChunkBox(world);
                if (mode == 2 && continuousAttack) {
                    if (resumeSpawnerContinuous()) {
                        mobsLeftInWave = (lastMobsLeftInWave += acquireEntities());
                        mod_Invasion.log("mobsLeftInWave: " + mobsLeftInWave);
                        mod_Invasion.log("mobsToKillInWave: " + mobsToKillInWave);
                    }
                } else {
                    resumeSpawnerInvasion();
                    acquireEntities();
                }
                attackerAI.onResume();
                resumedFromNBT = false;
            }
            try {
                tickCount = (tickCount + 1) % 60;
                if (tickCount == 0) {
                    bindPlayers();
                    updateMobList();
                }

                if (mode == 1 || mode == 3) {
                    doInvasion(50);
                } else if (this.mode == 2) {
                    doContinuous(50);
                }
            } catch (WaveSpawnerException e) {
                mod_Invasion.log(e.getMessage());
                e.printStackTrace();
                stop();
            }
        }

        cleanupTimer = (cleanupTimer + 1) % 40;
        if (cleanupTimer == 0) {
            if (!world.getBlockState(getPos()).isOf(InvBlocks.NEXUS_CORE)) {
                mod_Invasion.setInvasionEnded(this);
                stop();
                markDirty();
                markRemoved();
                mod_Invasion.log("Stranded Nexus entity trying to delete itself...");
            }
        }
    }

    public void emergencyStop() {
        mod_Invasion.log("Nexus manually stopped by command");
        stop();
        killAllMobs();
    }

    public void debugStatus() {
        for (Map.Entry entry : this.getBoundPlayers().entrySet()) {
            EntityPlayerMP player = ((EntityPlayerMP) FMLCommonHandler.instance().getMinecraftServerInstance()
                    .getConfigurationManager().func_152612_a((String) entry.getKey()));
            player.addChatComponentMessage(new ChatComponentText("Current Time: " + getWorld().getWorldTime()));
            player.addChatComponentMessage(new ChatComponentText("Time to next: " + this.nextAttackTime));
            player.addChatComponentMessage(new ChatComponentText("Days to attack: " + this.daysToAttack));
            player.addChatComponentMessage(new ChatComponentText("Mobs left: " + this.mobsLeftInWave));
            player.addChatComponentMessage(new ChatComponentText("Mode: " + this.mode));
        }
    }

    public void debugStartInvaion(int startWave) {
        mod_Invasion.tryGetInvasionPermission(this);
        startInvasion(startWave);
        this.activated = true;
    }

    public void createBolt(BlockPos target, int t) {
        getWorld().spawnEntity(new EntityIMBolt(getWorld(), getPos().toCenterPos(), target.toCenterPos(), t, 1));
    }

    public boolean setSpawnRadius(int radius) {
        if ((!waveSpawner.isActive()) && (radius > 8)) {
            spawnRadius = radius;
            waveSpawner.setRadius(radius);
            boundingBoxToRadius = getChunkBox(getWorld());
            return true;
        }

        return false;
    }

    @Override
    public void attackNexus(int damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
            if (this.mode == 1) {
                theEnd();
            }
        }
        while (this.hp + 5 <= this.lastHp) {
            mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.DARK_RED, "invmod.message.nexus.hpat", (this.lastHp - 5));
            this.lastHp -= 5;
            playSoundForBoundPlayers("mob.blaze.hit");
        }
    }

    @Override
    public void registerMobDied() {
        nexusKills++;
        mobsLeftInWave--;
        if (mobsLeftInWave <= 0) {
            if (lastMobsLeftInWave > 0) {
                mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.GREEN, "invmod.message.nexus.stableagain");
                mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.GREEN, "invmod.message.nexus.unleashingenergy");
                lastMobsLeftInWave = mobsLeftInWave;
            }
            return;
        }
        while (mobsLeftInWave + mobsToKillInWave * 0.1F <= lastMobsLeftInWave) {
            mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.GREEN, "invmod.message.nexus.stabilizedto", "" + Formatting.DARK_GREEN + (100 - 100 * mobsLeftInWave / mobsToKillInWave) + "%");
            this.lastMobsLeftInWave = ((int) (lastMobsLeftInWave - mobsToKillInWave * 0.1F));
        }
    }

    public void registerMobClose() {
    }

    @Override
    public void askForRespawn(EntityIMLiving entity) {
        mod_Invasion.log("Stuck entity asking for respawn: " + entity);
        this.waveSpawner.askForRespawn(entity);
    }

    @Override
    public AttackerAI getAttackerAI() {
        return this.attackerAI;
    }

    protected void setActivationTimer(int i) {
        this.activationTimer = i;
    }

    protected void setNexusLevel(int i) {
        this.nexusLevel = i;
    }

    protected void setNexusKills(int i) {
        this.nexusKills = i;
    }

    protected void setGeneration(int i) {
        this.generation = i;
    }

    protected void setNexusPowerLevel(int i) {
        this.powerLevel = i;
    }

    protected void setCookTime(int i) {
        this.cookTime = i;
    }

    protected void setWave(int wave) {
        this.currentWave = wave;
    }

    private void startInvasion(int startWave) {
        this.boundingBoxToRadius = computeSpawnArea();
        if (mode == 2 && continuousAttack) {
            mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.RED, "invmod.message.nexus.alreadyactivated");
            return;
        }

        if ((this.mode == 0) || (this.mode == 2)) {
            if (this.waveSpawner.isReady()) {
                try {
                    this.currentWave = startWave;
                    this.waveSpawner.beginNextWave(this.currentWave);
                    if (this.mode == 0)
                        setMode(1);
                    else {
                        setMode(3);
                    }
                    bindPlayers();
                    this.hp = this.maxHp;
                    this.lastHp = this.maxHp;
                    this.waveDelayTimer = -1L;
                    this.timer = System.currentTimeMillis();
                    String boundPlayers = Formatting.AQUA + "";
                    for (UUID playerId : getBoundPlayers().keySet()) {
                        PlayerEntity player = getWorld().getPlayerByUuid(playerId);
                        if (player != null) {
                            boundPlayers += player.getGameProfile().getName() + Formatting.DARK_AQUA + ", " + Formatting.AQUA;
                        }
                    }
                    boundPlayers = boundPlayers.substring(0, boundPlayers.length() - 4);
                    mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.DARK_AQUA, "invmod.message.nexus.listboundplayers", boundPlayers);
                    mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.RED, "invmod.message.nexus.firstwavesoon");
                    playSoundForBoundPlayers("invmod:rumble1");
                } catch (WaveSpawnerException e) {
                    stop();
                    mod_Invasion.log(e.getMessage());
                    mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.DARK_RED, e.getMessage());
                }
            } else {
                mod_Invasion.log("Wave spawner is not in ready state");
            }
        } else {
            mod_Invasion.log("Tried to activate Nexus while already active");
        }
    }

    private void startContinuousPlay() {
        this.boundingBoxToRadius = getChunkBox(getWorld());
        if (mode == 4 && waveSpawner.isReady() && mod_Invasion.tryGetInvasionPermission(this)) {
            setMode(2);
            hp = maxHp;
            lastHp = maxHp;
            lastPowerLevel = powerLevel;
            lastWorldTime = getWorld().getTime();
            nextAttackTime = ((int) (lastWorldTime / 24000L * 24000L) + 14000);
            if ((this.lastWorldTime % 24000L > 12000L) && (lastWorldTime % 24000L < 16000L)) {
                mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.RED, "invmod.message.nexus.nightlooming");
            } else {
                mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.GREEN, "invmod.message.nexus.activatedandstable");
            }
        } else {
            mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.DARK_RED, "invmod.message.nexus.couldnotactivate");
        }
    }

    private void doInvasion(int elapsed) throws WaveSpawnerException {
        if (waveSpawner.isActive()) {
            if (hp <= 0) {
                theEnd();
            } else {
                generateFlux(1);
                if (waveSpawner.isWaveComplete()) {
                    if (waveDelayTimer == -1L) {
                        mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.GREEN, "invmod.message.wave.complete", "" + Formatting.DARK_GREEN + this.currentWave);
                        playSoundForBoundPlayers("invmod:chime1");
                        waveDelayTimer = 0L;
                        waveDelay = waveSpawner.getWaveRestTime();
                    } else {
                        waveDelayTimer += elapsed;
                        if (waveDelayTimer > waveDelay) {
                            currentWave += 1;
                            mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.RED, "invmod.message.wave.begin", "" + Formatting.DARK_RED + currentWave);
                            waveSpawner.beginNextWave(currentWave);
                            waveDelayTimer = -1L;
                            playSoundForBoundPlayers("invmod:rumble1");
                            if (currentWave > nexusLevel) {
                                nexusLevel = currentWave;
                            }
                        }
                    }
                } else {
                    waveSpawner.spawn(elapsed);
                }
            }
        }
    }

    private void playSoundForBoundPlayers(SoundEvent sound) {
        for (Map.Entry<UUID, Long> entry : getBoundPlayers().entrySet()) {
            try {
                // should have getPlayerForUsername at the end
                PlayerEntity player = getWorld().getPlayerByUuid(entry.getKey());
                if (player != null) {
                    player.getWorld().playSound(null, player.getBlockPos(), sound, SoundCategory.AMBIENT, 1F, 1F);
                }
            } catch (Exception name) {
                System.out.println("Problem while trying to play sound " + sound + " at player " + entry.getKey());
            }
        }
    }

    private void doContinuous(int elapsed) {
        this.powerLevelTimer += elapsed;
        if (this.powerLevelTimer > 2200) {
            this.powerLevelTimer -= 2200;
            generateFlux(5 + (int) (5 * this.powerLevel / 1550.0F));
            if ((this.nexusItemStacks[0] == null)
                    || (this.nexusItemStacks[0].getItem() != mod_Invasion.itemDampingAgent)) {
                this.powerLevel += 1;
            }
        }

        if ((this.nexusItemStacks[0] != null)
                && (this.nexusItemStacks[0].getItem() == mod_Invasion.itemStrongDampingAgent)) {
            if ((this.powerLevel >= 0) && (!this.continuousAttack)) {
                this.powerLevel -= 1;
                if (this.powerLevel < 0) {
                    stop();
                }
            }
        }

        if (!this.continuousAttack) {
            long currentTime = getWorld().getWorldTime();
            int timeOfDay = (int) (this.lastWorldTime % 24000L);
            if ((timeOfDay < 12000) && (currentTime % 24000L >= 12000L)
                    && (currentTime + 12000L > this.nextAttackTime)) {
                mod_Invasion.sendMessageToPlayers(this.getBoundPlayers(), Formatting.RED,
                        "invmod.message.nexus.nightlooming");
            }
            if (this.lastWorldTime > currentTime) {
                this.nextAttackTime = ((int) (this.nextAttackTime - (this.lastWorldTime - currentTime)));
            }
            this.lastWorldTime = currentTime;

            if (this.lastWorldTime >= this.nextAttackTime) {
                float difficulty = 1.0F + this.powerLevel / 4500;
                float tierLevel = 1.0F + this.powerLevel / 4500;
                int timeSeconds = 240;
                try {
                    Wave wave = this.waveBuilder.generateWave(difficulty, tierLevel, timeSeconds);
                    this.mobsLeftInWave = (this.lastMobsLeftInWave = this.mobsToKillInWave = (int) (wave
                            .getTotalMobAmount() * 0.8F));
                    this.waveSpawner.beginNextWave(wave);
                    this.continuousAttack = true;
                    int days = mod_Invasion.getMinContinuousModeDays() + getWorld().rand.nextInt(
                            1 + mod_Invasion.getMaxContinuousModeDays() - mod_Invasion.getMinContinuousModeDays());
                    this.nextAttackTime = ((int) (currentTime / 24000L * 24000L) + 14000 + days * 24000);
                    this.hp = (this.lastHp = 100);
                    this.zapTimer = 0;
                    this.waveDelayTimer = -1L;
                    mod_Invasion.sendMessageToPlayers(this.getBoundPlayers(), Formatting.RED,
                            "invmod.message.nexus.destabilizing");
                    playSoundForBoundPlayers("invmod:rumble1");
                } catch (WaveSpawnerException e) {
                    mod_Invasion.log(e.getMessage());
                    e.printStackTrace();
                    stop();
                }

            }

        } else if (this.hp <= 0) {
            this.continuousAttack = false;
            continuousNexusHurt();
        } else if (this.waveSpawner.isWaveComplete()) {

            if (this.waveDelayTimer == -1L) {
                this.waveDelayTimer = 0L;
                this.waveDelay = this.waveSpawner.getWaveRestTime();
            } else {

                this.waveDelayTimer += elapsed;
                if ((this.waveDelayTimer > this.waveDelay) && (this.zapTimer < -200)) {
                    this.waveDelayTimer = -1L;
                    this.continuousAttack = false;
                    this.waveSpawner.stop();
                    this.hp = 100;
                    this.lastHp = 100;
                    this.lastPowerLevel = this.powerLevel;
                }
            }

            this.zapTimer -= 1;
            if (this.mobsLeftInWave <= 0) {
                if ((this.zapTimer <= 0) && (zapEnemy(1))) {
                    zapEnemy(0);
                    this.zapTimer = 23;
                }
            }
        } else {
            try {
                this.waveSpawner.spawn(elapsed);
            } catch (WaveSpawnerException e) {
                mod_Invasion.log(e.getMessage());
                e.printStackTrace();
                stop();
            }
        }
    }

    private void updateStatus() {
        if (this.nexusItemStacks[0] != null) {
            if ((this.nexusItemStacks[0].getItem() == mod_Invasion.itemIMTrap)
                    && (this.nexusItemStacks[0].getItemDamage() == 0)) {
                if (this.cookTime < 1200) {
                    if (this.mode == 0)
                        this.cookTime += 1;
                    else {
                        this.cookTime += 9;
                    }
                }
                if (this.cookTime >= 1200) {
                    if (this.nexusItemStacks[1] == null) {
                        this.nexusItemStacks[1] = new ItemStack(mod_Invasion.itemIMTrap, 1, 1);
                        if (--this.nexusItemStacks[0].stackSize <= 0)
                            this.nexusItemStacks[0] = null;
                        this.cookTime = 0;
                    } else if ((this.nexusItemStacks[1].getItem() == mod_Invasion.itemIMTrap)
                            && (this.nexusItemStacks[1].getItemDamage() == 1)) {
                        this.nexusItemStacks[1].stackSize += 1;
                        if (--this.nexusItemStacks[0].stackSize <= 0)
                            this.nexusItemStacks[0] = null;
                        this.cookTime = 0;
                    }
                }
            } else if ((this.nexusItemStacks[0].getItem() == mod_Invasion.itemRiftFlux)
                    && (this.nexusItemStacks[0].getItemDamage() == 1)) {
                if ((this.cookTime < 1200) && (this.nexusLevel >= 10)) {
                    this.cookTime += 5;
                }

                if (this.cookTime >= 1200) {
                    if (this.nexusItemStacks[1] == null) {
                        this.nexusItemStacks[1] = new ItemStack(mod_Invasion.itemStrongCatalyst, 1);
                        if (--this.nexusItemStacks[0].stackSize <= 0)
                            this.nexusItemStacks[0] = null;
                        this.cookTime = 0;
                    }
                }
            }
        } else {
            this.cookTime = 0;
        }

        if (this.activationTimer >= 400) {
            this.activationTimer = 0;
            if ((mod_Invasion.tryGetInvasionPermission(this)) && (this.nexusItemStacks[0] != null)) {
                if (this.nexusItemStacks[0].getItem() == mod_Invasion.itemNexusCatalyst) {
                    this.nexusItemStacks[0].stackSize -= 1;
                    if (this.nexusItemStacks[0].stackSize == 0)
                        this.nexusItemStacks[0] = null;
                    this.activated = true;
                    startInvasion(1);
                } else if (this.nexusItemStacks[0].getItem() == mod_Invasion.itemStrongCatalyst) {
                    this.nexusItemStacks[0].stackSize -= 1;
                    if (this.nexusItemStacks[0].stackSize == 0)
                        this.nexusItemStacks[0] = null;
                    this.activated = true;
                    startInvasion(10);
                } else if (this.nexusItemStacks[0].getItem() == mod_Invasion.itemStableNexusCatalyst) {
                    this.nexusItemStacks[0].stackSize -= 1;
                    if (this.nexusItemStacks[0].stackSize == 0)
                        this.nexusItemStacks[0] = null;
                    this.activated = true;
                    startContinuousPlay();
                }
            }

        } else if ((this.mode == 0) || (this.mode == 4)) {
            if (this.nexusItemStacks[0] != null) {
                if ((this.nexusItemStacks[0].getItem() == mod_Invasion.itemNexusCatalyst)
                        || (this.nexusItemStacks[0].getItem() == mod_Invasion.itemStrongCatalyst)) {
                    this.activationTimer += 1;
                    this.mode = 0;
                } else if (this.nexusItemStacks[0].getItem() == mod_Invasion.itemStableNexusCatalyst) {
                    this.activationTimer += 1;
                    this.mode = 4;
                }
            } else {
                this.activationTimer = 0;
            }
        } else if (this.mode == 2) {
            if (this.nexusItemStacks[0] != null) {
                if ((this.nexusItemStacks[0].getItem() == mod_Invasion.itemNexusCatalyst)
                        || (this.nexusItemStacks[0].getItem() == mod_Invasion.itemStrongCatalyst)) {
                    this.activationTimer += 1;
                }
            } else
                this.activationTimer = 0;
        }
    }

    private void generateFlux(int increment) {
        generation += increment;
        if (generation >= 3000) {
            ItemStack currentGeneratedItem = getStack(1);
            if (currentGeneratedItem.isEmpty()) {
                setStack(1, InvItems.RIFT_FLUX.getDefaultStack());
                generation -= 3000;
            } else if (currentGeneratedItem.isOf(InvItems.RIFT_FLUX)) {
                currentGeneratedItem.increment(1);
                generation -= 3000;
            }

        }
    }

    private void stop() {
        if (mode == 3) {
            setMode(2);
            int days = mod_Invasion.getMinContinuousModeDays() + getWorld().getRandom().nextInt(1 + mod_Invasion.getMaxContinuousModeDays() - mod_Invasion.getMinContinuousModeDays());
            this.nextAttackTime = ((int) (getWorld().getTime() / 24000L * 24000L) + 14000 + days * 24000);
        } else {
            setMode(0);
        }

        waveSpawner.stop();
        mod_Invasion.setInvasionEnded(this);
        activationTimer = 0;
        currentWave = 0;
        errorState = 0;
        activated = false;
    }

    private void bindPlayers() {
        for (PlayerEntity entityPlayer : getWorld().getEntitiesByClass(PlayerEntity.class, boundingBoxToRadius, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
            long time = System.currentTimeMillis();
            if (!boundPlayers.containsKey(entityPlayer.getDisplayName())) {
                boundPlayers.put(entityPlayer.getUuid(), time);
                mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.DARK_GREEN, "invmod.message.nexus.lifenowbound", Formatting.GREEN + entityPlayer.getDisplayName().getString() + (entityPlayer.getDisplayName().getString().toLowerCase().endsWith("s") ? "'" : "'s"));
            } else if (time - boundPlayers.get(entityPlayer.getDisplayName()).longValue() > 300000L) {
                boundPlayers.put(entityPlayer.getUuid(), time);
                mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.DARK_GREEN, "invmod.message.nexus.lifenowbound", Formatting.GREEN + entityPlayer.getDisplayName().getString() + (entityPlayer.getDisplayName().getString().toLowerCase().endsWith("s") ? "'" : "'s"));
            }
        }
    }

    private void updateMobList() {
        this.mobList = getWorld().getEntitiesByClass(EntityIMLiving.class, boundingBoxToRadius, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
        this.mobsSorted = false;
    }

    protected void setMode(int i) {
        this.mode = i;
        if (this.mode == 0)
            setActive(false);
        else
            setActive(true);
    }

    private void setActive(boolean flag) {
        if (getWorld() instanceof ServerWorld sw) {
            sw.setBlockState(getPos(), getCachedState().with(BlockNexus.LIT, flag));
        }
    }

    private int acquireEntities() {
        List<EntityIMLiving> entities = getWorld().getEntitiesByClass(EntityIMLiving.class, boundingBoxToRadius.expand(10.0D, 128.0D, 10.0D), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
        for (EntityIMLiving entity : entities) {
            entity.acquiredByNexus(this);
        }
        mod_Invasion.log("Acquired " + entities.size() + " entities after state restore");
        return entities.size();
    }

    private void theEnd() {
        if (!getWorld().isRemote) {
            mod_Invasion.sendMessageToPlayers(getWorld(), getBoundPlayers(), Formatting.DARK_RED, "invmod.message.nexus.destroyed");
            stop();
            long time = System.currentTimeMillis();
            for (Map.Entry<UUID, Long> entry : getBoundPlayers().entrySet()) {
                if (time - entry.getValue() < 300000L) {
                    PlayerEntity player = getWorld().getPlayerByUuid(entry.getKey());
                    if (player != null) {
                        player.getEntityWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_DEATH, 4, 1);
                        player.attackEntityFrom(DamageSource.magic, 500.0F);

                    }

                }

            }

            this.boundPlayers.clear();
            killAllMobs();
        }
    }

    private void continuousNexusHurt() {
        mod_Invasion.sendMessageToPlayers(getBoundPlayers(), Formatting.DARK_RED, "invmod.message.nexus.severelydamaged");
        for (Map.Entry<UUID, Long> entry : boundPlayers.entrySet()) {
            PlayerEntity player = getWorld().getPlayerByUuid(entry.getKey());
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.AMBIENT, 4, 1);
        }
        killAllMobs();
        this.waveSpawner.stop();
        this.powerLevel = ((int) ((this.powerLevel - (this.powerLevel - this.lastPowerLevel)) * 0.7F));
        this.lastPowerLevel = this.powerLevel;
        if (this.powerLevel < 0) {
            this.powerLevel = 0;
            stop();
        }
    }

    private void killAllMobs() {
        DamageSource source = getWorld().getDamageSources().magic();
        // monsters
        for (EntityIMLiving mob : getWorld().getEntitiesByClass(EntityIMLiving.class, boundingBoxToRadius, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
            mob.damage(source, mob.getMaxHealth());
            mob.kill();
        }

        // wolves
        for (EntityIMWolf wolf : getWorld().getEntitiesByClass(EntityIMWolf.class, boundingBoxToRadius, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
            wolf.damage(source, wolf.getMaxHealth());
            wolf.kill();
        }
    }

    private boolean zapEnemy(int sfx) {
        if (mobList.isEmpty()) {
            return false;
        }

        if (!mobsSorted) {
            Collections.sort(mobList, ComparatorDistanceFrom.ofComparisonEntities(this));
        }
        EntityIMLiving mob = mobList.removeLast();
        mob.damage(mob.getDamageSources().magic(), 500);
        getWorld().spawnEntity(new EntityIMBolt(getWorld(), getPos().toCenterPos(), mob.getEyePos(), 15, sfx));
        return true;
    }

    private boolean resumeSpawnerContinuous() {
        try {
            mod_Invasion.tryGetInvasionPermission(this);
            float difficulty = 1.0F + this.powerLevel / 4500;
            float tierLevel = 1.0F + this.powerLevel / 4500;
            int timeSeconds = 240;
            Wave wave = this.waveBuilder.generateWave(difficulty, tierLevel, timeSeconds);
            this.mobsToKillInWave = ((int) (wave.getTotalMobAmount() * 0.8F));
            mod_Invasion.log("Original mobs to kill: " + this.mobsToKillInWave);
            this.mobsLeftInWave = (this.lastMobsLeftInWave = this.mobsToKillInWave
                    - this.waveSpawner.resumeFromState(wave, this.spawnerElapsedRestore, this.spawnRadius));
            return true;
        } catch (WaveSpawnerException e) {
            float tierLevel;
            mod_Invasion.log("Error resuming spawner: " + e.getMessage());
            this.waveSpawner.stop();
            return false;
        } finally {
            mod_Invasion.setInvasionEnded(this);
        }
    }

    private boolean resumeSpawnerInvasion() {
        try {
            mod_Invasion.tryGetInvasionPermission(this);
            this.waveSpawner.resumeFromState(this.currentWave, this.spawnerElapsedRestore, this.spawnRadius);
            return true;
        } catch (WaveSpawnerException e) {
            mod_Invasion.log("Error resuming spawner: " + e.getMessage());
            this.waveSpawner.stop();
            return false;
        } finally {
            mod_Invasion.setInvasionEnded(this);
        }
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        mod_Invasion.log("Restoring TileEntityNexus from NBT");
        super.readFromNBT(nbttagcompound);
        // added 0 to gettaglist, because it asked an int
        NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 0);
        this.nexusItemStacks = new ItemStack[getSizeInventory()];
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.getCompoundTagAt(i);
            byte byte0 = nbttagcompound1.getByte("Slot");
            if ((byte0 >= 0) && (byte0 < this.nexusItemStacks.length)) {
                this.nexusItemStacks[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }

        }
        // added 0 to gettaglist, because it asked an int
        nbttaglist = nbttagcompound.getTagList("boundPlayers", 0);
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            this.boundPlayers.put(((NBTTagCompound) nbttaglist.getCompoundTagAt(i)).getString("name"),
                    Long.valueOf(System.currentTimeMillis()));
            mod_Invasion
                    .log("Added bound player: " + ((NBTTagCompound) nbttaglist.getCompoundTagAt(i)).getString("name"));
        }

        this.activationTimer = nbttagcompound.getShort("activationTimer");
        this.mode = nbttagcompound.getInteger("mode");
        this.currentWave = nbttagcompound.getShort("currentWave");
        this.spawnRadius = nbttagcompound.getShort("spawnRadius");
        this.nexusLevel = nbttagcompound.getShort("nexusLevel");
        this.hp = nbttagcompound.getShort("hp");
        this.nexusKills = nbttagcompound.getInteger("nexusKills");
        this.generation = nbttagcompound.getShort("generation");
        this.powerLevel = nbttagcompound.getInteger("powerLevel");
        this.lastPowerLevel = nbttagcompound.getInteger("lastPowerLevel");
        this.nextAttackTime = nbttagcompound.getInteger("nextAttackTime");
        this.daysToAttack = nbttagcompound.getInteger("daysToAttack");
        this.continuousAttack = nbttagcompound.getBoolean("continuousAttack");
        this.activated = nbttagcompound.getBoolean("activated");

        this.boundingBoxToRadius.setBounds(this.xCoord - (this.spawnRadius + 10), this.yCoord - (this.spawnRadius + 40),
                this.zCoord - (this.spawnRadius + 10), this.xCoord + (this.spawnRadius + 10),
                this.yCoord + (this.spawnRadius + 40), this.zCoord + (this.spawnRadius + 10));

        mod_Invasion.log("activationTimer = " + this.activationTimer);
        mod_Invasion.log("mode = " + this.mode);
        mod_Invasion.log("currentWave = " + this.currentWave);
        mod_Invasion.log("spawnRadius = " + this.spawnRadius);
        mod_Invasion.log("nexusLevel = " + this.nexusLevel);
        mod_Invasion.log("hp = " + this.hp);
        mod_Invasion.log("nexusKills = " + this.nexusKills);
        mod_Invasion.log("powerLevel = " + this.powerLevel);
        mod_Invasion.log("lastPowerLevel = " + this.lastPowerLevel);
        mod_Invasion.log("nextAttackTime = " + this.nextAttackTime);

        this.waveSpawner.setRadius(this.spawnRadius);
        if ((this.mode == 1) || (this.mode == 3) || ((this.mode == 2) && (this.continuousAttack))) {
            mod_Invasion.log("Nexus is active; flagging for restore");
            this.resumedFromNBT = true;
            this.spawnerElapsedRestore = nbttagcompound.getLong("spawnerElapsed");
            mod_Invasion.log("spawnerElapsed = " + this.spawnerElapsedRestore);
        }

        this.attackerAI.readFromNBT(nbttagcompound);
    }

    public void writeToNBT(NBTTagCompound nbttagcompound) {
        if (this.mode != 0) {
            mod_Invasion.setNexusUnloaded(this);
        }
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setShort("activationTimer", (short) this.activationTimer);
        nbttagcompound.setShort("currentWave", (short) this.currentWave);
        nbttagcompound.setShort("spawnRadius", (short) this.spawnRadius);
        nbttagcompound.setShort("nexusLevel", (short) this.nexusLevel);
        nbttagcompound.setShort("hp", (short) this.hp);
        nbttagcompound.setInteger("nexusKills", this.nexusKills);
        nbttagcompound.setShort("generation", (short) this.generation);
        nbttagcompound.setLong("spawnerElapsed", this.waveSpawner.getElapsedTime());
        nbttagcompound.setInteger("mode", this.mode);
        nbttagcompound.setInteger("powerLevel", this.powerLevel);
        nbttagcompound.setInteger("lastPowerLevel", this.lastPowerLevel);
        nbttagcompound.setInteger("nextAttackTime", this.nextAttackTime);
        nbttagcompound.setInteger("daysToAttack", this.daysToAttack);
        nbttagcompound.setBoolean("continuousAttack", this.continuousAttack);
        nbttagcompound.setBoolean("activated", this.activated);

        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.nexusItemStacks.length; i++) {
            if (this.nexusItemStacks[i] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) i);
                this.nexusItemStacks[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        nbttagcompound.setTag("Items", nbttaglist);

        NBTTagList nbttaglist2 = new NBTTagList();
        for (Map.Entry entry : this.boundPlayers.entrySet()) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setString("name", (String) entry.getKey());
            nbttaglist2.appendTag(nbttagcompound1);
        }
        nbttagcompound.setTag("boundPlayers", nbttaglist2);

        this.attackerAI.writeToNBT(nbttagcompound);
    }


}