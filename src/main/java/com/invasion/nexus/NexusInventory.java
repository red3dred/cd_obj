package com.invasion.nexus;

import com.invasion.item.InvItems;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;

public class NexusInventory extends SimpleInventory {
    static final int MAX_FLUX_GENERATION_TIME = 3000;
    static final int MAX_TRAP_COOK_TIME = 1200;

    private int cookTime;
    private int accumulatedFlux;

    public NexusInventory() {
        super(2);
    }

    public int getFluxProgress() {
        return accumulatedFlux;
    }

    public void setFlugProgress(int time) {
        accumulatedFlux = time;
    }

    public int getCookTime() {
        return cookTime;
    }

    public void setCookTime(int time) {
        cookTime = time;
    }

    public void tick(INexusAccess nexus) {
        tickCookTime(nexus, getStack(0), getStack(1));
    }

    public void generateFlux(int increment) {
        accumulatedFlux += increment;
        if (accumulatedFlux >= MAX_FLUX_GENERATION_TIME) {
            ItemStack currentGeneratedItem = getStack(1);
            if (currentGeneratedItem.isEmpty()) {
                setStack(1, InvItems.RIFT_FLUX.getDefaultStack());
                accumulatedFlux -= MAX_FLUX_GENERATION_TIME;
            } else if (currentGeneratedItem.isOf(InvItems.RIFT_FLUX)) {
                currentGeneratedItem.increment(1);
                accumulatedFlux -= MAX_FLUX_GENERATION_TIME;
            }
        }
    }

    private void tickCookTime(INexusAccess nexus, ItemStack firstStack, ItemStack secondStack) {
        if (!firstStack.isEmpty()) {
            if (firstStack.isOf(InvItems.EMPTY_TRAP)) {
                if (cookTime < MAX_TRAP_COOK_TIME) {
                    cookTime += nexus.getMode() == Mode.STOPPED ? 1 : 9;
                } else {
                    if (secondStack.isEmpty()) {
                        setStack(1, InvItems.FLAME_TRAP.getDefaultStack());
                        firstStack.decrement(1);
                        cookTime = 0;
                    } else if (secondStack.isOf(InvItems.FLAME_TRAP) && secondStack.getCount() < secondStack.getMaxCount()) {
                        secondStack.increment(1);
                        firstStack.decrement(1);
                        cookTime = 0;
                    }
                }
            } else if (firstStack.isOf(InvItems.RIFT_FLUX)) {
                if (cookTime < MAX_TRAP_COOK_TIME && nexus.getLevel() >= 10) {
                    cookTime += 5;
                }

                if (cookTime >= MAX_TRAP_COOK_TIME) {
                    if (secondStack.isEmpty()) {
                        setStack(1, InvItems.STRONG_NEXUS_CATALYST.getDefaultStack());
                        firstStack.decrement(1);
                        cookTime = 0;
                    }
                }
            }
        } else {
            cookTime = 0;
        }
    }

    public void readNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
        accumulatedFlux = compound.getInt("accumulatedFlux");
        cookTime = compound.getInt("cookTime");
        readNbtList(compound.getList("Items", NbtElement.COMPOUND_TYPE), lookup);
    }

    public NbtCompound writeNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
        compound.putInt("accumulatedFlux", accumulatedFlux);
        compound.putInt("cookTime", cookTime);
        compound.put("Items", toNbtList(lookup));
        return compound;
    }
}
