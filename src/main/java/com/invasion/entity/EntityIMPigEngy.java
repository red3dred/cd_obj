package com.invasion.entity;

import com.invasion.INotifyTask;
import com.invasion.block.BlockMetadata;
import com.invasion.entity.ai.Goal;
import com.invasion.entity.ai.builder.ITerrainBuild;
import com.invasion.entity.ai.builder.ITerrainDig;
import com.invasion.entity.ai.builder.TerrainBuilder;
import com.invasion.entity.ai.builder.TerrainDigger;
import com.invasion.entity.ai.builder.TerrainModifier;
import com.invasion.entity.ai.goal.EntityAIAttackNexus;
import com.invasion.entity.ai.goal.EntityAIGoToNexus;
import com.invasion.entity.ai.goal.EntityAIKillEntity;
import com.invasion.entity.ai.goal.EntityAISimpleTarget;
import com.invasion.entity.ai.goal.EntityAIWanderIM;
import com.invasion.entity.pathfinding.INavigation;
import com.invasion.entity.pathfinding.NavigatorEngy;
import com.invasion.entity.pathfinding.Path;
import com.invasion.entity.pathfinding.PathAction;
import com.invasion.entity.pathfinding.PathCreator;
import com.invasion.item.InvItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

public class EntityIMPigEngy extends EntityIMMob implements ICanDig {
    private final TerrainModifier terrainModifier = new TerrainModifier(this, 2.8F);
    private final TerrainDigger terrainDigger = new TerrainDigger(this, terrainModifier, 1);
    private final TerrainBuilder terrainBuilder = new TerrainBuilder(this, terrainModifier, 1);

    private int askForScaffoldTimer;
    private float supportThisTick;

    public EntityIMPigEngy(EntityType<EntityIMPigEngy> type, World world) {
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
    protected INavigation createIMNavigation() {
        return new NavigatorEngy(this, new PathCreator(1200, 1500));
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new EntityAIKillEntity<>(this, PlayerEntity.class, 60));
        goalSelector.add(2, new EntityAIAttackNexus(this));
        goalSelector.add(3, new EntityAIGoToNexus(this));
        goalSelector.add(7, new EntityAIWanderIM(this));
        goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 7));
        goalSelector.add(9, new LookAtEntityGoal(this, EntityIMCreeper.class, 12));
        goalSelector.add(9, new LookAroundGoal(this));

        if (hasNexus()) {
            targetSelector.add(1, new EntityAISimpleTarget<>(this, PlayerEntity.class, 3, true));
        } else {
            targetSelector.add(1, new EntityAISimpleTarget<>(this, PlayerEntity.class, this::getSenseRange, false));
            targetSelector.add(2, new EntityAISimpleTarget<>(this, PlayerEntity.class, this::getAggroRange, true));
        }
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
        askForScaffoldTimer++;
        if (hasNexus()) {
            int yDifference = getNexus().getOrigin().getY() - getBlockPos().getY();
            int weight = yDifference > 1 ? Math.max(6000 / yDifference, 1) : 1;
            if (getAIGoal() == Goal.BREAK_NEXUS && ((getNavigatorNew().getLastPathDistanceToTarget() > 2 && askForScaffoldTimer <= 0) || getRandom().nextInt(weight) == 0)) {
                if (getNexus().getAttackerAI().askGenerateScaffolds(this)) {
                    getNavigatorNew().clearPath();
                    askForScaffoldTimer = 60;
                } else {
                    askForScaffoldTimer = 140;
                }
            }
        }
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
    public void onPathSet() {
        terrainModifier.cancelTask();
    }

    @Override
    public boolean onPathBlocked(Path path, INotifyTask notifee) {
        return !path.isFinished() && terrainDigger
                .askClearPosition(path.getPathPointFromIndex(path.getCurrentPathIndex()).pos, notifee, 1);
    }

    public ITerrainBuild getTerrainBuildEngy() {
        return terrainBuilder;
    }

    public ITerrainDig getTerrainDig() {
        return terrainDigger;
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

    public void supportForTick(EntityIMLiving entity, float amount) {
        supportThisTick += amount;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public static boolean canPlaceLadderAt(BlockView map, BlockPos pos) {
        if (BlockMetadata.isIndestructible(map.getBlockState(pos))) {
            BlockPos.Mutable mutable = pos.mutableCopy();
            if (map.getBlockState(mutable.set(pos).move(1, 0, 0)).isFullCube(map, mutable)
                    || map.getBlockState(mutable.set(pos).move(-1, 0, 0)).isFullCube(map, mutable)
                    || map.getBlockState(mutable.set(pos).move(0, 0, 1)).isFullCube(map, mutable)
                    || map.getBlockState(mutable.set(pos).move(0, 0, -1)).isFullCube(map, mutable)) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    @Override
    public String getLegacyName() {
        return "IMPigManEngineer-T1";
    }
}