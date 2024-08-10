package invmod.client.render;

import invmod.common.InvasionMod;
import invmod.common.entity.EntityIMThrower;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Stream;

class RenderThrower extends BipedEntityRenderer<EntityIMThrower, ModelThrower> {
	private static final List<Identifier> TEXTURES = Stream.of(
	        "textures/throwerT1.png",
	        "textures/throwerT2.png"
    ).map(InvasionMod::id).toList();

	public RenderThrower(EntityRendererFactory.Context ctx) {
		super(ctx, new ModelThrower(ModelThrower.getTexturedModelData().createModel()), 1.5F);
	}

    @Override
    protected void scale(EntityIMThrower entity, MatrixStack matrices, float amount) {
        matrices.scale(2.4F, 2.4F, 2.4F);
    }

	@Override
    public Identifier getTexture(EntityIMThrower entity) {
	    int id = entity.getTextureId() - 1;
	    return TEXTURES.get(id < 0 || id >= TEXTURES.size() ? 0 : id);
	}

}