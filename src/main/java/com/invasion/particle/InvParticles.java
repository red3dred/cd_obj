package com.invasion.particle;

import com.invasion.InvasionMod;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface InvParticles {
    SimpleParticleType DAZE = register("daze", FabricParticleTypes.simple(true));

    static <T extends ParticleType<?>> T register(String name, T type) {
        return Registry.register(Registries.PARTICLE_TYPE, InvasionMod.id(name), type);
    }

    static void bootstrap() {}
}
