package com.invasion.entity;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.invasion.nexus.INexusAccess;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public abstract class TieredIMMobEntity extends EntityIMMob {
    private static final TrackedData<Integer> TIER = DataTracker.registerData(TieredIMMobEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> FLAVOUR = DataTracker.registerData(TieredIMMobEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TEXTURE = DataTracker.registerData(TieredIMMobEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private boolean updatingAttributes;

    public TieredIMMobEntity(EntityType<? extends EntityIMMob> type, World world, @Nullable INexusAccess nexus) {
        super(type, world, nexus);
        initTieredAttributes();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TIER, 1);
        builder.add(FLAVOUR, 0);
        builder.add(TEXTURE, 0);
    }

    @Override
    public final int getTier() {
        return dataTracker.get(TIER);
    }

    public final void setTier(int tier) {
        if (tier != getTier()) {
            dataTracker.set(TIER, tier);
            onAttributesChanged();
        }
    }

    public final int getTextureId() {
        return dataTracker.get(TEXTURE);
    }

    public final void setTexture(int textureId) {
        if (textureId != getTextureId()) {
            dataTracker.set(TEXTURE, textureId);
            onAttributesChanged();
        }
    }

    public final int getFlavour() {
        return dataTracker.get(FLAVOUR);
    }

    public final void setFlavour(int flavour) {
        if (flavour != getFlavour()) {
            dataTracker.set(FLAVOUR, flavour);
            onAttributesChanged();
        }
    }

    protected void setAppearance(int tier, int flavour, int texture) {
        if (tier != getTier() || flavour != getFlavour() || texture != getTextureId()) {
            dataTracker.set(TIER, tier);
            dataTracker.set(FLAVOUR, flavour);
            dataTracker.set(TEXTURE, texture);
            onAttributesChanged();
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (data == TIER || data == FLAVOUR || data == TEXTURE) {
            onAttributesChanged();
        }
    }

    protected void onAttributesChanged() {
        if (updatingAttributes) {
            return;
        }
        updatingAttributes = true;
        try {
            if (!getWorld().isClient) {
                goalSelector.clear(Predicates.alwaysTrue());
                targetSelector.clear(Predicates.alwaysTrue());
                initGoals();
            }
            initTieredAttributes();
        } finally {
            updatingAttributes = false;
        }
    }

    protected abstract void initTieredAttributes();

    @Override
    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putInt("tier", getTier());
        compound.putInt("flavour", getFlavour());
        compound.putInt("texture", getTextureId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        setAppearance(compound.getInt("tier"), compound.getInt("flavour"), compound.getInt("texture"));
    }
}
