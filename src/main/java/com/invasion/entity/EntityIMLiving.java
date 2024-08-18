package com.invasion.entity;

import com.invasion.entity.ai.IMMoveHelper;
import com.invasion.entity.pathfinding.INavigation;
import com.invasion.entity.pathfinding.NavigatorIM;
import com.invasion.entity.pathfinding.PathCreator;
import com.invasion.entity.pathfinding.PathNavigateAdapter;
import com.invasion.nexus.IHasNexus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public abstract class EntityIMLiving extends HostileEntity implements NexusEntity, Stunnable {
    private final IHasNexus.Handle nexus = new IHasNexus.Handle(this::getWorld);

    private int stunTimer;

    protected int flammability = 2;

    public EntityIMLiving(EntityType<? extends EntityIMLiving> type, World world) {
        super(type, world);
        moveControl = new IMMoveHelper(this);
        resetHealth();
    }

    @Override
    public PathAwareEntity asEntity() {
        return this;
    }

    @Override
    protected final EntityNavigation createNavigation(World world) {
        return new PathNavigateAdapter(this, world, createIMNavigation());
    }

    protected INavigation createIMNavigation() {
        return new NavigatorIM(this, new PathCreator(700, 50));
    }

    @Override
    public Handle getNexusHandle() {
        return nexus;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (getBurnsInDay() && isAffectedByDaylight()) {
            sunlightDamageTick();
        }
    }

    @Override
    public void tick() {
        if (!getWorld().isClient) {
            if (stunTimer > 0) {
                stunTimer--;
                return;
            }
        }

        super.tick();
    }

    @Override
    public boolean damage(DamageSource source, float damage) {
        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            damage *= flammability;
        }

        return super.damage(source, damage);
    }

    @Override
    public boolean stun(int ticks) {
        stunTimer = Math.max(stunTimer, ticks);
        setVelocity(getVelocity().multiply(0, 1, 0));
        return true;
    }

    @Override
    public boolean isStunned() {
        return stunTimer > 0;
    }

    @Override
    public boolean canSee(Entity entity) {
        float distance = distanceTo(entity);
        return distance <= getSenseRange() || (super.canSee(entity) && distance <= getAggroRange());
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return super.canSpawn(world) && (hasNexus() || getLightLevelBelow8()) && getWorld().isTopSolid(getBlockPos().down(), this);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return hasNexus() ? 0 : super.getPathfindingFavor(pos, world);
    }

    @Override
    public final boolean canImmediatelyDespawn(double distanceSquared) {
        return !hasNexus();
    }

    @Override
    public final boolean cannotDespawn() {
        return hasNexus() || super.cannotDespawn();
    }

    protected void sunlightDamageTick() {
        setOnFireFor(8);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putInt("stunTimer", stunTimer);
        nexus.writeNbt(compound);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        stunTimer = compound.getInt("stunTimer");
        nexus.readNbt(compound);
    }
}