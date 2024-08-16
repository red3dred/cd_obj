package com.invasion.client.render;

import com.invasion.client.render.entity.RenderB;
import com.invasion.client.render.entity.RenderBolt;
import com.invasion.client.render.entity.RenderBoulder;
import com.invasion.client.render.entity.RenderEgg;
import com.invasion.client.render.entity.RenderGiantBird;
import com.invasion.client.render.entity.RenderIMCreeper;
import com.invasion.client.render.entity.RenderIMSkeleton;
import com.invasion.client.render.entity.RenderIMWolf;
import com.invasion.client.render.entity.RenderIMZombie;
import com.invasion.client.render.entity.RenderIMZombiePigman;
import com.invasion.client.render.entity.RenderImp;
import com.invasion.client.render.entity.RenderPigEngy;
import com.invasion.client.render.entity.RenderSpiderIM;
import com.invasion.client.render.entity.RenderThrower;
import com.invasion.client.render.entity.RenderTrap;
import com.invasion.entity.InvEntities;
import com.invasion.item.InvItems;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.util.Identifier;

public interface InvRenderers {
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
        EntityRendererRegistry.register(InvEntities.TRAP, RenderTrap::new);
        EntityRendererRegistry.register(InvEntities.BOLT, RenderBolt::new);
        EntityRendererRegistry.register(InvEntities.SFX, EmptyEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.SPAWN_PROXY, EmptyEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.SPIDER_EGG, RenderEgg::new);
        EntityRendererRegistry.register(InvEntities.CREEPER, RenderIMCreeper::new);
        EntityRendererRegistry.register(InvEntities.BIRD, RenderB::new);
        EntityRendererRegistry.register(InvEntities.VULTURE, RenderGiantBird::new);

        ModelPredicateProviderRegistry.register(InvItems.SEARING_BOW, Identifier.ofVanilla("pull"), (stack, world, entity, seed) -> {
            return entity == null || entity.getActiveItem() != stack ? 0.0F : (stack.getMaxUseTime(entity) - entity.getItemUseTimeLeft()) / 20F;
        });
    }
}
