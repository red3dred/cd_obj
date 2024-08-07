package invmod.client.render;

import invmod.common.InvasionMod;
import invmod.common.entity.EntityIMImp;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import org.lwjgl.opengl.GL11;

public class RenderImp extends LivingEntityRenderer<EntityIMImp, ModelImp> {
	private static final Identifier TEXTURE = InvasionMod.id("textures/imp.png");

	public RenderImp(EntityRendererFactory.Context ctx) {
		super(ctx, new ModelImp(), 0.3F);
	}

	@Override
    public Identifier getTexture(EntityIMImp entity) {
		return TEXTURE;
	}
}