package com.invasion.entity;

import com.invasion.InvasionMod;
import com.invasion.entity.ai.goal.EntityAIAttackNexus;
import com.invasion.entity.ai.goal.EntityAIGoToNexus;
import com.invasion.entity.ai.goal.EntityAIKillEntity;
import com.invasion.entity.ai.goal.EntityAISimpleTarget;
import com.invasion.entity.ai.goal.EntityAITargetOnNoNexusPath;
import com.invasion.entity.ai.goal.EntityAITargetRetaliate;
import com.invasion.entity.ai.goal.EntityAIWaitForEngy;
import com.invasion.entity.ai.goal.EntityAIWanderIM;
import com.invasion.nexus.INexusAccess;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class EntityIMImp extends EntityIMMob {
    public EntityIMImp(EntityType<EntityIMImp> type, World world, INexusAccess nexus) {
        super(type, world, nexus);
        setMovementSpeed(0.3F);
        setAttackStrength(3);
        setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
        setJumpHeight(1);
        setCanClimb(true);
    }

    public EntityIMImp(EntityType<EntityIMImp> type, World world) {
        this(type, world, null);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new EntityAIKillEntity<>(this, PlayerEntity.class, 40));
        goalSelector.add(2, new EntityAIAttackNexus(this));
        goalSelector.add(3, new EntityAIWaitForEngy(this, 4, true));
        goalSelector.add(4, new EntityAIKillEntity<>(this, MobEntity.class, 40));
        goalSelector.add(5, new EntityAIGoToNexus(this));
        goalSelector.add(6, new EntityAIWanderIM(this));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        goalSelector.add(8, new LookAtEntityGoal(this, EntityIMCreeper.class, 12));
        goalSelector.add(8, new LookAroundGoal(this));

        targetSelector.add(0, new EntityAITargetRetaliate<>(this, MobEntity.class, getAggroRange()));
        targetSelector.add(1, new EntityAISimpleTarget<>(this, PlayerEntity.class, getSenseRange(), false));
        targetSelector.add(2, new EntityAISimpleTarget<>(this, PlayerEntity.class, getAggroRange(), true));
        targetSelector.add(5, new RevengeGoal(this));
        targetSelector.add(3, new EntityAITargetOnNoNexusPath<>(this, EntityIMPigEngy.class, 3.5F));
    }

    @Override
    public boolean tryAttack(Entity entity) {
        if (super.tryAttack(entity)) {
            entity.setFireTicks(3);
            return true;
        }
        return false;
    }

}