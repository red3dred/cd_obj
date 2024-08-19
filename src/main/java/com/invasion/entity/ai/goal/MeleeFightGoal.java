package com.invasion.entity.ai.goal;

import com.invasion.entity.IHasAiGoals;
import com.invasion.entity.NexusEntity;
import com.invasion.entity.pathfinding.INavigation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;

public class MeleeFightGoal<T extends LivingEntity, E extends PathAwareEntity & NexusEntity> extends EntityAIMeleeAttack<T, E> {
	private int time;
	private float startingHealth;
	private int damageDealt;
	private int invulnCount;
	private float retreatHealthLossPercent;

	public MeleeFightGoal(E entity, Class<? extends T> targetClass, int attackDelay, float retreatHealthLossPercent) {
		super(entity, targetClass, attackDelay);
		this.retreatHealthLossPercent = retreatHealthLossPercent;
	}

	@Override
    public boolean canStart() {
		return mob.hasGoal(IHasAiGoals.Goal.MELEE_TARGET) && hasValidTarget();
	}

	@Override
    public boolean shouldContinue() {
		return (mob.hasGoal(IHasAiGoals.Goal.MELEE_TARGET) || isWaitingForTransition()) && hasValidTarget();
	}

	private boolean hasValidTarget() {
	    return mob.getTarget() instanceof LivingEntity target
                && target.isAlive()
                && target.getClass().isAssignableFrom(getTargetClass());
	}

	@Override
    public void start() {
		time = 0;
		startingHealth = mob.getHealth();
		damageDealt = 0;
		invulnCount = 0;
	}

	@Override
    public void tick() {
		updateDisengage();
		updatePath();
		super.tick();
		if (damageDealt > 0 || startingHealth - mob.getHealth() > 0) {
			time++;
		}
	}

	public void updatePath() {
		INavigation nav = mob.getNavigatorNew();
		if (mob.getTarget() != nav.getTargetEntity()) {
			nav.clearPath();
			nav.autoPathToEntity(mob.getTarget());
		}
	}

	protected void updateDisengage() {
		if (mob.hasGoal(IHasAiGoals.Goal.MELEE_TARGET) && shouldLeaveMelee()) {
			mob.transitionAIGoal(IHasAiGoals.Goal.LEAVE_MELEE);
		}
	}

	protected boolean isWaitingForTransition() {
		return mob.hasGoal(IHasAiGoals.Goal.LEAVE_MELEE) && mob.getPrevAIGoal() == IHasAiGoals.Goal.MELEE_TARGET;
	}

	@Override
    protected void attackEntity(LivingEntity target) {
		float h = target.getHealth();
		super.attackEntity(target);
		h -= target.getHealth();
		if (h <= 0) {
			invulnCount++;
		}
		damageDealt += (int)h;
	}

	protected boolean shouldLeaveMelee() {
		float damageReceived = startingHealth - mob.getHealth();
		return (time > 40 && damageReceived > mob.getMaxHealth() * retreatHealthLossPercent)
	        || (time > 100 && damageReceived - damageDealt > mob.getMaxHealth() * 0.66F * retreatHealthLossPercent)
	        || invulnCount >= 2;
	}
}
