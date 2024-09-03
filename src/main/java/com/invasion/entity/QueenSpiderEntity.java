package com.invasion.entity;

import java.util.ArrayList;
import java.util.List;

import com.invasion.InvTags;
import com.invasion.entity.ai.goal.LayEggGoal;
import com.invasion.entity.ai.goal.PredicatedGoal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public class QueenSpiderEntity extends NexusSpiderEntity implements Reproducer {
	public QueenSpiderEntity(EntityType<QueenSpiderEntity> type, World world) {
		super(type, world);
	}

    public static DefaultAttributeContainer.Builder createAttributes() {
        return SpiderEntity.createSpiderAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.59F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5)
                .add(EntityAttributes.GENERIC_GRAVITY, 0.18);
    }

    @Override
    protected float getGlobalScaleMultiplier() {
        return super.getGlobalScaleMultiplier() + 1F;
    }

    @Override
    protected void initExtraGoals() {
        goalSelector.add(1, new PredicatedGoal(new LayEggGoal(this, 1, () -> getOffspring(null)), () -> !isBaby()));
    }

    @Override
    public List<Entity> getOffspring(Entity partner) {
        List<Entity> offspring = new ArrayList<>();
        int offspringCount = 3 + getWorld().getRandom().nextInt(4);
        getWorld().getRegistryManager()
            .get(RegistryKeys.ENTITY_TYPE)
            .getEntryList(InvTags.Entities.QUEEN_SPIDER_OFFSPRING).ifPresent(named -> {
            for (int i = 0; i < offspringCount; i++) {
                named.getRandom(getWorld().getRandom()).ifPresent(type -> {
                    Entity child = type.value().create(getWorld());
                    if (child instanceof NexusEntity n) {
                        n.setNexus(getNexus());
                    }
                    if (child instanceof MobEntity l) {
                        l.setBaby(true);
                    }
                    offspring.add(child);
                });
            }
        });
        return offspring;
    }
}