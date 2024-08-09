package invmod.common.nexus;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.lwjgl.opengl.GL11;

public class GuiNexus extends HandledScreen<ContainerNexus>
{
  private static final ResourceLocation background = new ResourceLocation("invmod:textures/nexusgui.png");

  public GuiNexus(InventoryPlayer inventoryplayer, TileEntityNexus tileentityNexus)
  {

    super(new ContainerNexus(inventoryplayer, tileentityNexus));
  }

@Override
  protected void drawGuiContainerForegroundLayer(int x, int y)
  {
    this.fontRendererObj.drawString("Nexus - Level " + handler.getLevel(), 46, 6, 4210752);
    this.fontRendererObj.drawString(handler.getKills() + " mobs killed", 96, 60, 4210752);
    this.fontRendererObj.drawString("R: " + handler.getSpawnRadius(), 142, 72, 4210752);

    if ((handler.getMode() == 1) || (handler.getMode() == 3))
    {
      this.fontRendererObj.drawString("Activated!", 13, 62, 4210752);
      this.fontRendererObj.drawString("Wave " + handler.getCurrentWave(), 55, 37, 4210752);
    }
    else if (handler.getMode() == 2)
    {
      this.fontRendererObj.drawString("Power:", 56, 31, 4210752);
      this.fontRendererObj.drawString("" + handler.getPowerLevel(), 61, 44, 4210752);
    }

    if ((handler.getNexus().isActivating()) && (handler.getMode() == 0))
    {
      this.fontRendererObj.drawString("Activating...", 13, 62, 4210752);
      if (handler.getMode() != 4)
        this.fontRendererObj.drawString("Are you sure?", 8, 72, 4210752);
    }
  }
@Override
  protected void drawGuiContainerBackgroundLayer(float f, int un1, int un2)
  {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(background);
    int j = (this.width - this.xSize) / 2;
    int k = (this.height - this.ySize) / 2;
    drawTexturedModalRect(j, k, 0, 0, this.xSize, this.ySize);

    int l = handler.getGenerationProgressScaled(26);
    drawTexturedModalRect(j + 126, k + 28 + 26 - l, 185, 26 - l, 9, l);
    l = handler.getCookProgressScaled(18);
    drawTexturedModalRect(j + 31, k + 51, 204, 0, l, 2);

    if ((handler.getMode() == 1) || (handler.getMode() == 3))
    {
      drawTexturedModalRect(j + 19, k + 29, 176, 0, 9, 31);
      drawTexturedModalRect(j + 19, k + 19, 194, 0, 9, 9);
    }
    else if (handler.getMode() == 2)
    {
      drawTexturedModalRect(j + 19, k + 29, 176, 31, 9, 31);
    }

    if (((handler.getMode() == 0) || (handler.getMode() == 2)) && (handler.getNexus().isActivating()))
    {
      l = handler.getActivationProgressScaled(31);
      drawTexturedModalRect(j + 19, k + 29 + 31 - l, 176, 31 - l, 9, l);
    }
    else if ((handler.getMode() == 4) && (handler.getNexus().isActivating()))
    {
      l = handler.getActivationProgressScaled(31);
      drawTexturedModalRect(j + 19, k + 29 + 31 - l, 176, 62 - l, 9, l);
    }
  }
}