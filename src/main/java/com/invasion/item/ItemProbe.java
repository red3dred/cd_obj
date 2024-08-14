package com.invasion.item;

import org.jetbrains.annotations.Nullable;

import com.invasion.block.InvBlocks;
import com.invasion.block.TileEntityNexus;
import com.invasion.entity.EntityIMLiving;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

class ItemProbe extends Item {
    private final boolean isProbe;

    public ItemProbe(Settings settings, boolean isProbe) {
        super(settings);
        this.isProbe = isProbe;
    }

    @Override
    public int getEnchantability() {
        return 14;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) {
            return ActionResult.PASS;
        }
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        @Nullable
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResult.FAIL;
        }
        if (state.isOf(InvBlocks.NEXUS_CORE)) {
            TileEntityNexus nexus = (TileEntityNexus) world.getBlockEntity(pos);
            int newRange = nexus.getSpawnRadius();

            // check if the player wants to increase or decrease the range
            newRange += player.isSneaking() ? -8 : 8;
            // TODO: this check should be handled by the block entity, not here
            newRange = MathHelper.clamp(newRange, 32, 128);

            if (nexus.setSpawnRadius(newRange)) {
                player.sendMessage(Text.translatable("invmod.message.probe.rangechanged", Text.literal(nexus.getSpawnRadius() + "").formatted(Formatting.GREEN)).formatted(Formatting.DARK_GREEN));
            } else if (nexus.isActive()) {
                player.sendMessage(Text.translatable("invmod.message.probe.cannotchangerange", Text.literal(nexus.getSpawnRadius() + "")).formatted(Formatting.RED));
            }
            return ActionResult.SUCCESS;
        }

        if (isProbe) {
            float blockStrength = EntityIMLiving.getBlockStrength(pos, state, world);
            int strengthRounded = (int) ((blockStrength + 0.005D) * 100.0D) / 100;
            player.sendMessage(Text.translatable("invmod.message.probe.blockstrength", Text.literal(strengthRounded + "").formatted(Formatting.GREEN)).formatted(Formatting.DARK_GREEN));
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }
}