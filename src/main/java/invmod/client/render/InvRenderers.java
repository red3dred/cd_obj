package invmod.client.render;

import invmod.common.entity.EntityIMBird;
import invmod.common.entity.EntityIMBolt;
import invmod.common.entity.EntityIMBoulder;
import invmod.common.entity.EntityIMBurrower;
import invmod.common.entity.EntityIMCreeper;
import invmod.common.entity.EntityIMEgg;
import invmod.common.entity.EntityIMGiantBird;
import invmod.common.entity.EntityIMImp;
import invmod.common.entity.EntityIMPigEngy;
import invmod.common.entity.EntityIMSkeleton;
import invmod.common.entity.EntityIMSpawnProxy;
import invmod.common.entity.EntityIMSpider;
import invmod.common.entity.EntityIMThrower;
import invmod.common.entity.EntityIMTrap;
import invmod.common.entity.EntityIMWolf;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.EntityIMZombiePigman;
import invmod.common.entity.EntitySFX;
import invmod.common.entity.InvEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;

public interface InvRenderers {
    @SuppressWarnings("unchecked")
    static void bootstrap() {
        EntityRendererRegistry.register(InvEntities.ZOMBIE, RenderIMZombie::new);
        EntityRendererRegistry.register(InvEntities.ZOMBIE_PIGMAN, RenderIMZombiePigman::new);
        EntityRendererRegistry.register(InvEntities.SKELETON, RenderIMSkeleton::new);
        registerEntityRenderingHandler(EntityIMSpider.class, new RenderSpiderIM());
        EntityRendererRegistry.register(InvEntities.PIGMAN_ENGINEER, RenderPigEngy::new);
        EntityRendererRegistry.register(InvEntities.IMP, RenderImp::new);
        registerEntityRenderingHandler(EntityIMThrower.class, new RenderThrower(new ModelThrower(), 1.5F));
        EntityRendererRegistry.register(InvEntities.BOULDER, RenderBoulder::new);
        EntityRendererRegistry.register(InvEntities.WOLF, RenderIMWolf::new);
        EntityRendererRegistry.register(InvEntities.BOULDER, RenderBoulder::new);
        registerEntityRenderingHandler(EntityIMTrap.class, new RenderTrap(new ModelTrap()));
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
