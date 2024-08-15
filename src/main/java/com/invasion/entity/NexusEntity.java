package com.invasion.entity;

import com.invasion.INotifyTask;
import com.invasion.InvasionMod;
import com.invasion.entity.ai.Goal;
import com.invasion.entity.pathfinding.INavigation;
import com.invasion.entity.pathfinding.Path;
import com.invasion.entity.pathfinding.PathNavigateAdapter;

import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

public interface NexusEntity extends IHasNexus, IHasAiGoals {
    float DEFAULT_AIR_RESISTANCE = 0.9995F;
    float DEFAULT_GROUND_FRICTION = 0.546F;
    float DEFAULT_BASE_MOVEMENT_SPEED = 0.26F;

    @Override
    default double findDistanceToNexus() {
        if (!hasNexus()) {
            return Double.MAX_VALUE;
        }
        return Math.sqrt(getNexus().getOrigin().toCenterPos().squaredDistanceTo(asEntity().getX(), asEntity().getBodyY(0.5), asEntity().getZ()));
    }

    default INavigation getNavigatorNew() {
        return ((PathNavigateAdapter)asEntity().getNavigation()).getNewNavigator();
    }

    PathAwareEntity asEntity();

    default void onFollowingEntity(Entity entity) {
    }

    default void onPathSet() {
    }

    default boolean onPathBlocked(Path path, INotifyTask asker) {
        return false;
    }

    default void setMaxHealth(float health) {
        asEntity().getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(health);
    }

    default void setMaxHealthAndHealth(float health) {
        setMaxHealth(health);
        asEntity().setHealth(health);
    }

    default void setGravity(float acceleration) {
        asEntity().getAttributeInstance(EntityAttributes.GENERIC_GRAVITY).setBaseValue(acceleration);
    }

    @Deprecated
    default void setJumpHeight(int height) {
        asEntity().getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue(height);
    }

    default void setAttackStrength(double attackStrength) {
        asEntity().getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(attackStrength);
    }

    default double getAttackStrength() {
        return asEntity().getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    }

    default void setCanDestroyBlocks(boolean flag) {
        getNavigatorNew().getActor().setCanDestroyBlocks(flag);
    }

    default boolean getLightLevelBelow8() {
        BlockPos pos = asEntity().getBlockPos();
        return asEntity().getWorld().getLightLevel(LightType.SKY, pos) <= asEntity().getRandom().nextInt(32)
            && asEntity().getWorld().getLightLevel(LightType.BLOCK, pos) <= asEntity().getRandom().nextInt(8);
    }

    default int getTier() {
        return 1;
    }

    default boolean getDebugMode() {
        return InvasionMod.getConfig().debugMode;
    }

    @Deprecated
    default String getLegacyName() {
        return String.format("%s-T%d", getClass().getName().replace("Entity", ""), getTier());
    }

    @Override
    default Goal getAIGoal() {
        return getNavigatorNew().getAIGoal();
    }

    @Override
    default Goal getPrevAIGoal() {
        return getNavigatorNew().getPrevAIGoal();
    }

    @Override
    default Goal transitionAIGoal(Goal newGoal) {
        return getNavigatorNew().transitionAIGoal(newGoal);
    }
}
