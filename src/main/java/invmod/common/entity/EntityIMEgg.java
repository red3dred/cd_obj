package invmod.common.entity;

import invmod.common.InvSounds;
import invmod.common.InvasionMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class EntityIMEgg extends EntityIMLiving {
    private static final TrackedData<Boolean> HATCHED = DataTracker.registerData(EntityIMEgg.class, TrackedDataHandlerRegistry.BOOLEAN);

    private int hatchTime;
    private int ticks;

    private Entity[] contents;

    public EntityIMEgg(EntityType<EntityIMEgg> type, World world) {
        super(type, world, null);
    }

    public EntityIMEgg(Entity parent, Entity[] contents, int hatchTime) {
        super(InvEntities.SPIDER_EGG, parent.getWorld(), null);
        this.contents = contents;
        this.hatchTime = hatchTime;
        setBurnsInDay(false);
        setMovementSpeed(0.01F);
        setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
        setName("Spider Egg");
        setPosition(parent.getPos());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(HATCHED, false);
    }

    @Override
    public boolean isThreatTo(Entity entity) {
        return entity instanceof PlayerEntity;
    }

    public boolean isHatched() {
        return dataTracker.get(HATCHED);
    }

    public void setHatched(boolean hatched) {
        dataTracker.set(HATCHED, hatched);
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient) {
            ticks++;
            if (isHatched()) {
                if (ticks > hatchTime + 40)
                    discard();
            } else if (ticks > hatchTime) {
                hatch();
            }
        }
    }

    private void hatch() {
        playSound(InvSounds.ENTITY_EGG_HATCH, 1, 1);
        setHatched(true);
        if (!getWorld().isClient) {
            if (contents != null) {
                for (Entity entity : contents) {
                    entity.setPosition(getPos());
                    getWorld().spawnEntity(entity);
                }
            }
        }
    }

    @Override
    public String getLegacyName() {
        return "IMSpider-egg";
    }
}