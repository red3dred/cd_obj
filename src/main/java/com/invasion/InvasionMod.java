package com.invasion;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.invasion.block.InvBlocks;
import com.invasion.entity.InvEntities;
import com.invasion.item.InvItems;
import com.invasion.nexus.test.Tester;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class InvasionMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Tester.class);

    private static final ConfigInvasion CONFIG = new ConfigInvasion();

    public static void log(@Nullable String s) {
        if (InvasionMod.getConfig().enableLog && s != null) {
            InvasionMod.LOGGER.warn(s);
        }
    }

    public static ConfigInvasion getConfig() {
        return CONFIG;
    }

    public static Identifier id(String name) {
        return Identifier.of("invmod", name);
    }

    @Override
    public void onInitialize() {
        CONFIG.loadConfig(FabricLoader.getInstance().getConfigDir().resolve("invasion_config.cfg").toFile());
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> {
            dispatcher.register(InvasionCommand.create(dispatcher, registries));
        });
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            BountyHunter.of(world).tick();
        });
        InvBlocks.bootstrap();
        InvItems.bootstrap();
        InvSounds.boostrap();
        InvEntities.bootstrap();
    }
}
