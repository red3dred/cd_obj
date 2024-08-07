package invmod.client;

import invmod.common.ProxyCommon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

public class ProxyClient extends ProxyCommon {



	public <T extends Entity> void registerEntityRenderingHandler(Class<T> entityClass, EntityRenderer<T> renderer) {
	}

	public void printGuiMessage(Text message) {
	    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
	}

	@Override
    public void loadAnimations() {

	}
}