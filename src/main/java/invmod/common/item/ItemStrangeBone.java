package invmod.common.item;

import invmod.common.entity.EntityIMWolf;
import invmod.common.entity.IHasNexus;
import invmod.common.entity.InvEntities;

import org.jetbrains.annotations.Nullable;

import invmod.common.nexus.TileEntityNexus;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

class ItemStrangeBone extends Item {
    public ItemStrangeBone(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity.getWorld().isClient || !(entity instanceof WolfEntity wolf && wolf.isTamed()) || entity instanceof EntityIMWolf) {
            return ActionResult.PASS;
        }

        @Nullable
        TileEntityNexus nexus = IHasNexus.findNexus(entity.getWorld(), entity.getBlockPos());

        if (nexus == null) {
            user.sendMessage(Text.translatable("invmod.message.bone.nonearbynexus1").formatted(Formatting.RED));
            user.sendMessage(Text.translatable("invmod.message.bone.nonearbynexus2").formatted(Formatting.RED));
            return ActionResult.FAIL;
        }

        EntityIMWolf newWolf = wolf.convertTo(InvEntities.WOLF, true);
        newWolf.setNexus(nexus);

        wolf.getWorld().spawnEntity(newWolf);
        wolf.discard();
        stack.decrement(1);
        return ActionResult.SUCCESS;
    }
}