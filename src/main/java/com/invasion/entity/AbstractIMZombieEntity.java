package com.invasion.entity;

import com.invasion.entity.pathfinding.IMLandPathNodeMaker;
import com.invasion.entity.pathfinding.IMMobNavigation;
import com.invasion.nexus.ai.scaffold.ScaffoldView;
import com.invasion.util.math.PosUtils;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;

public abstract class AbstractIMZombieEntity extends TieredIMMobEntity implements Miner {

    private boolean fireImmune;

    public static boolean isTar(AbstractIMZombieEntity entity) {
        return entity.getTier() == 2 && entity.getFlavour() == 2;
    }

    protected AbstractIMZombieEntity(EntityType<? extends AbstractIMZombieEntity> type, World world, float diggingSpeed) {
        super(type, world);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new Navigation(this);
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

    }

    public abstract void updateAnimation(boolean override);

    public abstract int getTextureId();

    protected int getSwingSpeed() {
        return 10;
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
        getNavigatorNew().setCanDestroyBlocks(entity instanceof PigmanEngineerEntity || entity instanceof IMCreeperEntity);
    }

    public boolean isBrute() {
        return getTier() == 3;
    }

    @Override
    public float getSoundPitch() {
        return super.getSoundPitch() * (isBrute() ? 0.75F : 1);
    }

    public float scaleAmount() {
        if (getTier() == 2)
            return 1.12F;
        if (getTier() == 3) {
            return 1.21F;
        }
        return 1.0F;
    }

    @Override
    protected Text getDefaultName() {
        if (isBrute()) {
            return Text.translatable(getType().getUntranslatedName() + ".brute");
        }
        return super.getDefaultName();
    }

    protected static class Navigation extends IMMobNavigation {
        public Navigation(MobEntity entity) {
            super(entity);
        }

        @Override
        public PathNodeMaker createNodeMaker() {
            var nodeMaker = new NodeMaker();
            nodeMaker.setCanEnterOpenDoors(true);
            nodeMaker.setCanOpenDoors(true);
            nodeMaker.setCanSwim(true);
            nodeMaker.setCanClimbLadders(true);
            return nodeMaker;
        }

        class NodeMaker extends IMLandPathNodeMaker {
            @Override
            public float getDistancePenalty(PathNode previousNode, PathNode nextNode, CollisionView world) {
                world = context.getWorld();

                if (this.entity instanceof AbstractIMZombieEntity entity
                        && AbstractIMZombieEntity.isTar(entity)
                        && nextNode.type == PathNodeType.WATER) {
                    float multiplier = 1 + ScaffoldView.of(world).getMobDensity(nextNode.getBlockPos()) * 3;

                    if (nextNode.y > previousNode.y && canMineBlock(world, nextNode.getBlockPos(), context.getBlockState(nextNode.getBlockPos()))) {
                        multiplier += 2;
                    }

                    return 1.2F * multiplier;
                }
                return super.getDistancePenalty(previousNode, nextNode, world);
            }

            @Override
            public boolean canMineBlock(CollisionView world, BlockPos pos, BlockState state) {
                return super.canMineBlock(world, pos, state)
                        && getTargetPos() != null
                        && PosUtils.getInclination(getTargetPos(), pos) <= 2.144D;
            }
        }
    }
}
