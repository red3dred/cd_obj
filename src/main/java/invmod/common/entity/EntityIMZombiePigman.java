package invmod.common.entity;

import org.jetbrains.annotations.Nullable;

import invmod.common.InvSounds;
import invmod.common.InvasionMod;
import invmod.common.block.InvBlocks;
import invmod.common.entity.ai.EntityAIAttackNexus;
import invmod.common.entity.ai.EntityAICharge;
import invmod.common.entity.ai.EntityAIGoToNexus;
import invmod.common.entity.ai.EntityAIKillEntity;
import invmod.common.entity.ai.EntityAISimpleTarget;
import invmod.common.entity.ai.EntityAITargetOnNoNexusPath;
import invmod.common.entity.ai.EntityAITargetRetaliate;
import invmod.common.entity.ai.EntityAIWaitForEngy;
import invmod.common.entity.ai.EntityAIWanderIM;
import invmod.common.nexus.EntityConstruct;
import invmod.common.nexus.INexusAccess;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class EntityIMZombiePigman extends AbstractIMZombieEntity {
    private static final TrackedData<Boolean> CHARGING = DataTracker.registerData(EntityIMZombiePigman.class, TrackedDataHandlerRegistry.BOOLEAN);

    public EntityIMZombiePigman(EntityType<EntityIMZombiePigman> type, World world) {
        this(type, world, null);
    }

    public EntityIMZombiePigman(EntityType<EntityIMZombiePigman> type, World world, INexusAccess nexus) {
        super(type, world, nexus, 0.75F);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CHARGING, false);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(2, new EntityAIKillEntity<>(this, PlayerEntity.class, 40));
        goalSelector.add(3, new EntityAIAttackNexus(this));
        goalSelector.add(4, new EntityAIWaitForEngy(this, 4.0F, true));
        goalSelector.add(5, new EntityAIKillEntity<>(this, LivingEntity.class, 40));
        goalSelector.add(6, new EntityAIGoToNexus(this));
        goalSelector.add(7, new EntityAIWanderIM(this));
        goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        goalSelector.add(9, new LookAtEntityGoal(this, EntityIMCreeper.class, 12.0F));
        goalSelector.add(9, new LookAroundGoal(this));

        targetSelector.add(0, new EntityAITargetRetaliate<>(this, LivingEntity.class, getAggroRange()));
        targetSelector.add(2, new EntityAISimpleTarget<>(this, PlayerEntity.class, getAggroRange(), true));
        targetSelector.add(5, new RevengeGoal(this));

        if (getTier() == 3) {
            // goalSelector.add(4, new EntityAIStoop(this));
            goalSelector.add(1, new EntityAICharge<>(this, PlayerEntity.class, 0.75F));
        } else {
            // track players from sensing them
            targetSelector.add(1, new EntityAISimpleTarget<>(this, PlayerEntity.class, getAggroRange(), false));
            targetSelector.add(3, new EntityAITargetOnNoNexusPath<>(this, EntityIMPigEngy.class, 3.5F));
        }
    }

    @Override
    public void onSpawned(INexusAccess nexus, EntityConstruct spawnConditions) {
        super.onSpawned(nexus, spawnConditions);
        setTexture(spawnConditions.texture());
        setTier(spawnConditions.tier());
    }

    public boolean isCharging() {
        return dataTracker.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        dataTracker.set(CHARGING, charging);
    }

    @Override
    protected void updateTexture() {
        setTexture(MathHelper.clamp(getTier() - 1, 0, 2));
    }

    @Override
    public boolean isBigRenderTempHack() {
        return getTier() == 3;
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
            this.swingTimer = 0;
        }
        handSwingProgress = (float) this.swingTimer / (float) swingSpeed;

        if (isCharging()) {
            this.limbAnimator.updateLimbs(0.5F, 1);
            if (!getWorld().isClient) {
                for (BlockPos pos : BlockPos.iterateOutwards(getBlockPos(), 2, 2, 2)) {
                    BlockState block = getWorld().getBlockState(pos);
                    boolean mobgriefing = getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);

                    if (!block.isAir()) {
                        if (isBlockDestructible(getWorld(), pos, block) && !block.isOf(InvBlocks.NEXUS_CORE)) {
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

    protected int getSwingSpeed() {
        return 10;
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
    protected void setAttributes(int tier, int flavour) {
        if (tier == 1) {
            setName("Zombie Pigman");
            setMovementSpeed(0.25F);
            setAttackStrength(8);
            setFireImmune(true);
            equipStack(EquipmentSlot.MAINHAND, Items.GOLDEN_SWORD.getDefaultStack());
            setCanDestroyBlocks(true);
            setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));

        } else if (tier == 2) {
            setName("Zombie Pigman");
            setMovementSpeed(0.35F);
            setAttackStrength(12);
            setCanDestroyBlocks(true);
            setFireImmune(true);
            setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));

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
        } else if (tier == 3) {
            setName("Zombie Pigman Brute");
            setMovementSpeed(0.20F);
            setAttackStrength(18);
            setFireImmune(true);
            setCanDestroyBlocks(true);
            setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
        }
    }
}
