package com.invasion.entity;

import com.invasion.block.BlockMetadata;
import com.invasion.entity.pathfinding.IMLandPathNodeMaker;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

public interface Miner extends NexusEntity {
    default float getMaxSelfDamage() {
        return 6;
    }

    default float getSelfDamage() {
        return 2;
    }

    default BlockPos[] getBlockRemovalOrder(BlockPos pos) {
        BlockPos entityPos = asEntity().getBlockPos();
        if (entityPos.getY() >= pos.getY()) {
            return new BlockPos[] {
                pos,
                pos.up()
            };
        }

        return new BlockPos[] {
            pos.up(),
            entityPos.up(MathHelper.ceil(asEntity().getHeight())),
            pos
        };
    }

    default float getBlockRemovalCost(BlockPos pos) {
        return BlockMetadata.getStrength(pos, asEntity().getWorld().getBlockState(pos), asEntity().getWorld()) * 20;
    }

    default boolean canClearBlock(BlockPos pos) {
        return IMLandPathNodeMaker.canMineBlock(asEntity(), pos);
    }

    default void onBlockRemoved(BlockPos pos, BlockState state) {
        if (asEntity().getHealth() > asEntity().getMaxHealth() - getMaxSelfDamage()) {
            asEntity().damage(asEntity().getDamageSources().generic(), getSelfDamage());
        }

        if (asEntity().age % 5 == 0) {
            asEntity().playSound(state.getSoundGroup().getBreakSound(), 1.4F, 1F / (asEntity().getRandom().nextFloat() * 0.6F + 1));
        }
    }

    default BlockView getTerrain() {
        return asEntity().getWorld();
    }
}