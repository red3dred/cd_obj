package invmod.client.render;

import invmod.common.InvasionMod;
import invmod.common.entity.EntityIMTrap;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class RenderTrap extends EntityRenderer<EntityIMTrap> {
	private static final Identifier TEXTURE = InvasionMod.id("textures/trap.png");

	private final ModelTrap model;

	public RenderTrap(EntityRendererFactory.Context ctx) {
	    super(ctx);
	    shadowRadius = 0;
	    model = new ModelTrap(ModelTrap.getTexturedModelData().createModel());
	}

	@Override
    public void render(EntityIMTrap entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
	    matrices.push();
	    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
	    matrices.scale(1.3F, 1.3F, 1.3F);
	    model.render(matrices, vertices.getBuffer(model.getLayer(getTexture(entity))), light, 0);
	    matrices.pop();
	}

	@Override
    public Identifier getTexture(EntityIMTrap entity) {
		return TEXTURE;
	}
}