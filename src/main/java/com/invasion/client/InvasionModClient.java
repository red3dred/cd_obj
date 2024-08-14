package com.invasion.client;

import com.invasion.client.render.InvRenderers;
import com.invasion.client.render.animation.AnimationLoader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class InvasionModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        InvRenderers.bootstrap();

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(AnimationLoader.INSTANCE);
    }

}
