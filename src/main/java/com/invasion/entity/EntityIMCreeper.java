package com.invasion.entity;

import org.jetbrains.annotations.Nullable;

import com.invasion.INotifyTask;
import com.invasion.block.InvBlocks;
import com.invasion.entity.ai.goal.EntityAIAttackNexus;
import com.invasion.entity.ai.goal.EntityAICreeperIMSwell;
import com.invasion.entity.ai.goal.EntityAIGoToNexus;
import com.invasion.entity.ai.goal.EntityAIKillEntity;
import com.invasion.entity.ai.goal.EntityAISimpleTarget;
import com.invasion.entity.ai.goal.EntityAIWaitForEngy;
import com.invasion.entity.ai.goal.EntityAIWanderIM;
import com.invasion.entity.ai.goal.PredicatedGoal;
import com.invasion.entity.pathfinding.Actor;
import com.invasion.entity.pathfinding.INavigation;
import com.invasion.entity.pathfinding.NavigatorIM;
import com.invasion.entity.pathfinding.Path;
import com.invasion.entity.pathfinding.PathCreator;
import com.invasion.entity.pathfinding.PathNode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SkinOverlayOwner;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;

public class EntityIMCreeper extends TieredIMMobEntity implements ILeader, SkinOverlayOwner {
    private static final TrackedData<Integer> FUSE_SPEED = DataTracker.registerData(EntityIMCreeper.class, TrackedDataHandlerRegistry.INTEGER);

    private int currentFuseTime;
    private int lastFuseTime;
    private int fuseTime = 30;

    private boolean explosionDeath;
    private boolean commitToExplode;

    private Direction explodeDirection = Direction.UP;

    public EntityIMCreeper(EntityType<EntityIMCreeper> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return CreeperEntity.createCreeperAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.21);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(FUSE_SPEED, 0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new EntityAICreeperIMSwell(this));
        goalSelector.add(2, new FleeEntityGoal<>(this, CatEntity.class, 6.0F, 0.25D, 0.300000011920929D));
        goalSelector.add(3, new EntityAIKillEntity<>(this, PlayerEntity.class, 40));
        goalSelector.add(4, new EntityAIAttackNexus(this));
        goalSelector.add(5, new EntityAIWaitForEngy(this, 4.0F, true));
        goalSelector.add(6, new EntityAIKillEntity<>(this, MobEntity.class, 40));
        goalSelector.add(7, new EntityAIGoToNexus(this));
        goalSelector.add(8, new EntityAIWanderIM(this));
        goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 4.8F));
        goalSelector.add(9, new LookAroundGoal(this));
        targetSelector.add(0, new RevengeGoal(this));
        targetSelector.add(1, new PredicatedGoal(new EntityAISimpleTarget<>(this, PlayerEntity.class, 20.0F, true), this::hasNexus));
        targetSelector.add(1, new PredicatedGoal(new EntityAISimpleTarget<>(this, PlayerEntity.class, this::getSenseRange, false), () -> !hasNexus()));
        targetSelector.add(2, new PredicatedGoal(new EntityAISimpleTarget<>(this, PlayerEntity.class, this::getAggroRange, true), () -> !hasNexus()));
    }

    @Override
    protected INavigation createIMNavigation() {
        return new NavigatorIM(this, new PathCreator(700, 50)) {
            @Override
            protected <T extends Entity> Actor<T> createActor(T entity) {
                return new Actor<>(entity) {
                    @SuppressWarnings("deprecation")
                    @Override
                    public float getBlockPathCost(PathNode prevNode, PathNode node, BlockView terrainMap) {
                        BlockState state = terrainMap.getBlockState(node.pos);
                        if (!state.isAir() && !state.blocksMovement() && !state.isOf(InvBlocks.NEXUS_CORE)) {
                            return prevNode.distanceTo(node) * 12.0F;
                        }

                        return super.getBlockPathCost(prevNode, node, terrainMap);
                    }
                };
            }
        };
    }

    @Override
    public boolean onPathBlocked(Path path, INotifyTask notifee) {
        if (!path.isFinished()) {
            PathNode node = path.getPathPointFromIndex(path.getCurrentPathIndex());
            Vec3d delta = node.pos.toBottomCenterPos().subtract(getPos());
            float facing = (float) (Math.atan2(delta.getX(), delta.getZ()) * MathHelper.DEGREES_PER_RADIAN) - 90;
            explodeDirection = Direction.fromRotation(facing);
            commitToExplode = true;
            setFuseSpeed(1);
        }
        return false;
    }

    @Override
    public void tick() {
        if (explosionDeath) {
            explode();
        } else if (isAlive()) {
            this.lastFuseTime = currentFuseTime;
            int speed = getFuseSpeed();

            if (speed > 0) {
                if (commitToExplode) {
                    getMoveControl().moveTo(getX() + explodeDirection.getVector().getX(), getY(), getZ() + explodeDirection.getVector().getZ(), 0.1);
                }
                if (currentFuseTime == 0) {
                    playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1, 0.5F);
                }
            }
            currentFuseTime += speed;
            if (currentFuseTime < 0) {
                currentFuseTime = 0;
            }
            if (currentFuseTime >= fuseTime) {
                currentFuseTime = fuseTime;
                explosionDeath = true;
                // IM: Explosion moved to next tick so other mobs are allowed to tick their martyr reactions
            }
        }

        super.tick();
    }

    @Override
    public boolean isMartyr() {
        return explosionDeath;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (!(target instanceof GoatEntity)) {
            super.setTarget(target);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CREEPER_DEATH;
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        super.dropEquipment(world, source, causedByPlayer);
        Entity entity = source.getAttacker();
        if (entity != this && entity instanceof CreeperEntity creeperEntity && creeperEntity.shouldDropHead()) {
            creeperEntity.onHeadDropped();
            dropItem(Items.CREEPER_HEAD);
        }
    }

    @Override
    public boolean shouldRenderOverlay() {
        return getTier() > 1;
    }

    @Override
    public boolean tryAttack(Entity target) {
        return true;
    }

    public float getClientFuseTime(float tickDelta) {
        return MathHelper.lerp(tickDelta, (float)lastFuseTime, (float)currentFuseTime) / (fuseTime - 2);
    }

    protected void explode() {
        if (!getWorld().isClient) {
            // IN - Added explosion power based on tier
            float explosionPower = 2.1F * Math.max(getTier(), 1);
            getWorld().createExplosion(this, getX(), getY(), getZ(), explosionPower, false, ExplosionSourceType.MOB);
            discard();
        }
    }

    public int getFuseSpeed() {
        return dataTracker.get(FUSE_SPEED);
    }

    public void setFuseSpeed(int speed) {
        dataTracker.set(FUSE_SPEED, commitToExplode ? 1 : speed);
    }


    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putShort("Fuse", (short)fuseTime);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Fuse", NbtElement.NUMBER_TYPE)) {
            fuseTime = nbt.getShort("Fuse");
        }
    }

    @Override
    protected void initTieredAttributes() {

    }
}