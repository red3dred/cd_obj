package invmod.common.nexus;

import org.jetbrains.annotations.Nullable;

import invmod.common.mod_Invasion;
import invmod.common.entity.EntityIMBurrower;
import invmod.common.entity.EntityIMCreeper;
import invmod.common.entity.EntityIMImp;
import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.EntityIMPigEngy;
import invmod.common.entity.EntityIMSpider;
import invmod.common.entity.EntityIMThrower;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.EntityIMZombiePigman;
import invmod.common.entity.InvEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class MobBuilder {
    public EntityIMLiving createMobFromConstruct(EntityConstruct mobConstruct, World world, INexusAccess nexus) {
        @Nullable
        EntityType<? extends EntityIMLiving> type = getType(mobConstruct);
        if (type == null) {
            mod_Invasion.log("Missing mob type in MobBuilder: " + mobConstruct.entityType());
            return null;
        }

        EntityIMLiving entity = type.create(world);
        entity.onSpawned(nexus, mobConstruct);
        return entity;
    }

    @Nullable
    private EntityType<? extends EntityIMLiving> getType(EntityConstruct mobConstruct) {
        return switch (mobConstruct.entityType()) {
            case ZOMBIE -> InvEntities.ZOMBIE;
            case ZOMBIEPIGMAN -> InvEntities.ZOMBIE_PIGMAN;
            case SPIDER -> InvEntities.SPIDER;
            case SKELETON -> InvEntities.SKELETON;
            case PIG_ENGINEER -> InvEntities.PIGMAN_ENGINEER;
            case THROWER -> InvEntities.THROWER;
            case BURROWER -> InvEntities.BURROWER;
            case CREEPER -> InvEntities.CREEPER;
            case IMP -> InvEntities.IMP;
            default -> null;
        };
    }

    public interface BuildableMob {
        void onSpawned(INexusAccess nexus, EntityConstruct spawnConditions);
    }
}