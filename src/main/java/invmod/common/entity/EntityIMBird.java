package invmod.common.entity;

import invmod.client.render.AnimationRegistry;
import invmod.client.render.animation.AnimationAction;
import invmod.client.render.animation.AnimationState;
import invmod.common.nexus.INexusAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.potion.Potion;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EntityIMBird extends EntityIMFlying {
    private static final TrackedData<Boolean> CLAWS_FORWARD = DataTracker.registerData(EntityIMBird.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> BEAK_DOWN = DataTracker.registerData(EntityIMBird.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> ATTACKING_WITH_WINGS = DataTracker.registerData(EntityIMBird.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final WingController wingController = AnimationRegistry.instance().getAnimation("wing_flap_2_piece").createState(this, AnimationAction.WINGTUCK, WingController::new);
    private final LegController legController = AnimationRegistry.instance().getAnimation("bird_run").createState(this, AnimationAction.STAND, LegController::new);
    private final MouthController beakController = AnimationRegistry.instance().getAnimation("bird_beak").createState(this, AnimationAction.MOUTH_CLOSE, MouthController::new);

    private float carriedEntityYawOffset;
    private int tier;

    public EntityIMBird(World world) {
        this(world, null);
    }

    public EntityIMBird(World world, INexusAccess nexus) {
        super(world, nexus);
        setName("Bird");
        setGender(2);
        setBaseMoveSpeedStat(1.0F);
        setAttackStrength(1);
        setGravity(0.025F);
        setThrust(0.1F);
        setMaxPoweredFlightSpeed(0.5F);
        setLiftFactor(0.35F);
        setThrustComponentRatioMin(0.0F);
        setThrustComponentRatioMax(0.5F);
        setMaxTurnForce(getGravity() * 8.0F);
        setMoveState(MoveState.STANDING);
        setFlyState(FlyState.GROUNDED);
        setTier(1);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CLAWS_FORWARD, false);
        builder.add(ATTACKING_WITH_WINGS, false);
    }

    @Override
    public int getTier() {
        return tier;
    }

    protected void setTier(int tier) {
        this.tier = tier;
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

    @Override
    public String getSpecies() {
        return "Bird";
    }

}