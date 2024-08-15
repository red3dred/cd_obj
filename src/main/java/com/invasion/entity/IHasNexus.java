package com.invasion.entity;

import org.jetbrains.annotations.Nullable;

import com.invasion.block.InvBlocks;
import com.invasion.block.TileEntityNexus;
import com.invasion.nexus.INexusAccess;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHasNexus {
    @Nullable
    INexusAccess getNexus();

    void setNexus(@Nullable INexusAccess nexus);

    default boolean isAlwaysIndependant() {
        return false;
    }

    default void setEntityIndependent() {

    }

    default boolean hasNexus() {
        return getNexus() != null;
    }

    default void acquiredByNexus(INexusAccess nexus) {
        if (hasNexus() && !isAlwaysIndependant()) {
            setNexus(nexus);
        }
    }

    double findDistanceToNexus();

    @Nullable
    static TileEntityNexus findNexus(World world, BlockPos center) {
        for (BlockPos pos : BlockPos.iterateOutwards(center, 8, 5, 8)) {
            if (world.getBlockState(pos).isOf(InvBlocks.NEXUS_CORE)) {
                if (world.getBlockEntity(pos) instanceof TileEntityNexus nexus) {
                    return nexus;
                }
            }
        }
        return null;
    }
}
