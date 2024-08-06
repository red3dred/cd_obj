package invmod.common;

import invmod.common.block.InvBlocks;
import invmod.common.creativetab.CreativeTabInvmod;
import invmod.common.entity.EntityIMBird;
import invmod.common.entity.EntityIMBolt;
import invmod.common.entity.EntityIMBoulder;
import invmod.common.entity.EntityIMBurrower;
import invmod.common.entity.EntityIMCreeper;
import invmod.common.entity.EntityIMEgg;
import invmod.common.entity.EntityIMGiantBird;
import invmod.common.entity.EntityIMImp;
import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.EntityIMPigEngy;
import invmod.common.entity.EntityIMPrimedTNT;
import invmod.common.entity.EntityIMSkeleton;
import invmod.common.entity.EntityIMSpawnProxy;
import invmod.common.entity.EntityIMSpider;
import invmod.common.entity.EntityIMThrower;
import invmod.common.entity.EntityIMTrap;
import invmod.common.entity.EntityIMWolf;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.EntityIMZombiePigman;
import invmod.common.item.ItemDebugWand;
import invmod.common.item.ItemInfusedSword;
import invmod.common.item.ItemProbe;
import invmod.common.item.ItemSearingBow;
import invmod.common.item.ItemStrangeBone;
import invmod.common.item.ItemTrap;
import invmod.common.nexus.BlockNexus;
import invmod.common.nexus.IEntityIMPattern;
import invmod.common.nexus.IMWaveBuilder;
import invmod.common.nexus.MobBuilder;
import invmod.common.nexus.TileEntityNexus;
import invmod.common.util.ISelect;
import invmod.common.util.RandomSelectionPool;
import invmod.common.util.spawneggs.CustomTags;
import invmod.common.util.spawneggs.DispenserBehaviorSpawnEgg;
import invmod.common.util.spawneggs.ItemSpawnEgg;
import invmod.common.util.spawneggs.SpawnEggInfo;
import invmod.common.util.spawneggs.SpawnEggRegistry;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class mod_Invasion {

    public static ProxyCommon proxy;

    public static String recentNews;

    public static GuiHandler guiHandler;
    public static ConfigInvasion configInvasion;
    private static File configFile;
    private static boolean runFlag;
    private static long timer;
    private static long clientElapsed;
    private static long serverElapsed;
    private static boolean serverRunFlag;
    private static int killTimer;
    private static boolean loginFlag;
    private static HashMap<String, Long> deathList = new HashMap();
    private static MobBuilder defaultMobBuilder = new MobBuilder();

    private static ISelect<IEntityIMPattern> nightSpawnPool1;
    private static TileEntityNexus focusNexus;
    private static TileEntityNexus activeNexus;
    private static boolean isInvasionActive = false;
    public static final byte PACKET_SFX = 0;
    public static final byte PACKET_INV_MOB_SPAWN = 2;
    public static int entityId = 250;
    public static final String modid = "mod_Invasion";
    /*
     * NOOB HAUS: Default settings for blocks/items etc; used to write config
     * (common.Config) on first run
     */

    // Change this before releasing
    private static final boolean DEBUG_CONFIG = false;
    private static final int DEFAULT_GUI_ID_NEXUS = 76;
    private static final boolean DEFAULT_CRAFT_ITEMS_ENABLED = true;
    private static final boolean DEFAULT_NIGHT_SPAWNS_ENABLED = false;
    private static final int DEFAULT_MIN_CONT_MODE_DAYS = 2;
    private static final int DEFAULT_MAX_CONT_MODE_DAYS = 3;
    private static final int DEFAULT_NIGHT_MOB_SIGHT_RANGE = 20;
    private static final int DEFAULT_NIGHT_MOB_SENSE_RANGE = 8;
    private static final int DEFAULT_NIGHT_MOB_SPAWN_CHANCE = 30;
    private static final int DEFAULT_NIGHT_MOB_MAX_GROUP_SIZE = 3;
    private static final int DEFAULT_NIGHT_MOB_LIMIT_OVERRIDE = 70;
    private static final float DEFAULT_NIGHT_MOB_STATS_SCALING = 1.0F;
    private static final boolean DEFAULT_NIGHT_MOBS_BURN = true;
    public static final String[] DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS = { "zombie_t1_any", "zombie_t2_any_basic",
            "zombie_t2_plain", "zombie_t2_tar", "zombie_t2_pigman", "zombie_t3_any", "zombiePigman_t1_any",
            "zombiePigman_t2_any", "zombiePigman_t3_any", "spider_t1_any", "spider_t2_any", "pigengy_t1_any",
            "skeleton_t1_any", "thrower_t1", "thrower_t2", "creeper_t1_basic", "imp_t1" };
    public static final float[] DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS = { 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F };

    // NOOB HAUS: Declare them values.. Declare em good
    private static boolean alreadyNotified;
    private static boolean updateNotifications;
    private static boolean destructedBlocksDrop;
    private static boolean craftItemsEnabled;
    private static boolean debugMode;
    private static int guiIdNexus;
    private static int minContinuousModeDays;
    private static int maxContinuousModeDays;
    private static boolean nightSpawnsEnabled;
    private static int nightMobSightRange;
    private static int nightMobSenseRange;
    private static int nightMobSpawnChance;
    private static int nightMobMaxGroupSize;
    private static int maxNightMobs;
    private static float nightMobStatsScaling;
    private static boolean nightMobsBurnInDay;
    private static boolean enableLog;

    // mobhealth
    public static Map<String, Integer> mobHealthNightspawn = new HashMap<>();
    public static Map<String, Integer> mobHealthInvasion = new HashMap<>();
    // Creative tab declaration
    @Deprecated
    public static CreativeTabInvmod tabInvmod;

    // NOOB HAUS: The almighty Nexus Block declaration
    @Deprecated
    public static final BlockNexus blockNexus = InvBlocks.NEXUS_CORE;

    // NOOB HAUS: Item declarations

    public static mod_Invasion instance;

    public mod_Invasion() {
        instance = this;
        runFlag = true;
        serverRunFlag = true;
        loginFlag = false;
        timer = 0L;
        clientElapsed = 0L;
        guiHandler = new GuiHandler();
    }
    // NOOB HAUS: End wtf? I am not certain what this method is for..

    //@EventHandler
    public void preInit(/*FMLPreInitializationEvent event*/) {

        // First up, we check for the config file, write it if it doesn't exist; or

        // NOOB HAUS: Get the config file - store it into a variable; pass all that shiz
        // thru common.configInvasion
        configFile = proxy.getFile("/config/invasion_config.cfg");
        alreadyNotified = false;

        configInvasion = new ConfigInvasion();
        configInvasion.loadConfig(configFile);

        enableLog = configInvasion.getPropertyValueBoolean("enable-log-file", false);
        destructedBlocksDrop = configInvasion.getPropertyValueBoolean("destructed-blocks-drop", true);
        updateNotifications = configInvasion.getPropertyValueBoolean("update-messages-enabled", false);
        // soundsEnabled = configInvasion.getPropertyValueBoolean("sounds-enabled",
        // true);
        craftItemsEnabled = configInvasion.getPropertyValueBoolean("craft-items-enabled", true);
        debugMode = configInvasion.getPropertyValueBoolean("debug", false);
        guiIdNexus = configInvasion.getPropertyValueInt("guiID-Nexus", 76);

        minContinuousModeDays = configInvasion.getPropertyValueInt("min-days-to-attack", 2);
        maxContinuousModeDays = configInvasion.getPropertyValueInt("max-days-to-attack", 3);

        nightSpawnConfig();
        loadHealthConfig();
        // NOOB HAUS: Here a HashMap is done up for the block strength (what it takes
        // for IM mob to dig thru it)
        HashMap strengthOverrides = new HashMap();
        for (int i = 1; i < 4096; i++) {
            String property = configInvasion.getProperty("block" + i + "-strength", "null");
            if (property != "null") {
                float strength = Float.parseFloat(property);
                if (strength > 0.0F) {
                    strengthOverrides.put(Integer.valueOf(i), Float.valueOf(strength));
                    EntityIMLiving.putBlockStrength(Block.getBlockById(i), strength);
                    float pathCost = 1.0F + strength * 0.4F;
                    EntityIMLiving.putBlockCost(Block.getBlockById(i), pathCost);
                }
            }

        }

        configInvasion.saveConfig(configFile, strengthOverrides, DEBUG_CONFIG);

        // Load the Things!!
        loadEntities();
    }

    public void load(/*FMLInitializationEvent event*/) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, guiHandler);

        if (craftItemsEnabled) {
            addRecipes();
        }

        if (nightSpawnsEnabled) {
            BiomeGenBase[] biomes = { BiomeGenBase.plains, BiomeGenBase.extremeHills, BiomeGenBase.forest,
                    BiomeGenBase.taiga, BiomeGenBase.swampland, BiomeGenBase.forestHills, BiomeGenBase.taigaHills,
                    BiomeGenBase.extremeHillsEdge, BiomeGenBase.jungle, BiomeGenBase.jungleHills };
            EntityRegistry.addSpawn(EntityIMSpawnProxy.class, nightMobSpawnChance, 1, 1, EnumCreatureType.monster,
                    biomes);
            EntityRegistry.addSpawn(EntityZombie.class, 1, 1, 1, EnumCreatureType.monster, biomes);
            EntityRegistry.addSpawn(EntitySpider.class, 1, 1, 1, EnumCreatureType.monster, biomes);
            EntityRegistry.addSpawn(EntitySkeleton.class, 1, 1, 1, EnumCreatureType.monster, biomes);
        }

        if (maxNightMobs != 70) {
            try {
                Class c = EnumCreatureType.class;
                Object[] consts = c.getEnumConstants();
                Class sub = consts[0].getClass();
                Field field = sub.getDeclaredField("maxNumberOfCreature");
                field.setAccessible(true);
                field.set(EnumCreatureType.monster, Integer.valueOf(maxNightMobs));
            } catch (Exception e) {
                log(e.getMessage());
            }
        }

    }

    /*
     * @SubscribeEvent public void PlayerLoggedInEvent
     * (cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) { try {
     * if(getUpdateNotifications() && !alreadyNotified) { alreadyNotified=true;
     * //VersionChecker.checkForUpdates((EntityPlayerMP)event.player); } }
     * catch(Exception e) {} }
     */

    // load mobhealth config
    private void loadHealthConfig() {
        // Invasion spawns
        mobHealthInvasion.put("IMCreeper-T1-invasionSpawn-health", configInvasion.getPropertyValueInt("IMCreeper-T1-invasionSpawn-health", 20));
        mobHealthInvasion.put("IMVulture-T1-invasionSpawn-health", configInvasion.getPropertyValueInt("IMVulture-T1-invasionSpawn-health", 20));
        mobHealthInvasion.put("IMImp-T1-invasionSpawn-health", configInvasion.getPropertyValueInt("IMImp-T1-invasionSpawn-health", 20));
        mobHealthInvasion.put("IMPigManEngineer-T1-invasionSpawn-health", configInvasion.getPropertyValueInt("IMPigManEngineer-T1-invasionSpawn-health", 20));
        mobHealthInvasion.put("IMSkeleton-T1-invasionSpawn-health", configInvasion.getPropertyValueInt("IMSkeleton-T1-invasionSpawn-health", 20));
        mobHealthInvasion.put("IMSpider-T1-Spider-invasionSpawn-health", configInvasion.getPropertyValueInt("IMSpider-T1-Spider-invasionSpawn-health", 18));
        mobHealthInvasion.put("IMSpider-T1-Baby-Spider-invasionSpawn-health", configInvasion.getPropertyValueInt("IMSpider-T1-Baby-Spider-invasionSpawn-health", 3));
        mobHealthInvasion.put("IMSpider-T2-Jumping-Spider-invasionSpawn-health", configInvasion.getPropertyValueInt("IMSpider-T2-Jumping-Spider-invasionSpawn-health", 18));
        mobHealthInvasion.put("IMSpider-T2-Mother-Spider-invasionSpawn-health", configInvasion.getPropertyValueInt("IMSpider-T2-Mother-Spider-invasionSpawn-health", 23));
        mobHealthInvasion.put("IMThrower-T1-invasionSpawn-health", configInvasion.getPropertyValueInt("IMThrower-T1-invasionSpawn-health", 50));
        mobHealthInvasion.put("IMThrower-T2-invasionSpawn-health", configInvasion.getPropertyValueInt("IMThrower-T2-invasionSpawn-health", 70));
        mobHealthInvasion.put("IMZombie-T1-invasionSpawn-health", configInvasion.getPropertyValueInt("IMZombie-T1-invasionSpawn-health", 20));
        mobHealthInvasion.put("IMZombie-T2-invasionSpawn-health", configInvasion.getPropertyValueInt("IMZombie-T2-invasionSpawn-health", 30));
        mobHealthInvasion.put("IMZombie-T3-invasionSpawn-health", configInvasion.getPropertyValueInt("IMZombie-T3-invasionSpawn-health", 65));
        mobHealthInvasion.put("IMZombiePigman-T1-invasionSpawn-health", configInvasion.getPropertyValueInt("IMZombiePigman-T1-invasionSpawn-health", 20));
        mobHealthInvasion.put("IMZombiePigman-T2-invasionSpawn-health", configInvasion.getPropertyValueInt("IMZombiePigman-T2-invasionSpawn-health", 30));
        mobHealthInvasion.put("IMZombiePigman-T3-invasionSpawn-health", configInvasion.getPropertyValueInt("IMZombiePigman-T3-invasionSpawn-health", 65));

        // Night spawns
        mobHealthNightspawn.put("IMCreeper-T1-nightSpawn-health", configInvasion.getPropertyValueInt("IMCreeper-T1-nightSpawn-health", 20));
        mobHealthNightspawn.put("IMVulture-T1-nightSpawn-health", configInvasion.getPropertyValueInt("IMVulture-T1-nightSpawn-health", 20));
        mobHealthNightspawn.put("IMImp-T1-nightSpawn-health", configInvasion.getPropertyValueInt("IMImp-T1-nightSpawn-health", 20));
        mobHealthNightspawn.put("IMPigManEngineer-T1-nightSpawn-health", configInvasion.getPropertyValueInt("IMPigManEngineer-T1-nightSpawn-health", 20));
        mobHealthNightspawn.put("IMSkeleton-T1-nightSpawn-health", configInvasion.getPropertyValueInt("IMSkeleton-T1-nightSpawn-health", 20));
        mobHealthNightspawn.put("IMSpider-T1-Spider-nightSpawn-health", configInvasion.getPropertyValueInt("IMSpider-T1-Spider-nightSpawn-health", 18));
        mobHealthNightspawn.put("IMSpider-T1-Baby-Spider-nightSpawn-health", configInvasion.getPropertyValueInt("IMSpider-T1-Baby-Spider-nightSpawn-health", 3));
        mobHealthNightspawn.put("IMSpider-T2-Jumping-Spider-nightSpawn-health", configInvasion.getPropertyValueInt("IMSpider-T2-Jumping-Spider-nightSpawn-health", 18));
        mobHealthNightspawn.put("IMSpider-T2-Mother-Spider-nightSpawn-health", configInvasion.getPropertyValueInt("IMSpider-T2-Mother-Spider-nightSpawn-health", 23));
        mobHealthNightspawn.put("IMThrower-T1-nightSpawn-health", configInvasion.getPropertyValueInt("IMThrower-T1-nightSpawn-health", 50));
        mobHealthNightspawn.put("IMThrower-T2-nightSpawn-health", configInvasion.getPropertyValueInt("IMThrower-T2-nightSpawn-health", 70));
        mobHealthNightspawn.put("IMZombie-T1-nightSpawn-health", configInvasion.getPropertyValueInt("IMZombie-T1-nightSpawn-health", 20));
        mobHealthNightspawn.put("IMZombie-T2-nightSpawn-health", configInvasion.getPropertyValueInt("IMZombie-T2-nightSpawn-health", 30));
        mobHealthNightspawn.put("IMZombie-T3-nightSpawn-health", configInvasion.getPropertyValueInt("IMZombie-T3-nightSpawn-health", 65));
        mobHealthNightspawn.put("IMZombiePigman-T1-nightSpawn-health", configInvasion.getPropertyValueInt("IMZombiePigman-T1-nightSpawn-health", 20));
        mobHealthNightspawn.put("IMZombiePigman-T2-nightSpawn-health", configInvasion.getPropertyValueInt("IMZombiePigman-T2-nightSpawn-health", 30));
        mobHealthNightspawn.put("IMZombiePigman-T3-nightSpawn-health", configInvasion.getPropertyValueInt("IMZombiePigman-T3-nightSpawn-health", 65));
    }

    // Load Entities
    protected void loadEntities() {

        // Register Entities
        EntityRegistry.registerModEntity(EntityIMZombie.class, "IMZombie", 5, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMSkeleton.class, "IMSkeleton", 6, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMSpider.class, "IMSpider", 7, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMPigEngy.class, "IMPigEngy", 8, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMWolf.class, "IMWolf", 9, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMEgg.class, "IMEgg", 10, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMCreeper.class, "IMCreeper", 11, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMImp.class, "IMImp", 12, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMZombiePigman.class, "IMZombiePigman", 13, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMThrower.class, "IMThrower", 14, this, 128, 1, true);
        EntityRegistry.registerModEntity(EntityIMBoulder.class, "IMBoulder", 1, this, 36, 4, true);
        EntityRegistry.registerModEntity(EntityIMBolt.class, "IMBolt", 2, this, 36, 5, false);
        EntityRegistry.registerModEntity(EntityIMTrap.class, "IMTrap", 3, this, 36, 5, false);
        EntityRegistry.registerModEntity(EntityIMPrimedTNT.class, "IMPrimedTNT", 4, this, 36, 4, true);

        if (debugMode) {

            EntityRegistry.registerModEntity(EntityIMBird.class, "IMBird", 15, this, 128, 1, true);
            EntityRegistry.registerModEntity(EntityIMGiantBird.class, "IMGiantBird", 16, this, 128, 1, true);
        }

        // spawneggs' needed things and dispenser behavior
        GameRegistry.registerItem(itemSpawnEgg, itemSpawnEgg.getUnlocalizedName());
        BlockDispenser.dispenseBehaviorRegistry.putObject(itemSpawnEgg, new DispenserBehaviorSpawnEgg());

        // Add spawneggs
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 1, "IMZombie", "Zombie T1",
                CustomTags.IMZombie_T1(), 0x6B753F, 0x281B0A));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 2, "IMZombie", "Zombie T2",
                CustomTags.IMZombie_T2(), 0x497533, 0x7C7C7C));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 3, "IMZombie", "Tar Zombie T2",
                CustomTags.IMZombie_T2_tar(), 0x3A4225, 0x191C13));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 4, "IMZombie", "Zombie Brute T3",
                CustomTags.IMZombie_T3(), 0x586146, 0x1E4639));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 5, "IMSkeleton", "Skeleton T1",
                new NBTTagCompound(), 0x9B9B9B, 0x797979));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 6, "IMSpider", "Spider T1",
                new NBTTagCompound(), 0x504A3E, 0xA4121C));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 7, "IMSpider", "Spider T1 Baby",
                CustomTags.IMSpider_T1_baby(), 0x504A3E, 0xA4121C));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 8, "IMSpider", "Spider T2 Jumper",
                CustomTags.IMSpider_T2(), 0x444167, 0x0A0328));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 9, "IMSpider", "Spider T2 Mother",
                CustomTags.IMSpider_T2_mother(), 0x444167, 0x0A0328));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 10, "IMCreeper", "Creeper T1",
                new NBTTagCompound(), 0x238F1F, 0xA5AAA6));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 11, "IMPigEngy", "Pigman Engineer T1",
                new NBTTagCompound(), 0xEC9695, 0x420000));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 12, "IMThrower", "Thrower T1",
                new NBTTagCompound(), 0x545F37, 0x1D2D3E));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 13, "IMThrower", "Thrower T2",
                CustomTags.IMThrower_T2(), 0x5303814, 0x632808));
        SpawnEggRegistry.registerSpawnEgg(
                new SpawnEggInfo((short) 14, "IMImp", "Imp T1", new NBTTagCompound(), 0xB40113, 0xFF0000));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 15, "IMZombiePigman",
                "Zombie Pigman T1", CustomTags.IMZombiePigman_T1(), 0xEB8E91, 0x49652F));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 16, "IMZombiePigman",
                "Zombie Pigman T2", CustomTags.IMZombiePigman_T2(), 0xEB8E91, 0x49652F));
        SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 17, "IMZombiePigman",
                "Zombie Pigman T3", CustomTags.IMZombiePigman_T3(), 0xEB8E91, 0x49652F));

        if (debugMode) {
            SpawnEggRegistry.registerSpawnEgg(new SpawnEggInfo((short) 18, "IMGiantBird", "Vulture T1",
                    new NBTTagCompound(), 0x2B2B2B, 0xEA7EDC));
        }
    }

    // Register Recipes
    protected void addRecipes() {
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
    }

    protected void nightSpawnConfig() {
        nightSpawnsEnabled = configInvasion.getPropertyValueBoolean("night-spawns-enabled", false);
        nightMobSightRange = configInvasion.getPropertyValueInt("night-mob-sight-range", 20);
        nightMobSenseRange = configInvasion.getPropertyValueInt("night-mob-sense-range", 12);
        nightMobSpawnChance = configInvasion.getPropertyValueInt("night-mob-spawn-chance", 30);
        nightMobMaxGroupSize = configInvasion.getPropertyValueInt("night-mob-max-group-size", 3);
        maxNightMobs = configInvasion.getPropertyValueInt("mob-limit-override", 70);
        nightMobsBurnInDay = configInvasion.getPropertyValueBoolean("night-mobs-burn-in-day", true);

        String[] pool1Patterns = new String[DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS.length];
        float[] pool1Weights = new float[DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS.length];
        RandomSelectionPool mobPool = new RandomSelectionPool();
        nightSpawnPool1 = mobPool;
        if (DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS.length == DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS.length) {
            for (int i = 0; i < DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS.length; i++) {
                pool1Patterns[i] = configInvasion.getPropertyValueString("nm-spawnpool1-slot" + (1 + i),
                        DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS[i]);
                pool1Weights[i] = configInvasion.getPropertyValueFloat("nm-spawnpool1-slot" + (1 + i) + "-weight",
                        DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS[i]);

                if (IMWaveBuilder.isPatternNameValid(pool1Patterns[i])) {
                    log("Added entry for pattern 1 slot " + (i + 1));
                    mobPool.addEntry(IMWaveBuilder.getPattern(pool1Patterns[i]), pool1Weights[i]);
                } else {
                    log("Pattern 1 slot " + (i + 1) + " in config not recognized. Proceeding as blank.");
                    configInvasion.setProperty("nm-spawnpool1-slot" + (1 + i), "none");
                }
            }
        } else {
            log("Mob pattern table element mismatch. Ensure each slot has a probability weight");
        }
    }

    // broken after update, need to check tick stuff
