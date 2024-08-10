package invmod.client.render;

import invmod.common.entity.EntityIMBird;
import invmod.common.entity.EntityIMGiantBird;
import invmod.common.entity.InvEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;

public interface InvRenderers {
    @SuppressWarnings("unchecked")
    static void bootstrap() {
        EntityRendererRegistry.register(InvEntities.ZOMBIE, RenderIMZombie::new);
        EntityRendererRegistry.register(InvEntities.ZOMBIE_PIGMAN, RenderIMZombiePigman::new);
        EntityRendererRegistry.register(InvEntities.SKELETON, RenderIMSkeleton::new);
        EntityRendererRegistry.register(InvEntities.SPIDER, RenderSpiderIM::new);
        EntityRendererRegistry.register(InvEntities.PIGMAN_ENGINEER, RenderPigEngy::new);
        EntityRendererRegistry.register(InvEntities.IMP, RenderImp::new);
        EntityRendererRegistry.register(InvEntities.THROWER, RenderThrower::new);
        EntityRendererRegistry.register(InvEntities.BOULDER, RenderBoulder::new);
        EntityRendererRegistry.register(InvEntities.WOLF, RenderIMWolf::new);
        EntityRendererRegistry.register(InvEntities.BOULDER, RenderBoulder::new);
        EntityRendererRegistry.register(InvEntities.TRAP, RenderTrap::new);
        EntityRendererRegistry.register(InvEntities.BOLT, RenderBolt::new);
        EntityRendererRegistry.register(InvEntities.SFX, EmptyEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.SPAWN_PROXY, EmptyEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.SPIDER_EGG, RenderEgg::new);

        EntityRendererRegistry.register(InvEntities.CREEPER, RenderIMCreeper::new);
        Registries.ENTITY_TYPE.getOrEmpty(InvEntities.BIRD).ifPresent(type -> {
            EntityRendererRegistry.register((EntityType<EntityIMBird>)type, RenderB::new);
        });
        Registries.ENTITY_TYPE.getOrEmpty(InvEntities.GIANT_BIRD).ifPresent(type -> {
            EntityRendererRegistry.register((EntityType<EntityIMGiantBird>)type, RenderGiantBird::new);
        });
    }
}
