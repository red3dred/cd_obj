package invmod.client.render;

import invmod.common.entity.EntityIMBolt;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;

public class RenderBolt extends EmptyEntityRenderer<EntityIMBolt> {
    private static final int BOLT_COLOR = ColorHelper.Argb.getArgb((int)(0.5F * 255), (int)(0.5F * 255), (int)(0.6F * 255), (int)(0.6F * 255));

    public RenderBolt(Context context) {
        super(context);
    }

    @Override
    public void render(EntityIMBolt entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

    }

	/*public void render(EntityIMBolt entityBolt, double d, double d1, double d2, float f, float f1) {
		Tessellator tessellator = Tessellator.instance;
		GL11.glPushMatrix();
		GL11.glTranslatef((float) d, (float) d1, (float) d2);
		GL11.glRotatef(entityBolt.getYaw(), 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(entityBolt.getPitch(), 0.0F, 0.0F, 1.0F);
		float scale = 0.0625F;
		GL11.glScalef(scale, scale, scale);
		renderFromVertices(entityBolt, tessellator);
		GL11.glPopMatrix();
	}*/

	public void renderFromVertices(EntityIMBolt entityBolt, VertexConsumer tessellator) {
		double[][] vertices = entityBolt.getVertices();
		if (vertices == null) {
			return;
		}
		/*
		GL11.glDisable(3553);
		GL11.glDisable(2896);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 1);*/

		double[] xCoords = vertices[0];
		double[] yCoords = vertices[1];
		double[] zCoords = vertices[2];

		double drawWidth = -0.1D;
		for (int pass = 0; pass < 4; pass++) {
			drawWidth += 0.32D;
			for (int i = 1; i < yCoords.length; i++) {
				for (int j = 0; j < 5; j++) {
					double xOffset = 0.5D - drawWidth;
					double zOffset = 0.5D - drawWidth;
					if ((j == 1) || (j == 2)) {
						xOffset += drawWidth * 2.0D;
					}
					if ((j == 2) || (j == 3)) {
						zOffset += drawWidth * 2.0D;
					}
					tessellator.vertex(
					        (float)(xCoords[(i - 1)] + xOffset),
					        (float)(yCoords[(i - 1)] * 16.0D),
					        (float)(zCoords[(i - 1)] + zOffset),
					        BOLT_COLOR,
					        0, 0, 0, 0, 0, 0, 0);
	                   tessellator.vertex(
	                           (float)(xCoords[i] + xOffset),
	                           (float)(yCoords[i] * 16.0D),
	                           (float)(zCoords[i] + zOffset),
	                           BOLT_COLOR,
	                            0, 0, 0, 0, 0, 0, 0);
				}
			}
		}
		/*
		GL11.glDisable(3042);
		GL11.glEnable(2896);
		GL11.glEnable(3553);*/
	}
}