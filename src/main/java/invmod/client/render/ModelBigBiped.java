package invmod.client.render;

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

public class ModelBigBiped<T extends LivingEntity> extends BipedEntityModel<T> {
  public ModelBigBiped(ModelPart root) {
    super(root);
  }

  public static TexturedModelData getTexturedModelData(Dilation dilation, float pivotOffsetY) {
      return TexturedModelData.of(getModelData(dilation, pivotOffsetY), 64, 32);
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

  /*
  public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity)
  {
    this.head.yaw = (par4 / 57.295776F);
    this.head.pitch = (par5 / 57.295776F);
    this.headwear.yaw = this.head.yaw;
    this.headwear.pitch = this.head.pitch;
    this.rightArm.pitch = (MathHelper.cos(par1 * 0.6662F + 3.141593F) * 2 * par2 * 0.5F);
    this.leftArm.pitch = (MathHelper.cos(par1 * 0.6662F) * 2 * par2 * 0.5F);
    this.rightArm.roll = 0;
    this.leftArm.roll = 0;
    this.rightLeg.pitch = (MathHelper.cos(par1 * 0.6662F) * 1.4F * par2);
    this.leftLeg.pitch = (MathHelper.cos(par1 * 0.6662F + 3.141593F) * 1.4F * par2);
    this.rightLeg.yaw = 0;
    this.leftLeg.yaw = 0;

    if (this.isRiding)
    {
      this.rightArm.pitch += -0.6283186F;
      this.leftArm.pitch += -0.6283186F;
      this.rightLeg.pitch = -1.256637F;
      this.leftLeg.pitch = -1.256637F;
      this.rightLeg.yaw = 0.3141593F;
      this.leftLeg.yaw = -0.3141593F;
    }

    if (this.heldItemLeft != 0)
    {
      this.leftArm.pitch = (this.leftArm.pitch * 0.5F - 0.3141593F * this.heldItemLeft);
    }

    if (this.heldItemRight != 0)
    {
      this.rightArm.pitch = (this.rightArm.pitch * 0.5F - 0.3141593F * this.heldItemRight);
    }

    this.rightArm.yaw = 0;
    this.leftArm.yaw = 0;

    if (this.onGround > -9990)
    {
      float f = this.onGround;
      this.body.yaw = (MathHelper.sin(MathHelper.sqrt(f) * 3.141593F * 2) * 0.2F);
      this.rightArm.pivotZ = (MathHelper.sin(this.body.yaw) * 5);
      this.rightArm.pivotX = (-MathHelper.cos(this.body.yaw) * 5);
      this.leftArm.pivotZ = (-MathHelper.sin(this.body.yaw) * 5);
      this.leftArm.pivotX = (MathHelper.cos(this.body.yaw) * 5);
      this.rightArm.yaw += this.body.yaw;
      this.leftArm.yaw += this.body.yaw;
      this.leftArm.pitch += this.body.yaw;
      f = 1 - this.onGround;
      f *= f;
      f *= f;
      f = 1 - f;
      float f2 = MathHelper.sin(f * 3.141593F);
      float f4 = MathHelper.sin(this.onGround * 3.141593F) * -(this.head.pitch - 0.7F) * 0.75F;
      rightArm.pitch = ((float)(rightArm.pitch - (f2 * 1.2D + f4)));
      this.rightArm.yaw += this.body.yaw * 2;
      this.rightArm.roll = (MathHelper.sin(this.onGround * 3.141593F) * -0.4F);
    }

// TODO: The pivots are different from a regular BipedEntityModel
    if (this.isSneaking)
    {
      this.body.pitch = 0.7F;
      this.body.pivotY = 1.5F;
      this.rightLeg.pitch -= 0;
      this.leftLeg.pitch -= 0;
      this.rightArm.pitch += 0.4F;
      this.leftArm.pitch += 0.4F;
      this.rightLeg.pivotZ = 7;
      this.leftLeg.pivotZ = 7;
      this.rightLeg.pivotY = 12;
      this.leftLeg.pivotY = 12;
      this.rightArm.pivotY = 3.5F;
      this.leftArm.pivotY = 3.5F;
      this.head.pivotY = 3;
    }
    else
    {
      this.body.pitch = 0;
      this.body.pivotY = 0;
      this.rightLeg.pivotZ = 0;
      this.leftLeg.pivotZ = 0;
      this.rightLeg.pivotY = 12;
      this.leftLeg.pivotY = 12;
      this.rightArm.pivotY = 2;
      this.leftArm.pivotY = 2;
      this.head.pivotY = 0;
      this.rightArm.pivotX = -6;
      this.leftArm.pivotX = 6;
    }

    this.rightArm.roll += MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
    this.leftArm.roll -= MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
    this.rightArm.pitch += MathHelper.sin(par3 * 0.067F) * 0.05F;
    this.leftArm.pitch -= MathHelper.sin(par3 * 0.067F) * 0.05F;

    if (this.aimedBow)
    {
      float f1 = 0;
      float f3 = 0;
      this.rightArm.roll = 0;
      this.leftArm.roll = 0;
      this.rightArm.yaw = (-(0.1F - f1 * 0.6F) + this.head.yaw);
      this.leftArm.yaw = (0.1F - f1 * 0.6F + this.head.yaw + 0.4F);
      this.rightArm.pitch = (-1.570796F + this.head.pitch);
      this.leftArm.pitch = (-1.570796F + this.head.pitch);
      this.rightArm.pitch -= f1 * 1.2F - f3 * 0.4F;
      this.leftArm.pitch -= f1 * 1.2F - f3 * 0.4F;
      this.rightArm.roll += MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
      this.leftArm.roll -= MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
      this.rightArm.pitch += MathHelper.sin(par3 * 0.067F) * 0.05F;
      this.leftArm.pitch -= MathHelper.sin(par3 * 0.067F) * 0.05F;
    }
  }*/

}