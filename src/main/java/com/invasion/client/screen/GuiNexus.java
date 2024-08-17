package com.invasion.client.screen;

import com.invasion.InvasionMod;
import com.invasion.block.container.ContainerNexus;
import com.invasion.nexus.Mode;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GuiNexus extends HandledScreen<ContainerNexus> {
    private static final Identifier BACKGROUND = InvasionMod.id("textures/gui/nexus.png");

    public GuiNexus(ContainerNexus container, PlayerInventory inventory, Text title) {
        super(container, inventory, title);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
        context.drawText(textRenderer, "Nexus - Level " + handler.getLevel(), 46, 6, 0x404040, false);
        context.drawText(textRenderer, handler.getKills() + " mobs killed", 96, 60, 0x404040, false);
        context.drawText(textRenderer, "R: " + handler.getSpawnRadius(), 142, 72, 0x404040, false);

        if (handler.getMode() == Mode.STARTED || handler.getMode() == Mode.WAITING) {
            context.drawText(textRenderer, "Activated!", 13, 62, 4210752, false);
            context.drawText(textRenderer, "Wave " + handler.getCurrentWave(), 55, 37, 0x404040, false);
        } else if (handler.getMode() == Mode.CONTINUOUS) {
            context.drawText(textRenderer, "Power:", 56, 31, 4210752, false);
            context.drawText(textRenderer, "" + handler.getPowerLevel(), 61, 44, 0x404040, false);
        }

        if (handler.isActivating() && handler.getMode() == Mode.STOPPED) {
            context.drawText(textRenderer, "Activating...", 13, 62, 0x404040, false);
            if (handler.getMode() != Mode.STABLE) {
                context.drawText(textRenderer, "Are you sure?", 8, 72, 0x404040, false);
            }
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int j = (width - backgroundWidth) / 2;
        int k = (height - backgroundHeight) / 2;
        context.drawTexture(BACKGROUND, j, k, 0, 0, backgroundWidth, backgroundHeight);

        int l = handler.getGenerationProgressScaled(26);
        context.drawTexture(BACKGROUND, j + 126, k + 28 + 26 - l, 185, 26 - l, 9, l);
        context.drawTexture(BACKGROUND, j + 31, k + 51, 204, 0, handler.getCookProgressScaled(18), 2);

        if (handler.getMode() == Mode.STARTED || handler.getMode() == Mode.WAITING) {
            context.drawTexture(BACKGROUND, j + 19, k + 29, 176, 0, 9, 31);
            context.drawTexture(BACKGROUND, j + 19, k + 19, 194, 0, 9, 9);
        } else if (handler.getMode() == Mode.CONTINUOUS) {
            context.drawTexture(BACKGROUND, j + 19, k + 29, 176, 31, 9, 31);
        }

        if ((handler.getMode() == Mode.STOPPED || handler.getMode() == Mode.CONTINUOUS) && handler.isActivating()) {
            l = handler.getActivationProgressScaled(31);
            context.drawTexture(BACKGROUND, j + 19, k + 29 + 31 - l, 176, 31 - l, 9, l);
        } else if (handler.getMode() == Mode.STABLE && handler.isActivating()) {
            l = handler.getActivationProgressScaled(31);
            context.drawTexture(BACKGROUND, j + 19, k + 29 + 31 - l, 176, 62 - l, 9, l);
        }
    }
}