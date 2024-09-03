package com.invasion.entity;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.invasion.InvasionMod;
import com.invasion.entity.ai.IMSpiderMoveControl;
import com.invasion.entity.ai.goal.AttackNexusGoal;
import com.invasion.entity.ai.goal.GoToNexusGoal;
import com.invasion.entity.ai.goal.MobMeleeAttackGoal;
import com.invasion.entity.ai.goal.RallyBehindLeaderGoal;
import com.invasion.entity.ai.goal.ProvideSupportGoal;
import com.invasion.entity.ai.goal.target.CustomRangeActiveTargetGoal;
import com.invasion.entity.ai.goal.target.RetaliateGoal;
import com.invasion.entity.pathfinding.IMMobNavigation;
import com.invasion.nexus.IHasNexus;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PassiveEntity.PassiveData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class NexusSpiderEntity extends SpiderEntity implements NexusEntity, MountableEntity, Stunnable {
    private static final EntityAttributeModifier BABY_SPEED_BONUS = AttributeUtil.addPercentage(InvasionMod.id("baby_speed"), 50);
    private static final EntityAttributeModifier BABY_ATTACK_BONUS = AttributeUtil.multiplyTotal(InvasionMod.id("baby_attack"), 0.1F);

    private static final List<RegistryEntry<EntityAttribute>> GROWTH_SCALING_ATTRIBUTES = List.of(
            EntityAttributes.GENERIC_ATTACK_DAMAGE,
            EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
            EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE
    );

    private static final TrackedData<Boolean> CHILD = DataTracker.registerData(NexusSpiderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public static final int FULLY_GROWN_AGE = 0;
    public static final int MAX_TIME_TO_GROW = PassiveEntity.BABY_AGE;

    protected int ticksToGrow = FULLY_GROWN_AGE;

    private int stunTime;

    private final IHasNexus.Handle nexus = new IHasNexus.Handle(this::getWorld);

    public NexusSpiderEntity(EntityType<? extends NexusSpiderEntity> type, World world) {
        super(type, world);
        moveControl = new IMSpiderMoveControl(this);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return SpiderEntity.createSpiderAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.29F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3)
                .add(EntityAttributes.GENERIC_GRAVITY, 0.08);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CHILD, false);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new IMMobNavigation(this) {{
            setCanClimbLadders(true);
        }};
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new MobMeleeAttackGoal(this, 1.3F, false));
        goalSelector.add(1, new RallyBehindLeaderGoal<>(this, IMCreeperEntity.class, 4));
        goalSelector.add(2, new AttackNexusGoal<>(this));
        goalSelector.add(3, new ProvideSupportGoal(this, 5, false));
        initExtraGoals();
        goalSelector.add(5, new GoToNexusGoal(this));
        goalSelector.add(7, new WanderAroundFarGoal(this, 1));
        goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        goalSelector.add(9, new LookAroundGoal(this));
        goalSelector.add(10, new LookAtEntityGoal(this, IMCreeperEntity.class, 12));

        targetSelector.add(0, new RetaliateGoal(this));
        targetSelector.add(1, new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getSenseRange, false));
        targetSelector.add(2, new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getAggroRange, true));
        //targetSelector.add(3, new NoNexusPathGoal(this, new CustomRangeActiveTargetGoal<>(this, PigmanEngineerEntity.class, 3.5F)));
        targetSelector.add(4, new RevengeGoal(this));
    }

    protected void initExtraGoals() {

    }

    @Override
    public Handle getNexusHandle() {
        return nexus;
    }

    @Override
    public PathAwareEntity asEntity() {
        return this;
    }

    @Override
    public final float getScaleFactor() {
        return 1;
    }

    @Override
    public float getScale() {
        return super.getScale() * getGlobalScaleMultiplier();
    }

    protected float getGlobalScaleMultiplier() {
        return isBaby() ? 0.33F : 1;
    }

    @Override
    public boolean stun(int maxTicks) {
        stunTime = Math.max(stunTime, maxTicks);
        return true;
    }

    @Override
    public boolean isStunned() {
        return stunTime > 0;
    }

    @Override
    protected float getJumpVelocity(float strength) {
        return super.getJumpVelocity(strength + 0.41F);
    }

    @Override
    public int getNexusBoundAggroRange() {
        return 2 + (isBaby() ? 0 : 8);
    }

    @Override
    public boolean isBaby() {
        return dataTracker.get(CHILD);
    }

    @Override
    public void setBaby(boolean baby) {
        dataTracker.set(CHILD, baby);
        ticksToGrow = baby ? MAX_TIME_TO_GROW : FULLY_GROWN_AGE;
        if (getWorld() != null && !getWorld().isClient) {
            AttributeUtil.toggleAttribute(this, EntityAttributes.GENERIC_MOVEMENT_SPEED, BABY_SPEED_BONUS, isBaby());
            AttributeUtil.toggleAttribute(this, GROWTH_SCALING_ATTRIBUTES, BABY_ATTACK_BONUS, isBaby());
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (CHILD.equals(data)) {
            calculateDimensions();
        }

        super.onTrackedDataSet(data);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!getWorld().isClient && isAlive() && isBaby() && ++ticksToGrow >= 0) {
            setBaby(false);
        }
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        if (entityData == null) {
            entityData = new NexusSpiderData(new PassiveEntity.PassiveData(true));

            if ((world.getDifficulty() == Difficulty.HARD && random.nextFloat() < 0.1F * difficulty.getClampedLocalDifficulty())
                    || (hasNexus() && getNexus().getCurrentWave() > 5)) {
                ((NexusSpiderData)entityData).setEffect(random);
            }
        }

        PassiveEntity.PassiveData passiveData = ((NexusSpiderData)entityData).passiveData;
        if (passiveData.canSpawnBaby() && passiveData.getSpawnedCount() > 0 && world.getRandom().nextFloat() <= passiveData.getBabyChance()) {
            setBaby(true);
        }

        passiveData.countSpawned();

        super.initialize(world, difficulty, spawnReason, entityData);

        if (hasNexus()) {
            AttributeUtil.applyNexusWaveComplications(this, world, getNexus().getCurrentWave(), difficulty, spawnReason);
        }

        return entityData;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putBoolean("isChild", isBaby());
        compound.putInt("ticksToGrow", ticksToGrow);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        setBaby(compound.getBoolean("isChild"));
        ticksToGrow = compound.getInt("ticksToGrow");
    }

    public static class NexusSpiderData extends SpiderData {
        private final PassiveData passiveData;

        private NexusSpiderData(PassiveData passiveData) {
            this.passiveData = passiveData;
        }
    }
}