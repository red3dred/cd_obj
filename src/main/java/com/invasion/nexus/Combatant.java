package com.invasion.nexus;

import java.util.function.Predicate;

import com.invasion.InvasionMod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.predicate.entity.EntityPredicates;

public interface Combatant<T extends LivingEntity> extends IHasNexus {
    Predicate<Entity> PREDICATE = EntityPredicates.VALID_LIVING_ENTITY.and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR).and(i -> i instanceof Combatant);

    @Deprecated
    String getLegacyName();

    T asEntity();

    default void resetHealth() {
        T self = asEntity();
        float health = InvasionMod.getConfig().getHealth(this);
        self.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(health);
        self.setHealth(health);
    }

    @Override
    default double findDistanceToNexus() {
        if (!hasNexus()) {
            return Double.MAX_VALUE;
        }
        return Math.sqrt(getNexus().getOrigin().toCenterPos().squaredDistanceTo(asEntity().getX(), asEntity().getBodyY(0.5), asEntity().getZ()));
    }
}
