package invmod.common.block;

import invmod.common.InvasionMod;
import invmod.common.nexus.TileEntityNexus;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface InvBlockEntities {
    BlockEntityType<TileEntityNexus> NEXUS = register("nexus", BlockEntityType.Builder.create(TileEntityNexus::new, InvBlocks.NEXUS_CORE));

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.Builder<T> builder) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, InvasionMod.id(name), builder.build());
    }

    static void bootstrap() { }
}
