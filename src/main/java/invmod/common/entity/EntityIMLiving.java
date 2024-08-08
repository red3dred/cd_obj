package invmod.common.entity;

import invmod.common.IBlockAccessExtended;
import invmod.common.INotifyTask;
import invmod.common.IPathfindable;
import invmod.common.SparrowAPI;
import invmod.common.mod_Invasion;
import invmod.common.item.InvItems;
import invmod.common.nexus.EntityConstruct;
import invmod.common.nexus.INexusAccess;
import invmod.common.nexus.MobBuilder.BuildableMob;
import invmod.common.util.CoordsInt;
import invmod.common.util.Distance;
import invmod.common.util.IPosition;
import invmod.common.util.MathUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class EntityIMLiving extends HostileEntity implements Monster, IPathfindable, IPosition, IHasNexus, SparrowAPI, BuildableMob {
    protected static final float DEFAULT_SOFT_STRENGTH = 2.5F;
    protected static final float DEFAULT_HARD_STRENGTH = 5.5F;
    protected static final float DEFAULT_SOFT_COST = 2;
    protected static final float DEFAULT_HARD_COST = 3.2F;
    protected static final float AIR_BASE_COST = 1;
    protected static final Map<Block, Float> BLOCK_COSTS = Util.make(new HashMap<>(), costs -> {
        costs.put(Blocks.AIR, AIR_BASE_COST);
        costs.put(Blocks.LADDER, AIR_BASE_COST);
        costs.put(Blocks.STONE, DEFAULT_HARD_COST);
        costs.put(Blocks.STONE_BRICKS, DEFAULT_HARD_COST);
        costs.put(Blocks.COBBLESTONE, DEFAULT_HARD_COST);
        costs.put(Blocks.MOSSY_COBBLESTONE, DEFAULT_HARD_COST);
        costs.put(Blocks.BRICKS, DEFAULT_HARD_COST);
        costs.put(Blocks.OBSIDIAN, DEFAULT_HARD_COST);
        costs.put(Blocks.IRON_BLOCK, DEFAULT_HARD_COST);
        costs.put(Blocks.DIRT, DEFAULT_SOFT_COST);
        costs.put(Blocks.SAND, DEFAULT_SOFT_COST);
        costs.put(Blocks.GRAVEL, DEFAULT_SOFT_COST);
        costs.put(Blocks.GLASS, DEFAULT_SOFT_COST);
        costs.put(Blocks.OAK_LEAVES, DEFAULT_SOFT_COST);
        costs.put(Blocks.IRON_DOOR, 2.24F);
        costs.put(Blocks.OAK_DOOR, 1.4F);
        costs.put(Blocks.OAK_TRAPDOOR, 1.4F);
        costs.put(Blocks.SANDSTONE, DEFAULT_HARD_COST);
        costs.put(Blocks.OAK_LOG, DEFAULT_HARD_COST);
        costs.put(Blocks.OAK_PLANKS, DEFAULT_HARD_COST);
        costs.put(Blocks.GOLD_BLOCK, DEFAULT_HARD_COST);
        costs.put(Blocks.DIAMOND_BLOCK, DEFAULT_HARD_COST);
        costs.put(Blocks.OAK_FENCE, DEFAULT_HARD_COST);
        costs.put(Blocks.NETHERRACK, DEFAULT_HARD_COST);
        costs.put(Blocks.NETHER_BRICKS, DEFAULT_HARD_COST);
        costs.put(Blocks.SOUL_SAND, DEFAULT_SOFT_COST);
        costs.put(Blocks.GLOWSTONE, DEFAULT_SOFT_COST);
        costs.put(Blocks.TALL_GRASS, AIR_BASE_COST);
    });
    private static final Map<Block, Float> BLOCK_STRENGTHS = Util.make(new HashMap<>(), strengths -> {
        strengths.put(Blocks.AIR, 0.01F);
        strengths.put(Blocks.STONE, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.STONE_BRICKS, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.COBBLESTONE, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.MOSSY_COBBLESTONE, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.BRICKS, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.OBSIDIAN, 7.7F);
        strengths.put(Blocks.IRON_BLOCK, 7.7F);
        strengths.put(Blocks.DIRT, 3.125F);
        strengths.put(Blocks.GRASS_BLOCK, 3.125F);
        strengths.put(Blocks.SAND, DEFAULT_SOFT_STRENGTH);
        strengths.put(Blocks.GRAVEL, DEFAULT_SOFT_STRENGTH);
        strengths.put(Blocks.GLASS, DEFAULT_SOFT_STRENGTH);
        strengths.put(Blocks.OAK_LEAVES, 1.25F);
        strengths.put(Blocks.VINE, 1.25F);
        strengths.put(Blocks.IRON_DOOR, 15.4F);
        strengths.put(Blocks.OAK_DOOR, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.SANDSTONE, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.OAK_LOG, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.OAK_PLANKS, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.GOLD_BLOCK, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.DIAMOND_BLOCK, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.OAK_FENCE, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.NETHERRACK, 3.85F);
        strengths.put(Blocks.NETHER_BRICKS, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.SOUL_SAND, DEFAULT_SOFT_STRENGTH);
        strengths.put(Blocks.GLOWSTONE, DEFAULT_SOFT_STRENGTH);
        strengths.put(Blocks.TALL_GRASS, 0.3F);
        strengths.put(Blocks.DRAGON_EGG, 15F);
    });
    private static final Map<Block, BlockSpecial> SPECIAL_BLOCKS = Util.make(new HashMap<>(), specials -> {
        specials.put(Blocks.STONE, BlockSpecial.CONSTRUCTION_STONE);
        specials.put(Blocks.STONE_BRICKS, BlockSpecial.CONSTRUCTION_STONE);
        specials.put(Blocks.COBBLESTONE, BlockSpecial.CONSTRUCTION_STONE);
        specials.put(Blocks.MOSSY_COBBLESTONE, BlockSpecial.CONSTRUCTION_STONE);
        specials.put(Blocks.BRICKS, BlockSpecial.CONSTRUCTION_1);
        specials.put(Blocks.SANDSTONE, BlockSpecial.CONSTRUCTION_1);
        specials.put(Blocks.NETHER_BRICKS, BlockSpecial.CONSTRUCTION_1);
        specials.put(Blocks.OBSIDIAN, BlockSpecial.DEFLECTION_1);
    });
    private static final Map<Block, Integer> BLOCK_TYPES = Util.make(new HashMap<>(), types -> {
        types.put(Blocks.AIR, Integer.valueOf(1));
        types.put(Blocks.TALL_GRASS, Integer.valueOf(1));
        types.put(Blocks.DEAD_BUSH, Integer.valueOf(1));
        types.put(Blocks.POPPY, Integer.valueOf(1));
        types.put(Blocks.DANDELION, Integer.valueOf(1));
        types.put(Blocks.OAK_PRESSURE_PLATE, Integer.valueOf(1));
        types.put(Blocks.STONE_PRESSURE_PLATE, Integer.valueOf(1));
        types.put(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Integer.valueOf(1));
        types.put(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Integer.valueOf(1));
        types.put(Blocks.STONE_BUTTON, Integer.valueOf(1));
        types.put(Blocks.OAK_BUTTON, Integer.valueOf(1));
        types.put(Blocks.REDSTONE_TORCH, Integer.valueOf(1));
        types.put(Blocks.REDSTONE_WIRE, Integer.valueOf(1));
        types.put(Blocks.TORCH, Integer.valueOf(1));
        types.put(Blocks.LEVER, Integer.valueOf(1));
        types.put(Blocks.SUGAR_CANE, Integer.valueOf(1));
        types.put(Blocks.WHEAT, Integer.valueOf(1));
        types.put(Blocks.CARROTS, Integer.valueOf(1));
        types.put(Blocks.POTATOES, Integer.valueOf(1));
        types.put(Blocks.FIRE, Integer.valueOf(2));
        types.put(Blocks.BEDROCK, Integer.valueOf(2));
        types.put(Blocks.LAVA, Integer.valueOf(2));
        types.put(Blocks.END_PORTAL_FRAME, Integer.valueOf(2));
    });
    protected static final List<Block> UNDESTRUCTABLE_BLOCKS = Arrays.asList(
            Blocks.BEDROCK, Blocks.COMMAND_BLOCK,
            Blocks.END_PORTAL_FRAME, Blocks.LADDER, Blocks.CHEST
    );

    private static final TrackedData<Integer> MOVE_STATE = DataTracker.registerData(EntityIMLiving.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> ANGLES = DataTracker.registerData(EntityIMLiving.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> LABEL = DataTracker.registerData(EntityIMLiving.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> JUMPING = DataTracker.registerData(EntityIMLiving.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> CLIMBING = DataTracker.registerData(EntityIMLiving.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> CLINGING = DataTracker.registerData(EntityIMLiving.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final NavigatorIM bo;
    private final PathNavigateAdapter oldNavAdapter;
    private PathCreator pathSource;
    protected Goal currentGoal = Goal.NONE;
    protected Goal prevGoal = Goal.NONE;

    private float rotationRoll;
    private float rotationYawHeadIM;
    private float rotationPitchHead;
    private float prevRotationRoll;
    private float prevRotationYawHeadIM;
    private float prevRotationPitchHead;
    private int debugMode = mod_Invasion.isDebug() ? 1 : 0;
    private float airResistance = 0.9995F;
    private float groundFriction = 0.546F;
    private float gravityAcel = 0.08F;
    private float moveSpeed = 0.26F;
    private float moveSpeedBase = 0.26F;
    private float turnRate = 30.0F;
    private float pitchRate = 2.0F;
    private int rallyCooldown;
    private IPosition currentTargetPos = new CoordsInt(0, 0, 0);
    private IPosition lastBreathExtendPos = new CoordsInt(0, 0, 0);
    private String simplyID = "needID";
    private boolean shouldRenderLabel = mod_Invasion.isDebug();
    private int gender;
    private boolean isHostile = true;
    private boolean creatureRetaliates = true;
    protected INexusAccess targetNexus;
    protected float attackRange;
    private float maxHealth;
    protected int selfDamage = 2;
    protected int maxSelfDamage = 6;
    protected int maxDestructiveness;
    protected float blockRemoveSpeed = 1.0F;
    protected boolean floatsInWater = true;
    private CoordsInt collideSize = new CoordsInt(
            MathHelper.floor(getWidth() + 1),
            MathHelper.floor(getHeight() + 1),
            MathHelper.floor(getWidth() + 1)
    );
    private boolean canClimb;
    private boolean canDig = true;
    private boolean nexusBound;
    private boolean alwaysIndependent;
    private boolean burnsInDay;
    private int jumpHeight = 1;
    private int aggroRange;
    private int senseRange;
    private int stunTimer;
    protected int throttled;
    protected int throttled2;
    protected int pathThrottle;
    protected int destructionTimer;
    protected int flammability = 2;
    protected int destructiveness;
    protected Entity j;

    public EntityIMLiving(EntityType<? extends EntityIMLiving> type, World world, @Nullable INexusAccess nexus) {
        super(type, world);
        moveControl = new IMMoveHelper(this);
        pathSource = new PathCreator(700, 50);
        bo = new NavigatorIM(this, this.pathSource);
        oldNavAdapter = new PathNavigateAdapter(this.bo);
        setNexus(nexus);
        setAttackStrength(2);
        setMovementSpeed(0.26F);
        setMaxHealthAndHealth(mod_Invasion.getMobHealth(this));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(MOVE_STATE, MoveState.STANDING.ordinal());
        builder.add(JUMPING, false);
        builder.add(CLIMBING, false);
        builder.add(CLINGING, false);
        builder.add(ANGLES, MathUtil.packAnglesDeg(this.rotationRoll, this.rotationYawHeadIM, this.rotationPitchHead, 0.0F));
        builder.add(LABEL, "");
    }

    public EntityIMLiving setNexus(@Nullable INexusAccess nexus) {
        targetNexus = nexus;
        nexusBound = nexus != null;
        burnsInDay = nexusBound && mod_Invasion.getNightMobsBurnInDay();
        aggroRange = nexusBound ? 12 : mod_Invasion.getNightMobSightRange();
        senseRange = nexusBound ? 6 : mod_Invasion.getNightMobSenseRange();
        return this;
    }

    public void setAttackStrength(double attackStrength) {
        getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(attackStrength);
    }

    public double getAttackStrength() {
        return getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        this.prevRotationRoll = this.rotationRoll;
        this.prevRotationYawHeadIM = this.rotationYawHeadIM;
        this.prevRotationPitchHead = this.rotationPitchHead;
        if (!getWorld().isClient) {
            int packedAngles = MathUtil.packAnglesDeg(rotationRoll, rotationYawHeadIM, rotationPitchHead, 0);
            if (packedAngles != dataTracker.get(ANGLES)) {
                dataTracker.set(ANGLES, packedAngles);
            }
        }
    }

    @Override
    public void onSpawned(INexusAccess nexus, EntityConstruct spawnConditions) {
        setNexus(nexus);
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isClient) {
            dataTracker.set(CLINGING, super.isClimbing());
        }

        int air = getAir();

        if (air == 190) {
            lastBreathExtendPos = new CoordsInt(getBlockPos());
        } else if (air == 0) {
            IPosition pos = new CoordsInt(getBlockPos());
            if (Distance.distanceBetween(this.lastBreathExtendPos, pos) > 4) {
                lastBreathExtendPos = pos;
                setAir(180);
            }
        }
    }

    @Override
    public void baseTick() {
        if (!nexusBound) {
            @SuppressWarnings("deprecation")
            float brightness = getBrightnessAtEyes();
            if (brightness > 0.5F || getY() < 55) {
                age += 2;
            }
            if (getBurnsInDay()
                    && getWorld().isDay()
                    && !getWorld().isClient
                    && (brightness > 0.5F)
                    && (getWorld().isSkyVisible(getBlockPos()))
                    && (random.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F)) {
                sunlightDamageTick();
            }
        }
        super.baseTick();
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (data == ANGLES) {
            int packedAngles = dataTracker.get(ANGLES);
            rotationRoll = MathUtil.unpackAnglesDeg_1(packedAngles);
            rotationYawHeadIM = MathUtil.unpackAnglesDeg_2(packedAngles);
            rotationPitchHead = MathUtil.unpackAnglesDeg_3(packedAngles);
        }
        if (data == JUMPING) {
            super.setJumping(dataTracker.get(JUMPING));
        }
    }

    @Override
    public boolean damage(DamageSource damagesource, float damage) {
        if (super.damage(damagesource, damage)) {
            @Nullable
            Entity attacker = damagesource.getAttacker();
            if (attacker != null && attacker != this && isConnectedThroughVehicle(attacker)) {
                this.j = attacker;
            }
            return true;
        }

        return false;
    }

    public boolean stunEntity(int ticks) {
        stunTimer = Math.max(stunTimer, ticks);
        setVelocity(getVelocity().multiply(0, 1, 0));
        return true;
    }

    @Override
    public Vec3d applyMovementInput(Vec3d movementInput, float slipperiness) {
        Vec3d movement = super.applyMovementInput(movementInput, slipperiness);
        if (isOnGround() && !hasNoDrag()) {
            double friction = getGroundFriction() / 0.91D; // divide by initial friction, then multiply by our new friction
            return movement.multiply(friction, 1, friction);
        }
        return movement;
    }

    public void rally(Entity leader) {
        rallyCooldown = 300;
    }

    public void onFollowingEntity(Entity entity) {
    }

    public void onPathSet() {
    }

    public void onBlockRemoved(int x, int y, int z, int id) {
        if (getHealth() > maxHealth - maxSelfDamage) {
            damage(getDamageSources().generic(), selfDamage);
        }

        if ((throttled == 0) && ((id == 3) || (id == 2) || (id == 12) || (id == 13))) {
            playSound(SoundEvents.BLOCK_GRAVEL_STEP, 1.4F, 1F / (random.nextFloat() * 0.6F + 1));
            throttled = 5;
        } else {
            playSound(SoundEvents.BLOCK_STONE_STEP, 1.4F, 1F / (random.nextFloat() * 0.6F + 1));
            throttled = 5;
        }
    }

    public boolean avoidsBlock(BlockState block) {
        return !isInvulnerable()
                && (!isFireImmune() && (block.isIn(BlockTags.FIRE)
                        || block.isIn(BlockTags.CAMPFIRES)
                        || block.getFluidState().isIn(FluidTags.LAVA))
                || block.isOf(Blocks.BEDROCK) || block.isOf(Blocks.CACTUS));
    }

    public boolean ignoresBlock(Block block) {
        if ((block == Blocks.TALL_GRASS) || (block == Blocks.DEAD_BUSH) || (block == Blocks.POPPY)
                || (block == Blocks.DANDELION) || (block == Blocks.BROWN_MUSHROOM)
                || (block == Blocks.RED_MUSHROOM) || (block == Blocks.OAK_PRESSURE_PLATE)
                || (block == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE) || (block == Blocks.STONE_PRESSURE_PLATE)) {
            return true;
        }
        return false;
    }

    public boolean isBlockDestructible(BlockView world, BlockPos pos, BlockState state) {
        if (state.isAir() || !getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) || UNDESTRUCTABLE_BLOCKS.contains(state.getBlock()) || blockHasLadder(world, pos)) {
            return false;
        }
        return state.isIn(BlockTags.DOORS) || state.isIn(BlockTags.TRAPDOORS) || state.isSolidBlock(world, pos);
    }

    public boolean canEntityBeDetected(Entity entity) {
        float distance = distanceTo(entity);
        return (distance <= getSenseRange()) || ((canSee(entity)) && (distance <= getAggroRange()));
    }

    public double findDistanceToNexus() {
        if (this.targetNexus == null) {
            return 1.7976931348623157E+308D;
        }
        double x = targetNexus.getXCoord() + 0.5D - getX();
        double y = targetNexus.getYCoord() - getY() + getHeight() * 0.5D;
        double z = targetNexus.getZCoord() + 0.5D - getZ();
        return Math.sqrt(x * x + y * y + z * z);
    }

    @Nullable
    @Override
    public Entity findPlayerToAttack() {
        PlayerEntity entityPlayer = getWorld().getClosestPlayer(this, getSenseRange());
        if (entityPlayer != null) {
            return entityPlayer;
        }
        entityPlayer = getWorld().getClosestPlayer(this, getAggroRange());
        if (entityPlayer != null && canSee(entityPlayer)) {
            return entityPlayer;
        }
        return null;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbttagcompound) {
        super.writeCustomDataToNbt(nbttagcompound);
        nbttagcompound.putBoolean("alwaysIndependent", this.alwaysIndependent);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbttagcompound) {
        super.readCustomDataFromNbt(nbttagcompound);
        alwaysIndependent = nbttagcompound.getBoolean("alwaysIndependent");
        if (alwaysIndependent) {
            setAggroRange(mod_Invasion.getNightMobSightRange());
            setSenseRange(mod_Invasion.getNightMobSenseRange());
            setBurnsInDay(mod_Invasion.getNightMobsBurnInDay());
        }
    }

    public float getPrevRotationRoll() {
        return this.prevRotationRoll;
    }

    public float getRotationRoll() {
        return this.rotationRoll;
    }

    public float getPrevRotationYawHeadIM() {
        return this.prevRotationYawHeadIM;
    }

    public float getRotationYawHeadIM() {
        return this.rotationYawHeadIM;
    }

    public float getPrevRotationPitchHead() {
        return this.prevRotationPitchHead;
    }

    public float getRotationPitchHead() {
        return this.rotationPitchHead;
    }

    @Override
    public int getXCoord() {
        return getBlockPos().getX();
    }

    @Override
    public int getYCoord() {
        return getBlockPos().getY();
    }

    @Override
    public int getZCoord() {
        return getBlockPos().getZ();
    }

    public float getAttackRange() {
        return this.attackRange;
    }

    public void setMaxHealth(float health) {
        this.maxHealth = health;
        getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(health);
    }

    public void setMaxHealthAndHealth(float health) {
        setMaxHealth(health);
        setHealth(health);
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return canSpawn(world) && (nexusBound || getLightLevelBelow8()) && getWorld().isTopSolid(getBlockPos(), this);
    }

    @Deprecated
    public float getMoveSpeedStat() {
        return getMovementSpeed();
    }

    public float getBaseMoveSpeedStat() {
        return this.moveSpeedBase;
    }

    public int getJumpHeight() {
        return this.jumpHeight;
    }

    public float getBlockStrength(BlockPos pos) {
        return getBlockStrength(pos, getWorld().getBlockState(pos));
    }

    public float getBlockStrength(BlockPos pos, BlockState state) {
        return getBlockStrength(pos, state, getWorld());
    }

    @Deprecated
    public final float getBlockStrength(int x, int y, int z) {
        return getBlockStrength(new BlockPos(x, y, z));
    }

    @Deprecated
    public final float getBlockStrength(int x, int y, int z, Block block) {
        return getBlockStrength(new BlockPos(x, y, z), block.getDefaultState(), getWorld());
    }

    public boolean getCanClimb() {
        return this.canClimb;
    }

    public boolean getCanDigDown() {
        return this.canDig;
    }

    public int getAggroRange() {
        return this.aggroRange;
    }

    public int getSenseRange() {
        return this.senseRange;
    }

    public boolean getBurnsInDay() {
        return this.burnsInDay;
    }

    public int getDestructiveness() {
        return this.destructiveness;
    }

    public float getTurnRate() {
        return this.turnRate;
    }

    public float getPitchRate() {
        return this.pitchRate;
    }

    @Override
    public double getGravity() {
        return this.gravityAcel;
    }

    public float getAirResistance() {
        return this.airResistance;
    }

    public float getGroundFriction() {
        return this.groundFriction;
    }

    public CoordsInt getCollideSize() {
        return this.collideSize;
    }

    public static BlockSpecial getBlockSpecial(Block block2) {
        if (SPECIAL_BLOCKS.containsKey(block2)) {
            return SPECIAL_BLOCKS.get(block2);
        }
        return BlockSpecial.NONE;
    }

    public Goal getAIGoal() {
        return this.currentGoal;
    }

    public Goal getPrevAIGoal() {
        return this.prevGoal;
    }

    @Override
    public PathNavigateAdapter getNavigation() {
        return this.oldNavAdapter;
    }

    public INavigation getNavigatorNew() {
        return this.bo;
    }

    public IPathSource getPathSource() {
        return this.pathSource;
    }

    @Override
    public float getBlockPathWeight(int i, int j, int k) {
        if (this.nexusBound) {
            return 0.0F;
        }
        return 0.5F - getWorld().getLightBrightness(i, j, k);
    }

    @Override
    public float getBlockPathCost(PathNode prevNode, PathNode node, WorldAccess terrainMap) {
        return calcBlockPathCost(prevNode, node, terrainMap);
    }

    @Override
    public void getPathOptionsFromNode(WorldAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        calcPathOptions(terrainMap, currentNode, pathFinder);
    }

    public IPosition getCurrentTargetPos() {
        return this.currentTargetPos;
    }

    public IPosition[] getBlockRemovalOrder(int x, int y, int z) {
        if (getBlockPos().getY() >= y) {
            return new IPosition[] {
                    new CoordsInt(x, y, z),
                    new CoordsInt(x, y + 1, z)
            };
        }

        return new IPosition[] {
            new CoordsInt(x, y + 1, z),
            new CoordsInt(getBlockPos().up(this.collideSize.getYCoord())),
            new CoordsInt(x, y, z)
        };
    }

    @Override
    public INexusAccess getNexus() {
        return this.targetNexus;
    }

    public String getRenderLabel() {
        return dataTracker.get(LABEL);
    }

    public int getDebugMode() {
        return this.debugMode;
    }

    @Override
    public boolean isHostile() {
        return this.isHostile;
    }

    @Override
    public boolean isNeutral() {
        return this.creatureRetaliates;
    }

    @Override
    public boolean isThreatTo(Entity entity) {
        return isHostile && entity instanceof PlayerEntity;
    }

    @Override
    public Entity getAttackingTarget() {
        return getAttacking();
    }

    @Override
    public boolean isNPC() {
        return false;
    }

    @Override
    public int getGender() {
        return gender;
    }

    @Override
    public float getSize() {
        return getHeight() * getWidth();
    }

    @Override
    public String getSimplyID() {
        return simplyID;
    }

    public boolean isNexusBound() {
        return this.nexusBound;
    }

    @Override
    public boolean isHoldingOntoLadder() {
        return dataTracker.get(CLIMBING);
    }

    public void setIsHoldingIntoLadder(boolean flag) {
        dataTracker.set(CLIMBING, flag);
    }

    @Override
    public boolean isClimbing() {
        return dataTracker.get(CLINGING);
    }

    public boolean readyToRally() {
        return this.rallyCooldown == 0;
    }

    public boolean canSwimHorizontal() {
        return true;
    }

    public boolean canSwimVertical() {
        return true;
    }

    public boolean shouldRenderLabel() {
        return this.shouldRenderLabel;
    }

    @Override
    public void acquiredByNexus(INexusAccess nexus) {
        if ((this.targetNexus == null) && (!this.alwaysIndependent)) {
            this.targetNexus = nexus;
            this.nexusBound = true;
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (getHealth() <= 0 && targetNexus != null) {
            targetNexus.registerMobDied();
        }
    }

    public void setEntityIndependent() {
        this.targetNexus = null;
        this.nexusBound = false;
        this.alwaysIndependent = true;
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        this.collideSize = new CoordsInt(
                MathHelper.floor_double(width + 1.0F),
                MathHelper.floor_double(height + 1.0F),
                MathHelper.floor_double(width + 1.0F)
        );
    }

    public void setBurnsInDay(boolean flag) {
        this.burnsInDay = flag;
    }

    public void setAggroRange(int range) {
        this.aggroRange = range;
    }

    public void setSenseRange(int range) {
        this.senseRange = range;
    }

    @Override
    public void setJumping(boolean jumping) {
        super.setJumping(jumping);
        dataTracker.set(JUMPING, jumping);
    }

    public void setRenderLabel(String label) {
        dataTracker.set(LABEL, label);
    }

    public void setShouldRenderLabel(boolean flag) {
        this.shouldRenderLabel = flag;
    }

    public void setDebugMode(int mode) {
        this.debugMode = mode;
        onDebugChange();
    }

    @Override
    public void mobTick() {
        getNavigatorNew().onUpdateNavigation();
        if (rallyCooldown > 0) {
            rallyCooldown--;
        }
        if (getTarget() != null) {
            currentGoal = Goal.TARGET_ENTITY;
        } else if (targetNexus != null) {
            currentGoal = Goal.BREAK_NEXUS;
        } else {
            currentGoal = Goal.CHILL;
        }
    }

    @Override
    public boolean isAiDisabled() {
        return false;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !nexusBound;
    }

    @Override
    public boolean cannotDespawn() {
        return this.nexusBound || super.cannotDespawn();
    }

    protected void setRotationRoll(float roll) {
        this.rotationRoll = roll;
    }

    public void setRotationYawHeadIM(float yaw) {
        this.rotationYawHeadIM = yaw;
    }

    protected void setRotationPitchHead(float pitch) {
        this.rotationPitchHead = pitch;
    }

    protected void setAttackRange(float range) {
        this.attackRange = range;
    }

    protected void setCurrentTargetPos(IPosition pos) {
        this.currentTargetPos = pos;
    }

    @Override
    protected vstrengthsntity(Entity entity, float f) {
        if ((this.attackTime <= 0) && (f < 2.0F) && (entity.boundingBox.maxY > this.boundingBox.minY)
                && (entity.boundingBox.minY < this.boundingBox.maxY)) {
            this.attackTime = 38;
            tryAttack(entity);
        }
    }

    protected void sunlightDamageTick() {
        setFireTicks(8);
    }

    protected boolean onPathBlocked(Path path, INotifyTask asker) {
        return false;
    }

    @Override
    protected void dealFireDamage(int i) {
        super.dealFireDamage(i * this.flammability);
    }

    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        super.dropLoot(damageSource, causedByPlayer);
        if (random.nextInt(4) == 0) {
            dropStack(InvItems.SMALL_REMNANTS.getDefaultStack(), 0.0F);
        }
    }

    protected float calcBlockPathCost(PathNode prevNode, PathNode node, WorldAccess terrainMap) {
        float multiplier = 1.0F;
        if ((terrainMap instanceof IBlockAccessExtended)) {
            int mobDensity = ((IBlockAccessExtended) terrainMap).getLayeredData(node.xCoord, node.yCoord, node.zCoord)
                    & 0x7;
            multiplier += mobDensity * 3;
        }

        if (node.getYCoord() > prevNode.getYCoord() && getCollide(terrainMap, node.pos) == 2) {
            multiplier += 2.0F;
        }

        if (blockHasLadder(terrainMap, node.pos)) {
            multiplier += 5.0F;
        }

        if (node.action == PathAction.SWIM) {
            multiplier *= ((node.yCoord <= prevNode.yCoord) && !terrainMap.isAir(node.pos) ? 3.0F : 1.0F);
            return prevNode.distanceTo(node) * 1.3F * multiplier;
        }

        Block block = terrainMap.getBlock(node.xCoord, node.yCoord, node.zCoord);
        if (BLOCK_COSTS.containsKey(block)) {
            return prevNode.distanceTo(node) * BLOCK_COSTS.get(block).floatValue() * multiplier;
        }
        if (block.isCollidable()) {
            return prevNode.distanceTo(node) * 3.2F * multiplier;
        }

        return prevNode.distanceTo(node) * 1.0F * multiplier;
    }

    protected void calcPathOptions(WorldAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        if ((currentNode.yCoord <= 0) || (currentNode.yCoord > 255)) {
            return;
        }

        calcPathOptionsVertical(terrainMap, currentNode, pathFinder);

        if ((currentNode.action == PathAction.DIG)
                && (!canStandAt(terrainMap, currentNode.xCoord, currentNode.yCoord, currentNode.zCoord))) {
            return;
        }

        int height = getJumpHeight();
        for (int i = 1; i <= height; i++) {
            if (getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord + i, currentNode.zCoord) == 0) {
                height = i - 1;
            }
        }

        int maxFall = 8;
        for (int i = 0; i < 4; i++) {
            if (currentNode.action != PathAction.NONE) {
                if ((i == 0) && (currentNode.action == PathAction.LADDER_UP_NX)) {
                    height = 0;
                }
                if ((i == 1) && (currentNode.action == PathAction.LADDER_UP_PX)) {
                    height = 0;
                }
                if ((i == 2) && (currentNode.action == PathAction.LADDER_UP_NZ)) {
                    height = 0;
                }
                if ((i == 3) && (currentNode.action == PathAction.LADDER_UP_PZ)) {
                    height = 0;
                }
            }
            int yOffset = 0;
            int currentY = currentNode.yCoord + height;
            boolean passedLevel = false;
            do {
                yOffset = getNextLowestSafeYOffset(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentY,
                        currentNode.zCoord + CoordsInt.offsetAdjZ[i], maxFall + currentY - currentNode.yCoord);
                if (yOffset > 0)
                    break;
                if (yOffset > -maxFall) {
                    pathFinder.addNode(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentY + yOffset,
                            currentNode.zCoord + CoordsInt.offsetAdjZ[i], PathAction.NONE);
                }

                currentY += yOffset - 1;

                if ((!passedLevel) && (currentY <= currentNode.yCoord)) {
                    passedLevel = true;
                    if (currentY != currentNode.yCoord) {
                        addAdjacent(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord,
                                currentNode.zCoord + CoordsInt.offsetAdjZ[i], currentNode, pathFinder);
                    }

                }

            }

            while (currentY >= currentNode.yCoord);
        }

        if (canSwimHorizontal()) {
            for (int i = 0; i < 4; i++) {
                if (getCollide(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord,
                        currentNode.zCoord + CoordsInt.offsetAdjZ[i]) == -1)
                    pathFinder.addNode(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord,
                            currentNode.zCoord + CoordsInt.offsetAdjZ[i], PathAction.SWIM);
            }
        }
    }

    protected void calcPathOptionsVertical(WorldAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        int collideUp = getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord);
        if (collideUp > 0) {
            if (terrainMap.getBlock(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord) == Blocks.ladder) {
                int meta = terrainMap.getBlockMetadata(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord);
                PathAction action = PathAction.NONE;
                if (meta == 4)
                    action = PathAction.LADDER_UP_PX;
                else if (meta == 5)
                    action = PathAction.LADDER_UP_NX;
                else if (meta == 2)
                    action = PathAction.LADDER_UP_PZ;
                else if (meta == 3) {
                    action = PathAction.LADDER_UP_NZ;
                }

                if (currentNode.action == PathAction.NONE) {
                    pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, action);
                } else if ((currentNode.action == PathAction.LADDER_UP_PX)
                        || (currentNode.action == PathAction.LADDER_UP_NX)
                        || (currentNode.action == PathAction.LADDER_UP_PZ)
                        || (currentNode.action == PathAction.LADDER_UP_NZ)) {
                    if (action == currentNode.action) {
                        pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, action);
                    }
                } else {
                    pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, action);
                }
            } else if (getCanClimb()) {
                if (isAdjacentSolidBlock(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord)) {
                    pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.NONE);
                }
            }
        }
        int below = getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord);
        int above = getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord);
        if (getCanDigDown()) {
            if (below == 2) {
                pathFinder.addNode(currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord, PathAction.DIG);
            } else if (below == 1) {
                int maxFall = 5;
                int yOffset = getNextLowestSafeYOffset(terrainMap, currentNode.xCoord, currentNode.yCoord - 1,
                        currentNode.zCoord, maxFall);
                if (yOffset <= 0) {
                    pathFinder.addNode(currentNode.xCoord, currentNode.yCoord - 1 + yOffset, currentNode.zCoord,
                            PathAction.NONE);
                }
            }
        }

        if (canSwimVertical()) {
            if (below == -1) {
                pathFinder.addNode(currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord, PathAction.SWIM);
            }
            if (above == -1)
                pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.SWIM);
        }
    }

    protected void addAdjacent(WorldAccess terrainMap, int x, int y, int z, PathNode currentNode,
            PathfinderIM pathFinder) {
        if (getCollide(terrainMap, x, y, z) <= 0) {
            return;
        }
        if (getCanClimb()) {
            if (isAdjacentSolidBlock(terrainMap, x, y, z))
                pathFinder.addNode(x, y, z, PathAction.NONE);
        } else if (terrainMap.getBlock(x, y, z) == Blocks.ladder) {
            pathFinder.addNode(x, y, z, PathAction.NONE);
        }
    }

    protected boolean isAdjacentSolidBlock(WorldAccess terrainMap, int x, int y, int z) {
        if ((this.collideSize.getXCoord() == 1) && (this.collideSize.getZCoord() == 1)) {
            for (int i = 0; i < 4; i++) {
                Block block = terrainMap.getBlock(x + CoordsInt.offsetAdjX[i], y, z + CoordsInt.offsetAdjZ[i]);
                if ((block != Blocks.air) && (block.getMaterial().isSolid()))
                    return true;
            }
        } else if ((this.collideSize.getXCoord() == 2) && (this.collideSize.getZCoord() == 2)) {
            for (int i = 0; i < 8; i++) {
                Block block = terrainMap.getBlock(x + CoordsInt.offsetAdj2X[i], y, z + CoordsInt.offsetAdj2Z[i]);
                if ((block != Blocks.air) && (block.getMaterial().isSolid()))
                    return true;
            }
        }
        return false;
    }

    protected int getNextLowestSafeYOffset(BlockView world, BlockPos pos, int maxOffsetMagnitude) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int i = 0; i + pos.getY() > world.getBottomY() && i < maxOffsetMagnitude; i--) {
            mutable.set(pos).move(Direction.UP, i);
            if (canStandAtAndIsValid(world, mutable) || (canSwimHorizontal() && getCollide(world, mutable) == -1)) {
                return i;
            }
        }
        return 1;
    }

    protected boolean canStandAt(BlockView world, BlockPos pos) {
        boolean isSolidBlock = false;
        pos = pos.down();
        for (BlockPos p : BlockPos.iterate(pos, pos.add(collideSize.toBlockPos()))) {
            BlockState state = world.getBlockState(p);
            if (!state.isAir()) {
                if (!state.blocksMovement()) {
                    isSolidBlock = true;
                } else if (avoidsBlock(state.getBlock())) {
                    return false;
                }
            }
        }
        return isSolidBlock;
    }

    protected boolean canStandAtAndIsValid(BlockView world, BlockPos pos) {
        return getCollide(world, pos) > 0 && canStandAt(world, pos);
    }

    @SuppressWarnings("deprecation")
    protected boolean canStandOnBlock(BlockView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.isAir() && state.hasSolidTopSurface(world, pos, this) && !state.blocksMovement() && !avoidsBlock(state.getBlock());
    }

    protected boolean blockHasLadder(BlockView world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int i = 0; i < 4; i++) {
            mutable.set(pos.getX() + CoordsInt.offsetAdjX[i], pos.getY(), pos.getZ() + CoordsInt.offsetAdjZ[i]);
            if (world.getBlockState(mutable).isIn(BlockTags.CLIMBABLE)) {
                return true;
            }
        }
        return false;
    }

    protected int getCollide(BlockView terrainMap, BlockPos pos) {
        boolean destructibleFlag = false;
        boolean liquidFlag = false;

        for (BlockPos p : BlockPos.iterate(pos, this.getCollideSize().toBlockPos().add(pos))) {
            BlockState block = terrainMap.getBlockState(p);
            if (!block.isAir()) {
                if (block.isLiquid()) {
                    liquidFlag = true;
                } else if (!block.blocksMovement()) {
                    if (!isBlockDestructible(terrainMap, p, block)) {
                        return 0;
                    }
                    destructibleFlag = true;
                } else if (terrainMap.getBlockState(p.down()).isIn(BlockTags.WOODEN_FENCES)) {
                    if (isBlockDestructible(terrainMap, pos, Blocks.OAK_FENCE)) {
                        return 3;
                    }
                    return 0;
                }

                if (avoidsBlock(block)) {
                    return -2;
                }
            }
        }

        if (destructibleFlag) {
            return 2;
        }
        if (liquidFlag) {
            return -1;
        }
        return 1;
    }

    protected boolean getLightLevelBelow8() {
        BlockPos pos = getBlockPos();
        if (getWorld().getLightLevel(LightType.SKY, pos) > random.nextInt(32)) {
            return false;
        }

        return getWorld().getLightLevel(LightType.BLOCK, pos) <= random.nextInt(8);
    }

    protected void setAIGoal(Goal goal) {
        this.currentGoal = goal;
    }

    protected void setPrevAIGoal(Goal goal) {
        this.prevGoal = goal;
    }

    public void transitionAIGoal(Goal newGoal) {
        this.prevGoal = this.currentGoal;
        this.currentGoal = newGoal;
    }

    public MoveState getMoveState() {
        return MoveState.of(dataTracker.get(MOVE_STATE));
    }

    protected void setMoveState(MoveState moveState) {
        dataTracker.set(MOVE_STATE, moveState.ordinal());
    }

    protected void setDestructiveness(int x) {
        destructiveness = x;
    }

    protected void setGravity(float acceleration) {
        gravityAcel = acceleration;
    }

    protected void setGroundFriction(float frictionCoefficient) {
        groundFriction = frictionCoefficient;
    }

    protected void setCanClimb(boolean flag) {
        canClimb = flag;
    }

    protected void setJumpHeight(int height) {
        jumpHeight = height;
    }

    protected void setBaseMoveSpeedStat(float speed) {
        moveSpeedBase = speed;
        this.moveSpeed = speed;
    }

    public void setMoveSpeedStat(float speed) {
        this.moveSpeed = speed;
        getNavigatorNew().setSpeed(speed);
        typeser().setMoveSpeed(speed);
    }

    public void resetMoveSpeed() {
        setMoveSpeedStat(this.moveSpeedBase);
        getNavigatorNew().setSpeed(this.moveSpeedBase);
    }

    public void setTurnRate(float rate) {
        this.turnRate = rate;
    }

    protected void setName(String name) {
        setCustomName(Text.literal(name));
    }

    protected void setGender(int gender) {
        this.gender = gender;
    }

    protected void onDebugChange() {
    }

    public static int getBlockType(Block block) {
        return BLOCK_TYPES.getOrDefault(block, 0);
    }

    public static float getBlockStrength(BlockPos p, BlockState state, WorldView world) {
        BlockSpecial special = SPECIAL_BLOCKS.getOrDefault(state.getBlock(), BlockSpecial.NONE);
        int bonus = 0;
        BlockPos.Mutable pos = p.mutableCopy();
        float strength = BLOCK_STRENGTHS.getOrDefault(state.getBlock(), DEFAULT_SOFT_STRENGTH);
        switch (special) {
            case CONSTRUCTION_1:
                for (Direction direction : Direction.values()) {
                    if (world.getBlockState(pos.set(p).move(direction)).isOf(state.getBlock())) {
                        bonus++;
                    }
                }
                break;
            case CONSTRUCTION_STONE:
                for (Direction direction : Direction.values()) {
                    BlockState s = world.getBlockState(pos.set(p).move(direction));
                    if (s.isOf(Blocks.STONE)
                            || s.isOf(Blocks.COBBLESTONE)
                            || s.isOf(Blocks.MOSSY_COBBLESTONE)
                            || s.isOf(Blocks.STONE_BRICKS)) {
                        bonus++;
                    }
                }
                break;
            default:
        }
        return strength * (1 + bonus * 0.1F);
    }

    @Deprecated
    public static float getBlockStrength(int x, int y, int z, Block block, WorldView world) {
        return getBlockStrength(new BlockPos(x, y, z), block.getDefaultState(), world);
    }

    public static void putBlockStrength(Block block, float strength) {
        BLOCK_STRENGTHS.put(block, strength);
    }

    public static void putBlockCost(Block block, float cost) {
        BLOCK_COSTS.put(block, cost);
    }
}