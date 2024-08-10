package invmod.common;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;

public interface SparrowAPI {
    default boolean isStupidToAttack() {
        return false;
    }

    default boolean doNotVaporize() {
        return false;
    }

    default boolean isPredator() {
        return false;
    }

    default boolean isHostile() {
        return false;
    }

    default boolean isPeaceful() {
        return false;
    }

    default boolean isPrey() {
        return false;
    }

    default boolean isNeutral() {
        return false;
    }

    default boolean isUnkillable() {
        return false;
    }

    default boolean isThreatTo(Entity paramEntity) {
        return false;
    }

    default boolean isFriendOf(Entity paramEntity) {
        return false;
    }

    default boolean isNPC() {
        return false;
    }

    default int isPet() {
        return 0;
    }

    @Nullable
    default Entity getPetOwner() {
        return null;
    }

    Entity getAttackingTarget();

    float getSize();

    String getSpecies();

    default int getTier() {
        return 0;
    }

    int getGender();

    @Nullable
    default String customStringAndResponse(String paramString) {
        return null;
    }

    String getSimplyID();
}