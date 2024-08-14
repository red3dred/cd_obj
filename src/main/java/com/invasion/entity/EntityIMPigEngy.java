package com.invasion.entity;

import com.invasion.IBlockAccessExtended;
import com.invasion.INotifyTask;
import com.invasion.InvasionMod;
import com.invasion.block.DestructableType;
import com.invasion.block.InvBlocks;
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
import com.invasion.entity.pathfinding.IPathSource;
import com.invasion.entity.pathfinding.NavigatorEngy;
import com.invasion.entity.pathfinding.Path;
import com.invasion.entity.pathfinding.PathAction;
import com.invasion.entity.pathfinding.PathCreator;
import com.invasion.entity.pathfinding.PathNode;
import com.invasion.entity.pathfinding.PathfinderIM;
import com.invasion.entity.pathfinding.PathAction.Type;
import com.invasion.item.InvItems;
import com.invasion.nexus.INexusAccess;
import com.invasion.util.math.CoordsInt;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

public class EntityIMPigEngy extends EntityIMMob implements ICanDig {
    private static final int MAX_LADDER_TOWER_HEIGHT = 4;

    private final TerrainModifier terrainModifier = new TerrainModifier(this, 2.8F);
    private final TerrainDigger terrainDigger = new TerrainDigger(this, terrainModifier, 1);
    private final TerrainBuilder terrainBuilder = new TerrainBuilder(this, terrainModifier, 1);

    private int planks = 15;
    private int askForScaffoldTimer;
    private int tier = 1;
    private float supportThisTick;

    public EntityIMPigEngy(EntityType<EntityIMPigEngy> type, World world) {
        this(type, world, null);
    }

    public EntityIMPigEngy(EntityType<EntityIMPigEngy> type, World world, INexusAccess nexus) {
        super(type, world, nexus);
        setMovementSpeed(0.23F);
        setAttackStrength(2);
        selfDamage = 0;
        maxSelfDamage = 0;
        setCanDestroyBlocks(true);
        setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
        setCanDestroyBlocks(true);
        setJumpHeight(1);
        setCanClimb(false);
    }

    @Override
    protected IPathSource createPathSource() {
        return new PathCreator(1200, 1500);
    }

