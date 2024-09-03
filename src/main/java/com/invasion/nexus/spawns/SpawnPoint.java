package com.invasion.nexus.spawns;

import com.invasion.InvasionMod;
import com.invasion.util.math.PolarAngle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public record SpawnPoint(BlockPos pos, int angle, SpawnType type) implements PolarAngle, Comparable<PolarAngle> {
    @Override
    public int getAngle() {
        return this.angle;
    }

    public void applyTo(Entity entity) {
        entity.updatePositionAndAngles(pos().getX() + 0.5, pos().getY() + 0.5, pos().getZ() + 0.5, angle, 0);
    }

    public boolean isValidFor(WorldView world, MobEntity entity) {
        if (world.isOutOfHeightLimit(pos)) {
            InvasionMod.LOGGER.info("[Spawn] Spawn point was outside of build limit {}", pos);
            return false;
        }
        applyTo(entity);
        return entity.canSpawn(world) && world.isSpaceEmpty(entity);
    }

    public boolean trySpawnEntity(ServerWorld world, MobEntity entity) {
        if (isValidFor(world, entity)) {
            entity.initialize(world, world.getLocalDifficulty(entity.getBlockPos()), SpawnReason.STRUCTURE, null);
            world.spawnEntityAndPassengers(entity);
            return true;
        }
        return false;
    }

    public boolean columnEquals(SpawnPoint position) {
        return pos().getX() == position.pos().getX() && pos.getZ() == position.pos().getZ();
    }

    @Override
    public int compareTo(PolarAngle polarAngle) {
        if (angle < polarAngle.getAngle()) {
            return -1;
        }
        if (angle > polarAngle.getAngle()) {
            return 1;
        }

        return 0;
    }
}