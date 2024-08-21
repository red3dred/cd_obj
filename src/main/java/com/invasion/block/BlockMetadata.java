package com.invasion.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.invasion.InvasionMod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.CollisionView;

public class BlockMetadata {
    public static final float DEFAULT_SOFT_STRENGTH = 2.5F;
    public static final float DEFAULT_HARD_STRENGTH = 5.5F;
    public static final float DEFAULT_SOFT_COST = 2;
    public static final float DEFAULT_HARD_COST = 3.2F;
    public static final float AIR_BASE_COST = 1;



    private static final Map<Block, Float> BLOCK_COSTS = Util.make(new HashMap<>(), costs -> {
        costs.put(Blocks.AIR, AIR_BASE_COST);
        costs.put(Blocks.LADDER, AIR_BASE_COST);
        costs.put(Blocks.STONE, DEFAULT_HARD_COST);
        costs.put(Blocks.STONE_BRICKS, DEFAULT_HARD_COST);
        costs.put(Blocks.COBBLESTONE, DEFAULT_HARD_COST);
        costs.put(Blocks.MOSSY_COBBLESTONE, DEFAULT_HARD_COST);
        costs.put(Blocks.BRICKS, DEFAULT_HARD_COST);
        costs.put(Blocks.OBSIDIAN, DEFAULT_HARD_COST);
        costs.put(Blocks.IRON_BLOCK, DEFAULT_HARD_COST);
        costs.put(Blocks.DIRT, DEFAULT_SOFT_COST);
        costs.put(Blocks.SAND, DEFAULT_SOFT_COST);
        costs.put(Blocks.GRAVEL, DEFAULT_SOFT_COST);
        costs.put(Blocks.GLASS, DEFAULT_SOFT_COST);
        costs.put(Blocks.OAK_LEAVES, DEFAULT_SOFT_COST);
        costs.put(Blocks.IRON_DOOR, 2.24F);
        costs.put(Blocks.OAK_DOOR, 1.4F);
        costs.put(Blocks.OAK_TRAPDOOR, 1.4F);
        costs.put(Blocks.SANDSTONE, DEFAULT_HARD_COST);
        costs.put(Blocks.OAK_LOG, DEFAULT_HARD_COST);
        costs.put(Blocks.OAK_PLANKS, DEFAULT_HARD_COST);
        costs.put(Blocks.GOLD_BLOCK, DEFAULT_HARD_COST);
        costs.put(Blocks.DIAMOND_BLOCK, DEFAULT_HARD_COST);
        costs.put(Blocks.OAK_FENCE, DEFAULT_HARD_COST);
        costs.put(Blocks.NETHERRACK, DEFAULT_HARD_COST);
        costs.put(Blocks.NETHER_BRICKS, DEFAULT_HARD_COST);
        costs.put(Blocks.SOUL_SAND, DEFAULT_SOFT_COST);
        costs.put(Blocks.GLOWSTONE, DEFAULT_SOFT_COST);
        costs.put(Blocks.TALL_GRASS, AIR_BASE_COST);
    });
    private static final Map<Block, Float> BLOCK_STRENGTHS = Util.make(new HashMap<>(), strengths -> {
        strengths.put(Blocks.AIR, 0.01F);
        strengths.put(Blocks.STONE, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.STONE_BRICKS, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.COBBLESTONE, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.MOSSY_COBBLESTONE, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.BRICKS, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.OBSIDIAN, 7.7F);
        strengths.put(Blocks.IRON_BLOCK, 7.7F);
        strengths.put(Blocks.DIRT, 3.125F);
        strengths.put(Blocks.GRASS_BLOCK, 3.125F);
        strengths.put(Blocks.SAND, DEFAULT_SOFT_STRENGTH);
        strengths.put(Blocks.GRAVEL, DEFAULT_SOFT_STRENGTH);
        strengths.put(Blocks.GLASS, DEFAULT_SOFT_STRENGTH);
        strengths.put(Blocks.OAK_LEAVES, 1.25F);
        strengths.put(Blocks.VINE, 1.25F);
        strengths.put(Blocks.IRON_DOOR, 15.4F);
        strengths.put(Blocks.OAK_DOOR, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.SANDSTONE, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.OAK_LOG, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.OAK_PLANKS, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.GOLD_BLOCK, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.DIAMOND_BLOCK, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.OAK_FENCE, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.NETHERRACK, 3.85F);
        strengths.put(Blocks.NETHER_BRICKS, DEFAULT_HARD_STRENGTH);
        strengths.put(Blocks.SOUL_SAND, DEFAULT_SOFT_STRENGTH);
        strengths.put(Blocks.GLOWSTONE, DEFAULT_SOFT_STRENGTH);
        strengths.put(Blocks.TALL_GRASS, 0.3F);
        strengths.put(Blocks.DRAGON_EGG, 15F);
    });
    private static final Map<Block, Integer> BLOCK_TYPES = Util.make(new HashMap<>(), types -> {
        types.put(Blocks.AIR, 1);
        types.put(Blocks.TALL_GRASS, 1);
        types.put(Blocks.DEAD_BUSH, 1);
        types.put(Blocks.POPPY, 1);
        types.put(Blocks.DANDELION, 1);
        types.put(Blocks.OAK_PRESSURE_PLATE, 1);
        types.put(Blocks.STONE_PRESSURE_PLATE, 1);
        types.put(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1);
        types.put(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1);
        types.put(Blocks.STONE_BUTTON, 1);
        types.put(Blocks.OAK_BUTTON, 1);
        types.put(Blocks.REDSTONE_TORCH, 1);
        types.put(Blocks.REDSTONE_WIRE, 1);
        types.put(Blocks.TORCH, 1);
        types.put(Blocks.LEVER, 1);
        types.put(Blocks.SUGAR_CANE, 1);
        types.put(Blocks.WHEAT, 1);
        types.put(Blocks.CARROTS, 1);
        types.put(Blocks.POTATOES, 1);
        types.put(Blocks.FIRE, 2);
        types.put(Blocks.BEDROCK, 2);
        types.put(Blocks.LAVA, 2);
        types.put(Blocks.END_PORTAL_FRAME, 2);
    });
    private static final List<Block> UNDESTRUCTABLE_BLOCKS = List.of(
            Blocks.BEDROCK,
            Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.END_PORTAL_FRAME, Blocks.LADDER, Blocks.CHEST
    );

