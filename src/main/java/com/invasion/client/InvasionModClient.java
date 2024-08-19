package com.invasion.client;

import com.invasion.InvScreenHandlers;
import com.invasion.client.render.InvRenderers;
import com.invasion.client.render.animation.AnimationLoader;
import com.invasion.client.screen.NexusScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.resource.ResourceType;

public class InvasionModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        InvRenderers.bootstrap();

        HandledScreens.register(InvScreenHandlers.NEXUS, NexusScreen::new);

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(AnimationLoader.INSTANCE);
    }

}