    @Override
    protected INavigation createIMNavigation(IPathSource pathSource) {
        return new NavigatorEngy(this, pathSource);
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
            targetSelector.add(1, new EntityAISimpleTarget<>(this, PlayerEntity.class, getSenseRange(), false));
            targetSelector.add(2, new EntityAISimpleTarget<>(this, PlayerEntity.class, getAggroRange(), true));
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
            if (currentGoal == Goal.BREAK_NEXUS && ((getNavigatorNew().getLastPathDistanceToTarget() > 2 && askForScaffoldTimer <= 0) || getRandom().nextInt(weight) == 0)) {
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
    public BlockView getTerrain() {
        return getWorld();
    }

    @Override
    public BlockPos toBlockPos() {
        return getBlockPos();
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

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public float getBlockRemovalCost(BlockPos pos) {
        return getBlockStrength(pos) * 20;
    }

    @Override
    public boolean canClearBlock(BlockPos pos) {
        BlockState block = getWorld().getBlockState(pos);
        return block.isAir() || isBlockDestructible(getWorld(), pos, block);
    }

    @Override
    public boolean avoidsBlock(BlockState state) {
        return !avoidsBlock(state) || state.isIn(BlockTags.DOORS);
    }

    public void supportForTick(EntityIMLiving entity, float amount) {
        supportThisTick += amount;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public float getBlockPathCost(PathNode prevNode, PathNode node, BlockView terrainMap) {
        // TODO: why?
        if ((node.pos.getX() == -21) && (node.pos.getZ() == 180)) {
            planks = 10;
        }
        BlockState block = terrainMap.getBlockState(node.pos);
        float materialMultiplier = !block.isAir() && isBlockDestructible(terrainMap, node.pos, block) ? 3.2F : 1;

        if (node.action.getType() == PathAction.Type.BRIDGE) {
            return prevNode.distanceTo(node) * 1.7F * materialMultiplier;
        }

        if (node.action.getType() == PathAction.Type.SCAFFOLD) {
            return prevNode.distanceTo(node) * 0.5F;
        }

        if (node.action.getType() == PathAction.Type.LADDER && node.action.getBuildDirection() != Direction.UP) {
            return prevNode.distanceTo(node) * 1.3F * materialMultiplier;
        }

        if (node.action.getType() == PathAction.Type.TOWER) {
            return prevNode.distanceTo(node) * 1.4F;
        }

        float multiplier = 1 + (IBlockAccessExtended.getData(terrainMap, node.pos) & IBlockAccessExtended.MOB_DENSITY_FLAG);

        if (block.isAir() || block.isReplaceable()) {
            return prevNode.distanceTo(node) * multiplier;
        }
        if (block.isOf(Blocks.LADDER)) {
            return prevNode.distanceTo(node) * 0.7F * multiplier;
        }
        if (!block.isOf(InvBlocks.NEXUS_CORE) && !block.isSolidBlock(terrainMap, node.pos)) {
            return prevNode.distanceTo(node) * 3.2F;
        }

        return super.getBlockPathCost(prevNode, node, terrainMap);
    }

    @Override
    public void getPathOptionsFromNode(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        super.getPathOptionsFromNode(terrainMap, currentNode, pathFinder);
        if (planks <= 0) {
            return;
        }

        BlockPos.Mutable mutable = currentNode.pos.mutableCopy();
        for (Direction offset : CoordsInt.CARDINAL_DIRECTIONS) {
            if (getCollide(terrainMap, mutable.set(currentNode.pos).move(offset)) > DestructableType.UNBREAKABLE) {
                for (int yOffset = 0; yOffset > -MAX_LADDER_TOWER_HEIGHT; yOffset--) {
                    BlockState block = terrainMap.getBlockState(mutable.set(currentNode.pos).move(offset).move(Direction.UP, yOffset - 1));
                    if (!block.isAir()) {
                        break;
                    }
                    pathFinder.addNode(mutable.set(currentNode.pos).move(offset).move(Direction.UP, yOffset).toImmutable(), PathAction.BRIDGE);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void calcPathOptionsVertical(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        // TODO: why?
        if (currentNode.pos.getX() == -11 && currentNode.pos.getZ() == 177) {
            planks = 10;
        }
        super.calcPathOptionsVertical(terrainMap, currentNode, pathFinder);
        if (planks <= 0) {
            return;
        }

        BlockPos.Mutable mutable = currentNode.pos.mutableCopy().move(Direction.UP);

        if (getCollide(terrainMap, mutable) > DestructableType.UNBREAKABLE) {
            if (terrainMap.getBlockState(mutable).isAir()) {
                if (currentNode.action == PathAction.NONE) {
                    addAnyLadderPoint(terrainMap, currentNode, pathFinder);
                } else if (!continueLadder(terrainMap, currentNode, pathFinder)) {
                    addAnyLadderPoint(terrainMap, currentNode, pathFinder);
                }

            }

            if ((currentNode.action == PathAction.NONE) || (currentNode.action == PathAction.BRIDGE)) {
                int maxHeight = 4;
                for (int i = getCollideSize().getYCoord(); i < 4; i++) {
                    BlockState block = terrainMap.getBlockState(mutable.set(currentNode.pos).move(Direction.UP, i));
                    if (!block.isAir() && !block.blocksMovement()) {
                        maxHeight = i - getCollideSize().getYCoord();
                        break;
                    }

                }

                for (Direction facing : CoordsInt.CARDINAL_DIRECTIONS) {
                    BlockState block = terrainMap.getBlockState(mutable.set(currentNode.pos).move(facing));
                    if (block.isFullCube(terrainMap, mutable)) {
                        for (int height = 0; height < maxHeight; height++) {
                            block = terrainMap.getBlockState(mutable.set(currentNode.pos).move(facing).move(Direction.UP, height));
                            if (!block.isAir()) {
                                if (!block.isFullCube(terrainMap, mutable)) {
                                    break;
                                }
                                pathFinder.addNode(currentNode.pos.up(), PathAction.getLadderActionForDirection(facing));
                                break;
                            }
                        }
                    }
                }
            }

        }

        if (IBlockAccessExtended.getData(terrainMap, currentNode.pos.up()) == IBlockAccessExtended.EXT_DATA_SCAFFOLD_METAPOSITION) {
            pathFinder.addNode(currentNode.pos.up(), PathAction.SCAFFOLD_UP);
        }
    }

    protected void addAnyLadderPoint(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        BlockPos.Mutable mutable = currentNode.pos.mutableCopy();
        for (Direction facing : CoordsInt.CARDINAL_DIRECTIONS) {
            if (terrainMap.getBlockState(mutable.set(currentNode.pos).move(Direction.UP).move(facing)).isFullCube(terrainMap, mutable)) {
                pathFinder.addNode(currentNode.pos.up(), PathAction.getLadderActionForDirection(facing));
            }
        }
    }

    protected boolean continueLadder(BlockView terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        if (currentNode.action.getType() != Type.LADDER || currentNode.action.getBuildDirection() == Direction.UP) {
            return false;
        }

        BlockPos pos = currentNode.pos.offset(currentNode.action.getBuildDirection(), 1).up();
        if (terrainMap.getBlockState(pos).isFullCube(terrainMap, pos)) {
            pathFinder.addNode(currentNode.pos.up(), currentNode.action);
        }
        return true;
    }

    public static boolean canPlaceLadderAt(BlockView map, BlockPos pos) {
        if (EntityIMLiving.UNDESTRUCTABLE_BLOCKS.contains(map.getBlockState(pos).getBlock())) {
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
        return "IMPigManEngineer-T" + getTier();
    }
}