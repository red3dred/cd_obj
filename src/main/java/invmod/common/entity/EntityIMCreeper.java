package invmod.common.entity;

import org.jetbrains.annotations.Nullable;

import invmod.common.INotifyTask;
import invmod.common.block.InvBlocks;
import invmod.common.entity.ai.EntityAIAttackNexus;
import invmod.common.entity.ai.EntityAICreeperIMSwell;
import invmod.common.entity.ai.EntityAIGoToNexus;
import invmod.common.entity.ai.EntityAIKillEntity;
import invmod.common.entity.ai.EntityAISimpleTarget;
import invmod.common.entity.ai.EntityAITargetRetaliate;
import invmod.common.entity.ai.EntityAIWaitForEngy;
import invmod.common.entity.ai.EntityAIWanderIM;
import invmod.common.nexus.INexusAccess;
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
import net.minecraft.entity.mob.HostileEntity;
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

public class EntityIMCreeper extends EntityIMMob implements ILeader, SkinOverlayOwner {
    private static final TrackedData<Integer> FUSE_SPEED = DataTracker.registerData(EntityIMCreeper.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TIER = DataTracker.registerData(EntityIMSpider.class, TrackedDataHandlerRegistry.INTEGER);

    private int currentFuseTime;
    private int lastFuseTime;
    private int fuseTime = 30;

    private boolean explosionDeath;
    private boolean commitToExplode;

    private Direction explodeDirection = Direction.UP;

    public EntityIMCreeper(EntityType<EntityIMCreeper> type, World world) {
        this(type, world, null);
    }

    public EntityIMCreeper(EntityType<EntityIMCreeper> type, World world, INexusAccess nexus) {
        super(type, world, nexus);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.21);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(FUSE_SPEED, 0);
        builder.add(TIER, 1);
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
        targetSelector.add(0, new EntityAITargetRetaliate<>(this, MobEntity.class, 12.0F));
        if (this.hasNexus()) {
            targetSelector.add(1, new EntityAISimpleTarget<>(this, PlayerEntity.class, 20.0F, true));
        } else {
            targetSelector.add(1, new EntityAISimpleTarget<>(this, PlayerEntity.class, this.getSenseRange(), false));
            targetSelector.add(2, new EntityAISimpleTarget<>(this, PlayerEntity.class, this.getAggroRange(), true));
        }
        targetSelector.add(3, new RevengeGoal(this));
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
                    getMoveControl().moveTo(
                            getX() + explodeDirection.getVector().getX(),
                            getY(),
                            getZ() + explodeDirection.getVector().getZ(), 0
                    );
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
    public int getTier() {
        return dataTracker.get(TIER);
    }

    public void setTier(int tier) {
        dataTracker.set(TIER, tier);
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

    @SuppressWarnings("deprecation")
    @Override
    public float getBlockPathCost(PathNode prevNode, PathNode node, BlockView terrainMap) {
        BlockState state = terrainMap.getBlockState(node.pos);
        if (!state.isAir() && !state.blocksMovement() && !state.isOf(InvBlocks.NEXUS_CORE)) {
            return prevNode.distanceTo(node) * 12.0F;
        }

        return super.getBlockPathCost(prevNode, node, terrainMap);
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
        nbt.putInt("tier", getTier());
        nbt.putShort("Fuse", (short)fuseTime);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setTier(nbt.getInt("tier"));
        if (nbt.contains("Fuse", NbtElement.NUMBER_TYPE)) {
            fuseTime = nbt.getShort("Fuse");
        }
    }

}