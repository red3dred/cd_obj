package com.invasion.block.container;

import org.jetbrains.annotations.Nullable;

import com.invasion.InvScreenHandlers;
import com.invasion.block.InvBlocks;
import com.invasion.nexus.Mode;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public class NexusScreenHandler extends ScreenHandler {
    private final PropertyDelegate properties;
    private final ScreenHandlerContext context;

    public NexusScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, new SimpleInventory(2), new ArrayPropertyDelegate(10), ScreenHandlerContext.EMPTY);
    }

    public NexusScreenHandler(int syncId, PlayerInventory playerInventory, Inventory nexusInventory, PropertyDelegate properties, ScreenHandlerContext context) {
        super(InvScreenHandlers.NEXUS, syncId);
        this.context = context;
        this.properties = properties;
        addProperties(properties);
        addSlot(new Slot(nexusInventory, 0, 32, 33));
        addSlot(new OutputSlot(nexusInventory, 1, 102, 33));

        // inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public int getActivationTimer() {
        return properties.get(0);
    }

    public Mode getMode() {
        return Mode.forId(properties.get(1));
    }

    public int getCurrentWave() {
        return properties.get(2);
    }

    public int getLevel() {
        return properties.get(3);
    }

    public int getKills() {
        return properties.get(4);
    }

    public int getSpawnRadius() {
        return properties.get(5);
    }

    public int getGeneration() {
        return properties.get(6);
    }

    public int getPowerLevel() {
        return properties.get(7);
    }

    public int getCookTime() {
        return properties.get(8);
    }

    public boolean isActivating() {
        return properties.get(9) != 0;
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
        return canUse(context, entityplayer, InvBlocks.NEXUS_CORE);
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