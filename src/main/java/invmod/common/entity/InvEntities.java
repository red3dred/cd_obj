package invmod.common.entity;

import invmod.common.InvasionMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface InvEntities {
    EntityType<EntityIMSkeleton> SKELETON = register("skeleton", EntityType.Builder.<EntityIMSkeleton>create(EntityIMSkeleton::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.99F).eyeHeight(1.74F).vehicleAttachment(-0.7F).maxTrackingRange(8));
    EntityType<EntityIMZombie> ZOMBIE = register("zombie", EntityType.Builder.<EntityIMZombie>create(EntityIMZombie::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.95F).eyeHeight(1.74F).passengerAttachments(2.0125F).vehicleAttachment(-0.7F).maxTrackingRange(8));
    EntityType<EntityIMZombiePigman> ZOMBIE_PIGMAN = register("pigman", EntityType.Builder.<EntityIMZombiePigman>create(EntityIMZombiePigman::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.95F).eyeHeight(1.74F).passengerAttachments(2.0125F).vehicleAttachment(-0.7F).maxTrackingRange(8));
    EntityType<EntityIMPigEngy> PIGMAN_ENGINEER = register("pigman_engineer", EntityType.Builder.<EntityIMPigEngy>create(EntityIMPigEngy::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.95F).eyeHeight(1.74F).passengerAttachments(2.0125F).vehicleAttachment(-0.7F).maxTrackingRange(8));
    @Deprecated
    EntityType<EntitySFX> SFX = register("sfx", EntityType.Builder.<EntitySFX>create(EntitySFX::new, SpawnGroup.MISC)
            .dimensions(0.5F, 0.5F).maxTrackingRange(8));
    EntityType<EntityIMSpawnProxy> SPAWN_PROXY = register("spawn_proxy", EntityType.Builder.<EntityIMSpawnProxy>create(EntityIMSpawnProxy::new, SpawnGroup.MISC)
            .dimensions(0.5F, 0.5F).maxTrackingRange(8));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registries.ENTITY_TYPE, InvasionMod.id(name), builder.build());
    }

    static void bootstrap() { }
}
