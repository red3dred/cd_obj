package invmod.common.creativetab;

import invmod.common.mod_Invasion;
import net.minecraft.item.Item;

@Deprecated(since ="Not needed")
public class CreativeTabInvmod extends CreativeTabs{

	public CreativeTabInvmod() {
		super("invasionTab");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem()
	{
	return Item.getItemFromBlock(mod_Invasion.blockNexus);
	}

}