//	public static boolean onClientTick()
//	{
//		if (runFlag)
//		{
//			if ((soundsEnabled) && (!soundHandler.soundsInstalled()))
//			{
//				proxy.printGuiMessage("Invasion Mod Warning: Failed to auto-install sounds. You can disable this process in config or give a bug report");
//			}
//			runFlag = false;
//		}
//
//		return true;
//	}

    // broken after update, need to fix tick stuff
//	@Override
//	public static boolean onServerTick()
//	{
//		if (serverRunFlag)
//		{
//			timer = System.currentTimeMillis();
//			serverRunFlag = false;
//		}
//
//		serverElapsed -= timer;
//		timer = System.currentTimeMillis();
//		serverElapsed += timer;
//		if (serverElapsed >= 100L)
//		{
//			serverElapsed -= 100L;
//
//			if (loginFlag)
//			{
//				killTimer += 1;
//			}
//
//			if (killTimer > 35)
//			{
//				killTimer = 0;
//				loginFlag = false;
//				for (Map.Entry entry : deathList.entrySet())
//				{
//					if (System.currentTimeMillis() - ((Long) entry.getValue()).longValue() > 300000L)
//					{
//						deathList.remove(entry.getKey());
//					}
//					else
//					{
//						for (World world : DimensionManager.getWorlds())
//						{
//							EntityPlayer player = world.getPlayerEntityByName((String) entry.getKey());
//							if (player != null)
//							{
//								player.attackEntityFrom(DamageSource.magic, 500.0F);
//								player.setDead();
//								deathList.remove(player.getDisplayName());
//								broadcastToAll("Nexus energies caught up to " + player.getDisplayName());
//							}
//						}
//					}
//				}
//			}
//		}
//
//		return true;
//	}

    public static void addToDeathList(String username, long timeStamp) {
        deathList.put(username, Long.valueOf(timeStamp));
    }

    @Override
    public String toString() {
        return "mod_Invasion";
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (logOut != null)
                logOut.close();
        } catch (Exception e) {
            logOut = null;
            log("Error closing invasion log file");
        } finally {
            super.finalize();
        }
    }

    public static boolean isInvasionActive() {
        return isInvasionActive;
    }

    public static boolean tryGetInvasionPermission(TileEntityNexus nexus) {
        if (nexus == activeNexus) {
            return true;
        }
        if (nexus == null) {
            String s = "Nexus entity invalid";
            log(s);
        } else {
            activeNexus = nexus;
            isInvasionActive = true;
            return true;
        }
        return false;
    }

    public static void setInvasionEnded(TileEntityNexus nexus) {
        if (activeNexus == nexus) {
            isInvasionActive = false;
        }
    }

    public static void setNexusUnloaded(TileEntityNexus nexus) {
        if (activeNexus == nexus) {
            nexus = null;
            isInvasionActive = false;
        }
    }

    public static void setNexusClicked(TileEntityNexus nexus) {
        focusNexus = nexus;
    }

    public static TileEntityNexus getActiveNexus() {
        return activeNexus;
    }

    public static TileEntityNexus getFocusNexus() {
        return focusNexus;
    }

    public static Entity[] getNightMobSpawns1(World world) {
        ISelect mobPool = getMobSpawnPool();
        int numberOfMobs = world.getRandom().nextInt(nightMobMaxGroupSize) + 1;
        Entity[] entities = new Entity[numberOfMobs];
        for (int i = 0; i < numberOfMobs; i++) {
            EntityIMLiving mob = getMobBuilder().createMobFromConstruct(((IEntityIMPattern) mobPool.selectNext()).generateEntityConstruct(), world, null);
            mob.setEntityIndependent();
            // also set in entityLiving constructor, is needed for AI to function properly,
            // I believe
            mob.setAggroRange(getNightMobSightRange());
            mob.setSenseRange(getNightMobSenseRange());
            mob.setBurnsInDay(getNightMobsBurnInDay());
            entities[i] = mob;
        }
        return entities;
    }

    public static MobBuilder getMobBuilder() {
        return defaultMobBuilder;
    }

    public static ISelect<IEntityIMPattern> getMobSpawnPool() {
        return nightSpawnPool1;
    }

    public static int getMinContinuousModeDays() {
        return minContinuousModeDays;
    }

    public static int getMaxContinuousModeDays() {
        return maxContinuousModeDays;
    }

    public static int getNightMobSightRange() {
        return nightMobSightRange;
    }

    public static int getNightMobSenseRange() {
        return nightMobSenseRange;
    }

    public static boolean getNightMobsBurnInDay() {
        return nightMobsBurnInDay;
    }

    public static ItemStack getRenderHammerItem() {
        return new ItemStack(itemEngyHammer, 1);
    }

    public static int getGuiIdNexus() {
        return guiIdNexus;
    }

    public static mod_Invasion getLoadedInstance() {
        return instance;
    }

    @Deprecated(since = "Unused")
    public static void broadcastToAll(Formatting color, String message) {
        MinecraftServer server = null;
        server.getPlayerManager().sendToAll(new GameMessageS2CPacket(Text.translatable(message).formatted(color), false));
    }

    public static void sendMessageToPlayers(@Nullable HashMap<String, Long> hashMap, Formatting color, String message, Object... formatArgs) {
        if (hashMap != null) {
            // TODO: Refactor
            MinecraftServer server = null;
            for (Map.Entry<String, Long> entry : hashMap.entrySet()) {
                sendMessageToPlayer(server.getPlayerManager().getPlayer(entry.getKey()), color, message, formatArgs);
            }
        }
    }

    @Deprecated(since = "Unused")
    public static void sendMessageToPlayer(ServerPlayerEntity player, Formatting color, String message, Object... formatArgs) {
        if (player != null) {
            player.sendMessage(Text.translatable(message, formatArgs).formatted(color));
        }
    }

    public static void log(@Nullable String s) {
        if (enableLog && s != null) {
            InvasionMod.LOGGER.info(s);
        }
    }

    public static boolean isDebug() {
        return debugMode;
    }

    public static int getMobHealth(EntityIMLiving mob) {
        int health = 0;
        if (mob.isNexusBound()) {
            if (mobHealthInvasion.get(mob.toString() + "-invasionSpawn-health") != null) {
                health = mobHealthInvasion.get(mob.toString() + "-invasionSpawn-health");
            } else {
                return 20;
            }
        } else {
            if (mobHealthNightspawn.get(mob.toString() + "-nightSpawn-health") != null) {
                health = mobHealthNightspawn.get(mob.toString() + "-nightSpawn-health");
            } else {
                return 20;
            }
        }
        return health;
    }

    public static boolean getUpdateNotifications() {
        return updateNotifications;
    }

    public static String getRecentNews() {
        return recentNews;
    }

    public static boolean getDestructedBlocksDrop() {
        return destructedBlocksDrop;
    }
}