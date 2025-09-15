package com.invasion.nexus.test;

import java.util.List;
import java.util.UUID;

import com.invasion.nexus.Combatant;
import com.invasion.nexus.ControllableNexusAccess;
import com.invasion.nexus.Mode;
import com.invasion.nexus.Participants;
import com.invasion.nexus.ai.AttackerAI;

import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.damage.DamageSource;
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
    public void notifyCombatantRemoved(Combatant<?> combatant, RemovalReason reason) {
    }

    @Override
    public void damage(DamageSource source, int amount) {
    }

    @Override
    public boolean isActivating() {
        return false;
    }

    @Override
    public Mode getMode() {
        return Mode.STOPPED;
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

    @Override
    public boolean isDiscarded() {
        return false;
    }
}