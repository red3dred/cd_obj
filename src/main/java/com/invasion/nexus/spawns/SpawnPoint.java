package com.invasion.nexus.spawns;

import com.invasion.InvasionMod;
import com.invasion.util.math.IPolarAngle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public record SpawnPoint(BlockPos pos, int angle, SpawnType type) implements IPolarAngle, Comparable<IPolarAngle> {
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

    public boolean trySpawnEntity(World world, MobEntity entity) {
        return isValidFor(world, entity) && world.spawnEntity(entity);
    }

    public boolean columnEquals(SpawnPoint position) {
        return pos().getX() == position.pos().getX() && pos.getZ() == position.pos().getZ();
    }

    @Override
    public int compareTo(IPolarAngle polarAngle) {
        if (angle < polarAngle.getAngle()) {
            return -1;
        }
        if (angle > polarAngle.getAngle()) {
            return 1;
        }

        return 0;
    }
}