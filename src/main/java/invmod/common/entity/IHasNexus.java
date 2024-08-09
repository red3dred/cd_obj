package invmod.common.entity;

import org.jetbrains.annotations.Nullable;

import invmod.common.nexus.INexusAccess;

public interface IHasNexus {
    @Nullable
    INexusAccess getNexus();

    void setNexus(@Nullable INexusAccess nexus);

    boolean isAlwaysIndependant();

    void setEntityIndependent();

    default boolean hasNexus() {
        return getNexus() != null;
    }

    default void acquiredByNexus(INexusAccess nexus) {
        if (hasNexus() && !isAlwaysIndependant()) {
            setNexus(nexus);
        }
    }
}
