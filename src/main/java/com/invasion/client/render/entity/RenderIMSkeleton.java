package com.invasion.client.render.entity;

import com.invasion.entity.EntityIMSkeleton;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.util.Identifier;

/**
 * Copy of SkeletonEntityRenderer with the entity class changed
 *
 * @see net.minecraft.client.render.entity.SkeletonEntityRenderer
 */
public class RenderIMSkeleton extends BipedEntityRenderer<EntityIMSkeleton, SkeletonEntityModel<EntityIMSkeleton>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/skeleton/skeleton.png");

    public RenderIMSkeleton(EntityRendererFactory.Context context) {
        this(context, EntityModelLayers.SKELETON, EntityModelLayers.SKELETON_INNER_ARMOR, EntityModelLayers.SKELETON_OUTER_ARMOR);
    }

    public RenderIMSkeleton(EntityRendererFactory.Context ctx, EntityModelLayer layer, EntityModelLayer legArmorLayer, EntityModelLayer bodyArmorLayer) {
        this(ctx, legArmorLayer, bodyArmorLayer, new SkeletonEntityModel<>(ctx.getPart(layer)));
    }

    public RenderIMSkeleton(EntityRendererFactory.Context context, EntityModelLayer lagArmorLayer, EntityModelLayer bodyArmorLayer, SkeletonEntityModel<EntityIMSkeleton> model) {
        super(context, model, 0.5F);
        addFeature(new ArmorFeatureRenderer<>(this, new SkeletonEntityModel<>(context.getPart(lagArmorLayer)), new SkeletonEntityModel<>(context.getPart(bodyArmorLayer)), context.getModelManager()));
    }

    @Override
    public Identifier getTexture(EntityIMSkeleton abstractSkeletonEntity) {
        return TEXTURE;
    }

    @Override
    protected boolean isShaking(EntityIMSkeleton abstractSkeletonEntity) {
        // TODO:
        return false;//abstractSkeletonEntity.isShaking();
    }
}