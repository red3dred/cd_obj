package com.invasion;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public interface InvTags {

    interface Entities {
        TagKey<EntityType<?>> QUEEN_SPIDER_OFFSPRING = entity("queen_spider_offspring");

        private static TagKey<EntityType<?>> entity(String name) {
            return TagKey.of(RegistryKeys.ENTITY_TYPE, InvasionMod.id(name));
        }
    }
}
