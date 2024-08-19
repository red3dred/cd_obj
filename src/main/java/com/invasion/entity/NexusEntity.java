package com.invasion.entity;

import org.jetbrains.annotations.Nullable;

import com.invasion.Notifiable;
import com.invasion.InvasionMod;
import com.invasion.entity.pathfinding.Navigation;
import com.invasion.entity.pathfinding.Path;
import com.invasion.entity.pathfinding.PathNavigateAdapter;
import com.invasion.nexus.Combatant;
import com.invasion.nexus.EntityConstruct;
import com.invasion.nexus.EntityConstruct.BuildableMob;
import com.invasion.nexus.IHasNexus;
import com.invasion.nexus.INexusAccess;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.entity.EntityLike;

public interface NexusEntity extends IHasNexus, BuildableMob, HasAiGoals, EntityLike, Combatant<PathAwareEntity> {
    @Deprecated
    static NbtComponent createVariant(int flavour, int tier) {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("flavour", flavour);
        nbt.putInt("tier", tier);
        return NbtComponent.of(nbt);
    }
    float DEFAULT_AIR_RESISTANCE = 0.9995F;
    float DEFAULT_GROUND_FRICTION = 0.546F;
    float DEFAULT_BASE_MOVEMENT_SPEED = 0.26F;

    default Navigation getNavigatorNew() {
        return ((PathNavigateAdapter)asEntity().getNavigation()).getNewNavigator();
    }

    default int getAggroRange() {
        return hasNexus() ? getNexusBoundAggroRange() : InvasionMod.getConfig().nightMobSightRange;
    }

    default int getSenseRange() {
        return hasNexus() ? getNexusBoundSenseRange() : InvasionMod.getConfig().nightMobSenseRange;
    }

    default int getNexusBoundSenseRange() {
        return 6;
    }

    default int getNexusBoundAggroRange() {
        return 12;
    }

    default boolean getBurnsInDay() {
        return !hasNexus() && InvasionMod.getConfig().nightMobsBurnInDay;
    }

    default void setIsHoldingIntoLadder(boolean flag) {
        asEntity().setSneaking(flag);
    }

    @Override
    default void onSpawned(@Nullable INexusAccess nexus, EntityConstruct spawnConditions) {
        setNexus(nexus);
    }

    default void onFollowingEntity(Entity entity) {
    }

    default void onPathSet() {
    }

    default boolean onPathBlocked(Path path, Notifiable asker) {
        return false;
    }

    @Deprecated
    default void setGravity(float acceleration) {
        asEntity().getAttributeInstance(EntityAttributes.GENERIC_GRAVITY).setBaseValue(acceleration);
    }

    default void setAttackStrength(double attackStrength) {
        asEntity().getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(attackStrength);
    }

    default void setBaseMovementSpeed(double speed) {
        asEntity().getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
    }

    default double getAttackStrength() {
        return asEntity().getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    }

    default void setCanDestroyBlocks(boolean flag) {
        getNavigatorNew().getNodeMaker().setCanDestroyBlocks(flag);
    }

    default boolean getLightLevelBelow8() {
        BlockPos pos = asEntity().getBlockPos();
        return asEntity().getWorld().getLightLevel(LightType.SKY, pos) <= asEntity().getRandom().nextInt(32)
            && asEntity().getWorld().getLightLevel(LightType.BLOCK, pos) <= asEntity().getRandom().nextInt(8);
    }

    @Override
    @Deprecated
    default String getLegacyName() {
        return String.format("%s-T1", getClass().getName().replace("Entity", ""));
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

    @Deprecated
    default void setName(String name) {
    }
}
