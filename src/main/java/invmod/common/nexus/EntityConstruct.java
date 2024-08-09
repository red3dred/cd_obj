package invmod.common.nexus;

import org.jetbrains.annotations.Nullable;

import invmod.common.entity.EntityIMLiving;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public record EntityConstruct (
        EntityType<? extends EntityIMLiving> entityType,
        int texture,
        int tier,
        int flavour,
        float scaling,
        int minAngle,
        int maxAngle
    ) {

    public EntityIMLiving createMob(INexusAccess nexus) {
        return createMob(nexus.getWorld(), nexus);
    }

    public EntityIMLiving createMob(World world, @Nullable INexusAccess nexus) {
        EntityIMLiving entity = entityType().create(world);
        entity.onSpawned(nexus, this);
        return entity;
    }

    public interface BuildableMob {
        void onSpawned(INexusAccess nexus, EntityConstruct spawnConditions);
    }
}