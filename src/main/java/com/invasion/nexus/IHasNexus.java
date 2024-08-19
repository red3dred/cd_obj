package com.invasion.nexus;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.invasion.block.InvBlocks;
import com.invasion.block.NexusBlockEntity;
import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public interface IHasNexus {

    Handle getNexusHandle();

    @Nullable
    default INexusAccess getNexus() {
        return getNexusHandle().get();
    }

    default void setNexus(@Nullable INexusAccess nexus) {
        getNexusHandle().set(nexus);
    }

    default boolean hasNexus() {
        return getNexus() != null;
    }

    double findDistanceToNexus();

    @Nullable
    static INexusAccess findNexus(World world, BlockPos center) {
        for (BlockPos pos : BlockPos.iterateOutwards(center, 8, 5, 8)) {
            if (world.getBlockState(pos).isOf(InvBlocks.NEXUS_CORE)) {
                if (world.getBlockEntity(pos) instanceof NexusBlockEntity nexus) {
                    return nexus.getNexus();
                }
            }
        }
        return null;
    }

    public final class Handle {
        @Nullable
        private UUID nexusId;
        @Nullable
        private GlobalPos globalPos;
        @Nullable
        private INexusAccess nexus;

        private final Supplier<World> worldGetter;

        public Handle(Supplier<World> worldGetter) {
            this.worldGetter = worldGetter;
        }

        public @Nullable INexusAccess get() {
            if (nexusId != null
                    && globalPos != null
                    && nexus == null
                    && worldGetter.get() instanceof ServerWorld sw
                    && sw.getServer().getWorld(globalPos.dimension()) instanceof ServerWorld world) {
                nexus = WorldNexusStorage.of(world).getNexus(nexusId);
                if (nexus == null) {
                    set(null);
                }
            }
            return nexus;
        }

        public void set(@Nullable INexusAccess nexus) {
            nexusId = nexus == null ? null : nexus.getUuid();
            globalPos = nexus == null ? null : GlobalPos.create(nexus.getWorld().getRegistryKey(), nexus.getOrigin());
            this.nexus = nexus;
        }

        public void readNbt(NbtCompound nbt) {
            nexus = null;
            globalPos = nbt.contains("globalPos") ? GlobalPos.CODEC.decode(NbtOps.INSTANCE, nbt.get("globalPos")).result().map(Pair::getFirst).orElse(null) : null;
            nexusId = nbt.containsUuid("nexusId") ? nbt.getUuid("nexusId") : null;
        }

        public Optional<GlobalPos> getPos() {
            return Optional.ofNullable(globalPos);
        }

        public NbtCompound writeNbt(NbtCompound nbt) {
            if (nexusId != null) {
                nbt.putUuid("nexusId", nexusId);
            }
            if (globalPos != null) {
                GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, globalPos).result().ifPresent(pos -> {
                    nbt.put("globalPos", pos);
                });
            }
            return nbt;
        }
    }
}
