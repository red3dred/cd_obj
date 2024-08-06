package invmod.common.nexus;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotOutput extends Slot {
    public SlotOutput(Inventory iinventory, int i, int j, int k) {
        super(iinventory, i, j, k);
    }

    public boolean isItemValid(ItemStack itemstack) {
        return false;
    }
}