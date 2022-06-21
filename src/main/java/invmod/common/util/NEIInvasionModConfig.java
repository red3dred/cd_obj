package invmod.common.util;

import net.minecraftforge.oredict.OreDictionary;
import codechicken.nei.api.IConfigureNEI;
import net.minecraft.item.ItemStack;
import codechicken.nei.api.ItemInfo;
import cpw.mods.fml.common.Optional;
import invmod.common.mod_Invasion;
import codechicken.nei.api.API;
import cpw.mods.fml.common.Mod;

// Doenerstyle

@Optional.Interface(iface = "codechicken.nei.api.API", modid = "NotEnoughItems")
public class NEIInvasionModConfig implements IConfigureNEI {
	
	@Optional.Method(modid="NotEnoughItems")
	@Override
	public String getName() {
		return mod_Invasion.class.getAnnotation(Mod.class).name();
	}
	
	@Optional.Method(modid="NotEnoughItems")
	@Override
	public String getVersion() {
		return mod_Invasion.class.getAnnotation(Mod.class).version();
	}
	
	@Optional.Method(modid="NotEnoughItems")
	@Override
	public void loadConfig() {
		API.hideItem(new ItemStack(mod_Invasion.itemEngyHammer));
	}
	
}