package invmod.common.entity;

import com.google.common.base.Predicates;

import invmod.common.IBlockAccessExtended;
import invmod.common.INotifyTask;
import invmod.common.nexus.INexusAccess;
import invmod.common.util.IPosition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public abstract class AbstractIMZombieEntity extends EntityIMMob implements ICanDig {
    private static final TrackedData<Integer> TIER = DataTracker.registerData(AbstractIMZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> FLAVOUR = DataTracker.registerData(AbstractIMZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TEXTURE = DataTracker.registerData(AbstractIMZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> SWINGING = DataTracker.registerData(AbstractIMZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    protected final TerrainModifier terrainModifier;
    protected final TerrainDigger terrainDigger;

    protected int swingTimer;

    protected AbstractIMZombieEntity(EntityType<? extends AbstractIMZombieEntity> type, World world, INexusAccess nexus, float diggingSpeed) {
        super(type, world, nexus);
        floatsInWater = true;
        terrainModifier = new TerrainModifier(this, diggingSpeed);
        terrainDigger = new TerrainDigger(this, terrainModifier, 1);
        setAttributes(getTier(), getFlavour());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TIER, 1);
        builder.add(FLAVOUR, 0);
        builder.add(TEXTURE, 0);
    }

    private void reInitGoals() {
        goalSelector.clear(Predicates.alwaysTrue());
        targetSelector.clear(Predicates.alwaysTrue());
        initGoals();
    }

    protected ITerrainDig getTerrainDig() {
        return this.terrainDigger;
    }

    public void setTexture(int textureId) {
        dataTracker.set(TEXTURE, textureId);
    }

    public int getTextureId() {
        return dataTracker.get(TEXTURE);
    }

    @Override
    public final int getTier() {
        return dataTracker.get(TIER);
    }

    public final void setTier(int tier) {
        tier = Math.max(1, tier);
        dataTracker.set(TIER, tier);
        setAttributes(tier, getFlavour());
        updateTexture();
        reInitGoals();

    }

    public final int getFlavour() {
        return dataTracker.get(FLAVOUR);
    }

    public final void setFlavour(int flavour) {
        dataTracker.set(FLAVOUR, flavour);
        setAttributes(getTier(), flavour);
        updateTexture();
        reInitGoals();
    }

    protected boolean isSwinging() {
        return dataTracker.get(SWINGING);
    }

    protected void setSwinging(boolean swinging) {
        dataTracker.set(SWINGING, swinging);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        updateAnimation(false);
        updateSound();
    }

    protected void updateSound() {
        if (terrainModifier.isBusy() && --throttled2 <= 0) {
            playSound("invmod:scrape" + (getRandom().nextInt(3) + 1), 0.85F, 1 / (getRandom().nextFloat() * 0.5F + 1));
            throttled2 = 45 + getRandom().nextInt(20);
        }
    }

    public abstract void updateAnimation(boolean override);

    @Override
    public void mobTick() {
        super.mobTick();
        this.terrainModifier.onUpdate();
    }

    @Override
    public void onPathSet() {
        terrainModifier.cancelTask();
    }

    @Override
    public boolean isPushable() {
        return super.getTier() != 3;
    }

    @Override
    protected int getNextAirUnderwater(int air) {
        if (getTier() == 2 && getFlavour() == 2) {
            return this.getNextAirOnLand(air);
        }
        return super.getNextAirUnderwater(air);
    }

    @Override
    public boolean tryAttack(Entity entity) {
        return getTier() == 3 && isSprinting() ? chargeAttack(entity) : super.tryAttack(entity);
    }

    protected boolean chargeAttack(Entity entity) {
        int knockback = 4;
        entity.damage(getDamageSources().mobAttack(this), (float)getAttackStrength() + 3);
        float yaw = getYaw() * MathHelper.RADIANS_PER_DEGREE;
        if (entity instanceof LivingEntity l) {
            l.takeKnockback(knockback, MathHelper.sin(yaw), MathHelper.cos(yaw));
        }
        setSprinting(false);
        playSound(SoundEvents.ENTITY_GENERIC_BIG_FALL, 1, 1);
        return true;
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
        if (getTier() != 3) {
            super.takeKnockback(strength, x, z);
        }
    }

    protected abstract void updateTexture();

    public abstract boolean isBigRenderTempHack();

    protected abstract void setAttributes(int tier, int flavour);

    public float scaleAmount() {
        if (getTier() == 2)
            return 1.12F;
        if (getTier() == 3) {
            return 1.21F;
        }
        return 1.0F;
    }

    @Override
    public BlockView getTerrain() {
        return getWorld();
    }

    @Override
    public float getBlockRemovalCost(BlockPos pos) {
        return getBlockStrength(pos) * 20;
    }

    @Override
    public boolean canClearBlock(BlockPos pos) {
        BlockState block = getWorld().getBlockState(pos);
        return block.isAir() || (isBlockDestructible(getWorld(), pos, block));
    }

    @Override
    protected boolean onPathBlocked(Path path, INotifyTask notifee) {
        if (!path.isFinished() && (hasNexus() || getAttacking() != null)) {
            if (path.getFinalPathPoint().distanceTo(path.getIntendedTarget()) > 2.2D && (path.getCurrentPathIndex() + 2) >= (path.getCurrentPathLength() / 2)) {
                return false;
            }
            PathNode node = path.getPathPointFromIndex(path.getCurrentPathIndex());

            if (terrainDigger.askClearPosition(node.pos, notifee, 1)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float getBlockPathCost(PathNode prevNode, PathNode node, BlockView terrainMap) {
        if (getTier() == 2 && getFlavour() == 2 && node.action == PathAction.SWIM) {
            float multiplier = 1;
            if ((terrainMap instanceof IBlockAccessExtended i)) {
                multiplier += (i.getData(node.pos) & IBlockAccessExtended.MOB_DENSITY_FLAG) * 3;
            }

            if (node.getYCoord() > prevNode.getYCoord() && getCollide(terrainMap, node.pos) == 2) {
                multiplier += 2;
            }

            return prevNode.distanceTo(node) * 1.2F * multiplier;
        }

        return super.getBlockPathCost(prevNode, node, terrainMap);
    }

    @Override
    public boolean isBlockDestructible(BlockView terrainMap, BlockPos pos, BlockState block) {
        return getDestructiveness() != 0
                && getCurrentTargetPos().getInclinationTo(pos) <= 2.144D
                && super.isBlockDestructible(terrainMap, pos, block);
    }

    @Override
    public void onBlockRemoved(BlockPos pos, BlockState state) {
    }

    @Override
    public void onFollowingEntity(Entity entity) {
        if (entity == null) {
            setDestructiveness(1);
        } else if (((entity instanceof EntityIMPigEngy)) || ((entity instanceof EntityIMCreeper))) {
            setDestructiveness(0);
        } else {
            setDestructiveness(1);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putInt("tier", getTier());
        compound.putInt("flavour", getFlavour());
        compound.putInt("textureId", getTextureId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        setTexture(compound.getInt("textureId"));
        setFlavour(compound.getInt("flavour"));
        setTier(compound.getInt("tier"));
    }
}
