package invmod.common.entity;

import invmod.common.IBlockAccessExtended;
import invmod.common.INotifyTask;
import invmod.common.mod_Invasion;
import invmod.common.entity.ai.EntityAIAttackNexus;
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

import java.util.List;

import com.google.common.base.Predicates;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EntityIMZombie extends AbstractIMZombieEntity {

    public EntityIMZombie(EntityType<EntityIMZombie> type, World world) {
        this(type, world, null);

    }

    public EntityIMZombie(EntityType<EntityIMZombie> type, World world, INexusAccess nexus) {
        super(type, world, nexus, 2F);
    }

    @Override
    protected void initGoals() {
        // added EntityAISwimming and increased all other tasks order numbers with 1
        goalSelector.add(0, new EntityAISwimming(this));
        goalSelector.add(1, new EntityAIKillEntity(this, PlayerEntity.class, 40));
        goalSelector.add(1, new EntityAIKillEntity(this, ServerPlayerEntity.class, 40));
        goalSelector.add(1, new EntityAIKillEntity(this, EntityGolem.class, 30));
        goalSelector.add(2, new EntityAIAttackNexus(this));
        goalSelector.add(3, new EntityAIWaitForEngy(this, 4.0F, true));
        goalSelector.add(4, new EntityAIKillEntity(this, EntityLiving.class, 40));
        goalSelector.add(5, new EntityAIGoToNexus(this));
        goalSelector.add(6, new EntityAIWanderIM(this));
        goalSelector.add(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        goalSelector.add(8, new EntityAIWatchClosest(this, EntityIMCreeper.class, 12.0F));
        goalSelector.add(8, new EntityAILookIdle(this));


        targetSelector.add(0, new EntityAITargetRetaliate(this, EntityLiving.class, mod_Invasion.getNightMobSightRange()));
        targetSelector.add(2, new EntityAISimpleTarget(this, EntityPlayer.class, mod_Invasion.getNightMobSightRange(), true));
        targetSelector.add(5, new EntityAIHurtByTarget(this, false));

        if (this.tier == 3) {
            goalSelector.add(4, new EntityAIStoop(this));
            goalSelector.add(3, new EntityAISprint(this));
        } else {
            // track players from sensing them
            targetSelector.add(1, new EntityAISimpleTarget(this, EntityPlayer.class, mod_Invasion.getNightMobSenseRange(), false));
            targetSelector.add(3, new EntityAITargetOnNoNexusPath(this, EntityIMPigEngy.class, 3.5F));
        }
    }

    @Override
    public void onSpawned(INexusAccess nexus, EntityConstruct spawnConditions) {
        super.onSpawned(nexus, spawnConditions);
        setTexture(spawnConditions.texture());
        setFlavour(spawnConditions.flavour());
        setTier(spawnConditions.tier());
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient && flammability >= 20 && isOnFire()) {
            doFireball();
        }
    }

    @Override
    protected void updateTexture() {
        if (tier == 1) {
            int r = random.nextInt(2);
            if (r == 0)
                setTexture(0);
            else if (r == 1)
                setTexture(1);
        } else if (tier == 2) {
            if (this.flavour == 2) {
                setTexture(5);
            } else if (this.flavour == 3) {
                setTexture(3);
            } else {
                int r = random.nextInt(2);
                if (r == 0)
                    setTexture(2);
                else if (r == 1)
                    setTexture(4);
            }
        } else if (tier == 3) {
            setTexture(6);
        }
    }

    @Override
    public ItemStack getHeldItem() {
        return this.defaultHeldItem;
    }

    @Override
    public boolean isBigRenderTempHack() {
        return this.tier == 3;
    }


    @Override
    public boolean canBePushed() {
        return this.tier != 3;
    }

    @Override
    public String getSpecies() {
        return "Zombie";
    }

    @Override
    public int getTier() {
        return this.tier < 3 ? 2 : 3;
    }

    @Override
    protected void sunlightDamageTick() {
        if (tier == 2 && flavour == 2) {
            damageEntity(DamageSource.generic, 3.0F);
        } else {
            super.sunlightDamageTick();
        }
    }

    @Override
    public void updateAnimation(boolean override) {
        if ((!getWorld().isRemote) && ((this.terrainModifier.isBusy()) || override)) {
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
    }

    @Override
    protected void updateSound() {
        if (this.terrainModifier.isBusy()) {
            if (--this.throttled2 <= 0) {
                getWorld().playSoundAtEntity(this, "invmod:scrape" + (rand.nextInt(3) + 1), 0.85F,
                        1.0F / (this.rand.nextFloat() * 0.5F + 1.0F));
                this.throttled2 = (45 + this.rand.nextInt(20));
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

        return "mob.zombie.say";
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_DEATH;
    }

    @Override
    protected Item getDropItem() {
        return Items.rotten_flesh;
    }

    @Override
    protected void dropFewItems(boolean flag, int bonus) {
        super.dropFewItems(flag, bonus);
        if (this.rand.nextFloat() < 0.35F) {
            dropItem(Items.rotten_flesh, 1);
        }

        if ((this.itemDrop != null) && (this.rand.nextFloat() < this.dropChance)) {
            entityDropItem(new ItemStack(this.itemDrop, 1, 0), 0.0F);
        }
    }

    private void doFireball() {
        int x = MathHelper.floor_double(this.posX);
        int y = MathHelper.floor_double(this.posY);
        int z = MathHelper.floor_double(this.posZ);
        int ii;
        for (ii = -1; ii < 2; ii++) {
            for (int j = -1; j < 2; j++) {
                for (int k = -1; k < 2; k++) {
                    if ((getWorld().getBlock(x + ii, y + j, z + k) == Blocks.air)
                            || (getWorld().getBlock(x + ii, y + j, z + k).getMaterial().getCanBurn())) {
                        getWorld().setBlock(x + ii, y + j, z + k, Blocks.fire);
                    }
                }
            }
        }

        List entities = getWorld().getOtherEntities(this, getBoundingBox().expand(1.5D, 1.5D, 1.5D));
        for (int el = entities.size() - 1; el >= 0; el--) {
            Entity entity = (Entity) entities.get(el);
            entity.setFire(8);
        }
        attackEntityFrom(DamageSource.inFire, 500.0F);
    }

    @Override
    protected void setAttributes(int tier, int flavour) {
        if (tier == 1) {
            this.tier = 1;
            if (flavour == 0) {
                setName("Zombie");
                setGender(1);
                setBaseMoveSpeedStat(0.19F);
                this.setAttackStrength(4);
                this.selfDamage = 3;
                this.maxSelfDamage = 6;
                this.maxDestructiveness = 2;
                this.flammability = 3;
                setDestructiveness(2);
                setMaxHealthAndHealth(mod_Invasion.getMobHealth(this));
            } else if (flavour == 1) {
                setName("Zombie");
                setGender(1);
                setBaseMoveSpeedStat(0.19F);
                this.setAttackStrength(6);
                this.selfDamage = 3;
                this.maxSelfDamage = 6;
                this.maxDestructiveness = 0;
                this.flammability = 3;
                this.defaultHeldItem = Items.WOODEN_SWORD.getDefaultStack();
                this.itemDrop = Items.WOODEN_SWORD;
                this.dropChance = 0.2F;
                setDestructiveness(0);
                setMaxHealthAndHealth(mod_Invasion.getMobHealth(this));
            }
        } else if (tier == 2) {
            this.tier = 2;
            if (flavour == 0) {
                setName("Zombie");
                setGender(1);
                setBaseMoveSpeedStat(0.19F);
                this.setAttackStrength(7);
                this.selfDamage = 4;
                this.maxSelfDamage = 12;
                this.maxDestructiveness = 2;
                this.flammability = 4;
                this.itemDrop = Items.IRON_CHESTPLATE;
                this.dropChance = 0.25F;
                setDestructiveness(2);
                setMaxHealthAndHealth(mod_Invasion.getMobHealth(this));
            } else if (flavour == 1) {
                setName("Zombie");
                setGender(1);
                setBaseMoveSpeedStat(0.19F);
                this.attackStrength = 10;
                this.selfDamage = 3;
                this.maxSelfDamage = 9;
                this.maxDestructiveness = 0;
                this.itemDrop = Items.IRON_SWORD;
                this.dropChance = 0.25F;
                this.defaultHeldItem = Items.IRON_SWORD.getDefaultStack();
                setDestructiveness(0);
                setMaxHealthAndHealth(mod_Invasion.getMobHealth(this));
            } else if (flavour == 2) {
                setName("Tar Zombie");
                setGender(1);
                setBaseMoveSpeedStat(0.19F);
                this.attackStrength = 5;
                this.selfDamage = 3;
                this.maxSelfDamage = 9;
                this.maxDestructiveness = 2;
                this.flammability = 30;
                this.floatsInWater = false;
                setDestructiveness(2);
                setMaxHealthAndHealth(mod_Invasion.getMobHealth(this));
            } else if (flavour == 3) {
                setName("Zombie Pigman");
                setGender(1);
                setBaseMoveSpeedStat(0.25F);
                this.attackStrength = 8;
                this.maxDestructiveness = 2;
                this.isImmuneToFire = true;
                this.defaultHeldItem = Items.GOLDEN_SWORD.getDefaultStack();
                setDestructiveness(2);
                setMaxHealthAndHealth(mod_Invasion.getMobHealth(this));
            }
        } else if (tier == 3) {
            this.tier = 3;
            if (flavour == 0) {
                setName("Zombie Brute");
                setGender(1);
                setBaseMoveSpeedStat(0.17F);
                this.attackStrength = 18;
                this.selfDamage = 4;
                this.maxSelfDamage = 20;
                this.maxDestructiveness = 2;
                this.flammability = 4;
                this.dropChance = 0.0F;
                setDestructiveness(2);
                setMaxHealthAndHealth(mod_Invasion.getMobHealth(this));
            }
        }
    }
}
