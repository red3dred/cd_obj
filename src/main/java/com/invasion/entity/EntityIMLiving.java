package com.invasion.entity;

import com.invasion.entity.ai.ClimbableMoveControl;
import com.invasion.entity.pathfinding.IMMobNavigation;
import com.invasion.entity.pathfinding.IMNavigation;
import com.invasion.entity.pathfinding.Navigation;
import com.invasion.entity.pathfinding.PathCreator;
import com.invasion.nexus.IHasNexus;
import com.invasion.particle.InvParticles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

@Deprecated
public abstract class EntityIMLiving extends HostileEntity implements NexusEntity, Stunnable {
    private final IHasNexus.Handle nexus = new IHasNexus.Handle(this::getWorld);

    private int stunTimer;

    protected int flammability = 2;

    public EntityIMLiving(EntityType<? extends EntityIMLiving> type, World world) {
        super(type, world);
        moveControl = new ClimbableMoveControl(this);
        resetHealth();
    }

    @Override
    public PathAwareEntity asEntity() {
        return this;
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new IMMobNavigation(this, createIMNavigation().getActor());
    }

    @Deprecated
    protected Navigation createIMNavigation() {
        return new IMNavigation(this, new PathCreator(700, 50));
    }

    @Override
    public Handle getNexusHandle() {
        return nexus;
    }

    @Override
    public void tickMovement() {
        if (isStunned()) {
            if (!getWorld().isClient && age % 10 == 0) {
                ((ServerWorld)getWorld()).spawnParticles(InvParticles.DAZE, getX(), getEyeY(), getZ(), 1, 0, 0, 0, 0);
            }
            stunTimer--;
        }
        super.tickMovement();
        if (getBurnsInDay() && isAffectedByDaylight()) {
            sunlightDamageTick();
        }
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