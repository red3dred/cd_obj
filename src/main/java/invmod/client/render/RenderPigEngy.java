package invmod.client.render;

import invmod.common.InvasionMod;
import invmod.common.entity.EntityIMPigEngy;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class RenderPigEngy extends BipedEntityRenderer<EntityIMPigEngy, BipedEntityModel<EntityIMPigEngy>> {
    private static final Identifier TEXTURE = InvasionMod.id("textures/pigengT1.png");

    public RenderPigEngy(Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public Identifier getTexture(EntityIMPigEngy entity) {
        return TEXTURE;
    }
}