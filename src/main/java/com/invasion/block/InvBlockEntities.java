package com.invasion.block;

import com.invasion.InvasionMod;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface InvBlockEntities {
    BlockEntityType<NexusBlockEntity> NEXUS = register("nexus", BlockEntityType.Builder.create(NexusBlockEntity::new, InvBlocks.NEXUS_CORE));

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.Builder<T> builder) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, InvasionMod.id(name), builder.build());
    }

    static void bootstrap() { }
}
