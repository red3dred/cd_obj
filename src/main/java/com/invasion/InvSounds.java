package com.invasion;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public interface InvSounds {
    SoundEvent ENTITY_SCRAPE = register("entity.scrape");
    SoundEvent ENTITY_EXPLODE = register("entity.explode");
    SoundEvent ENTITY_CRASH = register("entity.crash");

    SoundEvent BLOCK_NEXUS_RUMBLE = register("block.nexus.rumble");
    SoundEvent BLOCK_NEXUS_CHIME = register("block.nexus.chime");

    SoundEvent ENTITY_THROWER_RAGE = register("entity.thrower.rage");

    SoundEvent ENTITY_BOULDER_LAND = register("entity.boulder.land");

    SoundEvent ENTITY_LIGHTNING_ZAP = register("entity.lightning.zap");
    SoundEvent ENTITY_VULTURE_SQUAWK = register("entity.vulture.squawk");
    SoundEvent ENTITY_VULTURE_DEATH = register("entity.vulture.death");
    //SoundEvent ENTITY_VULTURE_HISS = register("entity.vulture.hiss");
    SoundEvent ENTITY_VULTURE_SCREECH = register("entity.vulture.screech");
    //SoundEvent ENTITY_VULTURE_LONG_SCREECH = register("entity.vulture.long_screech");
    SoundEvent ENTITY_SPIDER_EGG_HATCH = register("entity.spider_egg.hatch");
    SoundEvent ENTITY_BIG_ZOMBIE_AMBIENT = register("entity.big_zombie.ambient");

    private static SoundEvent register(String name) {
        Identifier id = InvasionMod.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    static void boostrap() {}
}
