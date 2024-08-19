package com.invasion.block;

import com.invasion.InvasionMod;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public interface InvBlocks {
    NexusBlock NEXUS_CORE = register("nexus_core", new NexusBlock(Settings.create()
            .resistance(6000000).hardness(3).sounds(BlockSoundGroup.GLASS).emissiveLighting(Blocks::always)
            .luminance(state -> state.get(NexusBlock.LIT) ? 15 : 8)
    ));

    private static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, InvasionMod.id(name), block);
    }

    static void bootstrap() {
        InvBlockEntities.bootstrap();
    }
}
