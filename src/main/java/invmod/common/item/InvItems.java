package invmod.common.item;

import java.util.ArrayList;
import java.util.List;

import invmod.common.InvasionMod;
import invmod.common.mod_Invasion;
import invmod.common.block.InvBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public interface InvItems {
    List<Item> REGISTRY = new ArrayList<>();

    Item PHASE_CRYSTAL = register("phase_crystal", new Item(new Item.Settings()));
    Item RIFT_FLUX = register("rift_flux", new Item(new Item.Settings()));
    Item SMALL_REMNANTS = register("small_remnants", new Item(new Item.Settings()));
    Item NEXUS_CATALYST = register("nexus_catalyst", new Item(new Item.Settings()));
    Item INFUSED_SWORD = register("infused_sword", new ItemInfusedSword());
    Item EMPTY_TRAP = register("empty_trap", new Item(new Item.Settings()));
    Item RIFT_TRAP = register("rift_trap", new ItemTrap(new Item.Settings(), 1));
    Item FLAME_TRAP = register("flame_trap", new ItemTrap(new Item.Settings(), 2));
    // TODO: Ice trap
    // Item XYZ_TRAP = register("xyz_trap", new ItemTrap(new Item.Settings()));
    Item SEARING_BOW = register("searing_bow", new ItemSearingBow(new Item.Settings().maxDamage(384)));
    Item CATALYST_MIXTURE = register("catalyst_mixture", new Item(new Item.Settings()));
    Item STABLE_CATALYST_MIXTURE = register("stable_catalyst_mixture", new Item(new Item.Settings()));
    Item STABLE_NEXUS_CATALYST = register("stable_nexus_catalys", new Item(new Item.Settings()));
    Item DAMPING_AGENT = register("damping_agent", new Item(new Item.Settings()));
    Item STRONG_DAMPING_AGENT = register("strong_damping_agent", new Item(new Item.Settings()));
    Item STRANGE_BONE = register("strange_bone", new ItemStrangeBone(new Item.Settings()));
    Item NEXUS_ADJUSTER = register("nexus_adjuster", new ItemProbe(new Item.Settings().maxCount(1), false));
    Item MATERIAL_PROBE = register("material_probe", new ItemProbe(new Item.Settings().maxCount(1), true));
    Item STRONG_CATALYST = register("strong_catalyst", new Item(new Item.Settings()));
    Item ENGY_HAMMER = register("engy_hammer", new Item(new Item.Settings())); // TODO: has 3d model
    //ItemSpawnEgg SPAWN_EGG;
    // TODO: Spawn eggs

    RegistryKey<Item> DEBUG_WAND = RegistryKey.of(RegistryKeys.ITEM, InvasionMod.id("debug_wand"));

    Item NEXUS_CORE = register("nexus_core", new BlockItem(InvBlocks.NEXUS_CORE, new Item.Settings()));

    private static <T extends Item> T register(String name, T item) {
        REGISTRY.add(item);
        return Registry.register(Registries.ITEM, InvasionMod.id(name), item);
    }

    static void bootstrap() {
        if (mod_Invasion.isDebug()) {
            register("debug_wand", new ItemDebugWand(new Item.Settings().maxCount(1)));
        }
        Identifier tabId = InvasionMod.id("invasion_mod");
        Registry.register(Registries.ITEM_GROUP, tabId, FabricItemGroup.builder().entries((context, entries) -> {
            REGISTRY.forEach(item -> entries.add(item.getDefaultStack()));
        }).icon(NEXUS_CORE::getDefaultStack).displayName(Text.translatable(Util.createTranslationKey("itemGroup", tabId))).build());
    }
}
