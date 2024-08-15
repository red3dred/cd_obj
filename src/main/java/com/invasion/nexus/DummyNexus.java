package com.invasion.nexus;

import java.util.List;
import java.util.UUID;

import com.invasion.entity.ai.AttackerAI;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DummyNexus implements ControllableNexusAccess {
    private World world;

    private final UUID uuid = UUID.randomUUID();

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public void damage(int damage) {
    }

    @Override
    public void registerMobDied() {
    }

    @Override
    public boolean isActivating() {
        return false;
    }

    @Override
    public int getMode() {
        return 0;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getSpawnRadius() {
        return 45;
    }

    @Override
    public int getCurrentWave() {
        return 1;
    }

    @Override
    public BlockPos getOrigin() {
        return BlockPos.ORIGIN;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public AttackerAI getAttackerAI() {
        return null;
    }

    @Override
    public Participants getParticipants() {
        return null;
    }

    @Override
    public boolean start(int wave) {
        return true;
    }

    @Override
    public void stop(boolean killEnemies) {
    }

    @Override
    public boolean setSpawnRadius(int radius) {
        return true;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public List<Text> getStatus() {
        return List.of();
    }
}