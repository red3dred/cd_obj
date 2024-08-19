package com.invasion.entity;

import com.invasion.IBlockAccessExtended;
import com.invasion.Notifiable;
import com.invasion.InvSounds;
import com.invasion.block.DestructableType;
import com.invasion.entity.ai.builder.ITerrainDig;
import com.invasion.entity.ai.builder.TerrainDigger;
import com.invasion.entity.ai.builder.TerrainModifier;
import com.invasion.entity.pathfinding.Actor;
import com.invasion.entity.pathfinding.Navigation;
import com.invasion.entity.pathfinding.IMNavigation;
import com.invasion.entity.pathfinding.Path;
import com.invasion.entity.pathfinding.PathAction;
import com.invasion.entity.pathfinding.PathCreator;
import com.invasion.entity.pathfinding.PathNode;
import com.invasion.util.math.PosUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public abstract class AbstractIMZombieEntity extends TieredIMMobEntity implements Miner {
    private static final TrackedData<Boolean> SWINGING = DataTracker.registerData(AbstractIMZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    protected final TerrainModifier terrainModifier;
    protected final TerrainDigger terrainDigger;

    protected int swingTimer;
    protected int scrapeSoundCooldown;

    private boolean fireImmune;

    protected AbstractIMZombieEntity(EntityType<? extends AbstractIMZombieEntity> type, World world, float diggingSpeed) {
        super(type, world);
        terrainModifier = new TerrainModifier(this, diggingSpeed);
        terrainDigger = new TerrainDigger(this, terrainModifier, 1);
    }

    @Override
    protected Navigation createIMNavigation() {
        return new IMNavigation(this, new PathCreator(700, 50)) {
            @Override
            protected <T extends Entity> Actor<T> createActor(T entity) {
                return new Actor<>(entity) {
                    @Override
                    public float getPathNodePenalty(PathNode prevNode, PathNode node, BlockView terrainMap) {
                        if (getTier() == 2 && getFlavour() == 2 && node.action == PathAction.SWIM) {
                            float multiplier = 1 + (IBlockAccessExtended.getData(terrainMap, node.pos) & IBlockAccessExtended.MOB_DENSITY_FLAG) * 3;

                            if (node.pos.getY() > prevNode.pos.getY() && getNodeDestructability(terrainMap, node.pos) == DestructableType.DESTRUCTABLE) {
                                multiplier += 2;
                            }

                            return prevNode.distanceTo(node) * 1.2F * multiplier;
                        }

                        return super.getPathNodePenalty(prevNode, node, terrainMap);
                    }

                    @Override
                    public boolean isBlockDestructible(BlockView terrainMap, BlockPos pos, BlockState block) {
                        return super.isBlockDestructible(terrainMap, pos, block)
                                && getCurrentTargetPos().isPresent()
                                && PosUtils.getInclination(getCurrentTargetPos().get(), pos) <= 2.144D;
                    }
                };
            }
        };
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SWINGING, false);
    }

    protected ITerrainDig getTerrainDig() {
        return this.terrainDigger;
    }

    protected boolean isSwinging() {
        return dataTracker.get(SWINGING);
    }

    protected void setSwinging(boolean swinging) {
        dataTracker.set(SWINGING, swinging);
    }

    @Override
    public boolean isFireImmune() {
        return fireImmune || super.isFireImmune();
    }

    protected void setFireImmune(boolean fireImmune) {
        this.fireImmune = fireImmune;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        updateAnimation(false);
        updateSound();
    }

    protected void updateSound() {
        if (terrainModifier.isBusy() && --scrapeSoundCooldown <= 0) {
            playSound(InvSounds.ENTITY_SCRAPE, 0.85F, 1 / (getRandom().nextFloat() * 0.5F + 1));
            scrapeSoundCooldown = 45 + getRandom().nextInt(20);
        }
    }

    public abstract void updateAnimation(boolean override);


    public abstract int getTextureId();

    protected int getSwingSpeed() {
        return 10;
    }


    @Override
    public void mobTick() {
        super.mobTick();
        terrainModifier.onUpdate();
    }

    @Override
    public void onPathSet() {
        terrainModifier.cancelTask();
    }

    @Override
    public boolean isPushable() {
        return super.getTier() != 3;
    }

    @Override
    protected int getNextAirUnderwater(int air) {
        if (getTier() == 2 && getFlavour() == 2) {
            return getNextAirOnLand(air);
        }
        return super.getNextAirUnderwater(air);
    }

    @Override
    public boolean tryAttack(Entity entity) {
        return getTier() == 3 && isSprinting() ? chargeAttack(entity) : super.tryAttack(entity);
    }

    protected boolean chargeAttack(Entity entity) {
        int knockback = 4;
        entity.damage(getDamageSources().mobAttack(this), (float)getAttackStrength() + 3);
        float yaw = getYaw() * MathHelper.RADIANS_PER_DEGREE;
        if (entity instanceof LivingEntity l) {
            l.takeKnockback(knockback, MathHelper.sin(yaw), MathHelper.cos(yaw));
        }
        setSprinting(false);
        playSound(SoundEvents.ENTITY_GENERIC_BIG_FALL, 1, 1);
        return true;
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
        if (getTier() != 3) {
            super.takeKnockback(strength, x, z);
        }
    }

    @Override
    public void onFollowingEntity(Entity entity) {
        getNavigatorNew().getNodeMaker().setCanDestroyBlocks(entity instanceof PigmanEngineerEntity || entity instanceof IMCreeperEntity);
    }

    @Override
    public boolean onPathBlocked(Path path, Notifiable notifee) {
        if (!path.isFinished() && (hasNexus() || getAttacking() != null)) {
            if (path.getFinalPathPoint().distanceTo(path.getIntendedTarget()) > 2.2D && path.getCurrentPathIndex() + 2 >= path.getCurrentPathLength() / 2) {
                return false;
            }
            PathNode node = path.getPathPointFromIndex(path.getCurrentPathIndex());

            if (terrainDigger.askClearPosition(node.pos, notifee, 1)) {
                return true;
            }
        }
        return false;
    }

    public float scaleAmount() {
        if (getTier() == 2)
            return 1.12F;
        if (getTier() == 3) {
            return 1.21F;
        }
        return 1.0F;
    }
}
