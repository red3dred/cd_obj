package com.invasion;

import com.invasion.block.container.NexusScreenHandler;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public interface InvScreenHandlers {
    ScreenHandlerType<NexusScreenHandler> NEXUS = register("nexus", new ScreenHandlerType<>(NexusScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    static <T extends ScreenHandler> ScreenHandlerType<T> register(String name, ScreenHandlerType<T> type) {
        return Registry.register(Registries.SCREEN_HANDLER, InvasionMod.id(name), type);
    }

    static void bootstrap() { }
}
