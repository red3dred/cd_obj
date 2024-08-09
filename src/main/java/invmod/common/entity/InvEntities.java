package invmod.common.entity;

import java.lang.reflect.Field;

import invmod.common.ConfigInvasion;
import invmod.common.InvasionMod;
import invmod.common.mod_Invasion;
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

    static void bootstrap() {
        EntityRegistry.registerModEntity(EntityIMBoulder.class, "IMBoulder", 1, this, 36, 4, true);
        EntityRegistry.registerModEntity(EntityIMBolt.class, "IMBolt", 2, this, 36, 5, false);
        EntityRegistry.registerModEntity(EntityIMTrap.class, "IMTrap", 3, this, 36, 5, false);
        EntityRegistry.registerModEntity(EntityIMPrimedTNT.class, "IMPrimedTNT", 4, this, 36, 4, true);
        //EntityRegistry.registerModEntity(EntityIMZombie.class, "IMZombie", 5, this, 128, 1, true);
        //EntityRegistry.registerModEntity(EntityIMSkeleton.class, "IMSkeleton", 6, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMSpider.class, "IMSpider", 7, this, 128, 1, true);
        //EntityRegistry.registerModEntity(EntityIMPigEngy.class, "IMPigEngy", 8, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMWolf.class, "IMWolf", 9, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMEgg.class, "IMEgg", 10, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMCreeper.class, "IMCreeper", 11, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMImp.class, "IMImp", 12, this, 128, 1, true);
        //EntityRegistry.registerModEntity(EntityIMZombiePigman.class, "IMZombiePigman", 13, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMThrower.class, "IMThrower", 14, this, 128, 1, true);

        if (debugMode) {
            EntityRegistry.registerModEntity(EntityIMBird.class, "IMBird", 15, this, 128, 1, true);
            EntityRegistry.registerModEntity(EntityIMGiantBird.class, "IMGiantBird", 16, this, 128, 1, true);
        }

        ConfigInvasion config = InvasionMod.getConfig();

        if (config.nightSpawnsEnabled) {
            BiomeGenBase[] biomes = {
                    BiomeGenBase.plains, BiomeGenBase.extremeHills, BiomeGenBase.forest,
                    BiomeGenBase.taiga, BiomeGenBase.swampland, BiomeGenBase.forestHills, BiomeGenBase.taigaHills,
                    BiomeGenBase.extremeHillsEdge, BiomeGenBase.jungle, BiomeGenBase.jungleHills
            };
            EntityRegistry.addSpawn(EntityIMSpawnProxy.class, config.nightMobSpawnChance, 1, 1, SpawnGroup.MONSTER, biomes);
            EntityRegistry.addSpawn(EntityIMZombie.class, 1, 1, 1, SpawnGroup.MONSTER, biomes);
            EntityRegistry.addSpawn(EntityIMSpider.class, 1, 1, 1, SpawnGroup.MONSTER, biomes);
            EntityRegistry.addSpawn(EntityIMSkeleton.class, 1, 1, 1, SpawnGroup.MONSTER, biomes);
        }

        if (config.maxNightMobs != 70) {
            try {
                // TODO: Use a mixin for this. Reflection is slow and won't work in a obfuscated environment
                Field field = SpawnGroup.class.getDeclaredField("capacity");
                field.setAccessible(true);
                field.set(SpawnGroup.MONSTER, config.maxNightMobs);
            } catch (Exception e) {
                InvasionMod.LOGGER.error("Error whilst updating max hostile entity cap", e);
            }
        }
    }
}
