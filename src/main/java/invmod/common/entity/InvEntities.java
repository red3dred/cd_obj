package invmod.common.entity;

import invmod.common.InvasionMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface InvEntities {
    EntityType<EntityIMSkeleton> SKELETON = register("skeleton", EntityType.Builder.<EntityIMSkeleton>create(EntityIMSkeleton::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.99F)
            .eyeHeight(1.74F)
            .vehicleAttachment(-0.7F)
            .maxTrackingRange(8));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registries.ENTITY_TYPE, InvasionMod.id(name), builder.build());
    }

    static void bootstrap() { }
}
