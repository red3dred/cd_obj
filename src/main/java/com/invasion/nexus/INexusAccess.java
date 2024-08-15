package com.invasion.nexus;

import java.util.List;
import java.util.UUID;

import com.invasion.entity.ai.AttackerAI;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface INexusAccess {
    long BIND_EXPIRE_TIME = 300000L;
    long TICKS_PER_DAY = World.field_30969;//24000
    long SUNSET_TIME = 12000L;
    long HALF_DAY_TIME = 14000L;
    long NIGHT_TIME = 16000L;

    int WAVE_DURATION = 240;

    UUID getUuid();

    boolean isDiscarded();

    BlockPos getOrigin();

    void registerMobDied();

    boolean isActivating();

    Mode getMode();

    int getLevel();

    int getSpawnRadius();

    int getCurrentWave();

    World getWorld();

    AttackerAI getAttackerAI();

    Participants getParticipants();

    boolean isActive();

    void damage(int amount);

    List<Text> getStatus();
}