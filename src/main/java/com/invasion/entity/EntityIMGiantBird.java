package com.invasion.entity;

import org.joml.Vector3f;

import com.invasion.InvSounds;
import com.invasion.entity.ai.goal.EntityAIBirdFight;
import com.invasion.entity.ai.goal.BirdOfPreyGoal;
import com.invasion.entity.ai.goal.FlyingCircleTargetGoal;
import com.invasion.entity.ai.goal.FlyingStrikeGoal;
import com.invasion.entity.ai.goal.FlyingTackleGoal;
import com.invasion.entity.ai.goal.PickUpEntityGoal;
import com.invasion.entity.ai.goal.StabiliseFlightGoal;
import com.invasion.entity.ai.goal.SwoopGoal;
import com.invasion.entity.ai.goal.target.CustomRangeActiveTargetGoal;
import com.invasion.entity.ai.goal.LookAtTargetGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityIMGiantBird extends EntityIMBird {
    private static final Vector3f PICKUP_OFFSET = new Vector3f(0, 0.2F, -0.92F);
    private static final float MODEL_ROTATION_OFFSET_Y = 1.9F;
    private static final byte TRIGGER_SQUAWK = 10;
    private static final byte TRIGGER_SCREECH = 11;
    private static final byte TRIGGER_DEATHSOUND = 12;

    public EntityIMGiantBird(EntityType<EntityIMGiantBird> type, World world) {
        super(type, world);
        setThrust(0.028F);
        setMaxPoweredFlightSpeed(0.9F);
        setLiftFactor(0.35F);
        setThrustComponentRatioMin(0.0F);
        setThrustComponentRatioMax(0.5F);
        setMaxTurnForce((float)getGravity() * 8.0F);
    }

    public static DefaultAttributeContainer.Builder createVultureAttributes() {
        return createBirdAttributes()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5)
                .add(EntityAttributes.GENERIC_GRAVITY, 0.03F)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4F);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwoopGoal(this));

        goalSelector.add(3, new BirdOfPreyGoal(this));
        goalSelector.add(4, new FlyingStrikeGoal(this));
        goalSelector.add(4, new FlyingTackleGoal(this));
        goalSelector.add(4, new PickUpEntityGoal(this, PICKUP_OFFSET, 1.5F, 1.5F, 20, 45, 45));
        goalSelector.add(4, new StabiliseFlightGoal(this, 35));
        goalSelector.add(4, new FlyingCircleTargetGoal(this, 300, 16.0F, 45.0F));
        goalSelector.add(4, new EntityAIBirdFight<>(this, ZombieEntity.class, 25, 0.4F));
        goalSelector.add(4, new LookAtTargetGoal(this));

        targetSelector.add(2, new CustomRangeActiveTargetGoal<>(this, ZombieEntity.class, 58.0F, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (getDebugMode() && !getWorld().isClient) {
            setCustomName(Text.literal(getAIGoal() + "\n" + getNavigatorNew()));
        }
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater) {
        super.updatePassengerPosition(passenger, positionUpdater);
        passenger.setYaw(getCarriedEntityYawOffset() + getYaw());
    }

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        double x = PICKUP_OFFSET.x;
        double y = -MODEL_ROTATION_OFFSET_Y;
        double z = -PICKUP_OFFSET.z;

        double dAngle = getPitch() * MathHelper.RADIANS_PER_DEGREE;
        double sinF = Math.sin(dAngle);
        double cosF = Math.cos(dAngle);
        double tmp = z * cosF - y * sinF;
        y = y * cosF + z * sinF;
        z = tmp;

        dAngle = getYaw() * MathHelper.RADIANS_PER_DEGREE;
        sinF = Math.sin(dAngle);
        cosF = Math.cos(dAngle);

        return new Vec3d(
                x * cosF - z * sinF,
                y + MODEL_ROTATION_OFFSET_Y,
                z * cosF + x * sinF
        );
    }

    @Override
    public void doScreech() {
        if (!getWorld().isClient) {
            playSound(InvSounds.ENTITY_VULTURE_SCREECH, 6, 1 + (getRandom().nextFloat() * 0.2F - 0.1F));
            getWorld().sendEntityStatus(this, TRIGGER_SCREECH);
        } else {
            setBeakState(35);
        }
    }

    @Override
    public void doMeleeSound() {
        doSquawk();
    }

    @Override
    protected void doHurtSound() {
        doSquawk();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return InvSounds.ENTITY_VULTURE_SQUAWK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return InvSounds.ENTITY_VULTURE_DEATH;
    }

    @Override
    protected void doDeathSound() {
        if (!getWorld().isClient) {
            getWorld().sendEntityStatus(this, TRIGGER_DEATHSOUND);
        } else {
            setBeakState(25);
        }
    }

    @Override
    public void handleStatus(byte status) {
        super.handleStatus(status);
        if (status == TRIGGER_SQUAWK) {
            doSquawk();
        } else if (status == TRIGGER_SCREECH) {
            doScreech();
        } else if (status == TRIGGER_DEATHSOUND) {
            doDeathSound();
        }
    }

    private void doSquawk() {
        if (!getWorld().isClient) {
            playSound(InvSounds.ENTITY_VULTURE_SQUAWK, 1.9F, 1.0F + getRandom().nextFloat() * 0.2F - 0.1F);
            getWorld().sendEntityStatus(this, TRIGGER_SQUAWK);
        } else {
            setBeakState(10);
        }
    }

    @Override
    public String getLegacyName() {
        return "IMVulture-T" + getTier();
    }
}