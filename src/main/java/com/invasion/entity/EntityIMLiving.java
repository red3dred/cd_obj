package com.invasion.entity;

import org.jetbrains.annotations.Nullable;

import com.invasion.ConfigInvasion;
import com.invasion.InvasionMod;
import com.invasion.entity.ai.IMMoveHelper;
import com.invasion.entity.ai.MoveState;
import com.invasion.entity.pathfinding.INavigation;
import com.invasion.entity.pathfinding.IPathSource;
import com.invasion.entity.pathfinding.NavigatorIM;
import com.invasion.entity.pathfinding.PathCreator;
import com.invasion.entity.pathfinding.PathNavigateAdapter;
import com.invasion.nexus.IHasNexus;
import com.invasion.nexus.INexusAccess;
import com.invasion.util.math.MathUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.entity.EntityChangeListener;

public abstract class EntityIMLiving extends HostileEntity implements NexusEntity, Stunnable {
    private static final TrackedData<Integer> MOVE_STATE = DataTracker.registerData(EntityIMLiving.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> ANGLES = DataTracker.registerData(EntityIMLiving.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> LABEL = DataTracker.registerData(EntityIMLiving.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> CLIMBING = DataTracker.registerData(EntityIMLiving.class, TrackedDataHandlerRegistry.BOOLEAN);

    protected float airResistance = DEFAULT_AIR_RESISTANCE;
    private float groundFriction = DEFAULT_GROUND_FRICTION;

    private float turnRate = 30;

    private final IHasNexus.Handle nexus = new IHasNexus.Handle(this::getWorld);

    protected int selfDamage = 2;
    protected int maxSelfDamage = 6;

    protected int blockBreakSoundCooldown;

    private boolean alwaysIndependent;
    private boolean burnsInDay;

    private int aggroRange;
    private int senseRange;
    private int stunTimer;

    protected int flammability = 2;

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
        return new PathNavigateAdapter(this, world, createIMNavigation(createPathSource()));
    }

    protected INavigation createIMNavigation(IPathSource pathSource) {
        return new NavigatorIM(this, pathSource);
    }

    protected IPathSource createPathSource() {
        return new PathCreator(700, 50);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(MOVE_STATE, MoveState.STANDING.ordinal());
        builder.add(CLIMBING, false);
        builder.add(ANGLES, MathUtil.packAnglesDeg(getBodyYaw(), getHeadYaw(), getPitch(), 0));
        builder.add(LABEL, "");
    }

    @Override
    public INexusAccess getNexus() {
        return nexus.get();
    }

    @Override
    public void setNexus(@Nullable INexusAccess nexus) {
        this.nexus.set(nexus);
        setBurnsInDay(nexus != null && InvasionMod.getConfig().nightMobsBurnInDay);
        setAggroRange(nexus != null ? 12 : InvasionMod.getConfig().nightMobSightRange);
        setSenseRange(nexus != null ? 6 : InvasionMod.getConfig().nightMobSenseRange);
        if (nexus != null) {
            setChangeListener(new EntityChangeListener() {
                @Override
                public void updateEntityPosition() {
                }

                @Override
                public void remove(RemovalReason reason) {
                    if (hasNexus() && reason == RemovalReason.KILLED) {
                        getNexus().registerMobDied();
                    }
                }
            });
        }
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
    public void tick() {
        if (!getWorld().isClient) {
            if (stunTimer > 0) {
                stunTimer--;
                return;
            }
        }

        super.tick();
    }

    @Override
    public void baseTick() {
        if (!hasNexus()) {
            @SuppressWarnings("deprecation")
            float brightness = getBrightnessAtEyes();
            if (brightness > 0.5F || getY() < 55) {
                age += 2;
            }
            if (getBurnsInDay() && isAffectedByDaylight()) {
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
    }

    @Override
    public boolean damage(DamageSource source, float damage) {
        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            damage *= flammability;
        }

        return super.damage(source, damage);
    }

    @Override
    public boolean stun(int ticks) {
        stunTimer = Math.max(stunTimer, ticks);
        setVelocity(getVelocity().multiply(0, 1, 0));
        return true;
    }

    @Override
    public boolean isStunned() {
        return stunTimer > 0;
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

    @Override
    public boolean canSee(Entity entity) {
        float distance = distanceTo(entity);
        return distance <= getSenseRange() || (super.canSee(entity) && distance <= getAggroRange());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putBoolean("alwaysIndependent", alwaysIndependent);
        compound.putInt("stunTimer", stunTimer);
        nexus.writeNbt(compound);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        alwaysIndependent = compound.getBoolean("alwaysIndependent");
        stunTimer = compound.getInt("stunTimer");
        nexus.readNbt(compound);
        if (alwaysIndependent) {
            ConfigInvasion config = InvasionMod.getConfig();
            setAggroRange(config.nightMobSightRange);
            setSenseRange(config.nightMobSenseRange);
            setBurnsInDay(config.nightMobsBurnInDay);
        }
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return canSpawn(world) && (hasNexus() || getLightLevelBelow8()) && getWorld().isTopSolid(getBlockPos(), this);
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

    public float getTurnRate() {
        return turnRate;
    }

    public void setTurnRate(float rate) {
        this.turnRate = rate;
    }

    public float getGroundFriction() {
        return groundFriction;
    }

    public void setGroundFriction(float frictionCoefficient) {
        groundFriction = frictionCoefficient;
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return hasNexus() ? 0 : 0.5F - world.getLightLevel(pos);
    }

    public String getRenderLabel() {
        return dataTracker.get(LABEL);
    }

    @Deprecated
    public void setIsHoldingIntoLadder(boolean flag) {
        setSneaking(flag);
    }

    @Override
    public boolean isClimbing() {
        return dataTracker.get(CLIMBING);
    }

    protected void setClimbing(boolean climbing) {
        dataTracker.set(CLIMBING, climbing);
    }

    @Override
    public boolean shouldRenderName() {
        return getDebugMode() || super.shouldRenderName();
    }

    @Deprecated
    public void setRenderLabel(String label) {
        dataTracker.set(LABEL, label);
    }

    @Override
    public final boolean canImmediatelyDespawn(double distanceSquared) {
        return !hasNexus();
    }

    @Override
    public final boolean cannotDespawn() {
        return hasNexus() || super.cannotDespawn();
    }

    protected void sunlightDamageTick() {
        setOnFireFor(8);
    }

    @Override
    public PathAwareEntity asEntity() {
        return this;
    }

    public MoveState getMoveState() {
        return MoveState.of(dataTracker.get(MOVE_STATE));
    }

    public void setMoveState(MoveState moveState) {
        dataTracker.set(MOVE_STATE, moveState.ordinal());
    }

    @Override
    public void setMovementSpeed(float movementSpeed) {
        super.setMovementSpeed(movementSpeed);
        getNavigatorNew().setSpeed(speed);
    }

    @Deprecated
    protected void setName(String name) {
    }
}