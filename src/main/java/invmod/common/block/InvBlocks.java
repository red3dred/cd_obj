package invmod.common.block;

import invmod.common.InvasionMod;
import invmod.common.nexus.BlockNexus;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface InvBlocks {
    BlockNexus NEXUS_CORE = register("nexus_core", new BlockNexus());

    private static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, InvasionMod.id(name), block);
    }

    static void bootstrap() {
        InvBlockEntities.bootstrap();
    }
}
