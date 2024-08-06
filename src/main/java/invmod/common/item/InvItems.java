package invmod.common.item;

import java.util.ArrayList;
import java.util.List;

import invmod.common.InvasionMod;
import invmod.common.mod_Invasion;
import invmod.common.util.spawneggs.ItemSpawnEgg;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public interface InvItems {
    List<Item> REGISTRY = new ArrayList<>();

    Item PHASE_CRYSTAL = register("phase_crystal", new ItemPhaseCrystal());
    Item RIFT_FLUX = register("rift_flux", new ItemRiftFlux());
    Item SMALL_REMNANTS = register("small_remnants", new ItemSmallRemnants());
    Item NEXUS_CATALYST = register("nexus_catalyst", new ItemNexusCatalyst());
    Item INFUSED_SWORD = register("infused_sword", new ItemInfusedSword());
    Item EMPTY_TRAP = register("empty_trap", new Item(new Item.Settings()));
    Item RIFT_TRAP = register("rift_trap", new ItemTrap(new Item.Settings(), 1));
    Item FLAME_TRAP = register("flame_trap", new ItemTrap(new Item.Settings(), 2));
    // TODO: Ice trap
    // Item XYZ_TRAP = register("xyz_trap", new ItemTrap(new Item.Settings()));
    Item SEARING_BOW = register("searing_bow", new ItemSearingBow());
    Item CATALYST_MIXTURE = register("catalyst_mixture", new ItemCatalystMixture());
    Item STABLE_CATALYST_MIXTURE = register("stable_catalyst_mixture", new ItemStableCatalystMixture());
    Item STABLE_NEXUS_CATALYST = register("stable_nexus_catalys", new ItemStableNexusCatalyst());
    Item DAMPING_AGENT = register("damping_agent", new ItemDampingAgent());
    Item STRONG_DAMPING_AGENT = register("strong_damping_agent", new ItemStrongDampingAgent());
    Item STRANGE_BONE = register("strange_bone", new ItemStrangeBone());
    Item PROBE = register("probe", new ItemProbe());
    Item STRONG_CATALYST = register("strong_catalyst", new ItemStrongCatalyst());
    Item ENGY_HAMMER = register("engy_hammer", new ItemEngyHammer());
    //ItemSpawnEgg SPAWN_EGG;

    private static <T extends Item> T register(String name, T item) {
        REGISTRY.add(item);
        return Registry.register(Registries.ITEM, InvasionMod.id(name), item);
    }

    static void bootstrap() {
        if (mod_Invasion.isDebug()) {
            register("debug_wand", new ItemDebugWand());
        }
        Identifier tabId = InvasionMod.id("invasion_mod");
        Registry.register(Registries.ITEM_GROUP, tabId, FabricItemGroup.builder().entries((context, entries) -> {
            REGISTRY.forEach(item -> entries.add(item.getDefaultStack()));
        }).icon(() -> mod_Invasion.blockNexus.asItem().getDefaultStack()).displayName(Text.translatable(Util.createTranslationKey("itemGroup", tabId))).build());
    }
}
