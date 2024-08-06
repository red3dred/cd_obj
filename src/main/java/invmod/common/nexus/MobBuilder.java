package invmod.common.nexus;

import invmod.common.mod_Invasion;
import invmod.common.entity.EntityIMBurrower;
import invmod.common.entity.EntityIMCreeper;
import invmod.common.entity.EntityIMImp;
import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.EntityIMPigEngy;
import invmod.common.entity.EntityIMSkeleton;
import invmod.common.entity.EntityIMSpider;
import invmod.common.entity.EntityIMThrower;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.EntityIMZombiePigman;
import net.minecraft.world.World;

public class MobBuilder {
    public EntityIMLiving createMobFromConstruct(EntityConstruct mobConstruct, World world, INexusAccess nexus) {
        switch (mobConstruct.entityType()) {
            case ZOMBIE:
                EntityIMZombie zombie = new EntityIMZombie(world, nexus);
                zombie.setTexture(mobConstruct.texture());
                zombie.setFlavour(mobConstruct.flavour());
                zombie.setTier(mobConstruct.tier());
                return zombie;
            case ZOMBIEPIGMAN:
                EntityIMZombiePigman zombiePigman = new EntityIMZombiePigman(world, nexus);
                zombiePigman.setTexture(mobConstruct.texture());
                zombiePigman.setTier(mobConstruct.tier());
                return zombiePigman;
            case SPIDER:
                EntityIMSpider spider = new EntityIMSpider(world, nexus);
                spider.setTexture(mobConstruct.texture());
                spider.setFlavour(mobConstruct.flavour());
                spider.setTier(mobConstruct.tier());
                return spider;
            case SKELETON:
                return new EntityIMSkeleton(world, nexus);
            case PIG_ENGINEER:
                return new EntityIMPigEngy(world, nexus);
            case THROWER:
                EntityIMThrower thrower = new EntityIMThrower(world, nexus);
                thrower.setTexture(mobConstruct.tier());
                thrower.setTier(mobConstruct.tier());
                return thrower;
            case BURROWER:
                return new EntityIMBurrower(world, nexus);
            case CREEPER:
                return new EntityIMCreeper(world, nexus);
            case IMP:
                return new EntityIMImp(world, nexus);
            default:
                mod_Invasion.log("Missing mob type in MobBuilder: " + mobConstruct.entityType());
                return null;
        }
    }
}