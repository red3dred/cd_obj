package invmod.client.render;

import invmod.common.entity.EntityIMThrower;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public class ModelThrower extends BipedEntityModel<EntityIMThrower> {

    public ModelThrower(ModelPart root) {
        super(root);
    }

    public static ModelData getModelData(Dilation dilation, float pivotOffsetY) {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(16, 14).cuboid(-2, -2, -2, 4, 2, 4, dilation), ModelTransform.pivot(0, 16 + pivotOffsetY, 4));
        root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(16, 14).cuboid(-2, -2, -2, 4, 2, 4, dilation.add(0.5F)), ModelTransform.pivot(0, 16 + pivotOffsetY, 4));
        root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 1).cuboid(-7, 2, -4, 12, 4, 9, dilation), ModelTransform.pivot(-0.4F, 16 + pivotOffsetY, 3));
        root.addChild(EntityModelPartNames.JACKET, ModelPartBuilder.create().uv(0, 23).cuboid(-3.666667F, 0, 0, 12, 2, 7, dilation), ModelTransform.pivot(-3, 16 + pivotOffsetY, 0));
        root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(39, 22).cuboid(-3, 0, -1.466667F, 3, 7, 3, dilation), ModelTransform.pivot(-6.566667F, 16 + pivotOffsetY, 5));
        root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(0, 0, -1, 2, 4, 2, dilation), ModelTransform.pivot(5, 16 + pivotOffsetY, 5));
        root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 14).cuboid(-2, 0, -2, 4, 2, 4, dilation), ModelTransform.pivot(-4.066667F, 22 + pivotOffsetY, 4));
        root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 14).mirrored().cuboid(-2, 0, -2, 4, 2, 4, dilation), ModelTransform.pivot(3, 32 + pivotOffsetY, 4));
        return data;
    }

    public static TexturedModelData getTexturedModelData() {
        return TexturedModelData.of(getModelData(Dilation.NONE, 0), 64, 32);
    }

    /*
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
      this.bipedHead.rotateAngleY = (f3 / 57.29578F);
      this.bipedHead.rotateAngleX = (f4 / 57.29578F);
      this.bipedRightArm.rotateAngleX = (MathHelper.cos(f * 0.6662F + 3.141593F) * 2 * f1 * 0.5F);
      this.bipedLeftArm.rotateAngleX = (MathHelper.cos(f * 0.6662F) * 2 * f1 * 0.5F);
      this.bipedRightArm.rotateAngleZ = 0;
      this.bipedLeftArm.rotateAngleZ = 0;
      this.bipedRightLeg.rotateAngleX = (MathHelper.cos(f * 0.6662F) * 1.4F * f1);
      this.bipedLeftLeg.rotateAngleX = (MathHelper.cos(f * 0.6662F + 3.141593F) * 1.4F * f1);
      this.bipedRightLeg.rotateAngleY = 0;
      this.bipedLeftLeg.rotateAngleY = 0;
      if (this.isRiding) {
        this.bipedRightArm.rotateAngleX += -0.6283185F;
        this.bipedLeftArm.rotateAngleX += -0.6283185F;
        this.bipedRightLeg.rotateAngleX = -1.256637F;
        this.bipedLeftLeg.rotateAngleX = -1.256637F;
        this.bipedRightLeg.rotateAngleY = 0.314159F;
        this.bipedLeftLeg.rotateAngleY = -0.314159F;
      }
      if (this.heldItemLeft) {
        this.bipedLeftArm.rotateAngleX = (this.bipedLeftArm.rotateAngleX * 0.5F - 0.314159F);
      }
      if (this.heldItemRight) {
        this.bipedRightArm.rotateAngleX = (this.bipedRightArm.rotateAngleX * 0.5F - 0.314159F);
      }
      this.bipedRightArm.rotateAngleY = 0;
      this.bipedLeftArm.rotateAngleY = 0;

      this.bipedRightArm.rotateAngleZ += MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
      this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
      this.bipedRightArm.rotateAngleX += MathHelper.sin(f2 * 0.067F) * 0.05F;
      this.bipedLeftArm.rotateAngleX -= MathHelper.sin(f2 * 0.067F) * 0.05F;
    }*/
}