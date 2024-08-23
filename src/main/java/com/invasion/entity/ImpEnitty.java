package com.invasion.entity;

import com.invasion.entity.ai.goal.AttackNexusGoal;
import com.invasion.entity.ai.goal.GoToNexusGoal;
import com.invasion.entity.ai.goal.KillEntityGoal;
import com.invasion.entity.ai.goal.NoNexusPathGoal;
import com.invasion.entity.ai.goal.target.CustomRangeActiveTargetGoal;
import com.invasion.entity.ai.goal.target.RetaliateGoal;
import com.invasion.entity.ai.goal.ProvideSupportGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class ImpEnitty extends IMMobEntity {
    public ImpEnitty(EntityType<ImpEnitty> type, World world) {
        super(type, world);
        getNavigatorNew().getActor().setCanClimb(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new KillEntityGoal<>(this, PlayerEntity.class, 40));
        goalSelector.add(2, new AttackNexusGoal<>(this));
        goalSelector.add(3, new ProvideSupportGoal(this, 4, true));
        goalSelector.add(4, new KillEntityGoal<>(this, MobEntity.class, 40));
        goalSelector.add(5, new GoToNexusGoal(this));
        goalSelector.add(6, new WanderAroundFarGoal(this, 1));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        goalSelector.add(8, new LookAtEntityGoal(this, IMCreeperEntity.class, 12));
        goalSelector.add(8, new LookAroundGoal(this));

        targetSelector.add(0, new RetaliateGoal(this));
        targetSelector.add(1, new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getSenseRange, false));
        targetSelector.add(2, new CustomRangeActiveTargetGoal<>(this, PlayerEntity.class, this::getAggroRange, true));
        targetSelector.add(5, new RevengeGoal(this));
        targetSelector.add(3, new NoNexusPathGoal(this, new CustomRangeActiveTargetGoal<>(this, PigmanEngineerEntity.class, 3.5F)));
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