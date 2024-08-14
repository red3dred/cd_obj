package com.invasion.entity;

import java.util.Comparator;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import com.invasion.InvasionMod;
import com.invasion.block.InvBlocks;
import com.invasion.item.InvItems;
import com.invasion.nexus.INexusAccess;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityIMWolf extends WolfEntity implements IHasNexus {
    @Nullable
    private INexusAccess nexus;
    private Optional<GlobalPos> nexusPos = Optional.empty();

    private int updateTimer;
    private boolean loadedFromNBT;

    public EntityIMWolf(EntityType<EntityIMWolf> type, World world) {
        this(type, world, null);
    }

    public EntityIMWolf(EntityType<EntityIMWolf> type, World world, @Nullable INexusAccess nexus) {
        super(type, world);
        setNexus(nexus);
        setHealth(getMaxHealth());
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        targetSelector.add(5, new ActiveTargetGoal<>(this, HostileEntity.class, true));
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (loadedFromNBT || updateTimer++ > 40) {
            loadedFromNBT = false;
            checkNexus();
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean success = super.tryAttack(target);
        if (success) {
            heal(4);
        }
        return success;
    }

    @Override
    protected void updateAttributesForTamed() {
        getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);
        super.updateAttributesForTamed();
    }

    @Override
    protected void updatePostDeath() {
        if (++deathTime >= 120) {
            getWorld().sendEntityStatus(this, EntityStatuses.ADD_DEATH_PARTICLES);
            remove(Entity.RemovalReason.KILLED);
            for (int j = 0; j < 20; j++) {
                getWorld().addParticle(ParticleTypes.EXPLOSION,
                        getParticleX(2),
                        getRandomBodyY(),
                        getParticleZ(2),
                        getRandom().nextGaussian() * 0.02D,
                        getRandom().nextGaussian() * 0.02D,
                        getRandom().nextGaussian() * 0.02D
                );
            }
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!respawnAtNexus()) {
            super.onDeath(source);
        }
    }

    public boolean respawnAtNexus() {
        checkNexus();
        if ((!getWorld().isClient) && hasNexus() && getNexus().getMode() != 0) {
            EntityIMWolf wolf = InvEntities.WOLF.create(getWorld());
            BlockPos center = getNexus().getOrigin();
            Optional<Vec3d> respawnPoint = BlockPos.streamOutwards(center, 5, 3, 5).map(BlockPos::toBottomCenterPos)
                    .filter(pos -> {
                        wolf.setPosition(pos);
                        return wolf.canSpawn(getWorld(), SpawnReason.MOB_SUMMONED);
                    }).sorted(Comparator.comparingDouble(pos -> center.getSquaredDistance(pos.x, pos.y, pos.z)))
                    .findAny();

            if (respawnPoint.isPresent()) {
                wolf.copyFrom(this);
                wolf.setNexus(getNexus());
                wolf.setPosition(respawnPoint.get());
                wolf.setRotation(0, 0);
                wolf.heal(60.0F);
                getWorld().spawnEntity(wolf);
                return true;
            }
        }
        InvasionMod.LOGGER.warn("No respawn spot for wolf");
        return false;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(InvItems.STRANGE_BONE) && isOwner(player)) {
            INexusAccess newNexus = IHasNexus.findNexus(getWorld(), getBlockPos());
            if (newNexus != null && newNexus != getNexus()) {
                setNexus(newNexus);
                stack.decrementUnlessCreative(1, player);
                setHealth(25);
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        nexusPos.flatMap(pos -> GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).result()).ifPresent(pos -> {
            compound.put("nexusPos", pos);
        });
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        if (compound.contains("nexusPos")) {
            nexusPos = GlobalPos.CODEC.decode(NbtOps.INSTANCE, compound.get("nexusPos")).result().map(Pair::getFirst);
        } else {
            nexusPos = Optional.empty();
        }
        loadedFromNBT = true;
    }

    private void checkNexus() {
        if (!(getWorld() instanceof ServerWorld sw)) {
            return;
        }
        setNexus(nexusPos.map(nexusPos -> {
            @Nullable
            World world = sw.getServer().getWorld(nexusPos.dimension());
            if (world != null
                    && world.getBlockState(nexusPos.pos()).isOf(InvBlocks.NEXUS_CORE)
                    && world.getBlockEntity(nexusPos.pos()) instanceof INexusAccess nexus) {
                return nexus;
            }
            return null;
        }).orElse(null));
    }

    @Override
    public @Nullable INexusAccess getNexus() {
        return nexus;
    }

    @Override
    public void setNexus(@Nullable INexusAccess nexus) {
        this.nexus = nexus;
        nexusPos = Optional.ofNullable(nexus).map(n -> GlobalPos.create(n.getWorld().getRegistryKey(), n.getOrigin()));
    }

    @Override
    public boolean isAlwaysIndependant() {
        return false;
    }

    @Override
    public void setEntityIndependent() {
    }

    @Override
    public double findDistanceToNexus() {
        if (!hasNexus()) {
            return Double.MAX_VALUE;
        }
        return Math.sqrt(getNexus().getOrigin().toCenterPos().squaredDistanceTo(getX(), getBodyY(0.5), getZ()));
    }
}