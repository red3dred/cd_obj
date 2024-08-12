package invmod.common.nexus;

import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.ai.AttackerAI;
import invmod.common.util.IPosition;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public interface INexusAccess extends IPosition {

    boolean forceStart(int wave);

    void stop(boolean killEnemies);

    void attackNexus(int paramInt);

    void registerMobDied();

    boolean isActivating();

    int getMode();

    int getSpawnRadius();

    boolean setSpawnRadius(int radius);

    int getCurrentWave();

    World getWorld();

    List<EntityIMLiving> getMobList();

    AttackerAI getAttackerAI();

    boolean isActive();

    void askForRespawn(EntityIMLiving paramEntityIMLiving);

    Map<UUID, Long> getBoundPlayers();

    default void sendWarning(String translationKey, Object...params) {
        sendMessage(Formatting.RED, translationKey, params);
    }

    default void sendNotice(String translationKey, Object...params) {
        sendMessage(Formatting.DARK_GREEN, translationKey, params);
    }

    void sendMessage(Formatting color, String translationKey, Object...params);

    List<Text> getStatus();
}