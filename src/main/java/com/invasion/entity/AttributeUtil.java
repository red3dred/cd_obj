package com.invasion.entity;

import java.util.ArrayList;
import java.util.List;

import com.invasion.InvasionMod;

import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;

public interface AttributeUtil {
    Identifier NEXUS_WAVE_DIFFICULTY_BUFFS = InvasionMod.id("nexus_wave_difficulty_buff");
    List<RegistryEntry<EntityAttribute>> TIER_SCALING_ATTRIBUTES = List.of(
            EntityAttributes.GENERIC_ATTACK_DAMAGE,
            EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
            EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE
    );

    static void toggleAttribute(MobEntity entity, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, boolean apply) {
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        instance.removeModifier(modifier.id());
        if (apply) {
            instance.addTemporaryModifier(modifier);
        }
    }

    static void toggleAttribute(MobEntity entity, List<RegistryEntry<EntityAttribute>> attributes, EntityAttributeModifier modifier, boolean apply) {
        attributes.forEach(attribute -> toggleAttribute(entity, attribute, modifier, apply));
    }

    static EntityAttributeModifier addToBase(Identifier id, float amount) {
        return new EntityAttributeModifier(id, amount, EntityAttributeModifier.Operation.ADD_VALUE);
    }

    static EntityAttributeModifier addPercentage(Identifier id, float amount) {
        return new EntityAttributeModifier(id, amount / 100F, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    static EntityAttributeModifier multiplyTotal(Identifier id, float multiplier) {
        return new EntityAttributeModifier(id, multiplier - 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    static void applyNexusWaveComplications(MobEntity entity, ServerWorldAccess world, int currentWave, LocalDifficulty difficulty, SpawnReason spawnReason) {
        toggleAttribute(entity, TIER_SCALING_ATTRIBUTES, multiplyTotal(NEXUS_WAVE_DIFFICULTY_BUFFS, 1.0001F * currentWave), true);

        if (entity instanceof MountableEntity mountable) {
            mountable.generateJockey(world, currentWave, difficulty, spawnReason);
        }

        if (currentWave > 5) {
            int effectAttempts = currentWave - 15;
            var unfairEffects = new ArrayList<>(List.of(
                    StatusEffects.RESISTANCE,
                    StatusEffects.FIRE_RESISTANCE,
                    StatusEffects.WATER_BREATHING,
                    StatusEffects.JUMP_BOOST,
                    StatusEffects.WIND_CHARGED
            ));
            while (--effectAttempts > 0 && !unfairEffects.isEmpty()) {
                Random random = world.getRandom();
                if (random.nextInt(100) == 0) {
                    entity.addStatusEffect(new StatusEffectInstance(unfairEffects.remove(random.nextInt(unfairEffects.size())), -1));
                }
            }
        }

        if (currentWave > 15) {
            int effectAttempts = currentWave - 15;
            var unfairEffects = new ArrayList<>(List.of(
                    StatusEffects.INFESTED,
                    StatusEffects.SLOW_FALLING,
                    StatusEffects.OOZING,
                    StatusEffects.WEAVING
            ));
            while (--effectAttempts > 0 && !unfairEffects.isEmpty()) {
                Random random = world.getRandom();
                if (random.nextInt(100) == 0) {
                    entity.addStatusEffect(new StatusEffectInstance(unfairEffects.remove(random.nextInt(unfairEffects.size())), -1));
                }
            }
        }
    }
}
