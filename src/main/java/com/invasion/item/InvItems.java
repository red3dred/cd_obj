package com.invasion.item;

import java.util.ArrayList;
import java.util.List;

import com.invasion.InvasionMod;
import com.invasion.block.InvBlocks;
import com.invasion.entity.EntityIMTrap;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.registry.FuelRegistry;
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

    Item INFUSED_SWORD = register("infused_sword", new ItemInfusedSword());
    Item SEARING_BOW = register("searing_bow", new ItemSearingBow(new Item.Settings().maxDamage(384)));
    Item ENGY_HAMMER = register("engineer_hammer", new Item(new Item.Settings())); // TODO: has 3d model

    Item EMPTY_TRAP = register("empty_trap", new Item(new Item.Settings()));
    Item RIFT_TRAP = register("rift_trap", new ItemTrap(new Item.Settings(), EntityIMTrap.Type.RIFT));
    Item FLAME_TRAP = register("flame_trap", new ItemTrap(new Item.Settings(), EntityIMTrap.Type.FIRE));
    // TODO: Ice trap
    // Item XYZ_TRAP = register("xyz_trap", new ItemTrap(new Item.Settings()));

    Item CATALYST_MIXTURE = register("catalyst_mixture", new Item(new Item.Settings()));
    Item STABLE_CATALYST_MIXTURE = register("stable_catalyst_mixture", new Item(new Item.Settings()));

    Item NEXUS_CATALYST = register("nexus_catalyst", new Item(new Item.Settings()));
    Item STABLE_NEXUS_CATALYST = register("stable_nexus_catalys", new Item(new Item.Settings()));
    Item STRONG_NEXUS_CATALYST = register("strong_nexus_catalyst", new Item(new Item.Settings()));

    Item DAMPING_AGENT = register("damping_agent", new Item(new Item.Settings()));
    Item STRONG_DAMPING_AGENT = register("strong_damping_agent", new Item(new Item.Settings()));

    Item STRANGE_BONE = register("strange_bone", new ItemStrangeBone(new Item.Settings()));
    Item NEXUS_ADJUSTER = register("nexus_adjuster", new ItemProbe(new Item.Settings().maxCount(1), false));
    Item MATERIAL_PROBE = register("material_probe", new ItemProbe(new Item.Settings().maxCount(1), true));

    //ItemSpawnEgg SPAWN_EGG;
    // TODO: Spawn eggs

    RegistryKey<Item> DEBUG_WAND = RegistryKey.of(RegistryKeys.ITEM, InvasionMod.id("debug_wand"));

    Item NEXUS_CORE = register("nexus_core", new BlockItem(InvBlocks.NEXUS_CORE, new Item.Settings()));

    private static <T extends Item> T register(String name, T item) {
        REGISTRY.add(item);
        return Registry.register(Registries.ITEM, InvasionMod.id(name), item);
    }

    static void bootstrap() {
        if (InvasionMod.getConfig().debugMode) {
            register("debug_wand", new ItemDebugWand(new Item.Settings().maxCount(1)));
        }
        Identifier tabId = InvasionMod.id("invasion_mod");
        Registry.register(Registries.ITEM_GROUP, tabId, FabricItemGroup.builder().entries((context, entries) -> {
            REGISTRY.forEach(item -> entries.add(item.getDefaultStack()));
        }).icon(NEXUS_CORE::getDefaultStack).displayName(Text.translatable(Util.createTranslationKey("itemGroup", tabId))).build());

        // spawneggs' needed things and dispenser behavior
        //GameRegistry.registerItem(itemSpawnEgg, itemSpawnEgg.getUnlocalizedName());
        //BlockDispenser.dispenseBehaviorRegistry.putObject(itemSpawnEgg, new DispenserBehaviorSpawnEgg());

        // Add spawneggs
        /*
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 1, "IMZombie", "Zombie T1", CustomTags.IMZombie_T1(), 0x6B753F, 0x281B0A));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 2, "IMZombie", "Zombie T2", CustomTags.IMZombie_T2(), 0x497533, 0x7C7C7C));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 3, "IMZombie", "Tar Zombie T2", CustomTags.IMZombie_T2_tar(), 0x3A4225, 0x191C13));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 4, "IMZombie", "Zombie Brute T3", CustomTags.IMZombie_T3(), 0x586146, 0x1E4639));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 5, "IMSkeleton", "Skeleton T1", new NBTTagCompound(), 0x9B9B9B, 0x797979));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 6, "IMSpider", "Spider T1", new NBTTagCompound(), 0x504A3E, 0xA4121C));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 7, "IMSpider", "Spider T1 Baby", CustomTags.IMSpider_T1_baby(), 0x504A3E, 0xA4121C));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 8, "IMSpider", "Spider T2 Jumper", CustomTags.IMSpider_T2(), 0x444167, 0x0A0328));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 9, "IMSpider", "Spider T2 Mother", CustomTags.IMSpider_T2_mother(), 0x444167, 0x0A0328));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 10, "IMCreeper", "Creeper T1", new NBTTagCompound(), 0x238F1F, 0xA5AAA6));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 11, "IMPigEngy", "Pigman Engineer T1", new NBTTagCompound(), 0xEC9695, 0x420000));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 12, "IMThrower", "Thrower T1", new NBTTagCompound(), 0x545F37, 0x1D2D3E));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 13, "IMThrower", "Thrower T2", CustomTags.IMThrower_T2(), 0x5303814, 0x632808));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 14, "IMImp", "Imp T1", new NBTTagCompound(), 0xB40113, 0xFF0000));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 15, "IMZombiePigman", "Zombie Pigman T1", CustomTags.IMZombiePigman_T1(), 0xEB8E91, 0x49652F));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 16, "IMZombiePigman", "Zombie Pigman T2", CustomTags.IMZombiePigman_T2(), 0xEB8E91, 0x49652F));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 17, "IMZombiePigman", "Zombie Pigman T3", CustomTags.IMZombiePigman_T3(), 0xEB8E91, 0x49652F));

        if (debugMode) {
            SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 18, "IMGiantBird", "Vulture T1", new NBTTagCompound(), 0x2B2B2B, 0xEA7EDC));
        }*/

        /*
        GameRegistry.addRecipe(new ItemStack(blockNexus, 1), new Object[] { " X ", "#D#", " # ", Character.valueOf('X'),
                itemPhaseCrystal, Character.valueOf('#'), Items.redstone, Character.valueOf('D'), Blocks.obsidian });

        GameRegistry.addRecipe(new ItemStack(itemPhaseCrystal, 1),
                new Object[] { " X ", "#D#", " X ", Character.valueOf('X'), new ItemStack(Items.dye, 1, 4),
                        Character.valueOf('#'), Items.redstone, Character.valueOf('D'), Items.diamond });

        GameRegistry.addRecipe(new ItemStack(itemPhaseCrystal, 1),
                new Object[] { " X ", "#D#", " X ", Character.valueOf('X'), Items.redstone, Character.valueOf('#'),
                        new ItemStack(Items.dye, 1, 4), Character.valueOf('D'), Items.diamond });

        GameRegistry.addRecipe(new ItemStack(itemRiftFlux, 1),
                new Object[] { "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(itemSmallRemnants, 1) });

        GameRegistry.addRecipe(new ItemStack(itemInfusedSword, 1),
                new Object[] { "X  ", "X# ", "X  ", Character.valueOf('X'), new ItemStack(itemRiftFlux, 1),
                        Character.valueOf('#'), new ItemStack(Items.diamond_sword, 1, OreDictionary.WILDCARD_VALUE) });

        GameRegistry.addRecipe(new ItemStack(itemCatalystMixture, 1),
                new Object[] { "   ", "D#H", " X ", Character.valueOf('X'), Items.bowl, Character.valueOf('#'),
                        Items.redstone, Character.valueOf('D'), Items.bone, Character.valueOf('H'),
                        Items.rotten_flesh });

        GameRegistry.addRecipe(new ItemStack(itemCatalystMixture, 1),
                new Object[] { "   ", "H#D", " X ", Character.valueOf('X'), Items.bowl, Character.valueOf('#'),
                        Items.redstone, Character.valueOf('D'), Items.bone, Character.valueOf('H'),
                        Items.rotten_flesh });

        GameRegistry.addRecipe(new ItemStack(itemStableCatalystMixture, 1),
                new Object[] { "   ", "D#D", " X ", Character.valueOf('X'), Items.bowl, Character.valueOf('#'),
                        Items.coal, Character.valueOf('D'), Items.bone, Character.valueOf('H'), Items.rotten_flesh });

        GameRegistry.addRecipe(new ItemStack(itemDampingAgent, 1),
                new Object[] { "   ", "#X#", "   ", Character.valueOf('X'), new ItemStack(itemRiftFlux, 1),
                        Character.valueOf('#'), new ItemStack(Items.dye, 1, 4) });

        GameRegistry.addRecipe(new ItemStack(itemStrongDampingAgent, 1),
                new Object[] { " X ", " X ", " X ", Character.valueOf('X'), itemDampingAgent });

        GameRegistry.addRecipe(new ItemStack(itemStrongDampingAgent, 1),
                new Object[] { "   ", "XXX", "   ", Character.valueOf('X'), itemDampingAgent });

        GameRegistry.addRecipe(new ItemStack(itemStrangeBone, 1), new Object[] { "   ", "X#X", "   ",
                Character.valueOf('X'), new ItemStack(itemRiftFlux, 1), Character.valueOf('#'), Items.bone });

        GameRegistry.addRecipe(new ItemStack(itemSearingBow, 1),
                new Object[] { "XXX", "X# ", "X  ", Character.valueOf('X'), new ItemStack(itemRiftFlux, 1),
                        Character.valueOf('#'), new ItemStack(Items.bow, 1, OreDictionary.WILDCARD_VALUE) });

        GameRegistry.addRecipe(new ItemStack(Items.gunpowder, 16),
                new Object[] { " X ", " X ", " X ", Character.valueOf('X'), new ItemStack(itemRiftFlux, 1) });
        GameRegistry.addRecipe(new ItemStack(Items.gunpowder, 16),
                new Object[] { "   ", "XXX", "   ", Character.valueOf('X'), new ItemStack(itemRiftFlux, 1) });

        GameRegistry.addRecipe(new ItemStack(Items.diamond, 1),
                new Object[] { " X ", "X X", " X ", Character.valueOf('X'), new ItemStack(itemRiftFlux, 1) });

        GameRegistry.addRecipe(new ItemStack(Items.iron_ingot, 4),
                new Object[] { "   ", " X ", "   ", Character.valueOf('X'), new ItemStack(itemRiftFlux, 1) });

        GameRegistry.addRecipe(new ItemStack(Items.redstone, 24),
                new Object[] { "   ", "X X", "   ", Character.valueOf('X'), new ItemStack(itemRiftFlux, 1) });

        GameRegistry.addRecipe(new ItemStack(Items.dye, 12, 4),
                new Object[] { " X ", "   ", " X ", Character.valueOf('X'), new ItemStack(itemRiftFlux, 1) });

        GameRegistry.addRecipe(new ItemStack(itemIMTrap, 1, 0), new Object[] { " X ", "X#X", " X ",
                Character.valueOf('X'), Items.iron_ingot, Character.valueOf('#'), new ItemStack(itemRiftFlux, 1) });

        GameRegistry.addRecipe(new ItemStack(itemIMTrap, 1, 2), new Object[] { "   ", " # ", " X ",
                Character.valueOf('X'), new ItemStack(itemIMTrap, 1, 0), Character.valueOf('#'), Items.lava_bucket });

        GameRegistry.addRecipe(new ItemStack(itemProbe, 1, 0),
                new Object[] { " X ", "XX ", "XX ", Character.valueOf('X'), Items.iron_ingot });

        GameRegistry.addRecipe(new ItemStack(itemProbe, 1, 1),
                new Object[] { " D ", " # ", " X ", Character.valueOf('X'), Items.blaze_rod, Character.valueOf('#'),
                        itemPhaseCrystal, Character.valueOf('D'), new ItemStack(itemProbe, 1, 0) });

        GameRegistry.addSmelting(itemCatalystMixture, new ItemStack(itemNexusCatalyst), 1.0F);
        GameRegistry.addSmelting(itemStableCatalystMixture, new ItemStack(itemStableNexusCatalyst), 1.0F);
        */

        // TODO: Check whether we want this, lol
        FuelRegistry.INSTANCE.add(NEXUS_CATALYST, 1);
        FuelRegistry.INSTANCE.add(STABLE_NEXUS_CATALYST, 1);
    }
}
