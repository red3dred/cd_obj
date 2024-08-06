package invmod.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import invmod.common.block.InvBlocks;
import invmod.common.entity.InvEntities;
import invmod.common.item.InvItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;

public class InvasionMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    public static Identifier id(String name) {
        return Identifier.of("invmod", name);
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> {
            dispatcher.register(InvasionCommand.create(registries));
        });

        InvBlocks.bootstrap();
        InvItems.bootstrap();
        InvEntities.bootstrap();
    }
}
