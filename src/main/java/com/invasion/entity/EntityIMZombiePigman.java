package com.invasion.entity;

import org.jetbrains.annotations.Nullable;

import com.invasion.InvSounds;
import com.invasion.InvasionMod;
import com.invasion.block.InvBlocks;
import com.invasion.entity.ai.goal.EntityAIAttackNexus;
import com.invasion.entity.ai.goal.EntityAICharge;
import com.invasion.entity.ai.goal.EntityAIGoToNexus;
import com.invasion.entity.ai.goal.EntityAIKillEntity;
import com.invasion.entity.ai.goal.EntityAISimpleTarget;
import com.invasion.entity.ai.goal.NoNexusPathGoal;
import com.invasion.entity.ai.goal.EntityAITargetRetaliate;
import com.invasion.entity.ai.goal.EntityAIWaitForEngy;
import com.invasion.entity.ai.goal.PredicatedGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class EntityIMZombiePigman extends AbstractIMZombieEntity {
    private static final TrackedData<Boolean> CHARGING = DataTracker.registerData(EntityIMZombiePigman.class, TrackedDataHandlerRegistry.BOOLEAN);

    public EntityIMZombiePigman(EntityType<EntityIMZombiePigman> type, World world) {
        super(type, world, 0.75F);
        setFireImmune(true);
        setCanDestroyBlocks(true);
    }

    public static DefaultAttributeContainer.Builder createT1Attributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8);
    }

    public static DefaultAttributeContainer.Builder createT2Attributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12);
    }

    public static DefaultAttributeContainer.Builder createT3Attributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 18);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CHARGING, false);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new PredicatedGoal(new EntityAICharge<>(this, PlayerEntity.class, 0.75F), () -> getTier() == 3));
        goalSelector.add(2, new EntityAIKillEntity<>(this, PlayerEntity.class, 40));
        goalSelector.add(3, new EntityAIAttackNexus(this));
        goalSelector.add(4, new EntityAIWaitForEngy(this, 4.0F, true));
        goalSelector.add(5, new EntityAIKillEntity<>(this, LivingEntity.class, 40));
        goalSelector.add(6, new EntityAIGoToNexus(this));
        goalSelector.add(7, new WanderAroundFarGoal(this, 1));
        goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        goalSelector.add(9, new LookAtEntityGoal(this, EntityIMCreeper.class, 12.0F));
        goalSelector.add(9, new LookAroundGoal(this));

        targetSelector.add(0, new EntityAITargetRetaliate(this));
        targetSelector.add(1, new PredicatedGoal(new EntityAISimpleTarget<>(this, PlayerEntity.class, this::getAggroRange, false), () -> getTier() != 3));
        targetSelector.add(2, new EntityAISimpleTarget<>(this, PlayerEntity.class, this::getAggroRange, true));
        targetSelector.add(3, new PredicatedGoal(new EntityAISimpleTarget<>(this, EntityIMPigEngy.class, 3.5F), () -> getTier() != 3 && NoNexusPathGoal.isLostPathToNexus(this)));
        targetSelector.add(5, new RevengeGoal(this));
    }

    public boolean isCharging() {
        return dataTracker.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        dataTracker.set(CHARGING, charging);
    }

    @Override
    public void updateAnimation(boolean override) {
        if (!getWorld().isClient && (terrainModifier.isBusy() || override)) {
            setSwinging(true);
        }
        int swingSpeed = getSwingSpeed();
        if (isSwinging()) {
            swingTimer++;
            if (swingTimer >= swingSpeed) {
                swingTimer = 0;
                setSwinging(false);
            }
        } else {
            swingTimer = 0;
        }
        handSwingProgress = (float) swingTimer / (float) swingSpeed;

        if (isCharging()) {
            limbAnimator.updateLimbs(0.5F, 1);
            if (!getWorld().isClient) {
                for (BlockPos pos : BlockPos.iterateOutwards(getBlockPos(), 2, 2, 2)) {
                    BlockState block = getWorld().getBlockState(pos);
                    boolean mobgriefing = getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);

                    if (!block.isAir()) {
                        if (getNavigatorNew().getActor().isBlockDestructible(getWorld(), pos, block) && !block.isOf(InvBlocks.NEXUS_CORE)) {
                            playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.2F, 0.5F);
                            if (mobgriefing) {
                                getWorld().breakBlock(pos, InvasionMod.getConfig().destructedBlocksDrop);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (getTier() == 3) {
            return getRandom().nextInt(3) == 0 ? InvSounds.ENTITY_BIG_ZOMBIE_AMBIENT : null;
        }

        return SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_DEATH;
    }

    @Override
    public int getTextureId() {
        return MathHelper.clamp(getTier() - 1, 0, 2);
    }

    @Override
    protected Text getDefaultName() {
        if (getTier() == 3) {
            return Text.translatable(getType().getUntranslatedName() + ".brute");
        }
        return super.getDefaultName();
    }

    @Override
    protected void initTieredAttributes() {
        if (getTier() == 1) {
            setBaseMovementSpeed(0.25F);
            setAttackStrength(8);
            equipStack(EquipmentSlot.MAINHAND, Items.GOLDEN_SWORD.getDefaultStack());
        } else if (getTier() == 2) {
            setBaseMovementSpeed(0.35F);
            setAttackStrength(12);

            if (getRandom().nextInt(5) == 1) {
                equipStack(EquipmentSlot.HEAD, Items.GOLDEN_HELMET.getDefaultStack());
            }

            if (getRandom().nextInt(5) == 1) {
                equipStack(EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE.getDefaultStack());
            }

            if (getRandom().nextInt(5) == 1) {
                equipStack(EquipmentSlot.LEGS, Items.GOLDEN_LEGGINGS.getDefaultStack());
            }

            if (getRandom().nextInt(5) == 1) {
                equipStack(EquipmentSlot.FEET, Items.GOLDEN_BOOTS.getDefaultStack());
            }
        } else if (getTier() == 3) {
            setBaseMovementSpeed(0.20F);
            setAttackStrength(18);
        }
    }
}
