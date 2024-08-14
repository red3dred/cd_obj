package com.invasion.block;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Util;

public enum BlockSpecial {
    CONSTRUCTION_1,
    CONSTRUCTION_STONE,
    DEFLECTION_1,
    NONE;

    private static final Map<Block, BlockSpecial> VALUES = Util.make(new HashMap<>(), specials -> {
        specials.put(Blocks.STONE, CONSTRUCTION_STONE);
        specials.put(Blocks.STONE_BRICKS, CONSTRUCTION_STONE);
        specials.put(Blocks.COBBLESTONE, CONSTRUCTION_STONE);
        specials.put(Blocks.MOSSY_COBBLESTONE, CONSTRUCTION_STONE);
        specials.put(Blocks.BRICKS, CONSTRUCTION_1);
        specials.put(Blocks.SANDSTONE, CONSTRUCTION_1);
        specials.put(Blocks.NETHER_BRICKS, CONSTRUCTION_1);
        specials.put(Blocks.OBSIDIAN, DEFLECTION_1);
    });

    public static BlockSpecial of(BlockState state) {
        return of(state.getBlock());
    }

    public static BlockSpecial of(Block block) {
        return VALUES.getOrDefault(block, BlockSpecial.NONE);
    }
}