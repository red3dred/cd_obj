package invmod.common.entity.ai;

import invmod.common.block.InvBlocks;
import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.Goal;
import invmod.common.nexus.TileEntityNexus;
import net.minecraft.util.math.BlockPos;

public class EntityAIAttackNexus extends net.minecraft.entity.ai.goal.Goal {
    private EntityIMLiving mob;
    private boolean attacked;

    private int cooldown;

    public EntityAIAttackNexus(EntityIMLiving par1EntityLiving) {
        mob = par1EntityLiving;
    }

    @Override
    public boolean canStart() {
        if (cooldown == 0 && mob.getAIGoal() == Goal.BREAK_NEXUS && mob.findDistanceToNexus() > 4) {
            cooldown = 5;
            return false;
        }

        return isNexusInRange();
    }

    @Override
    public void start() {
        cooldown = 40;
    }

    @Override
    public boolean shouldContinue() {
        return !attacked;
    }

    @Override
    public void tick() {
        if (cooldown == 0) {
            if (isNexusInRange()) {
                if (mob instanceof EntityIMZombie) {
                    ((EntityIMZombie) mob).updateAnimation(true);
                }
                mob.getNexus().attackNexus(2);
            }
            cooldown = 20;
            attacked = true;
        }
    }

    @Override
    public void stop() {
        attacked = false;
    }

    private boolean isNexusInRange() {
        return BlockPos.stream(mob.getBoundingBox().expand(1)).anyMatch(pos -> {
            return mob.getWorld().getBlockState(pos).isOf(InvBlocks.NEXUS_CORE)
                    && mob.getWorld().getBlockEntity(pos) instanceof TileEntityNexus nexus
                    && nexus == mob.getNexus();
        });
    }
}