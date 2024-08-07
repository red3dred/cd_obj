package invmod.common.entity;

import org.jetbrains.annotations.Nullable;

import invmod.common.nexus.INexusAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public abstract class EntityIMMob extends EntityIMLiving {
    public EntityIMMob(EntityType<? extends EntityIMMob> type, World world, @Nullable INexusAccess nexus) {
        super(type, world, nexus);
    }
}