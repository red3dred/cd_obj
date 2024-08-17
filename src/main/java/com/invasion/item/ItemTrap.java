package com.invasion.item;

import com.invasion.entity.EntityIMTrap;
import com.invasion.entity.InvEntities;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemTrap extends Item {

    private final EntityIMTrap.Type trapType;

    public ItemTrap(Settings settings, EntityIMTrap.Type trapType) {
        super(settings.maxCount(64).maxDamage(0));
        this.trapType = trapType;
    }

    /*
     * @SideOnly(Side.CLIENT)
     *
     * @Override public void registerIcons(IIconRegister par1IconRegister) {
     * this.emptyIcon = par1IconRegister.registerIcon("invmod:trapEmpty");
     * this.riftIcon = par1IconRegister.registerIcon("invmod:trapPurple");
     * this.flameIcon = par1IconRegister.registerIcon("invmod:trapRed"); }
     */
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getSide() == Direction.UP) {
            World world = context.getWorld();
            Vec3d pos = context.getBlockPos().offset(context.getSide()).toBottomCenterPos();
            EntityIMTrap trap = new EntityIMTrap(InvEntities.TRAP, world, pos.getX(), pos.getY(), pos.getZ(), trapType);

            if (trap.isValidPlacement()
                    && world.getEntitiesByClass(EntityIMTrap.class, trap.getBoundingBox(),
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