package com.invasion.block.container;

import org.jetbrains.annotations.Nullable;

import com.invasion.block.TileEntityNexus;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ContainerNexus extends ScreenHandler {
    private final TileEntityNexus nexus;

    public ContainerNexus(int syncId, PlayerInventory inventoryplayer, TileEntityNexus tileEntityNexus) {
        // TODO: Screen handler type
        super(null, syncId);
        nexus = tileEntityNexus;
        addSlot(new Slot(tileEntityNexus, 0, 32, 33));
        addSlot(new SlotOutput(tileEntityNexus, 1, 102, 33));

        // inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventoryplayer, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventoryplayer, col, 8 + col * 18, 142));
        }

        addProperties(nexus.properties);

    }

    public TileEntityNexus getNexus() {
        return nexus;
    }

    public int getActivationTimer() {
        return nexus.properties.get(0);
    }

    public int getMode() {
        return nexus.properties.get(1);
    }

    public int getCurrentWave() {
        return nexus.properties.get(2);
    }

    public int getLevel() {
        return nexus.properties.get(3);
    }

    public int getKills() {
        return nexus.properties.get(4);
    }

    public int getSpawnRadius() {
        return nexus.properties.get(5);
    }

    public int getGeneration() {
        return nexus.properties.get(6);
    }

    public int getPowerLevel() {
        return nexus.properties.get(7);
    }

    public int getCookTime() {
        return nexus.properties.get(8);
    }

    public int getActivationProgressScaled(int i) {
        return getActivationTimer() * i / 400;
    }

    public int getGenerationProgressScaled(int i) {
        return getGeneration() * i / 3000;
    }

    public int getCookProgressScaled(int i) {
        return getCookTime() * i / 1200;
    }

    @Override
    public boolean canUse(PlayerEntity entityplayer) {
        return nexus.canPlayerUse(entityplayer);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        @Nullable
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack remainder = stack.copy();

        if (index == 1) {
            if (!insertItem(stack, 2, 38, true)) {
                return ItemStack.EMPTY;
            }
        } else if ((index >= 2) && (index < 38)) {
            if (!insertItem(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!insertItem(stack, 2, 38, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (stack.getCount() == remainder.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, stack);
        return remainder;
    }
}