package com.invasion.item;

import java.util.ArrayList;
import java.util.List;
import com.invasion.InvasionMod;
import com.invasion.block.InvBlocks;
import com.invasion.entity.TrapEntity;
import com.invasion.entity.InvEntities;
import com.invasion.entity.NexusEntity;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public interface InvItems {
    List<Item> REGISTRY = new ArrayList<>();
    List<Item> SPAWN_EGGS = new ArrayList<>();

    Item PHASE_CRYSTAL = register("phase_crystal", new Item(new Item.Settings()));
    Item RIFT_FLUX = register("rift_flux", new Item(new Item.Settings()));
    Item SMALL_REMNANTS = register("small_remnants", new Item(new Item.Settings()));

    Item INFUSED_SWORD = register("infused_sword", new InfusedSwordItem());
    Item SEARING_BOW = register("searing_bow", new SearingBowItem(new Item.Settings().maxDamage(384)));
    Item ENGY_HAMMER = register("engineer_hammer", new Item(new Item.Settings()));

    Item EMPTY_TRAP = register("empty_trap", new Item(new Item.Settings()));
    Item RIFT_TRAP = register("rift_trap", new TrapItem(new Item.Settings(), TrapEntity.Type.RIFT));
    Item FLAME_TRAP = register("flame_trap", new TrapItem(new Item.Settings(), TrapEntity.Type.FIRE));
    // TODO: Ice trap
    // Item XYZ_TRAP = register("xyz_trap", new ItemTrap(new Item.Settings()));

    Item CATALYST_MIXTURE = register("catalyst_mixture", new Item(new Item.Settings()));
    Item STABLE_CATALYST_MIXTURE = register("stable_catalyst_mixture", new Item(new Item.Settings()));

    Item NEXUS_CATALYST = register("nexus_catalyst", new Item(new Item.Settings()));
    Item STABLE_NEXUS_CATALYST = register("stable_nexus_catalyst", new Item(new Item.Settings()));
    Item STRONG_NEXUS_CATALYST = register("strong_nexus_catalyst", new Item(new Item.Settings()));

    Item DAMPING_AGENT = register("damping_agent", new Item(new Item.Settings()));
    Item STRONG_DAMPING_AGENT = register("strong_damping_agent", new Item(new Item.Settings()));

    Item STRANGE_BONE = register("strange_bone", new StrangeBoneItem(new Item.Settings()));
    Item NEXUS_ADJUSTER = register("nexus_adjuster", new ProbeItem(new Item.Settings().maxCount(1), false));
    Item MATERIAL_PROBE = register("material_probe", new ProbeItem(new Item.Settings().maxCount(1), true));

    //ItemSpawnEgg SPAWN_EGG;
    // TODO: Spawn eggs

    RegistryKey<Item> DEBUG_WAND = RegistryKey.of(RegistryKeys.ITEM, InvasionMod.id("debug_wand"));

    Item NEXUS_CORE = register("nexus_core", new BlockItem(InvBlocks.NEXUS_CORE, new Item.Settings()));

    Item ZOMBIE_SPAWN_EGG = register("zombie_spawn_egg", createSpawnEgg(InvEntities.ZOMBIE, 0x6B753F, 0x281B0A, NexusEntity.createVariant(0, 1)));
    Item TIER_TWO_ZOMBIE_SPAWN_EGG = register("tier_two_zombie_spawn_egg", createSpawnEgg(InvEntities.ZOMBIE, 0x497533, 0x7C7C7C, NexusEntity.createVariant(0, 2)));
    Item TAR_ZOMBIE_SPAWN_EGG = register("tar_zombie_spawn_egg", createSpawnEgg(InvEntities.ZOMBIE, 0x3A4225, 0x191C13, NexusEntity.createVariant(2, 2)));
    Item ZOMBIE_BRUTE_SPAWN_EGG = register("zombie_brute_spawn_egg", createSpawnEgg(InvEntities.ZOMBIE, 0x3A4225, 0x191C13, NexusEntity.createVariant(0, 3)));
    Item SKELETON_SPAWN_EGG = register("skeleton_spawn_egg", createSpawnEgg(InvEntities.SKELETON, 0x9B9B9B, 0x797979));

    Item SPIDER_SPAWN_EGG = register("spider_spawn_egg", createSpawnEgg(InvEntities.SPIDER, 0x504A3E, 0xA4121C));
    Item JUMPING_SPIDER_SPAWN_EGG = register("jumping_spider_spawn_egg", createSpawnEgg(InvEntities.JUMPING_SPIDER, 0x444167, 0x0A0328));
    Item MOTHER_SPIDER_SPAWN_EGG = register("mother_spider_spawn_egg", createSpawnEgg(InvEntities.QUEEN_SPIDER, 0x444167, 0x0A0328));

    Item CREEPER_SPAWN_EGG = register("creeper_spawn_egg", createSpawnEgg(InvEntities.CREEPER, 0x238F1F, 0xA5AAA6));
    Item PIGMAN_ENGINEER_SPAWN_EGG = register("pigman_engineer_spawn_egg", createSpawnEgg(InvEntities.PIGMAN_ENGINEER, 0xEC9695, 0x420000));
    Item THROWER_SPAWN_EGG = register("thrower_spawn_egg", createSpawnEgg(InvEntities.THROWER, 0x545F37, 0x1D2D3E));
    Item BIG_THROWER_SPAWN_EGG = register("big_thrower_spawn_egg", createSpawnEgg(InvEntities.THROWER, 0x5303814, 0x632808, NexusEntity.createVariant(0, 2)));
    Item IMP_SPAWN_EGG = register("imp_spawn_egg", createSpawnEgg(InvEntities.IMP, 0xB40113, 0xFF0000));
    Item ZOMBIE_PIGMAN_SPAWN_EGG = register("pigman_zombie_spawn_egg", createSpawnEgg(InvEntities.ZOMBIE_PIGMAN, 0xEB8E91, 0x49652F, NexusEntity.createVariant(1, 1)));
    Item TIER_TWO_ZOMBIE_PIGMAN_SPAWN_EGG = register("tier_two_pigman_zombie_spawn_egg", createSpawnEgg(InvEntities.ZOMBIE_PIGMAN, 0xEB8E91, 0x49652F, NexusEntity.createVariant(1, 2)));
    Item ZOMBIE_PIGMAN_BRUTE_SPAWN_EGG = register("zombie_pigman_brute_spawn_egg", createSpawnEgg(InvEntities.ZOMBIE_PIGMAN, 0xEB8E91, 0x49652F, NexusEntity.createVariant(1, 3)));

    RegistryKey<Item> BIRD_SPAWN_EGG = RegistryKey.of(RegistryKeys.ITEM, InvasionMod.id("bird_spawn_egg"));
    RegistryKey<Item> VULTURE_SPAWN_EGG = RegistryKey.of(RegistryKeys.ITEM, InvasionMod.id("vulture_spawn_egg"));

    private static Item createSpawnEgg(EntityType<? extends MobEntity> type, int primaryColor, int secondaryColor, NbtComponent data) {
        return new SpawnEggItem(type, primaryColor, secondaryColor, new Item.Settings().component(DataComponentTypes.ENTITY_DATA, data));
    }

    private static Item createSpawnEgg(EntityType<? extends MobEntity> type, int primaryColor, int secondaryColor) {
        return new SpawnEggItem(type, primaryColor, secondaryColor, new Item.Settings());
    }

    private static <T extends Item> T register(String name, T item) {
        REGISTRY.add(item);
        return Registry.register(Registries.ITEM, InvasionMod.id(name), item);
    }

    static void bootstrap() {
        if (InvasionMod.getConfig().debugMode) {
            register("debug_wand", new DebugWandItem(new Item.Settings().maxCount(1)));
            register("bird_spawn_egg", createSpawnEgg(InvEntities.BIRD, 0x2B2B2B, 0xEA7EDC));
            register("vulture_spawn_egg", createSpawnEgg(InvEntities.VULTURE, 0x2B2B2B, 0xEA7EDC));
        }
        Identifier tabId = InvasionMod.id("invasion_mod");
        Registry.register(Registries.ITEM_GROUP, tabId, FabricItemGroup.builder().entries((context, entries) -> {
            REGISTRY.forEach(item -> entries.add(item.getDefaultStack()));
        }).icon(NEXUS_CORE::getDefaultStack).displayName(Text.translatable(Util.createTranslationKey("itemGroup", tabId))).build());

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(event -> {
            SPAWN_EGGS.forEach(event::add);
        });

        FuelRegistry.INSTANCE.add(NEXUS_CATALYST, 10);
        FuelRegistry.INSTANCE.add(STABLE_NEXUS_CATALYST, 16);
    }
}
