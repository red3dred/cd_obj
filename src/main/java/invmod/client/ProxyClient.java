package invmod.client;

import invmod.client.render.AnimationRegistry;
import invmod.client.render.ModelIMSkeleton;
import invmod.client.render.ModelImp;
import invmod.client.render.ModelThrower;
import invmod.client.render.ModelTrap;
import invmod.client.render.RenderB;
import invmod.client.render.RenderBolt;
import invmod.client.render.RenderBoulder;
import invmod.client.render.RenderBurrower;
import invmod.client.render.RenderEgg;
import invmod.client.render.RenderGiantBird;
import invmod.client.render.RenderIMCreeper;
import invmod.client.render.RenderIMSkeleton;
import invmod.client.render.RenderIMWolf;
import invmod.client.render.RenderIMZombie;
import invmod.client.render.RenderIMZombiePigman;
import invmod.client.render.RenderImp;
import invmod.client.render.RenderInvis;
import invmod.client.render.RenderPigEngy;
import invmod.client.render.RenderSpiderIM;
import invmod.client.render.RenderThrower;
import invmod.client.render.RenderTrap;
import invmod.client.render.animation.Animation;
import invmod.client.render.animation.AnimationAction;
import invmod.client.render.animation.AnimationPhaseInfo;
import invmod.client.render.animation.BonesBirdLegs;
import invmod.client.render.animation.BonesMouth;
import invmod.client.render.animation.BonesWings;
import invmod.client.render.animation.InterpType;
import invmod.client.render.animation.KeyFrame;
import invmod.client.render.animation.Transition;
import invmod.common.ProxyCommon;
import invmod.common.entity.EntityIMBird;
import invmod.common.entity.EntityIMBolt;
import invmod.common.entity.EntityIMBoulder;
import invmod.common.entity.EntityIMBurrower;
import invmod.common.entity.EntityIMCreeper;
import invmod.common.entity.EntityIMEgg;
import invmod.common.entity.EntityIMGiantBird;
import invmod.common.entity.EntityIMImp;
import invmod.common.entity.EntityIMPigEngy;
import invmod.common.entity.EntityIMSkeleton;
import invmod.common.entity.EntityIMSpawnProxy;
import invmod.common.entity.EntityIMSpider;
import invmod.common.entity.EntityIMThrower;
import invmod.common.entity.EntityIMTrap;
import invmod.common.entity.EntityIMWolf;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.EntityIMZombiePigman;
import invmod.common.entity.EntitySFX;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class ProxyClient extends ProxyCommon {



	public <T extends Entity> void registerEntityRenderingHandler(Class<T> entityClass, EntityRenderer<T> renderer) {
	}

	public void printGuiMessage(Text message) {
	    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
	}

	@Override
    public void loadAnimations() {

	}

	@Override
    public File getFile(String fileName) {
		return new File(FMLClientHandler.instance().getClient().mcDataDir.getPath() + fileName);
	}
}