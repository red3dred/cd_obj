package invmod.common.item;

import invmod.common.entity.EntityIMBird;
import invmod.common.entity.EntityIMCreeper;
import invmod.common.entity.EntityIMPigEngy;
import invmod.common.entity.EntityIMSkeleton;
import invmod.common.entity.EntityIMSpider;
import invmod.common.entity.EntityIMThrower;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.InvEntities;
import invmod.common.block.InvBlocks;
import invmod.common.nexus.TileEntityNexus;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

class ItemDebugWand extends Item {
    private TileEntityNexus nexus;

    public ItemDebugWand(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        if (!(context.getWorld() instanceof ServerWorld world)) {
            return ActionResult.PASS;
        }

        BlockState state = world.getBlockState(context.getBlockPos());
        if (state.isOf(InvBlocks.NEXUS_CORE)) {
            this.nexus = ((TileEntityNexus) world.getBlockEntity(context.getBlockPos()));
            return ActionResult.SUCCESS;
        }

        BlockPos pos = context.getBlockPos().offset(context.getSide());
        EntityIMBird bird = InvEntities.VULTURE.create(world);
        bird.setNexus(nexus);
        bird.setPosition(pos.toBottomCenterPos());

        ZombieEntity zombie2 = new ZombieEntity(world);
        zombie2.setPosition(pos.toBottomCenterPos());

        EntityType.WOLF.create(world, w -> {}, pos, SpawnReason.COMMAND, true, false);

        Entity entity1 = new EntityIMPigEngy(world);
        entity1.setPosition(pos.toBottomCenterPos());

        EntityIMZombie zombie = InvEntities.ZOMBIE.create(world);
        zombie.setNexus(nexus);
        zombie.setTexture(0);
        zombie.setFlavour(0);
        zombie.setTier(1);

        zombie.setPosition(pos.toBottomCenterPos());

        if (this.nexus != null) {
            EntityIMPigEngy entity = InvEntities.PIGMAN_ENGINEER.create(world);
            entity.setNexus(nexus);
            entity.setPosition(pos.toBottomCenterPos());

            zombie = new EntityIMZombie(InvEntities.ZOMBIE, world, this.nexus);
            zombie.setNexus(nexus);
            zombie.setTexture(0);
            zombie.setFlavour(0);
            zombie.setTier(2);
            zombie.setPosition(pos.toBottomCenterPos());

            EntityIMThrower thrower = InvEntities.THROWER.create(world);
            thrower.setNexus(nexus);
            thrower.setPosition(pos.toBottomCenterPos());

            EntityIMCreeper creep = InvEntities.CREEPER.create(world);
            creep.setNexus(nexus);
            creep.setPosition(pos.toBottomCenterPos());

            EntityIMSpider spider = InvEntities.SPIDER.create(world);
            spider.setNexus(nexus);
            spider.setTexture(0);
            spider.setFlavour(0);
            spider.setTier(2);

            spider.setPosition(pos.toBottomCenterPos());

            EntityIMSkeleton skeleton = InvEntities.SKELETON.create(world);
            skeleton.setNexus(nexus);
            skeleton.setPosition(pos.toBottomCenterPos());
        }

        EntityIMSpider entity = InvEntities.SPIDER.create(world);
        entity.setNexus(nexus);
        entity.setTexture(0);
        entity.setFlavour(1);
        entity.setTier(2);

        entity.setPosition(pos.toBottomCenterPos());

        EntityIMCreeper creep = InvEntities.CREEPER.create(world);
        creep.setNexus(nexus);
        creep.setPosition(150.5D, 64.0D, 271.5D);

        return ActionResult.SUCCESS;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof WolfEntity wolf && attacker instanceof PlayerEntity player) {
            wolf.setOwner(player);
            return true;
        }
        return false;
    }
}