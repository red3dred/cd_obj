package com.invasion.entity;

import com.invasion.entity.ai.IMSpiderMoveControl;
import com.invasion.entity.ai.goal.AttackNexusGoal;
import com.invasion.entity.ai.goal.GoToNexusGoal;
import com.invasion.entity.ai.goal.KillEntityGoal;
import com.invasion.entity.ai.goal.LayEggGoal;
import com.invasion.entity.ai.goal.PounceGoal;
import com.invasion.entity.ai.goal.RallyBehindLeaderGoal;
import com.invasion.entity.ai.goal.NoNexusPathGoal;
import com.invasion.entity.ai.goal.WaitForSupportGoal;
import com.invasion.entity.ai.goal.PredicatedGoal;
import com.invasion.entity.ai.goal.target.CustomRangeActiveTargetGoal;
import com.invasion.entity.ai.goal.target.RetaliateGoal;
import com.invasion.nexus.EntityConstruct;
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
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityIMSpider extends TieredIMMobEntity implements Reproducer {
    private static final TrackedData<Boolean> CLIMBING = DataTracker.registerData(EntityIMSpider.class, TrackedDataHandlerRegistry.BOOLEAN);

	private int airborneTime;
    // TODO: Add this entity to EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS
	public EntityIMSpider(EntityType<EntityIMSpider> type, World world) {
		super(type, world);
		moveControl = new IMSpiderMoveControl(this);
		getNavigatorNew().getNodeMaker().setCanClimb(true);
	}

    public static DefaultAttributeContainer.Builder createT1V0Attributes() {
        return SpiderEntity.createSpiderAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.29F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3)
                .add(EntityAttributes.GENERIC_GRAVITY, 0.08);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CLIMBING, false);
    }

	@Override
    protected void initGoals() {
		goalSelector.add(0, new SwimGoal(this));
		goalSelector.add(1, new KillEntityGoal<>(this, PlayerEntity.class, 40));
		goalSelector.add(1, new RallyBehindLeaderGoal<>(this, IMCreeperEntity.class, 4));
		goalSelector.add(1, new PredicatedGoal(new LayEggGoal(this, 1), () -> getTier() == 2 && getFlavour() == 1));
		goalSelector.add(2, new AttackNexusGoal(this));
		goalSelector.add(3, new WaitForSupportGoal(this, 5, false));
		goalSelector.add(3, new PredicatedGoal(new PounceGoal(this, 0.2F, 1.55F, 18), () -> getTier() == 2 && getFlavour() == 0));
		goalSelector.add(3, new PredicatedGoal(new PounceGoal(this, 0.2F, 1.55F, 18), () -> getTier() != 2 && getFlavour() == 1));
		goalSelector.add(4, new KillEntityGoal<>(this, MobEntity.class, 40));
		goalSelector.add(5, new GoToNexusGoal(this));
		goalSelector.add(7, new WanderAroundFarGoal(this, 1));
		goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8));
		goalSelector.add(9, new LookAroundGoal(this));
        goalSelector.add(10, new LookAtEntityGoal(this, IMCreeperEntity.class, 12));

		targetSelector.add(0, new RetaliateGoal(this));
		targetSelector.add(1, new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getSenseRange, false));
		targetSelector.add(2, new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getAggroRange, true));
		targetSelector.add(3, new NoNexusPathGoal(this, new CustomRangeActiveTargetGoal<>(this, PigmanEngineerEntity.class, 3.5F)));
		targetSelector.add(4, new RevengeGoal(this));
	}

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient) {
            setClimbing(horizontalCollision);
        }
    }

    protected void setClimbing(boolean climbing) {
        dataTracker.set(CLIMBING, climbing);
    }

	@Override
	protected float getJumpVelocity(float strength) {
	    return super.getJumpVelocity(strength + 0.41F);
	}

	@Override
    public int getNexusBoundAggroRange() {
	    return 2 + (8 * getTier());
	}

	@Override
    protected Text getDefaultName() {
	    if (getTier() == 1 && getFlavour() == 1) {
	        return Text.translatable(getType().getUntranslatedName() + ".baby");
	    }
	    if (getTier() == 2 && getFlavour() == 0) {
            return Text.translatable(getType().getUntranslatedName() + ".jumping");
        }
	    if (getTier() == 2 && getFlavour() == 1) {
            return Text.translatable(getType().getUntranslatedName() + ".mother");
        }
	    return super.getDefaultName();
	}

	@Override
	protected void initTieredAttributes() {
        //setSize(1.4F, 0.9F);
        if (getTier() == 1) {
            if (getFlavour() == 0) {
                setBaseMovementSpeed(0.29F);
                setAttackStrength(3);
            } else if (getFlavour() == 1) {
                //setSize(0.42F, 0.3F);
                setBaseMovementSpeed(0.34F);
                setAttackStrength(1);
            }
        } else if (getTier() == 2) {
            if (getFlavour() == 0) {
                setBaseMovementSpeed(0.3F);
                setAttackStrength(5);
                setGravity(0.043F);
            } else if (getFlavour() == 1) {
                //setSize(2.8F, 1.8F);
                setBaseMovementSpeed(0.22F);
                setAttackStrength(4);
            }
        }
	}

	@Deprecated
	@Override
    public String getLegacyName() {
	    return String.format("%s-T%d-%s", getClass().getName().replace("Entity", ""), getTier(), getDisplayName());
	}

	@Override
	public Vec3d getVehicleAttachmentPos(Entity vehicle) {
		return super.getVehicleAttachmentPos(vehicle).multiply(1, 0.75D, 1).subtract(0, 0.5D, 0);
	}

	@Override
    public float getScaleFactor() {
		if (getTier() == 1 && getFlavour() == 1) {
			return 0.35F;
		}
		if (getTier() == 2 && getFlavour() == 1) {
			return 1.3F;
		}
		return 1;
	}

	@Override
	public Entity[] getOffspring(Entity partner) {
		if (getTier() == 2 && getFlavour() == 1) {
			EntityConstruct template = new EntityConstruct(InvEntities.SPIDER, 1, 0, 1, 1.0F, 0, 0);
			Entity[] offSpring = new Entity[6];
			for (int i = 0; i < offSpring.length; i++) {
				offSpring[i] = template.createMob(getWorld(), getNexus());
			}
			return offSpring;
		}

		return null;
	}

	public int getAirborneTime() {
		return airborneTime;
	}

	@Override
	public boolean isPushable() {
		return !isClimbing();
	}

	@Override
	public boolean isClimbing() {
		return dataTracker.get(CLIMBING);
	}

	public void setAirborneTime(int time) {
		this.airborneTime = time;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_SPIDER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_SPIDER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_SPIDER_DEATH;
	}

	@Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
	    // TODO: Add this to EntityTypeTags.FALL_DAMAGE_IMMUNE
	    return false;
	}
}