package com.invasion;

import net.minecraft.block.Block;
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

    interface Blocks {
        TagKey<Block> STONE_CONSTRUCTION_BONUS_MATERIALS = block("stone_construction_bonus_materials");
        TagKey<Block> SOLID_CONSTRUCTION_MATERIALS = block("solid_construction_materials");
        TagKey<Block> BRITTLE_CONSTRUCTION_MATERIALS = block("brittle_construction_materials");
        TagKey<Block> REPULSIVE_CONSTRUCTION_MATERIALS = block("repulsive_construction_materials");

        private static TagKey<Block> block(String name) {
            return TagKey.of(RegistryKeys.BLOCK, InvasionMod.id(name));
        }
    }
}
