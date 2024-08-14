package com.invasion;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public interface InvSounds {
    SoundEvent SCRAPE = register("scrape");
    SoundEvent ENTITY_LIGHTNING_ZAP = register("zap");
    SoundEvent ENTITY_VULTURE_SQUAWK = register("v_squawk");
    SoundEvent ENTITY_VULTURE_DEATH = register("v_death");
    SoundEvent ENTITY_VULTURE_SCREECH = register("v_screech");
    SoundEvent ENTITY_EGG_HATCH = register("egghatch");
    SoundEvent ENTITY_BIG_ZOMBIE_AMBIENT = register("bigzombie");

    private static SoundEvent register(String name) {
        Identifier id = InvasionMod.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    static void boostrap() {}
}
