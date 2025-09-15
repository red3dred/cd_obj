package com.invasion.entity;

import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import com.invasion.InvSounds;
import com.invasion.item.InvItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.entity.data.DataTracker.Builder;

public class TrapEntity extends Entity {
    private static final TrackedData<String> TYPE = DataTracker.registerData(TrapEntity.class, TrackedDataHandlerRegistry.STRING);

    private int timeTriggered;

    public TrapEntity(EntityType<TrapEntity> type, World world) {
        super(type, world);
    }

    public TrapEntity(EntityType<TrapEntity> type, World world, double x, double y, double z) {
        this(type, world, x, y, z, Type.EMPTY);
    }

    public TrapEntity(EntityType<TrapEntity> type, World world, double x, double y, double z, Type trapType) {
        this(type, world);
        setTrapType(trapType);
        updatePositionAndAngles(x, y, z, 0, 0);
    }

    @Override
    protected void initDataTracker(Builder builder) {
        builder.add(TYPE, Type.EMPTY.asString());
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            if (age % 20 == 0 && getTrapType() == Type.RIFT) {
                doRiftParticles();
            }
            return;
        }

        if (!isValidPlacement()) {
            dropItem(InvItems.EMPTY_TRAP);
            discard();
        }

        if (getTrapType() != Type.EMPTY) {
            if (age < 60) {
                if (age % 10 == 0) {
                    playSound(InvSounds.ENTITY_TRAP_COUNTDOWN, 0.1F, 1);
                }
                return;
            } else if (age == 60) {
                playSound(InvSounds.ENTITY_TRAP_READY, 0.1F, 1);
            } else {
                List<LivingEntity> targets = getWorld().getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(0.1), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
                if (targets.size() > 0) {
                    if (timeTriggered <= 0) {
                        timeTriggered = age + 250;
                    }

                    if (age > timeTriggered) {
                        if (targets.stream().anyMatch(this::trapEffect)) {
                            setTrapType(Type.EMPTY);
                        }
                    } else {
                        int timeRemaining = timeTriggered - age;
                        if (age % Math.max(timeRemaining / 10, 1) == 0) {
                            playSound(InvSounds.ENTITY_TRAP_READY, 0.1F, 1);
                        }
                    }
                } else {
                    if (timeTriggered > 0) {
                        if (age > timeTriggered) {
                            trapEffect(null);
                            setTrapType(Type.EMPTY);
                        } else {
                            int timeRemaining = timeTriggered - age;
                            if (age % Math.max(timeRemaining / 10, 1) == 0) {
                                playSound(InvSounds.ENTITY_TRAP_READY, 0.1F, 1);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean trapEffect(@Nullable LivingEntity triggerEntity) {
        if (getTrapType() == Type.RIFT) {
            if (triggerEntity != null) {
                triggerEntity.damage(getDamageSources().magic(), triggerEntity instanceof PlayerEntity ? 12 : 38);
            }

            for (Entity entity : getWorld().getOtherEntities(this, getBoundingBox().expand(2, 1, 2))) {
                if (entity instanceof TrapEntity) {
                    continue;
                }
                entity.damage(getDamageSources().magic(), 8);
                if (entity instanceof Stunnable l) {
                    l.stun(60);
                }
            }
            playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.5F, getRandom().nextFloat() * 0.25F + 0.55F);
        } else if (getTrapType() == Type.FIRE) {
            playSound(SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.5F, 1.15F / (getRandom().nextFloat() * 0.3F + 1));
            doFireball(1.1F, 8);
        }

        return true;
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (age > 30 && getTrapType() == Type.EMPTY) {
            ItemStack drop = InvItems.EMPTY_TRAP.getDefaultStack();
            if (!player.giveItemStack(drop)) {
                player.dropItem(drop, false);
            }
            playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1, 1);
            discard();
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ActionResult result = super.interact(player, hand);
        if (result != ActionResult.PASS || getTrapType() == Type.EMPTY) {
            return result;
        }
        ItemStack curItem = player.getStackInHand(hand);
        if (curItem.isOf(InvItems.MATERIAL_PROBE)) {
            ItemStack drop = getTrapType().getDroppedStack();
            if (!player.giveItemStack(drop)) {
                player.dropItem(drop, false);
            }
            playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1, 1);
            discard();
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public Type getTrapType() {
        return Type.of(dataTracker.get(TYPE));
    }

    public void setTrapType(Type type) {
        dataTracker.set(TYPE, type.asString());
    }

    public boolean isValidPlacement() {
        BlockPos below = getBlockPos().down();
        BlockState supportingState = getWorld().getBlockState(below);
        return supportingState.hasSolidTopSurface(getWorld(), below, this) && getWorld().doesNotIntersectEntities(this);
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean canHit() {
        return true;
    }


    private void doFireball(float size, int initialDamage) {
        int sz = MathHelper.ceil(size);
        for (BlockPos pos : BlockPos.iterateOutwards(getBlockPos(), sz, sz, sz)) {
            if (!pos.equals(getBlockPos())) {
                BlockState state = getWorld().getBlockState(pos);
                if (state.isAir() || state.isBurnable()) {
                    getWorld().setBlockState(pos, Blocks.FIRE.getDefaultState());
                }
            }
        }

        for (Entity entity : getWorld().getOtherEntities(this, getBoundingBox().expand(size))) {
            entity.setFireTicks(8);
            entity.damage(getDamageSources().onFire(), initialDamage);
        }
    }

    private void doRiftParticles() {
        for (int i = 0; i < 300; i++) {
            double x = getRandom().nextTriangular(0, 3);
            double z = getRandom().nextTriangular(0, 3);
            getWorld().addParticle(ParticleTypes.PORTAL, getX() + x, getY() + 2, getZ() + z, -x / 3F, -2, -z / 3F);
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound compound) {
        setTrapType(Type.of(compound.getString("type")));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound compound) {
        compound.putString("type", getTrapType().asString());
    }

    public enum Type implements StringIdentifiable {
        EMPTY(() -> InvItems.EMPTY_TRAP),
        RIFT(() -> InvItems.RIFT_TRAP),
        FIRE(() -> InvItems.FLAME_TRAP);

        @SuppressWarnings("deprecation")
        public static final EnumCodec<Type> CODEC = StringIdentifiable.createCodec(Type::values);

        @SuppressWarnings("deprecation")
        public static Type of(String id) {
            return CODEC.byId(id, EMPTY);
        }

        private final String name = name().toLowerCase(Locale.ROOT);
        private final ItemConvertible item;

        Type(ItemConvertible item) {
            this.item = item;
        }

        public ItemStack getDroppedStack() {
            return item.asItem().getDefaultStack();
        }

        @Override
        public String asString() {
            return name;
        }
    }

}