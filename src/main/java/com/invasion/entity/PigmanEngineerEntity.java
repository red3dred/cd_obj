package com.invasion.entity;

import com.invasion.Notifiable;
import com.invasion.entity.ai.builder.TerrainBuilder;
import com.invasion.entity.ai.builder.TerrainModifier;
import com.invasion.entity.ai.goal.AttackNexusGoal;
import com.invasion.entity.ai.goal.GoToNexusGoal;
import com.invasion.entity.ai.goal.MineBlockGoal;
import com.invasion.entity.ai.goal.MobMeleeAttackGoal;
import com.invasion.entity.ai.goal.PredicatedGoal;
import com.invasion.entity.ai.goal.target.CustomRangeActiveTargetGoal;
import com.invasion.entity.pathfinding.BuilderIMMobNavigation;
import com.invasion.entity.pathfinding.path.PathAction;
import com.invasion.item.InvItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

public class PigmanEngineerEntity extends IMMobEntity implements Miner {
    private final TerrainModifier terrainModifier = new TerrainModifier(this, 2.8F);
    private final TerrainBuilder terrainBuilder = new TerrainBuilder(this, 1);

    private float supportThisTick;

    public PigmanEngineerEntity(EntityType<PigmanEngineerEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0)
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new BuilderIMMobNavigation(this);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(0, new MineBlockGoal(this));
        goalSelector.add(1, new MobMeleeAttackGoal(this, 1, false));
        goalSelector.add(2, new AttackNexusGoal<>(this));
        goalSelector.add(3, new GoToNexusGoal(this));
        goalSelector.add(7, new WanderAroundFarGoal(this, 1));
        goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 7));
        goalSelector.add(9, new LookAtEntityGoal(this, IMCreeperEntity.class, 12));
        goalSelector.add(9, new LookAroundGoal(this));

        targetSelector.add(1, new PredicatedGoal(new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, 3, true), this::hasNexus));
        targetSelector.add(1, new PredicatedGoal(new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getSenseRange, false), () -> !hasNexus()));
        targetSelector.add(2, new PredicatedGoal(new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getAggroRange, true), () -> !hasNexus()));
        targetSelector.add(3, new RevengeGoal(this));
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        Item heldItem = switch (getRandom().nextInt(3)) {
            case 0 -> Items.LADDER;
            case 1 -> Items.IRON_PICKAXE;
            default -> InvItems.ENGY_HAMMER;
        };
        equipStack(EquipmentSlot.MAINHAND, heldItem.getDefaultStack());
    }

    @Override
    public float getMaxSelfDamage() {
        return 0;
    }

    @Override
    public float getSelfDamage() {
        return 0;
    }

    @Override
    public void mobTick() {
        super.mobTick();
        terrainModifier.onUpdate();
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        terrainBuilder.setBuildRate(1 + supportThisTick * 0.33F);
        supportThisTick = 0;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        updateAnimation();
    }

    protected void updateAnimation() {
        if (!getWorld().isClient && terrainModifier.isBusy()) {
            swingHand(Hand.MAIN_HAND);
            PathAction currentAction = getNavigatorNew().getCurrentWorkingAction();
            if (currentAction == PathAction.NONE) {
                equipStack(EquipmentSlot.MAINHAND, Items.IRON_PICKAXE.getDefaultStack());
            } else {
                equipStack(EquipmentSlot.MAINHAND, InvItems.ENGY_HAMMER.getDefaultStack());
            }
        }
    }

    @Override
    public boolean handlePathAction(BlockPos pos, PathAction action, Notifiable asker) {
        if (action.getType() == PathAction.Type.BRIDGE) {
            return terrainModifier.submitJob(pos, asker, terrainBuilder::askBuildBridge);
        }

        if (action.getType() == PathAction.Type.SCAFFOLD) {
            return terrainModifier.submitJob(pos, asker, terrainBuilder::askBuildScaffoldLayer);
        }

        if (action.getType() == PathAction.Type.LADDER) {
            Direction direction = action.getOrientation();
            if (direction == Direction.UP) {
                return terrainModifier.submitJob(pos, asker, p -> terrainBuilder.askBuildLadderTower(p, direction, (int)getRandom().nextTriangular(10, 4)));
            }
            return terrainModifier.submitJob(pos, asker, terrainBuilder::askBuildLadder);
        }
        return true;
    }

    @Override
    public void onPathSet() {
        terrainModifier.cancelTask();
    }

    @Override
    protected SoundEvent getAmbientSound() {
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

    public void supportForTick(MobEntity entity, float amount) {
        supportThisTick += amount;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Deprecated
    @Override
    public String getLegacyName() {
        return "IMPigManEngineer-T1";
    }
}