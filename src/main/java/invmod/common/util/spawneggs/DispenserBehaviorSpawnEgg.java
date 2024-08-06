package invmod.common.util.spawneggs;

import net.minecraft.block.dispenser.ItemDispenserBehavior;

// TODO: The regular dispenser behaviour for spawn eggs can do what we need
@Deprecated
public class DispenserBehaviorSpawnEgg extends ItemDispenserBehavior {
/*
	@Override
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
	    Position position = DispenserBlock.getOutputLocation(pointer);

		Entity entity = ItemSpawnEgg.spawnCreature(pointer.blockEntity().getWorld(), stack, position.getX(), position.getY(), position.getZ());
		if (entity instanceof LivingEntity l && stack.hasDisplayName()) {
			l.setCustomName(stack.getName());
		}

		stack.split(1);
		return stack;
	}
	*/
}