    public static int getBlockType(BlockState state) {
        return BLOCK_TYPES.getOrDefault(state.getBlock(), 0);
    }

    public static boolean isIndestructible(BlockState state) {
        return state.getBlock().getHardness() < 0
                || state.getPistonBehavior() == PistonBehavior.BLOCK
                || UNDESTRUCTABLE_BLOCKS.contains(state.getBlock());
    }

    public static Optional<Float> getCost(BlockState state) {
        return InvasionMod.getConfig().getBlockCost(state.getBlock())
                .or(() -> Optional.ofNullable(BLOCK_COSTS.get(state.getBlock())));
    }

    public static float getStrength(BlockPos pos, BlockState state, CollisionView world) {
        BlockSpecial special = BlockSpecial.of(state);
        int bonus = 0;
        BlockPos.Mutable mutable = pos.mutableCopy();
        float strength = InvasionMod.getConfig().getBlockStrength(state.getBlock()).orElseGet(() -> BLOCK_STRENGTHS.getOrDefault(state.getBlock(), DEFAULT_SOFT_STRENGTH));
        switch (special) {
            case CONSTRUCTION_1:
                for (Direction direction : Direction.values()) {
                    if (world.getBlockState(mutable.set(pos).move(direction)).isOf(state.getBlock())) {
                        bonus++;
                    }
                }
                break;
            case CONSTRUCTION_STONE:
                for (Direction direction : Direction.values()) {
                    BlockState s = world.getBlockState(mutable.set(pos).move(direction));
                    if (s.isOf(Blocks.STONE)
                            || s.isOf(Blocks.COBBLESTONE)
                            || s.isOf(Blocks.MOSSY_COBBLESTONE)
                            || s.isOf(Blocks.STONE_BRICKS)) {
                        bonus++;
                    }
                }
                break;
            default:
        }
        return strength * (1 + bonus * 0.1F);
    }
}
