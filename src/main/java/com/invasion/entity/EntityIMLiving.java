package com.invasion.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.invasion.ConfigInvasion;
import com.invasion.IBlockAccessExtended;
import com.invasion.INotifyTask;
import com.invasion.IPathfindable;
import com.invasion.InvasionMod;
import com.invasion.block.BlockSpecial;
import com.invasion.block.DestructableType;
import com.invasion.entity.ai.Goal;
import com.invasion.entity.ai.IMMoveHelper;
import com.invasion.entity.ai.MoveState;
import com.invasion.entity.pathfinding.INavigation;
import com.invasion.entity.pathfinding.IPathSource;
import com.invasion.entity.pathfinding.NavigatorIM;
import com.invasion.entity.pathfinding.Path;
import com.invasion.entity.pathfinding.PathAction;
import com.invasion.entity.pathfinding.PathCreator;
import com.invasion.entity.pathfinding.PathNavigateAdapter;
import com.invasion.entity.pathfinding.PathNode;
import com.invasion.entity.pathfinding.PathfinderIM;
import com.invasion.item.InvItems;
import com.invasion.nexus.EntityConstruct;
import com.invasion.nexus.INexusAccess;
import com.invasion.nexus.EntityConstruct.BuildableMob;
import com.invasion.util.math.CoordsInt;
import com.invasion.util.math.Distance;
import com.invasion.util.math.IPosition;
import com.invasion.util.math.MathUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class EntityIMLiving extends HostileEntity implements IPathfindable, IPosition, IHasNexus, IHasAiGoals, BuildableMob {
    protected static final float DEFAULT_SOFT_STRENGTH = 2.5F;
    protected static final float DEFAULT_HARD_STRENGTH = 5.5F;
    protected static final float DEFAULT_SOFT_COST = 2;
    protected static final float DEFAULT_HARD_COST = 3.2F;
    protected static final float AIR_BASE_COST = 1;
    private static final Map<Block, Float> BLOCK_COSTS = Util.make(new HashMap<>(), costs -> {
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

    public static final float DEFAULT_AIR_RESISTANCE = 0.9995F;
    public static final float DEFAULT_GROUND_FRICTION = 0.546F;
    public static final float DEFAULT_BASE_MOVEMENT_SPEED = 0.26F;

    private INavigation newNavigation;
    private IPathSource pathSource;

    protected Goal currentGoal = Goal.NONE;
    protected Goal prevGoal = Goal.NONE;

    private float rotationRoll;
    private float prevRotationRoll;

    protected float airResistance = DEFAULT_AIR_RESISTANCE;
    private float groundFriction = DEFAULT_GROUND_FRICTION;
    private float moveSpeedBase = DEFAULT_BASE_MOVEMENT_SPEED;

    private float turnRate = 30;
    private float pitchRate = 2;

    private int rallyCooldown;

    private IPosition currentTargetPos = new CoordsInt(0, 0, 0);
    private IPosition lastBreathExtendPos = new CoordsInt(0, 0, 0);

    private boolean isHostile = true;
    private boolean creatureRetaliates = true;

    @Nullable
    private INexusAccess targetNexus;

    protected int selfDamage = 2;
    protected int maxSelfDamage = 6;
    @Deprecated
    protected int maxDestructiveness;
    protected float blockRemoveSpeed = 1.0F;
    protected boolean floatsInWater = true;

    private CoordsInt collideSize = new CoordsInt(
            MathHelper.ceil(getWidth()),
            MathHelper.ceil(getHeight()),
            MathHelper.ceil(getWidth())
    );

    private boolean canClimb;
    private boolean canDig = true;

    private boolean alwaysIndependent;
    private boolean burnsInDay;
    private boolean fireImmune;

    private int aggroRange;
    private int senseRange;
    private int stunTimer;
    protected int throttled;
    protected int throttled2;
    protected int pathThrottle;
    protected int destructionTimer;
    protected int flammability = 2;
    private boolean canMin;

    @Nullable
    protected Entity j;

    public EntityIMLiving(EntityType<? extends EntityIMLiving> type, World world, @Nullable INexusAccess nexus) {
        super(type, world);
        moveControl = new IMMoveHelper(this);
        setNexus(nexus);
        setAttackStrength(2);
        setMovementSpeed(0.26F);
        setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new PathNavigateAdapter(this, world, getNavigatorNew());
    }

    public final INavigation getNavigatorNew() {
        if (newNavigation == null) {
            newNavigation = createIMNavigation(getPathSource());
        }
        return newNavigation;
    }

    protected INavigation createIMNavigation(IPathSource pathSource) {
        return new NavigatorIM(this, pathSource);
    }

    public final IPathSource getPathSource() {
        if (pathSource == null) {
            pathSource = createPathSource();
        }
        return this.pathSource;
    }

    protected IPathSource createPathSource() {
        return new PathCreator(700, 50);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(MOVE_STATE, MoveState.STANDING.ordinal());
        builder.add(JUMPING, false);
        builder.add(CLIMBING, false);
        builder.add(CLINGING, false);
        builder.add(ANGLES, MathUtil.packAnglesDeg(getBodyYaw(), getHeadYaw(), getPitch(), 0));
        builder.add(LABEL, "");
    }

    @Override
    public void setNexus(@Nullable INexusAccess nexus) {
        targetNexus = nexus;
        burnsInDay = nexus != null && InvasionMod.getConfig().nightMobsBurnInDay;
        aggroRange = nexus != null ? 12 : InvasionMod.getConfig().nightMobSightRange;
        senseRange = nexus != null ? 6 : InvasionMod.getConfig().nightMobSenseRange;
    }

    @Override
    public boolean isAlwaysIndependant() {
        return alwaysIndependent;
    }

    @Override
    public void setEntityIndependent() {
        setNexus(null);
        alwaysIndependent = true;
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
        if (!getWorld().isClient) {
            int packedAngles = MathUtil.packAnglesDeg(getBodyYaw(), getHeadYaw(), getPitch(), 0);
            if (packedAngles != dataTracker.get(ANGLES)) {
                dataTracker.set(ANGLES, packedAngles);
            }
        }
    }

    @Override
    public void onSpawned(@Nullable INexusAccess nexus, EntityConstruct spawnConditions) {
        setNexus(nexus);
        if (nexus == null) {
            setEntityIndependent();
        }
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
        if (!hasNexus()) {
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
            setBodyYaw(MathUtil.unpackAnglesDeg_1(packedAngles));
            setHeadYaw(MathUtil.unpackAnglesDeg_2(packedAngles));
            setPitch(MathUtil.unpackAnglesDeg_3(packedAngles));
        }
        if (data == JUMPING) {
            super.setJumping(dataTracker.get(JUMPING));
        }
    }

    @Override
    public boolean damage(DamageSource source, float damage) {
        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            damage *= flammability;
        }

        if (super.damage(source, damage)) {
            @Nullable
            Entity attacker = source.getAttacker();
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
            double friction = (getGroundFriction() * airResistance) / 0.91D; // divide by initial friction, then multiply by our new friction
            return movement.multiply(friction, airResistance, friction);
        }
        return movement.multiply(airResistance);
    }

    public void rally(ILeader leader) {
        rallyCooldown = 300;
    }

    public void onFollowingEntity(Entity entity) {
    }

    public void onPathSet() {
    }

    public void onBlockRemoved(int x, int y, int z, int id) {
        if (getHealth() > getMaxHealth() - maxSelfDamage) {
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

    @Deprecated
    public boolean avoidsBlock(int block) {
        return false;
    }

    public boolean avoidsBlock(BlockState state) {
        return !isInvulnerable()
                && (!isFireImmune() && (state.isIn(BlockTags.FIRE)
                        || state.isIn(BlockTags.CAMPFIRES)
                        || state.getFluidState().isIn(FluidTags.LAVA))
                || state.isOf(Blocks.BEDROCK)
                || state.isOf(Blocks.CACTUS));
    }

    public boolean isBlockDestructible(BlockView world, BlockPos pos, BlockState state) {
        if (state.getHardness(world, pos) < 0 || state.isOf(Blocks.COMMAND_BLOCK) || state.isOf(Blocks.CHAIN_COMMAND_BLOCK) || state.isOf(Blocks.REPEATING_COMMAND_BLOCK)) {
            return false;
        }
        if (state.isAir() || !canDestroyBlocks() || UNDESTRUCTABLE_BLOCKS.contains(state.getBlock()) || blockHasLadder(world, pos)) {
            return false;
        }
        return state.isIn(BlockTags.DOORS) || state.isIn(BlockTags.TRAPDOORS) || state.isSolidBlock(world, pos);
    }

    @Override
    public boolean canSee(Entity entity) {
        float distance = distanceTo(entity);
        return distance <= getSenseRange() || (super.canSee(entity) && distance <= getAggroRange());
    }

    @Override
    public double findDistanceToNexus() {
        if (!hasNexus()) {
            return Double.MAX_VALUE;
        }
        return Math.sqrt(getNexus().toBlockPos().toCenterPos().squaredDistanceTo(getX(), getBodyY(0.5), getZ()));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putBoolean("alwaysIndependent", alwaysIndependent);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        alwaysIndependent = compound.getBoolean("alwaysIndependent");
        if (alwaysIndependent) {
            ConfigInvasion config = InvasionMod.getConfig();
            setAggroRange(config.nightMobSightRange);
            setSenseRange(config.nightMobSenseRange);
            setBurnsInDay(config.nightMobsBurnInDay);
        }
    }

    public float getPrevRotationRoll() {
        return prevRotationRoll;
    }

    public float getRotationRoll() {
        return rotationRoll;
    }

    public void setRotationRoll(float roll) {
        this.rotationRoll = roll;
    }

    @Deprecated
    public float getPrevRotationYawHeadIM() {
        return prevHeadYaw;
    }

    @Deprecated
    public float getRotationYawHeadIM() {
        return getHeadYaw();
    }

    @Deprecated
    public float getPrevRotationPitchHead() {
        return prevPitch;
    }

    @Deprecated
    public float getRotationPitchHead() {
        return getPitch();
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

    @Deprecated
    public float getAttackRange() {
        return 0;
    }

    public void setMaxHealth(float health) {
        getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(health);
    }

    public void setMaxHealthAndHealth(float health) {
        setMaxHealth(health);
        setHealth(health);
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return canSpawn(world) && (hasNexus() || getLightLevelBelow8()) && getWorld().isTopSolid(getBlockPos(), this);
    }

    public float getBaseMoveSpeedStat() {
        return moveSpeedBase;
    }

    @Deprecated
    protected void setJumpHeight(int height) {
        getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue(height);
    }

    public float getBlockStrength(BlockPos pos) {
        return getBlockStrength(pos, getWorld().getBlockState(pos));
    }

    public float getBlockStrength(BlockPos pos, BlockState state) {
        return getBlockStrength(pos, state, getWorld());
    }

    public boolean getCanClimb() {
        return canClimb;
    }

    protected void setCanClimb(boolean flag) {
        canClimb = flag;
    }

    public boolean getCanDigDown() {
        return canDig;
    }

    public int getAggroRange() {
        return aggroRange;
    }

    public void setAggroRange(int range) {
        this.aggroRange = range;
    }

    public int getSenseRange() {
        return senseRange;
    }

    public void setSenseRange(int range) {
        this.senseRange = range;
    }

    public boolean getBurnsInDay() {
        return burnsInDay;
    }

    public void setBurnsInDay(boolean flag) {
        this.burnsInDay = flag;
    }

    public boolean canDestroyBlocks() {
        return canMin && getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
    }

    protected void setCanDestroyBlocks(boolean flag) {
        canMin = flag;
    }


    public float getTurnRate() {
        return turnRate;
    }

    public float getPitchRate() {
        return pitchRate;
    }

    protected void setGravity(float acceleration) {
        getAttributeInstance(EntityAttributes.GENERIC_GRAVITY).setBaseValue(acceleration);
    }

    public float getGroundFriction() {
        return groundFriction;
    }

    public CoordsInt getCollideSize() {
        return collideSize;
    }

    @Override
    public Goal getAIGoal() {
        return currentGoal;
    }

    @Override
    public Goal getPrevAIGoal() {
        return prevGoal;
    }

    @Override
    public Goal transitionAIGoal(Goal newGoal) {
        prevGoal = currentGoal;
        currentGoal = newGoal;
        return newGoal;
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return hasNexus() ? 0 : 0.5F - world.getLightLevel(pos);
    }

    public IPosition getCurrentTargetPos() {
        return currentTargetPos;
    }

    public void setCurrentTargetPos(IPosition pos) {
        currentTargetPos = pos;
    }

    @Override
    public INexusAccess getNexus() {
        return this.targetNexus;
    }

    public String getRenderLabel() {
        return dataTracker.get(LABEL);
    }

    public final boolean getDebugMode() {
        return InvasionMod.getConfig().debugMode;
    }

    @Deprecated
    public final boolean isHostile() {
        return this.isHostile;
    }

    @Deprecated
    public final boolean isNeutral() {
        return this.creatureRetaliates;
    }

    @Deprecated
    public boolean isThreatTo(Entity entity) {
        return isHostile && entity instanceof PlayerEntity;
    }

    public float getSize() {
        return getHeight() * getWidth();
    }

    public int getTier() {
        return 1;
    }

    @Override
    public boolean isHoldingOntoLadder() {
        return dataTracker.get(CLINGING);
    }

    public void setIsHoldingIntoLadder(boolean flag) {
        dataTracker.set(CLINGING, flag);
    }

    @Override
    public boolean isClimbing() {
        return dataTracker.get(CLIMBING);
    }

    protected void setClimbing(boolean climbing) {
        dataTracker.set(CLIMBING, climbing);
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

    @Override
    public boolean shouldRenderName() {
        return InvasionMod.getConfig().debugMode || super.shouldRenderName();
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (getHealth() <= 0 && getNexus() != null) {
            getNexus().registerMobDied();
        }
    }

    @Override
    public boolean recalculateDimensions(EntityDimensions previous) {
        boolean result = super.recalculateDimensions(previous);
        collideSize = new CoordsInt(getBlockPos().add(1, 1, 1));
        return result;
    }

    @Override
    public void setJumping(boolean jumping) {
        super.setJumping(jumping);
        dataTracker.set(JUMPING, jumping);
    }

    @Deprecated
    public void setRenderLabel(String label) {
        dataTracker.set(LABEL, label);
    }

    @Override
    protected void mobTick() {
        getNavigatorNew().onUpdateNavigation();
        if (rallyCooldown > 0) {
            rallyCooldown--;
        }
        if (getTarget() != null) {
            currentGoal = Goal.TARGET_ENTITY;
        } else if (getNexus() != null) {
            currentGoal = Goal.BREAK_NEXUS;
        } else {
            currentGoal = Goal.CHILL;
        }
    }

    @Override
    public final boolean canImmediatelyDespawn(double distanceSquared) {
        return !hasNexus();
    }

    @Override
    public final boolean cannotDespawn() {
        return hasNexus() || super.cannotDespawn();
    }

    @Override
    public boolean isFireImmune() {
        return fireImmune || super.isFireImmune();
    }

    protected void setFireImmune(boolean fireImmune) {
        this.fireImmune = fireImmune;
    }

    protected void sunlightDamageTick() {
        setFireTicks(8);
    }

    public boolean onPathBlocked(Path path, INotifyTask asker) {
        return false;
    }

    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        super.dropLoot(damageSource, causedByPlayer);
        if (random.nextInt(4) == 0) {
            dropStack(InvItems.SMALL_REMNANTS.getDefaultStack(), 0);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getBlockPathCost(PathNode prevNode, PathNode node, BlockView terrainMap) {
        float multiplier = 1 + ((IBlockAccessExtended.getData(terrainMap, node.pos) & IBlockAccessExtended.MOB_DENSITY_FLAG) * 3);

        if (node.getYCoord() > prevNode.getYCoord() && getCollide(terrainMap, node.pos) == DestructableType.DESTRUCTABLE) {
            multiplier += 2;
        }

        if (blockHasLadder(terrainMap, node.pos)) {
            multiplier += 5;
        }

        if (node.action == PathAction.SWIM) {
            multiplier *= ((node.getYCoord() <= prevNode.getYCoord()) && !terrainMap.getBlockState(node.pos).isAir() ? 3 : 1);
            return prevNode.distanceTo(node) * 1.3F * multiplier;
        }

        BlockState block = terrainMap.getBlockState(node.pos);
        return prevNode.distanceTo(node) * getBlockCost(block).orElse(block.isSolid() ? 3.2F : 1) * multiplier;
    }

    @Override
    public void getPathOptionsFromNode(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        if (getWorld().isOutOfHeightLimit(currentNode.getYCoord())) {
            return;
        }

        calcPathOptionsVertical(terrainMap, currentNode, pathFinder);

        if (currentNode.action == PathAction.DIG && !canStandAt(terrainMap, currentNode.pos)) {
            return;
        }

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int height = MathHelper.ceil(getStepHeight());
        for (int i = 1; i <= height; i++) {
            if (getCollide(terrainMap, mutable.set(currentNode.pos).move(Direction.UP, i)) == DestructableType.UNBREAKABLE) {
                height = i - 1;
            }
        }

        int maxFall = 8;
        for (Direction facing : CoordsInt.CARDINAL_DIRECTIONS) {
            if (currentNode.action != PathAction.NONE) {
                if (facing == Direction.EAST && currentNode.action == PathAction.LADDER_UP_NX) {
                    height = 0;
                }
                if (facing == Direction.WEST && currentNode.action == PathAction.LADDER_UP_PX) {
                    height = 0;
                }
                if (facing == Direction.SOUTH && currentNode.action == PathAction.LADDER_UP_NZ) {
                    height = 0;
                }
                if (facing == Direction.NORTH && currentNode.action == PathAction.LADDER_UP_PZ) {
                    height = 0;
                }
            }
            int currentY = currentNode.getYCoord() + height;
            boolean passedLevel = false;
            do {
                int yOffset = getNextLowestSafeYOffset(terrainMap,
                        mutable.set(currentNode.pos).setY(currentY).move(facing),
                        maxFall + currentY - currentNode.getYCoord()
                );
                if (yOffset > 0) {
                    break;
                }
                if (yOffset > -maxFall) {
                    pathFinder.addNode(mutable.move(Direction.UP, yOffset).toImmutable(), PathAction.NONE);
                }

                currentY += yOffset - 1;

                if ((!passedLevel) && (currentY <= currentNode.getYCoord())) {
                    passedLevel = true;
                    if (currentY != currentNode.getYCoord()) {
                        addAdjacent(terrainMap, currentNode.pos.offset(facing), currentNode, pathFinder);
                    }

                }

            } while (currentY >= currentNode.getYCoord());
        }

        if (canSwimHorizontal()) {
            for (Direction offset : CoordsInt.CARDINAL_DIRECTIONS) {
                if (getCollide(terrainMap, mutable.set(currentNode.pos).move(offset)) == -1) {
                    pathFinder.addNode(mutable.toImmutable(), PathAction.SWIM);
                }
            }
        }
    }

    protected void calcPathOptionsVertical(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        int collideUp = getCollide(terrainMap, currentNode.pos.up());
        if (collideUp > DestructableType.UNBREAKABLE) {
            BlockState state = terrainMap.getBlockState(currentNode.pos.up());
            if (state.isIn(BlockTags.CLIMBABLE)) {
                Direction facing = state.getOrEmpty(HorizontalFacingBlock.FACING).orElse(null);
                PathAction action = PathAction.getLadderActionForDirection(facing);

                if (currentNode.action == PathAction.NONE) {
                    pathFinder.addNode(currentNode.pos.up(), action);
                } else if (currentNode.action.getType() == PathAction.Type.LADDER && currentNode.action.getBuildDirection() != Direction.UP) {
                    if (action == currentNode.action) {
                        pathFinder.addNode(currentNode.pos.up(), action);
                    }
                } else {
                    pathFinder.addNode(currentNode.pos.up(), action);
                }
            } else if (getCanClimb()) {
                if (isAdjacentSolidBlock(terrainMap, currentNode.pos.up())) {
                    pathFinder.addNode(currentNode.pos.up(), PathAction.NONE);
                }
            }
        }
        int below = getCollide(terrainMap, currentNode.pos.down());
        int above = getCollide(terrainMap, currentNode.pos.up());
        if (getCanDigDown()) {
            if (below == DestructableType.DESTRUCTABLE) {
                pathFinder.addNode(currentNode.pos.down(), PathAction.DIG);
            } else if (below == DestructableType.TERRAIN) {
                int yOffset = getNextLowestSafeYOffset(terrainMap, currentNode.pos.down(), 5);
                if (yOffset <= 0) {
                    pathFinder.addNode(currentNode.pos.up(yOffset - 1), PathAction.NONE);
                }
            }
        }

        if (canSwimVertical()) {
            if (below == -1) {
                pathFinder.addNode(currentNode.pos.down(), PathAction.SWIM);
            }
            if (above == -1) {
                pathFinder.addNode(currentNode.pos.up(), PathAction.SWIM);
            }
        }
    }

    protected final void addAdjacent(BlockView terrainMap, BlockPos pos, PathNode currentNode, PathfinderIM pathFinder) {
        if (getCollide(terrainMap, pos) <= DestructableType.UNBREAKABLE) {
            return;
        }
        if (getCanClimb()) {
            if (isAdjacentSolidBlock(terrainMap, pos)) {
                pathFinder.addNode(pos, PathAction.NONE);
            }
        } else if (terrainMap.getBlockState(pos).isIn(BlockTags.CLIMBABLE)) {
            pathFinder.addNode(pos, PathAction.NONE);
        }
    }

    protected final boolean isAdjacentSolidBlock(BlockView terrainMap, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Vec3i offset
                : collideSize.getXCoord() == 1 && collideSize.getZCoord() == 1 ? CoordsInt.OFFSET_ADJACENT
                : collideSize.getXCoord() == 2 && collideSize.getZCoord() == 2 ? CoordsInt.OFFSET_ADJACENT_2
                : CoordsInt.ZERO) {
            BlockState state = terrainMap.getBlockState(mutable.set(pos).add(offset));
            if (!state.isAir() && state.isSolidBlock(terrainMap, mutable)) {
                return true;
            }
        }
        return false;
    }

    protected final int getNextLowestSafeYOffset(BlockView world, BlockPos pos, int maxOffsetMagnitude) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int i = 0; i + pos.getY() > world.getBottomY() && i < maxOffsetMagnitude; i--) {
            mutable.set(pos).move(Direction.UP, i);
            if (canStandAtAndIsValid(world, mutable) || (canSwimHorizontal() && getCollide(world, mutable) == -1)) {
                return i;
            }
        }
        return 1;
    }

    @SuppressWarnings("deprecation")
    public final boolean canStandAt(BlockView world, BlockPos pos) {
        boolean isSolidBlock = false;
        pos = pos.down();
        for (BlockPos p : BlockPos.iterate(pos, pos.add(collideSize.toBlockPos()))) {
            BlockState state = world.getBlockState(p);
            if (!state.isAir()) {
                if (!state.blocksMovement()) {
                    isSolidBlock = true;
                } else if (avoidsBlock(state)) {
                    return false;
                }
            }
        }
        return isSolidBlock;
    }

    @Override
    public final BlockPos toBlockPos() {
        return getBlockPos();
    }

    public boolean canStandAtAndIsValid(BlockView world, BlockPos pos) {
        return getCollide(world, pos) > DestructableType.UNBREAKABLE && canStandAt(world, pos);
    }

    @SuppressWarnings("deprecation")
    protected boolean canStandOnBlock(BlockView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.isAir() && state.hasSolidTopSurface(world, pos, this) && !state.blocksMovement() && !avoidsBlock(state);
    }

    protected boolean blockHasLadder(BlockView world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Direction offset : CoordsInt.CARDINAL_DIRECTIONS) {
            if (world.getBlockState(mutable.set(pos).move(offset)).isIn(BlockTags.CLIMBABLE)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    protected final int getCollide(BlockView terrainMap, BlockPos pos) {
        boolean destructibleFlag = false;
        boolean liquidFlag = false;

        for (BlockPos p : BlockPos.iterate(pos, getCollideSize().toBlockPos().add(pos))) {
            BlockState state = terrainMap.getBlockState(p);
            if (!state.isAir()) {
                if (state.isLiquid()) {
                    liquidFlag = true;
                } else if (!state.blocksMovement()) {
                    if (!isBlockDestructible(terrainMap, p, state)) {
                        return DestructableType.UNBREAKABLE;
                    }
                    destructibleFlag = true;
                } else {
                    state = terrainMap.getBlockState(p.down());
                    if (state.isIn(BlockTags.WOODEN_FENCES)) {
                        return isBlockDestructible(terrainMap, pos, state) ? DestructableType.BREAKABLE_BARRIER : DestructableType.UNBREAKABLE;
                    }
                }

                if (avoidsBlock(state)) {
                    return DestructableType.REPELLANT;
                }
            }
        }
        return destructibleFlag ? DestructableType.DESTRUCTABLE : liquidFlag ? DestructableType.FLUID : DestructableType.TERRAIN;
    }

    protected boolean getLightLevelBelow8() {
        BlockPos pos = getBlockPos();
        return getWorld().getLightLevel(LightType.SKY, pos) <= random.nextInt(32)
            && getWorld().getLightLevel(LightType.BLOCK, pos) <= random.nextInt(8);
    }

    public MoveState getMoveState() {
        return MoveState.of(dataTracker.get(MOVE_STATE));
    }

    public void setMoveState(MoveState moveState) {
        dataTracker.set(MOVE_STATE, moveState.ordinal());
    }

    public void setGroundFriction(float frictionCoefficient) {
        groundFriction = frictionCoefficient;
    }

    @Deprecated
    protected void setBaseMoveSpeedStat(float speed) {
        moveSpeedBase = speed;
        setMovementSpeed(speed);
    }

    @Deprecated
    public void setMoveSpeedStat(float speed) {
        setMovementSpeed(speed);
    }

    @Override
    public void setMovementSpeed(float movementSpeed) {
        super.setMovementSpeed(movementSpeed);
        getNavigatorNew().setSpeed(speed);
    }

    public void setTurnRate(float rate) {
        this.turnRate = rate;
    }

    @Deprecated
    protected void setName(String name) {
    }

    protected void onDebugChange() {
    }

    @Deprecated
    public String getLegacyName() {
        return String.format("%s-T%d", getClass().getName().replace("Entity", ""), getTier());
    }

    public static BlockSpecial getBlockSpecial(Block block2) {
        return SPECIAL_BLOCKS.getOrDefault(block2, BlockSpecial.NONE);
    }

    public static int getBlockType(Block block) {
        return BLOCK_TYPES.getOrDefault(block, 0);
    }

    public static Optional<Float> getBlockCost(BlockState state) {
        return InvasionMod.getConfig().getBlockCost(state.getBlock())
                .or(() -> Optional.ofNullable(BLOCK_COSTS.get(state.getBlock())));
    }

    public static float getBlockStrength(BlockPos p, BlockState state, WorldView world) {
        BlockSpecial special = SPECIAL_BLOCKS.getOrDefault(state.getBlock(), BlockSpecial.NONE);
        int bonus = 0;
        BlockPos.Mutable pos = p.mutableCopy();
        float strength = InvasionMod.getConfig().getBlockStrength(state.getBlock()).orElseGet(() -> BLOCK_STRENGTHS.getOrDefault(state.getBlock(), DEFAULT_SOFT_STRENGTH));
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
}