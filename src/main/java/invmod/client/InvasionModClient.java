package invmod.client;

import invmod.client.render.InvRenderers;
import invmod.client.render.animation.AnimationLoader;
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
