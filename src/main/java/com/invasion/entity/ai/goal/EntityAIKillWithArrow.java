package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMLiving;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;

/**
 * AI for an entity to shoot arrows.
 * Is this even being used?
 */
@Deprecated
public class EntityAIKillWithArrow<T extends LivingEntity> extends KillEntityGoal<T>
{
	private float attackRangeSq;

	public EntityAIKillWithArrow(EntityIMLiving entity, Class<? extends T> targetClass, int attackDelay, float attackRange) {
		super(entity, targetClass, attackDelay);
		this.attackRangeSq = (attackRange * attackRange);
	}

	@Override
    public void tick() {
		super.tick();
		LivingEntity target = getTarget();
		if (mob.squaredDistanceTo(target) < 36 && mob.canSee(target)) {
		    mob.getNavigatorNew().haltForTick();
		}
	}

	@Override
    protected void attackEntity(Entity target) {
		setAttackTime(getAttackDelay());
		if (target instanceof LivingEntity l && mob instanceof RangedAttackMob attacker) {
		    attacker.shootAt(l, 1);
		} else {
    		ArrowEntity projectile = new ArrowEntity(mob.getWorld(), mob, Items.ARROW.getDefaultStack(), null);
            double dX = target.getX() - mob.getX();
            double dY = target.getBodyY(0.3333333333333333) - projectile.getY();
            double dZ = target.getZ() - mob.getZ();
            double horLength = Math.sqrt(dX * dX + dZ * dZ);
            projectile.setVelocity(dX, dY + horLength * 0.2F, dZ, 1.6F, 14 - mob.getWorld().getDifficulty().getId() * 4);
            mob.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1, 1 / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
            mob.getWorld().spawnEntity(projectile);
		}
	}

	@Override
    protected boolean canAttackEntity(Entity target) {
		return getAttackTime() <= 0
		        && mob.squaredDistanceTo(target) < attackRangeSq
		        && mob.canSee(target);
	}
}