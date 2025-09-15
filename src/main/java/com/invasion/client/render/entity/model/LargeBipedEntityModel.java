package com.invasion.client.render.entity.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.LivingEntity;

/**
 * Enlarged version of a normal biped.
 * Has a slightly bigger head and torso and wider-set limbs.
 *
 * @param <T> The entity type
 */
public class LargeBipedEntityModel<T extends LivingEntity> extends BipedEntityModel<T> {
  public LargeBipedEntityModel(ModelPart root) {
    super(root);
  }

  public static TexturedModelData getTexturedModelData(Dilation dilation, float pivotOffsetY) {
      return TexturedModelData.of(getModelData(dilation, pivotOffsetY), 64, 64);
  }

  public static ModelData getModelData(Dilation dilation, float pivotOffsetY) {
      ModelData data = new ModelData();
      ModelPartData root = data.getRoot();
      root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-3.5F, -7, -3.5F, 7, 7, 7, dilation), ModelTransform.pivot(0, 0 + pivotOffsetY, 0));
      root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-3.5F, -7, -3.5F, 7, 7, 7, dilation.add(0.5F)), ModelTransform.pivot(0, 0 + pivotOffsetY, 0));

      root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 15).cuboid(-5, 0, -3, 10, 12, 5, dilation), ModelTransform.pivot(0, 0 + pivotOffsetY, 0));

      root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(46, 15).cuboid(-3, -2, -2, 4, 12, 4, dilation), ModelTransform.pivot(-6, 2 + pivotOffsetY, 0));
      root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(46, 15).mirrored().cuboid(-1, -2, -2, 4, 12, 4, dilation), ModelTransform.pivot(6, 2 + pivotOffsetY, 0));

      root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 16).cuboid(-2, 0, -2, 4, 12, 4, dilation), ModelTransform.pivot(-2F, 12 + pivotOffsetY, 0));
      root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-2, 0, -2, 4, 12, 4, dilation), ModelTransform.pivot(2F, 12 + pivotOffsetY, 0));
      return data;
  }
}