package invmod.client.render;

import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelB extends ModelBase
{
  private ModelPart batHead;
  private ModelPart batBody;
  private ModelPart batRightWing;
  private ModelPart batLeftWing;
  private ModelPart batOuterRightWing;
  private ModelPart batOuterLeftWing;

  public ModelB()
  {
    this.textureWidth = 64;
    this.textureHeight = 64;
    this.batHead = new ModelPart(this, 0, 0);
    this.batHead.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6);
    ModelPart modelrenderer = new ModelPart(this, 24, 0);
    modelrenderer.addBox(-4.0F, -6.0F, -2.0F, 3, 4, 1);
    this.batHead.addChild(modelrenderer);
    ModelPart modelrenderer1 = new ModelPart(this, 24, 0);
    modelrenderer1.mirror = true;
    modelrenderer1.addBox(1.0F, -6.0F, -2.0F, 3, 4, 1);
    this.batHead.addChild(modelrenderer1);
    this.batBody = new ModelPart(this, 0, 16);
    this.batBody.addBox(-3.0F, 4.0F, -3.0F, 6, 12, 6);
    this.batBody.setTextureOffset(0, 34).addBox(-5.0F, 16.0F, 0.0F, 10, 6, 1);
    this.batRightWing = new ModelPart(this, 42, 0);
    this.batRightWing.addBox(-12.0F, 1.0F, 1.5F, 10, 16, 1);
    this.batOuterRightWing = new ModelPart(this, 24, 16);
    this.batOuterRightWing.setRotationPoint(-12.0F, 1.0F, 1.5F);
    this.batOuterRightWing.addBox(-8.0F, 1.0F, 0.0F, 8, 12, 1);
    this.batLeftWing = new ModelPart(this, 42, 0);
    this.batLeftWing.mirror = true;
    this.batLeftWing.addBox(2.0F, 1.0F, 1.5F, 10, 16, 1);
    this.batOuterLeftWing = new ModelPart(this, 24, 16);
    this.batOuterLeftWing.mirror = true;
    this.batOuterLeftWing.setRotationPoint(12.0F, 1.0F, 1.5F);
    this.batOuterLeftWing.addBox(0.0F, 1.0F, 0.0F, 8, 12, 1);
    this.batBody.addChild(this.batRightWing);
    this.batBody.addChild(this.batLeftWing);
    this.batRightWing.addChild(this.batOuterRightWing);
    this.batLeftWing.addChild(this.batOuterLeftWing);
  }

  public int getBatSize()
  {
    return 36;
  }

  public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
  {
    this.batHead.rotateAngleX = (par6 / 57.295776F);
    this.batHead.rotateAngleY = (par5 / 57.295776F);
    this.batHead.rotateAngleZ = 0.0F;
    this.batHead.setRotationPoint(0.0F, 0.0F, 0.0F);
    this.batRightWing.setRotationPoint(0.0F, 0.0F, 0.0F);
    this.batLeftWing.setRotationPoint(0.0F, 0.0F, 0.0F);
    this.batBody.rotateAngleX = (0.7853982F + MathHelper.cos(par4 * 0.1F) * 0.15F);
    this.batBody.rotateAngleY = 0.0F;
    this.batRightWing.rotateAngleY = (MathHelper.cos(par4 * 1.3F) * 3.141593F * 0.25F);
    this.batLeftWing.rotateAngleY = (-this.batRightWing.rotateAngleY);
    this.batOuterRightWing.rotateAngleY = (this.batRightWing.rotateAngleY * 0.5F);
    this.batOuterLeftWing.rotateAngleY = (-this.batRightWing.rotateAngleY * 0.5F);

    this.batHead.render(par7);
    this.batBody.render(par7);
  }
}