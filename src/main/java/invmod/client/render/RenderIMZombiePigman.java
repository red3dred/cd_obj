package invmod.client.render;

import invmod.common.InvasionMod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Stream;

class RenderIMZombiePigman extends RenderIMZombie {
    static final List<Identifier> TEXTURES = Stream.of(
            "textures/pigzombie64x32.png",
            "textures/pigzombie64x32.png",
            "textures/zombiePigmanT3.png"
    ).map(InvasionMod::id).toList();

	public RenderIMZombiePigman(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

    @Override
    protected List<Identifier> getTextures() {
        return TEXTURES;
    }
}