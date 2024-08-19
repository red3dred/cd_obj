package com.invasion.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class EntityIMPrimedTNT extends BoulderEntity {
    public EntityIMPrimedTNT(EntityType<EntityIMPrimedTNT> type, World world) {
        super(type, world);
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        convertIntoExplosive();
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        convertIntoExplosive();
    }

    private void convertIntoExplosive() {
        discard();
        TntEntity tnt = new TntEntity(getWorld(), getX(), getY(), getZ(), getOwner() instanceof LivingEntity l ? l : null);
        tnt.setVelocity(getVelocity());
        getWorld().spawnEntity(tnt);
    }
}
