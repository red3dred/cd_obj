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
import net.minecraft.client.render.entity.model.ZombieEntityModel;

public interface InvRenderers {


    static void bootstrap() {
        registerEntityRenderingHandler(EntityIMZombie.class, new RenderIMZombie(new ZombieEntityModel(0, true), 0.5F));
        registerEntityRenderingHandler(EntityIMZombiePigman.class, new RenderIMZombiePigman(new ModelZombie(0, true), 0.5F));
        EntityRendererRegistry.register(InvEntities.SKELETON, RenderIMSkeleton::new);
        registerEntityRenderingHandler(EntityIMSpider.class, new RenderSpiderIM());
        registerEntityRenderingHandler(EntityIMPigEngy.class, new RenderPigEngy(new ModelBiped(), 0.5F));
        EntityRendererRegistry.register(InvEntities.IMP, RenderImp::new);
        registerEntityRenderingHandler(EntityIMThrower.class, new RenderThrower(new ModelThrower(), 1.5F));
        registerEntityRenderingHandler(EntityIMBurrower.class, new RenderBurrower());
        registerEntityRenderingHandler(EntityIMWolf.class, new RenderIMWolf());
        registerEntityRenderingHandler(EntityIMBoulder.class, new RenderBoulder());
        registerEntityRenderingHandler(EntityIMTrap.class, new RenderTrap(new ModelTrap()));
        registerEntityRenderingHandler(EntityIMBolt.class, new RenderBolt());
        registerEntityRenderingHandler(EntitySFX.class, new RenderInvis());
        registerEntityRenderingHandler(EntityIMSpawnProxy.class, new RenderInvis());
        registerEntityRenderingHandler(EntityIMEgg.class, new RenderEgg());

        registerEntityRenderingHandler(EntityIMCreeper.class, new RenderIMCreeper());
        registerEntityRenderingHandler(EntityIMBird.class, new RenderB());
        registerEntityRenderingHandler(EntityIMGiantBird.class, new RenderGiantBird());
    }
}
