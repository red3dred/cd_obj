package com.invasion.item;

import com.invasion.entity.TrapEntity;
import com.invasion.entity.InvEntities;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TrapItem extends Item {

    private final TrapEntity.Type trapType;

    public TrapItem(Settings settings, TrapEntity.Type trapType) {
        super(settings.maxCount(64).maxDamage(0));
        this.trapType = trapType;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getSide() == Direction.UP) {
            World world = context.getWorld();
            Vec3d pos = context.getBlockPos().offset(context.getSide()).toBottomCenterPos();
            TrapEntity trap = new TrapEntity(InvEntities.TRAP, world, pos.getX(), pos.getY(), pos.getZ(), trapType);

            if (trap.isValidPlacement()
                    && world.getEntitiesByClass(TrapEntity.class, trap.getBoundingBox(),
                    EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR).isEmpty()) {
                if (!world.isClient) {
                    world.spawnEntity(trap);
                }

                context.getStack().decrementUnlessCreative(1, context.getPlayer());

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.FAIL;
    }
}