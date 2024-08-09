package invmod.common.entity;

import invmod.common.ConfigInvasion;
import invmod.common.IBlockAccessExtended;
import invmod.common.INotifyTask;
import invmod.common.InvasionMod;
import invmod.common.mod_Invasion;
import invmod.common.block.InvBlocks;
import invmod.common.entity.ai.EntityAIAttackNexus;
import invmod.common.entity.ai.EntityAICharge;
import invmod.common.entity.ai.EntityAIGoToNexus;
import invmod.common.entity.ai.EntityAIKillEntity;
import invmod.common.entity.ai.EntityAILeaderTarget;
import invmod.common.entity.ai.EntityAIRallyBehindEntity;
import invmod.common.entity.ai.EntityAISimpleTarget;
import invmod.common.entity.ai.EntityAISprint;
import invmod.common.entity.ai.EntityAIStoop;
import invmod.common.entity.ai.EntityAITargetOnNoNexusPath;
import invmod.common.entity.ai.EntityAITargetRetaliate;
import invmod.common.entity.ai.EntityAIWaitForEngy;
import invmod.common.entity.ai.EntityAIWanderIM;
import invmod.common.nexus.EntityConstruct;
import invmod.common.nexus.INexusAccess;
import invmod.common.util.IPosition;

import java.util.Calendar;
import java.util.List;

