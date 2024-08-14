package com.invasion.entity;

import java.util.List;

import com.invasion.INotifyTask;
import com.invasion.InvasionMod;
import com.invasion.block.InvBlocks;
import com.invasion.entity.ai.goal.EntityAIAttackNexus;
import com.invasion.entity.ai.goal.EntityAIGoToNexus;
import com.invasion.entity.ai.goal.EntityAIRandomBoulder;
import com.invasion.entity.ai.goal.EntityAISimpleTarget;
import com.invasion.entity.ai.goal.EntityAIThrowerKillEntity;
import com.invasion.entity.ai.goal.EntityAIWanderIM;
import com.invasion.entity.pathfinding.Path;
import com.invasion.entity.pathfinding.PathNode;
import com.invasion.nexus.EntityConstruct;
import com.invasion.nexus.INexusAccess;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityIMThrower extends EntityIMMob {
    private static final TrackedData<Integer> TIER = DataTracker.registerData(EntityIMThrower.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TEXTURE = DataTracker.registerData(EntityIMThrower.class, TrackedDataHandlerRegistry.INTEGER);

    private int throwTime;
    private int punchTimer;

    private boolean clearingPoint;

    private BlockPos pointToClear;

    private INotifyTask clearPointNotifee;

    private float launchSpeed = 1.0F;

    public EntityIMThrower(EntityType<EntityIMThrower> type, World world) {
        this(type, world, null);
    }

    public EntityIMThrower(EntityType<EntityIMThrower> type, World world, INexusAccess nexus) {
        super(type, world, nexus);
        setMovementSpeed(0.13F);
        setAttackStrength(10);
        selfDamage = 0;
        maxSelfDamage = 0;
        experiencePoints = 20;
        setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
        setCanDestroyBlocks(true);
        // setSize(1.8F, 1.95F);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TIER, 1);
        builder.add(TEXTURE, 0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        if (getTier() == 1) {
            goalSelector.add(1, new EntityAIThrowerKillEntity<>(this, PlayerEntity.class, 55, 60.0F, 1.0F));
        } else {
            goalSelector.add(1, new EntityAIThrowerKillEntity<>(this, PlayerEntity.class, 60, 90.0F, 1.5F));
        }
        goalSelector.add(2, new EntityAIAttackNexus(this));
        goalSelector.add(3, new EntityAIRandomBoulder(this, 3));
        goalSelector.add(4, new EntityAIGoToNexus(this));
        goalSelector.add(7, new EntityAIWanderIM(this));
        goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        goalSelector.add(9, new LookAtEntityGoal(this, EntityIMCreeper.class, 12));
        goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 16));
        goalSelector.add(10, new LookAroundGoal(this));

        targetSelector.add(1, new EntityAISimpleTarget<>(this, PlayerEntity.class, this.getSenseRange(), false));
        targetSelector.add(2, new EntityAISimpleTarget<>(this, PlayerEntity.class, this.getAggroRange(), true));
        targetSelector.add(3, new RevengeGoal(this));
    }

    @Override
    public void onSpawned(INexusAccess nexus, EntityConstruct spawnConditions) {
        super.onSpawned(nexus, spawnConditions);
        setTexture(spawnConditions.tier());
        setTier(spawnConditions.tier());
    }

    @Override
    public void mobTick() {
        super.mobTick();
        throwTime--;
        if (clearingPoint && clearPoint()) {
            clearingPoint = false;
            if (clearPointNotifee != null) {
                clearPointNotifee.notifyTask(INotifyTask.Status.SUCCESS);
            }
        }
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
        if (getTier() != 2) {
            super.takeKnockback(strength, x, z);
        }
    }

    public boolean canThrow() {
        return throwTime <= 0;
    }

    @Override
    public boolean onPathBlocked(Path path, INotifyTask notifee) {
        if (!path.isFinished()) {
            PathNode node = path.getPathPointFromIndex(path.getCurrentPathIndex());
            clearingPoint = true;
            clearPointNotifee = notifee;
            pointToClear = node.pos;
            return true;
        }
        return false;
    }

    public void setTexture(int textureId) {
        dataTracker.set(TEXTURE, textureId);
    }

    public int getTextureId() {
        return dataTracker.get(TEXTURE);
    }

    @Override
    public int getTier() {
        return dataTracker.get(TIER);
    }

    public void setTier(int tier) {
        dataTracker.set(TIER, tier);
        selfDamage = 0;
        maxSelfDamage = 0;
        clearingPoint = false;
        if (tier == 1) {
            setMovementSpeed(0.13F);
            setAttackStrength(10);
            experiencePoints = 20;
            setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
            setName("Thrower");
            setCanDestroyBlocks(true);
            // setSize(1.8F, 1.95F);
        } else if (tier == 2) {
            setMovementSpeed(0.23F);
            setAttackStrength(15);
            experiencePoints = 25;
            setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
            setName("Big Thrower");
            setCanDestroyBlocks(true);
            // setSize(2F, 2F);
        }

        if (tier == 1) {
            setTexture(1);
        } else if (tier == 2) {
            setTexture(2);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putInt("tier", getTier());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        setTexture(compound.getInt("tier"));
        setTier(compound.getInt("tier"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_DEATH;
    }

    protected boolean clearPoint() {
        if (--punchTimer <= 0) {
            int xOffsetR = 0;
            int zOffsetR = 0;
            int axisX = 0;
            int axisZ = 0;

            float facing = MathHelper.wrapDegrees(getYaw());

            if (facing >= 45 && facing < 135) {
                zOffsetR = -1;
                axisX = -1;
            } else if (facing >= 135 && facing < 225) {
                xOffsetR = -1;
                axisZ = -1;
            } else if (facing >= 225 && facing < 315) {
                zOffsetR = -1;
                axisX = 1;
            } else {
                xOffsetR = -1;
                axisZ = 1;
            }
            // this is a cheat, I should fix it where it get's the point to clear
            BlockPos targetPos = pointToClear.down();
            List<BlockPos> wideArea = List.of(
                    targetPos,
                    pointToClear,
                    targetPos.add(xOffsetR, 0, zOffsetR),
                    pointToClear.add(xOffsetR, 0, zOffsetR)
            );
            List<BlockPos> narrowArea = List.of(
                    pointToClear.add(-axisX, 0, -axisZ),
                    pointToClear.add(-axisX, 0, -axisZ).add(xOffsetR, 0, zOffsetR)
            );
            List<BlockPos> singleTarget = List.of(
                    pointToClear.add(-2 * axisX, 0, -2 * axisZ),
                    pointToClear.add(-2 * axisX, 0, -2 * axisZ).add(xOffsetR, 0, zOffsetR)
            );

            if (tryDestroyArea(wideArea) || tryDestroyArea(narrowArea) || tryDestroyArea(singleTarget)) {
                this.punchTimer = 160;
            } else {
                return true;
            }
        }
        return false;
    }

    protected final boolean tryDestroyArea(List<BlockPos> positions) {
        if (positions.stream().anyMatch(pos -> getWorld().getBlockState(pos).isSolidBlock(getWorld(), pos))) {
            positions.forEach(this::tryDestroyBlock);
            return true;
        }
        return false;
    }

    protected void tryDestroyBlock(BlockPos pos) {
        BlockState block = getWorld().getBlockState(pos);
        if (this.j != null) {
            if (block.isOf(InvBlocks.NEXUS_CORE)) {
                if (hasNexus() && canAttack() && pos.equals(getNexus().getOrigin())) {
                    getNexus().attackNexus(5);
                }
            } else {
                getWorld().breakBlock(pos, InvasionMod.getConfig().destructedBlocksDrop);
                if (throttled == 0) {
                    playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1, 0.4F);
                    throttled = 5;
                }
            }
        }
    }

    public boolean canAttack() {
        return getLastAttackTime() < (age - 60);
    }

    @Override
    public boolean tryAttack(Entity entity) {
        float distance = entity.distanceTo(this);
        if (throwTime <= 0 && distance > 4) {
            throwTime = 120;
            if (distance < 50) {
                if (canAttack()) {
                    throwProjectile(entity.getEyePos(), createProjectile(0));
                }
                throwTime = 120;
                return true;
            }
        }
        return super.tryAttack(entity);
    }

    public float getLaunchSpeed() {
        return launchSpeed;
    }

    public PersistentProjectileEntity createProjectile(int tier) {
        return tier == 1 ? InvEntities.TNT.create(getWorld()) : InvEntities.BOULDER.create(getWorld());
    }

    public void throwProjectile(Vec3d targetPosition) {
        throwProjectile(targetPosition, createProjectile(getTier()));
    }

    public void throwProjectile(Vec3d targetPosition, PersistentProjectileEntity projectile) {
        this.throwTime = 40;
        Vec3d eyePos = getEyePos();
        Vec3d delta = targetPosition.subtract(eyePos);
        double dXZ = delta.horizontalLength();

        projectile.setOwner(this);
        projectile.setPosition(eyePos);
        projectile.setVelocity(delta.x, delta.y + (dXZ * Math.tan(getThrowAngle(dXZ))), delta.z, getLaunchSpeed(), 0.05F);
        getWorld().spawnEntity(projectile);
    }

    private double getThrowAngle(double horDifference) {
        double p = getThrowPower(horDifference);
        return p <= 1 ? 0.5D * Math.asin(p) : 0.7853981633974483D;
    }

    public double getThrowPower(double horDifference) {
        return 0.025D * horDifference / MathHelper.square(getLaunchSpeed());
    }
}