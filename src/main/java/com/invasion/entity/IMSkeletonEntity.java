package com.invasion.entity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AvoidSunlightGoal;
import net.minecraft.entity.ai.goal.EscapeSunlightGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class IMSkeletonEntity extends IMMobEntity implements RangedAttackMob {
    public IMSkeletonEntity(EntityType<IMSkeletonEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createIMSkeletonAttributes() {
        return SkeletonEntity.createAbstractSkeletonAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.21);
    }

    @Override
    protected void initGoals() {
       /* goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new BowAttackGoal<>(this, 65D, 20, 16F));
        // goalSelector.add(1, new EntityAIRallyBehindEntity(this, EntityIMCreeper.class, 4.0F));
        goalSelector.add(3, new AttackNexusGoal(this));
        goalSelector.add(4, new GoToNexusGoal(this));
        goalSelector.add(5, new WanderAroundFarGoal(this, 1));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        goalSelector.add(6, new LookAroundGoal(this));
        goalSelector.add(6, new LookAtEntityGoal(this, IMCreeperEntity.class, 12));

        targetSelector.add(0, new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getSenseRange, false));
        targetSelector.add(1, new RevengeGoal(this));*/

        this.goalSelector.add(2, new AvoidSunlightGoal(this));
        this.goalSelector.add(3, new EscapeSunlightGoal(this, 1.0));
        this.goalSelector.add(3, new FleeEntityGoal(this, WolfEntity.class, 6.0F, 1.0, 1.2));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal(this, IronGolemEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SKELETON_DEATH;
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        ItemStack bow = getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
        ItemStack arrow = getProjectileType(bow);
        PersistentProjectileEntity projectile = createArrowProjectile(arrow, pullProgress, bow);
        double dX = target.getX() - getX();
        double dY = target.getBodyY(0.3333333333333333) - projectile.getY();
        double dZ = target.getZ() - getZ();
        double horLength = Math.sqrt(dX * dX + dZ * dZ);
        projectile.setVelocity(dX, dY + horLength * 0.2F, dZ, 1.6F, 14 - getWorld().getDifficulty().getId() * 4);
        playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1, 1 / (getRandom().nextFloat() * 0.4F + 0.8F));
        getWorld().spawnEntity(projectile);
    }

    protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier, @Nullable ItemStack shotFrom) {
        return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier, shotFrom);
    }
}