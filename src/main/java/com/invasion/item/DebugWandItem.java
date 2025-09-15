package com.invasion.item;

import org.jetbrains.annotations.Nullable;

import com.invasion.block.InvBlocks;
import com.invasion.block.NexusBlockEntity;
import com.invasion.entity.VultureEntity;
import com.invasion.entity.IMCreeperEntity;
import com.invasion.entity.PigmanEngineerEntity;
import com.invasion.entity.IMSkeletonEntity;
import com.invasion.entity.NexusSpiderEntity;
import com.invasion.entity.ThrowerEntity;
import com.invasion.entity.EntityIMZombie;
import com.invasion.entity.InvEntities;
import com.invasion.nexus.NexusAccess;

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

class DebugWandItem extends Item {
    @Nullable
    private NexusAccess nexus;

    public DebugWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        if (!(context.getWorld() instanceof ServerWorld world)) {
            return ActionResult.PASS;
        }

        BlockState state = world.getBlockState(context.getBlockPos());
        if (state.isOf(InvBlocks.NEXUS_CORE)) {
            this.nexus = ((NexusBlockEntity) world.getBlockEntity(context.getBlockPos())).getNexus();
            return ActionResult.SUCCESS;
        }

        if (nexus != null && nexus.getWorld() != world) {
            nexus = null;
        }

        BlockPos pos = context.getBlockPos().offset(context.getSide());
        VultureEntity bird = InvEntities.VULTURE.create(world);
        bird.setNexus(nexus);
        bird.setPosition(pos.toBottomCenterPos());

        ZombieEntity zombie2 = new ZombieEntity(world);
        zombie2.setPosition(pos.toBottomCenterPos());

        EntityType.WOLF.create(world, w -> {}, pos, SpawnReason.COMMAND, true, false);

        Entity entity1 = InvEntities.PIGMAN_ENGINEER.create(world);
        entity1.setPosition(pos.toBottomCenterPos());

        EntityIMZombie zombie = InvEntities.ZOMBIE.create(world);
        zombie.setNexus(nexus);
        zombie.setFlavour(0);
        zombie.setTier(1);

        zombie.setPosition(pos.toBottomCenterPos());

        if (this.nexus != null) {
            PigmanEngineerEntity entity = InvEntities.PIGMAN_ENGINEER.create(world);
            entity.setNexus(nexus);
            entity.setPosition(pos.toBottomCenterPos());

            zombie = new EntityIMZombie(InvEntities.ZOMBIE, world);
            zombie.setNexus(nexus);
            zombie.setFlavour(0);
            zombie.setTier(2);
            zombie.setPosition(pos.toBottomCenterPos());

            ThrowerEntity thrower = InvEntities.THROWER.create(world);
            thrower.setNexus(nexus);
            thrower.setPosition(pos.toBottomCenterPos());

            IMCreeperEntity creep = InvEntities.CREEPER.create(world);
            creep.setNexus(nexus);
            creep.setPosition(pos.toBottomCenterPos());

            NexusSpiderEntity spider = InvEntities.JUMPING_SPIDER.create(world);
            spider.setNexus(nexus);

            spider.setPosition(pos.toBottomCenterPos());

            IMSkeletonEntity skeleton = InvEntities.SKELETON.create(world);
            skeleton.setNexus(nexus);
            skeleton.setPosition(pos.toBottomCenterPos());
        }

        NexusSpiderEntity entity = InvEntities.QUEEN_SPIDER.create(world);
        entity.setNexus(nexus);

        entity.setPosition(pos.toBottomCenterPos());

        IMCreeperEntity creep = InvEntities.CREEPER.create(world);
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