import com.google.common.base.Predicates;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EntityIMZombiePigman extends AbstractIMZombieEntity {
    private static final TrackedData<Boolean> CHARGING = DataTracker.registerData(EntityIMZombiePigman.class, TrackedDataHandlerRegistry.BOOLEAN);

    public EntityIMZombiePigman(EntityType<EntityIMZombiePigman> type, World world) {
        this(type, world, null);
    }

    public EntityIMZombiePigman(EntityType<EntityIMZombiePigman> type, World world, INexusAccess nexus) {
        super(type, world, nexus, 0.75F);
        dropChance = 0.35F;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CHARGING, false);
    }

    @Override
    protected void initGoals() {
        // added entityaiswimming and increased all other tasksordernumers with 1
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(2, new EntityAIKillEntity<>(this, PlayerEntity.class, 40));
        goalSelector.add(3, new EntityAIAttackNexus(this));
        goalSelector.add(4, new EntityAIWaitForEngy(this, 4.0F, true));
        goalSelector.add(5, new EntityAIKillEntity<>(this, LivingEntity.class, 40));
        goalSelector.add(6, new EntityAIGoToNexus(this));
        goalSelector.add(7, new EntityAIWanderIM(this));
        goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        goalSelector.add(9, new LookAtEntityGoal(this, EntityIMCreeper.class, 12.0F));
        goalSelector.add(9, new LookAroundGoal(this));

        targetSelector.add(0, new EntityAITargetRetaliate<>(this, LivingEntity.class, getAggroRange()));
        targetSelector.add(2, new EntityAISimpleTarget<>(this, PlayerEntity.class, getAggroRange(), true));
        targetSelector.add(5, new RevengeGoal(this));

        if (getTier() == 3) {
            // goalSelector.add(4, new EntityAIStoop(this));
            goalSelector.add(1, new EntityAICharge<>(this, PlayerEntity.class, 0.75F));
        } else {
            // track players from sensing them
            targetSelector.add(1, new EntityAISimpleTarget<>(this, PlayerEntity.class, getAggroRange(), false));
            targetSelector.add(3, new EntityAITargetOnNoNexusPath<>(this, EntityIMPigEngy.class, 3.5F));
        }
    }

    @Override
    public void onSpawned(INexusAccess nexus, EntityConstruct spawnConditions) {
        super.onSpawned(nexus, spawnConditions);
        setTexture(spawnConditions.texture());
        setTier(spawnConditions.tier());
    }

    public boolean isCharging() {
        return dataTracker.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        dataTracker.set(CHARGING, charging);
    }

    @Override
    protected void updateTexture() {
        if (tier == 1) {
            setTexture(0);
        } else if (tier == 2) {
            setTexture(1);
        } else if (tier == 3) {
            setTexture(2);
        }
    }

    @Override
    public boolean isBigRenderTempHack() {
        return this.tier == 3;
    }

    @Override
    public String getSpecies() {
        return "ZombiePigman";
    }

    @Override
    public void updateAnimation(boolean override) {
        // System.out.println(this.getXCoord()+" "+this.getYCoord()+"
        // "+this.getZCoord()+" charging:"+this.isCharging());
        if ((!this.worldObj.isRemote) && ((this.terrainModifier.isBusy()) || override)) {
            setSwinging(true);
        }
        int swingSpeed = getSwingSpeed();
        if (isSwinging()) {
            this.swingTimer += 1;
            if (this.swingTimer >= swingSpeed) {
                this.swingTimer = 0;
                setSwinging(false);
            }
        } else {
            this.swingTimer = 0;
        }
        this.swingProgress = (float) this.swingTimer / (float) swingSpeed;

        if (isCharging()) {
            boolean mobgriefing = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
            this.limbSwingAmount = ((float) (this.limbSwingAmount + 0.5D));
            int x = this.getXCoord();
            int y = this.getYCoord();
            int z = this.getZCoord();
            if (!worldObj.isRemote) {
                for (int i = y; i <= y + 1; i++) {
                    for (int j = x - 1; j <= x + 1; j++) {
                        for (int k = z - 1; k <= z + 1; k++) {
                            Block block = worldObj.getBlock(j, i, k);
                            int meta = worldObj.getBlockMetadata(j, i, k);

                            if (block.getMaterial() != Material.air) {
                                if (isBlockDestructible(this.worldObj, j, i, k, block)
                                        && block != InvBlocks.NEXUS_CORE) {
                                    this.playSound("random.explode", 0.2F, 0.5F);
                                    if (InvasionMod.getConfig().destructedBlocksDrop) {
                                        block.dropBlockAsItem(this.worldObj, j, i, k, meta, 0);
                                    }
                                    worldObj.setBlock(j, i, k, Blocks.air);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected int getSwingSpeed() {
        return 10;
    }

    @Override
    protected String getLivingSound() {
        if (this.tier == 3) {
            return this.rand.nextInt(3) == 0 ? "invmod:bigzombie1" : null;
        }

        return "mob.zombiepig.zpig";
    }

    @Override
    protected String getHurtSound() {
        return "mob.zombiepig.zpighurt";
    }

    @Override
    protected String getDeathSound() {
        return "mob.zombiepig.zpigdeath";
    }

    @Override
    protected Item getDropItem() {
        return Items.gold_nugget;
    }

    @Override
    protected void dropFewItems(boolean flag, int bonus) {
        super.dropFewItems(flag, bonus);
        if (this.rand.nextFloat() < 0.35F) {
            dropItem(Items.gold_nugget, 1);
        }

        if ((this.itemDrop != null) && (this.rand.nextFloat() < this.dropChance)) {
            entityDropItem(new ItemStack(this.itemDrop, 1), 0.0F);
        }
    }

    @Override
    protected void setAttributes(int tier, int flavour) {
        this.tier = tier;
        if (tier == 1) {
            setName("Zombie Pigman");
            setGender(1);
            setBaseMoveSpeedStat(0.25F);
            this.attackStrength = 8;
            this.maxDestructiveness = 2;
            this.isImmuneToFire = true;
            this.defaultHeldItem = new ItemStack(Items.golden_sword, 1);
            setDestructiveness(2);
            setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));

        } else if (tier == 2) {
            setName("Zombie Pigman");
            setGender(1);
            setBaseMoveSpeedStat(0.35F);
            this.attackStrength = 12;
            this.maxDestructiveness = 2;
            this.isImmuneToFire = true;

            setDestructiveness(2);
            setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));

            if (this.rand.nextInt(5) == 1) {
                this.setCurrentItemOrArmor(1, new ItemStack(Items.golden_helmet, 1));
            }

            if (this.rand.nextInt(5) == 1) {
                this.setCurrentItemOrArmor(2, new ItemStack(Items.golden_chestplate, 1));
            }

            if (this.rand.nextInt(5) == 1) {
                this.setCurrentItemOrArmor(3, new ItemStack(Items.golden_leggings, 1));
            }

            if (this.rand.nextInt(5) == 1) {
                this.setCurrentItemOrArmor(4, new ItemStack(Items.golden_boots, 1));
            }

        } else if (tier == 3) {

            this.tier = 3;
            setName("Zombie Pigman Brute");
            setGender(1);
            setBaseMoveSpeedStat(0.20F);
            this.attackStrength = 18;
            this.maxDestructiveness = 2;
            this.isImmuneToFire = true;
            setDestructiveness(2);
            setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
        }
    }
}
