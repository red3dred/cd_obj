package invmod.common;

import java.util.Optional;

import invmod.common.nexus.INexusAccess;
import net.minecraft.server.world.ServerWorld;

@Deprecated
public class mod_Invasion {
    @Deprecated
    public static boolean tryGetInvasionPermission(INexusAccess nexus) {
        // TODO: Not implemented correctly
        return true;
    }

    @Deprecated
    public static void setInvasionEnded(INexusAccess nexus) {
    }

    @Deprecated
    public static void setNexusUnloaded(INexusAccess nexus) {
    }

    public static Optional<INexusAccess> getNexus(ServerWorld world) {
        // TODO: Store nexus with the world
        return Optional.empty();
    }
}