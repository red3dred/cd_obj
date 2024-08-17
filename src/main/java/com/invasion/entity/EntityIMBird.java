package com.invasion.entity;

import com.invasion.client.render.animation.AnimationAction;
import com.invasion.client.render.animation.AnimationRegistry;
import com.invasion.client.render.animation.AnimationState;
import com.invasion.entity.animation.LegController;
import com.invasion.entity.animation.MouthController;
import com.invasion.entity.animation.WingController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class EntityIMBird extends EntityIMFlying {
    @Deprecated
    private static final TrackedData<Integer> TIER = DataTracker.registerData(EntityIMBird.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CLAWS_FORWARD = DataTracker.registerData(EntityIMBird.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> BEAK_DOWN = DataTracker.registerData(EntityIMBird.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> ATTACKING_WITH_WINGS = DataTracker.registerData(EntityIMBird.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final WingController wingController = AnimationRegistry.instance().getAnimation("wing_flap_2_piece").createState(this, AnimationAction.WINGTUCK, WingController::new);
    private final LegController legController = AnimationRegistry.instance().getAnimation("bird_run").createState(this, AnimationAction.STAND, LegController::new);
    private final MouthController beakController = AnimationRegistry.instance().getAnimation("bird_beak").createState(this, AnimationAction.MOUTH_CLOSE, MouthController::new);

    private float carriedEntityYawOffset;

    public EntityIMBird(EntityType<? extends EntityIMBird> type, World world) {
        super(type, world);
        setThrust(0.1F);
        setMaxPoweredFlightSpeed(0.5F);
        setLiftFactor(0.35F);
        setThrustComponentRatioMin(0);
        setThrustComponentRatioMax(0.5F);
        setMaxTurnForce((float)getGravity() * 8);
    }

    public static DefaultAttributeContainer.Builder createBirdAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1)
                .add(EntityAttributes.GENERIC_GRAVITY, 0.025);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TIER, 1);
        builder.add(CLAWS_FORWARD, false);
        builder.add(ATTACKING_WITH_WINGS, false);
    }

    @Deprecated
    public int getTier() {
        return dataTracker.get(TIER);
    }

    @Deprecated
    protected void setTier(int tier) {
        tier = Math.max(1, tier);
        dataTracker.set(TIER, tier);
    }

    public boolean getClawsForward() {
        return dataTracker.get(CLAWS_FORWARD);
    }

    public void setClawsForward(boolean flag) {
        dataTracker.set(CLAWS_FORWARD, flag);
    }

    public boolean isAttackingWithWings() {
        return dataTracker.get(ATTACKING_WITH_WINGS);
    }

    public void setAttackingWithWings(boolean flag) {
        dataTracker.set(ATTACKING_WITH_WINGS, flag);
    }

    public boolean isBeakOpen() {
        return dataTracker.get(BEAK_DOWN);
    }

    protected void setBeakOpen(boolean flag) {
        dataTracker.set(BEAK_DOWN, flag);
    }

    public float getCarriedEntityYawOffset() {
        return carriedEntityYawOffset;
    }

    public AnimationState<?> getWingAnimationState() {
        return wingController.getState();
    }

    public float getLegSweepProgress() {
        return 1.0F;
    }

    public AnimationState<?> getLegAnimationState() {
        return legController.getState();
    }

    public AnimationState<?> getBeakAnimationState() {
        return beakController.getState();
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (getWorld().isClient) {
            updateFlapAnimation();
            updateLegAnimation();
            updateBeakAnimation();
        }
    }

    @Override
    protected void mobTick() {

    }

    public void doScreech() {
    }

    public void doMeleeSound() {
    }

    protected void doHurtSound() {
    }

    protected void doDeathSound() {
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        doDeathSound();
    }

    @Override
    protected void playHurtSound(DamageSource damageSource) {
        super.playHurtSound(damageSource);
        doHurtSound();
    }

    protected void setBeakState(int timeOpen) {
        beakController.setMouthState(timeOpen);
    }

    protected void onPickedUpEntity(Entity entity) {
        carriedEntityYawOffset = (entity.getYaw() - entity.getYaw());
    }

    protected void updateFlapAnimation() {
        wingController.update();
    }

    protected void updateLegAnimation() {
        legController.update();
    }

    protected void updateBeakAnimation() {
        beakController.update();
    }
}