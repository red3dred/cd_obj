package com.invasion.entity;

import java.util.List;

import com.invasion.InvSounds;
import com.invasion.entity.ai.goal.AttackNexusGoal;
import com.invasion.entity.ai.goal.GoToNexusGoal;
import com.invasion.entity.ai.goal.MineBlockGoal;
import com.invasion.entity.ai.goal.MobMeleeAttackGoal;
import com.invasion.entity.ai.goal.SprintGoal;
import com.invasion.entity.ai.goal.target.CustomRangeActiveTargetGoal;
import com.invasion.entity.ai.goal.target.RetaliateGoal;
import com.invasion.entity.ai.goal.StoopGoal;
import com.invasion.entity.ai.goal.ProvideSupportGoal;
import com.invasion.entity.ai.goal.NoNexusPathGoal;
import com.invasion.entity.ai.goal.PredicatedGoal;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityIMZombie extends AbstractIMZombieEntity {
    static final int OLD_ZOMBIE = 0;
    static final int ZOMBIE = 1;
    static final int ZOMBIE_T2 = 2;
    static final int ZOMBIE_PIGMAN = 3;
    static final int ZOMBIE_T2A = 4;
    static final int TAR = 5;
    static final int BRUTE = 6;

    // TODO: Account for self-harm when breaking blocks
    // selfDamage = points of damage dealt per block broken
    // masSelfDamage = max damage loss before the mob can no longer mine blocks. If health is less than (maxHealth - maxSelfDamage) it stops.
    protected int selfDamage = 2;
    protected int maxSelfDamage = 6;

    private static DefaultAttributeContainer.Builder createBaseAttributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.19F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0);
    }

    public static DefaultAttributeContainer.Builder createTierT1V0Attributes() {
        return createBaseAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0);
    }

    public static DefaultAttributeContainer.Builder createTierT1V1Attributes() {
        return createBaseAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0);
    }

    public static DefaultAttributeContainer.Builder createTierT2V0Attributes() {
        return createBaseAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0);
    }

    public static DefaultAttributeContainer.Builder createTierT2V1Attributes() {
        return createBaseAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0);
    }

    /**
     * Tar Zombie
     */
    public static DefaultAttributeContainer.Builder createTierT2V2ttributes() {
        return createBaseAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
    }
    /**
     * Zombie Pigman
     */
    public static DefaultAttributeContainer.Builder createTierT2V3ttributes() {
        return createBaseAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0);
    }
    /**
     * Zombie Brute
     */
    public static DefaultAttributeContainer.Builder createTierT3V0Attributes() {
        return createBaseAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.17F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 18.0);
    }

    public EntityIMZombie(EntityType<EntityIMZombie> type, World world) {
        super(type, world, 2F);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new PredicatedGoal(new SwimGoal(this), () -> getTier() != 2 || getFlavour() != 2));
        goalSelector.add(0, new MineBlockGoal(this));
        goalSelector.add(1, new MobMeleeAttackGoal(this, 40, false));
        goalSelector.add(2, new AttackNexusGoal<>(this));
        goalSelector.add(3, new ProvideSupportGoal(this, 4.0F, true));
        goalSelector.add(3, new PredicatedGoal(new SprintGoal<>(this), () -> getTier() == 3));
        goalSelector.add(4, new PredicatedGoal(new StoopGoal(this), () -> getTier() == 3));
        goalSelector.add(5, new GoToNexusGoal(this));
        goalSelector.add(6, new WanderAroundFarGoal(this, 1));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        goalSelector.add(8, new LookAtEntityGoal(this, IMCreeperEntity.class, 12));
        goalSelector.add(8, new LookAroundGoal(this));

        targetSelector.add(0, new RetaliateGoal(this));
        targetSelector.add(1, new PredicatedGoal(new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getSenseRange, false), () -> getTier() != 3));
        targetSelector.add(2, new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getAggroRange, true));
        targetSelector.add(3, new PredicatedGoal(new CustomRangeActiveTargetGoal<>(this, PigmanEngineerEntity.class, 3.5F), () -> getTier() != 3 && NoNexusPathGoal.isLostPathToNexus(this)));
        targetSelector.add(4, new CustomRangeActiveTargetGoal<>(this, MerchantEntity.class, this::getAggroRange, true));
        targetSelector.add(4, new CustomRangeActiveTargetGoal<>(this, IronGolemEntity.class, this::getAggroRange, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient && flammability >= 20 && isOnFire()) {
            doFireball();
        }
    }

    public boolean isBrute() {
        return getTier() == 3;
    }

    public boolean isTar() {
        return getTier() == 2 && getFlavour() == 2;
    }

    public boolean isPigman() {
        return getTier() == 2 && getFlavour() == 3;
    }

    @Override
    public float getSoundPitch() {
        return super.getSoundPitch() * (isBrute() ? 0.75F : 1);
    }

    @Override
    protected Text getDefaultName() {
        if (isTar()) {
            return Text.translatable(getType().getUntranslatedName() + ".tar");
        }
        if (isPigman()) {
            return Text.translatable(getType().getUntranslatedName() + ".pigman");
        }
        if (isBrute()) {
            return Text.translatable(getType().getUntranslatedName() + ".brute");
        }
        return super.getDefaultName();
    }

    @Override
    protected void initTieredAttributes() {
        setBaseMovementSpeed(0.19F);
        setAttackStrength(4);

        if (getTier() == 1) {
            maxSelfDamage = 6;
            selfDamage = 3;
            flammability = 3;

            if (getFlavour() == 0) {
                setCanDestroyBlocks(true);
            } else if (getFlavour() == 1) {
                setAttackStrength(6);
                setStackInHand(Hand.MAIN_HAND, Items.WOODEN_SWORD.getDefaultStack());
                setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.2F);
                setCanDestroyBlocks(false);
            }
        } else if (getTier() == 2) {
            setBaseMovementSpeed(0.19F);
            if (getFlavour() == 0) {
                setAttackStrength(7);
                selfDamage = 4;
                maxSelfDamage = 12;
                flammability = 4;
                equipStack(EquipmentSlot.CHEST, Items.IRON_CHESTPLATE.getDefaultStack());
                setEquipmentDropChance(EquipmentSlot.CHEST, 0.25F);
                setCanDestroyBlocks(true);
            } else if (getFlavour() == 1) {
                setAttackStrength(10);
                selfDamage = 3;
                maxSelfDamage = 9;
                setStackInHand(Hand.MAIN_HAND, Items.IRON_SWORD.getDefaultStack());
                setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.25F);
                setCanDestroyBlocks(false);
            }
        }

        if (isTar()) {
            setAttackStrength(5);
            selfDamage = 3;
            maxSelfDamage = 9;
            flammability = 30;
            setCanDestroyBlocks(true);
        }

        if (isPigman()) {
            setBaseMovementSpeed(0.25F);
            setAttackStrength(8);
            setFireImmune(true);
            setStackInHand(Hand.MAIN_HAND, Items.GOLDEN_SWORD.getDefaultStack());
            setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.2F);
            setCanDestroyBlocks(true);
        }

        if (isBrute()) {
            setBaseMovementSpeed(0.17F);
            setAttackStrength(18);
            selfDamage = 4;
            maxSelfDamage = 20;
            flammability = 4;
            equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            setEquipmentDropChance(EquipmentSlot.MAINHAND, 0);
            setCanDestroyBlocks(true);
        }
    }

    @Override
    public int getTextureId() {
        return switch(getTier()) {
            case 2 -> switch(getFlavour()) {
                case 2 -> TAR;
                case 3 -> ZOMBIE_PIGMAN;
                default -> (((int)(Math.abs(getUuid().getLeastSignificantBits()) % 2)) + 1) * 2; //2,4
            };
            case 3 -> BRUTE;
            default -> (int)(Math.abs(getUuid().getLeastSignificantBits()) % 2); // 0,1
        };
    }

    @Override
    protected void sunlightDamageTick() {
        if (isTar()) {
            damage(getDamageSources().generic(), 3);
        } else {
            super.sunlightDamageTick();
        }
    }

    @Override
    public void updateAnimation(boolean override) {
    }

    @Override
    protected void updateSound() {
        if (terrainModifier.isBusy()) {
            super.updateSound();
        }
    }

    @Override
    public SoundEvent getAmbientSound() {
        if (super.getTier() == 3) {
            return getRandom().nextInt(3) == 0 ? InvSounds.ENTITY_BIG_ZOMBIE_AMBIENT : null;
        }

        return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_DEATH;
    }

    private void doFireball() {
        for (BlockPos pos : BlockPos.iterateOutwards(getBlockPos(), 2, 2, 2)) {
            if (getWorld().isAir(pos) && getWorld().getBlockState(pos.down()).isBurnable()) {
                getWorld().setBlockState(pos, Blocks.FIRE.getDefaultState());
            }
        }

        List<Entity> entities = getWorld().getOtherEntities(this, getBoundingBox().expand(1.5, 1.5, 1.5));
        for (int el = entities.size() - 1; el >= 0; el--) {
            entities.get(el).setFireTicks(8);
        }
        damage(getDamageSources().explosion(this, this), 500);
    }
}
