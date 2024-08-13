package invmod.common.entity;

import invmod.common.block.InvBlocks;
import invmod.common.nexus.INexusAccess;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;

public class EntityIMBoulder extends PersistentProjectileEntity {
    public EntityIMBoulder(EntityType<? extends EntityIMBoulder> type, World world) {
        super(type, world);
    }

    public EntityIMBoulder(EntityType<? extends EntityIMBoulder> type, World world, double x, double y, double z) {
        super(type, x, y, z, world, ItemStack.EMPTY, null);
        setStack(getDefaultItemStack());
    }

    public EntityIMBoulder(EntityType<? extends EntityIMBoulder> type, World world, LivingEntity owner, float f) {
        super(type, owner, world, ItemStack.EMPTY, null);
        setStack(getDefaultItemStack());
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        return !getItemStack().isEmpty() && super.tryPickup(player);
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0F, 0.9F / (getRandom().nextFloat() * 0.2F + 0.9F));
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        super.onBlockHit(hit);
        BlockState block2 = getWorld().getBlockState(hit.getBlockPos());
        if (block2.isOf(InvBlocks.NEXUS_CORE) && getWorld().getBlockEntity(hit.getBlockPos()) instanceof INexusAccess nexus) {
            nexus.attackNexus(2);
        } else if (block2.getHardness(getWorld(), hit.getBlockPos()) >= 0) {

            if (!block2.isIn(BlockTags.WITHER_IMMUNE) && !block2.isIn(BlockTags.DRAGON_IMMUNE)) {
                if (EntityIMLiving.getBlockSpecial(block2.getBlock()) == BlockSpecial.DEFLECTION_1 && getRandom().nextInt(2) == 0) {
                    discard();
                    return;
                }
                if (getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                    getWorld().createExplosion(this, getX(), getY(), getZ(), 2, ExplosionSourceType.BLOCK);
                }
            }

        }
    }
}