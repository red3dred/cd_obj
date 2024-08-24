package com.invasion.entity;

import org.jetbrains.annotations.Nullable;

import com.invasion.InvSounds;
import com.invasion.InvasionMod;
import com.invasion.entity.ai.goal.AttackNexusGoal;
import com.invasion.entity.ai.goal.ChargeMobGoal;
import com.invasion.entity.ai.goal.GoToNexusGoal;
import com.invasion.entity.ai.goal.KillEntityGoal;
import com.invasion.entity.ai.goal.MineBlockGoal;
import com.invasion.entity.ai.goal.NoNexusPathGoal;
import com.invasion.entity.ai.goal.ProvideSupportGoal;
import com.invasion.entity.ai.goal.PredicatedGoal;
import com.invasion.entity.ai.goal.target.CustomRangeActiveTargetGoal;
import com.invasion.entity.ai.goal.target.RetaliateGoal;
import com.invasion.entity.pathfinding.IMLandPathNodeMaker;

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
import net.minecraft.particle.ParticleTypes;
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
        getNavigatorNew().setCanDestroyBlocks(true);
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
        goalSelector.add(0, new MineBlockGoal(this));
        goalSelector.add(1, new PredicatedGoal(new ChargeMobGoal<>(this, PlayerEntity.class, 0.75F), () -> getTier() == 3));
        goalSelector.add(2, new KillEntityGoal<>(this, PlayerEntity.class, 40));
        goalSelector.add(3, new AttackNexusGoal<>(this));
        goalSelector.add(4, new ProvideSupportGoal(this, 4.0F, true));
        goalSelector.add(5, new KillEntityGoal<>(this, LivingEntity.class, 40));
        goalSelector.add(6, new GoToNexusGoal(this));
        goalSelector.add(7, new WanderAroundFarGoal(this, 1));
        goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        goalSelector.add(9, new LookAtEntityGoal(this, IMCreeperEntity.class, 12.0F));
        goalSelector.add(9, new LookAroundGoal(this));

        targetSelector.add(0, new RetaliateGoal(this));
        targetSelector.add(1, new PredicatedGoal(new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getAggroRange, false), () -> getTier() != 3));
        targetSelector.add(2, new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getAggroRange, true));
        targetSelector.add(3, new PredicatedGoal(new CustomRangeActiveTargetGoal<>(this, PigmanEngineerEntity.class, 3.5F), () -> getTier() != 3 && NoNexusPathGoal.isLostPathToNexus(this)));
        targetSelector.add(5, new RevengeGoal(this));
    }

    public boolean isCharging() {
        return dataTracker.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        dataTracker.set(CHARGING, charging);
    }

    @Override
    public float getSoundPitch() {
        return super.getSoundPitch() * (getTier() == 3 ? 0.75F : 1);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (isCharging()) {
            boolean mobgriefing = !getWorld().isClient || getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
            boolean sound = false;

            BlockPos center = BlockPos.ofFloored(getCameraPosVec(1).add(getRotationVec(1).normalize()));

            for (BlockPos pos : BlockPos.iterate(center.add(-1, -1, -1), center.add(1, 1, 1))) {
                if (IMLandPathNodeMaker.canMineBlock(this, pos)) {
                    sound = true;
                    if (mobgriefing) {
                        getWorld().breakBlock(pos, InvasionMod.getConfig().destructedBlocksDrop);
                    }

                    for (int i = 0; i < 10; i++) {
                        double x = getRandom().nextTriangular(pos.getX() + 0.5, 0.5);
                        double y = getRandom().nextTriangular(pos.getY() + 0.5, 0.5);
                        double z = getRandom().nextTriangular(pos.getZ() + 0.5, 0.5);
                        getWorld().addParticle(ParticleTypes.CLOUD,
                                x, y, z,
                                pos.getX() + 0.5 - x,
                                pos.getY() + 0.5 - y,
                                pos.getZ() + 0.5 - z
                        );
                    }
                }
            }
            if (sound) {
                playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.2F, 0.5F);
            }
        }
    }

    @Override
    public void updateAnimation(boolean override) {

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
