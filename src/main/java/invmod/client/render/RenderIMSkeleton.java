package invmod.client.render;

import invmod.common.entity.EntityIMSkeleton;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.util.Identifier;

public class RenderIMSkeleton extends BipedEntityRenderer<EntityIMSkeleton, ModelIMSkeleton> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/skeleton/skeleton.png");

    public RenderIMSkeleton(EntityRendererFactory.Context ctx) {
        super(ctx, new ModelIMSkeleton(SkeletonEntityModel.getTexturedModelData().createModel()), 0.5F);
    }

    @Override
    public Identifier getTexture(EntityIMSkeleton entity) {
        return TEXTURE;
    }
}