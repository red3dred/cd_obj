package com.invasion.block;

import java.util.Arrays;
import java.util.List;

import com.invasion.InvTags;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public enum BlockSpecial {
    CONSTRUCTION_BRICKS(InvTags.Blocks.BRITTLE_CONSTRUCTION_MATERIALS),
    CONSTRUCTION_STONE(InvTags.Blocks.SOLID_CONSTRUCTION_MATERIALS),
    DEFLECTION(InvTags.Blocks.REPULSIVE_CONSTRUCTION_MATERIALS),
    NONE(BlockTags.AIR);

    private static final List<BlockSpecial> VALUES = Arrays.asList(values());

    private final TagKey<Block> tag;

    BlockSpecial(TagKey<Block> tag) {
        this.tag = tag;
    }

    public static BlockSpecial of(BlockState state) {
        for (BlockSpecial special : VALUES) {
            if (state.isIn(special.tag)) {
                return special;
            }
        }
        return NONE;
    }
}