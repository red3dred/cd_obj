package com.invasion.entity;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.invasion.Notifiable;
import com.invasion.InvSounds;
import com.invasion.InvasionMod;
import com.invasion.block.InvBlocks;
import com.invasion.entity.ai.goal.AttackNexusGoal;
import com.invasion.entity.ai.goal.GoToNexusGoal;
import com.invasion.entity.ai.goal.ThrowBoulderGoal;
import com.invasion.entity.ai.goal.target.CustomRangeActiveTargetGoal;
import com.invasion.entity.ai.goal.ThrowerKillEntityGoal;
import com.invasion.entity.ai.goal.PredicatedGoal;
import com.invasion.entity.pathfinding.Path;
import com.invasion.entity.pathfinding.PathNode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ThrowerEntity extends TieredIMMobEntity {
    private int throwTime;
    private int punchTimer;

    private boolean clearingPoint;

    private int blockBreakSoundCooldown;

    private BlockPos pointToClear;

    private Notifiable clearPointNotifee;

    private float launchSpeed = 1.0F;

    @Nullable
    protected Entity j;

    public ThrowerEntity(EntityType<ThrowerEntity> type, World world) {
        super(type, world);
        experiencePoints = 20;
        setCanDestroyBlocks(true);
        // setSize(1.8F, 1.95F);
    }

    public static DefaultAttributeContainer.Builder createT1V0Attributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.13F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10);
    }


    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new PredicatedGoal(new ThrowerKillEntityGoal<>(this, PlayerEntity.class, 55, 60.0F, 1.0F), () -> getTier() == 1));
        goalSelector.add(1, new PredicatedGoal(new ThrowerKillEntityGoal<>(this, PlayerEntity.class, 60, 90.0F, 1.5F), () -> getTier() == 1));
        goalSelector.add(2, new AttackNexusGoal(this));
        goalSelector.add(3, new ThrowBoulderGoal(this, 3));
        goalSelector.add(4, new GoToNexusGoal(this));
        goalSelector.add(7, new WanderAroundFarGoal(this, 1));
        goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        goalSelector.add(9, new LookAtEntityGoal(this, IMCreeperEntity.class, 12));
        goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 16));
        goalSelector.add(10, new LookAroundGoal(this));

        targetSelector.add(1, new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getSenseRange, false));
        targetSelector.add(2, new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getAggroRange, true));
        targetSelector.add(3, new RevengeGoal(this));
    }

    @Override
    public void mobTick() {
        super.mobTick();
        throwTime--;
        if (clearingPoint && clearPoint()) {
            clearingPoint = false;
            if (clearPointNotifee != null) {
                clearPointNotifee.notifyTask(Notifiable.Status.SUCCESS);
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float damage) {
        if (super.damage(source, damage)) {
            @Nullable
            Entity attacker = source.getAttacker();
            if (attacker != null && attacker != this && isConnectedThroughVehicle(attacker)) {
                j = attacker;
            }
            return true;
        }

        return false;
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
    public boolean onPathBlocked(Path path, Notifiable notifee) {
        if (!path.isFinished()) {
            PathNode node = path.getPathPointFromIndex(path.getCurrentPathIndex());
            clearingPoint = true;
            clearPointNotifee = notifee;
            pointToClear = node.pos;
            return true;
        }
        return false;
    }

    @Override
    protected Text getDefaultName() {
        if (getTier() == 2) {
            return Text.translatable(getType().getUntranslatedName() + ".big");
        }
        return super.getDefaultName();
    }

    @Override
    public void initTieredAttributes() {
        clearingPoint = false;
        if (getTier() == 1) {
            setBaseMovementSpeed(0.13F);
            setAttackStrength(10);
            experiencePoints = 20;
            // setSize(1.8F, 1.95F);
        } else if (getTier() == 2) {
            setBaseMovementSpeed(0.23F);
            setAttackStrength(15);
            experiencePoints = 25;
            // setSize(2F, 2F);
        }
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
                    getNexus().damage(5);
                }
            } else {
                getWorld().breakBlock(pos, InvasionMod.getConfig().destructedBlocksDrop);
                if (blockBreakSoundCooldown == 0) {
                    playSound(InvSounds.ENTITY_THROWER_RAGE, 1, 0.4F);
                    blockBreakSoundCooldown = 5;
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