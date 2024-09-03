package com.invasion.entity;

import java.util.List;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;

public interface MountableEntity extends NexusEntity {
    default void generateJockey(ServerWorldAccess world, int currentWave, LocalDifficulty difficulty, SpawnReason spawnReason) {
        PathAwareEntity self = asEntity();
        int jockyAttempsts = currentWave - 10;
        while (--jockyAttempsts > 0) {
            Random random = world.getRandom();
            if (random.nextInt(100) == 0) {
                HostileEntity jockey = getJockeyType(world).create(self.getWorld());
                if (jockey != null) {
                    if (jockey instanceof NexusSpiderEntity) {
                        jockey.setBaby(true);
                    }
                    jockey.refreshPositionAndAngles(self.getX(), self.getY(), self.getZ(), self.getYaw(), 0.0F);
                    jockey.initialize(world, difficulty, spawnReason, null);
                    if (jockey instanceof NexusEntity n) {
                        n.setNexus(getNexus());
                    }
                    AttributeUtil.applyNexusWaveComplications(jockey, world, currentWave / 2, difficulty, spawnReason);
                    jockey.startRiding(self);
                    break;
                }
            }
        }
    }

    default EntityType<? extends HostileEntity> getJockeyType(ServerWorldAccess world) {
        return Util.getRandom(List.of(
                InvEntities.SKELETON,
                InvEntities.ZOMBIE,
                InvEntities.ZOMBIE_PIGMAN,
                InvEntities.JUMPING_SPIDER,
                InvEntities.SPIDER
        ), world.getRandom());
    }
}
