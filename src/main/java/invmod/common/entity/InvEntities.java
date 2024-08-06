package invmod.common.entity;

import invmod.common.InvasionMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface InvEntities {

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registries.ENTITY_TYPE, InvasionMod.id(name), builder.build());
    }

    static void bootstrap() { }
}
