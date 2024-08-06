package invmod.common.item;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemInfusedSword extends SwordItem {
    public ItemInfusedSword() {
        super(CustomToolMaterial.INFUSED_GOLD, new Settings().maxCount(1));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (stack.isDamaged()) {
            stack.setDamage(stack.getDamage() - 1);
        }
        return true;
    }

    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        ToolComponent toolComponent = stack.get(DataComponentTypes.TOOL);
        return toolComponent != null ? toolComponent.getSpeed(state) : 1.0F;
    }

    // get break speed
    // should be getStrVsBlock
    /*
     * @Override public float func_150893_a(ItemStack par1ItemStack, Block
     * par2Block) { if (par2Block == Blocks.web) { return 15.0F; }
     *
     * Material material = par2Block.getMaterial(); return (material !=
     * Material.plants) && (material != Material.vine) && (material !=
     * Material.coral) && (material != Material.leaves) && (material !=
     * Material.sponge) && (material != Material.cactus) ? 1.0F : 1.5F; }
     */

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isDamaged()) {
            return TypedActionResult.fail(stack);
        }
        // if player isSneaking then refill hunger else refill health
        if (player.isSneaking()) {
            player.getHungerManager().add(6, 0.5f);
            world.playSound(player, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_BURP, player.getSoundCategory(),
                    0.5F, world.getRandom().nextFloat() * 0.1F + 0.9F);
        } else {
            player.heal(6.0F);

            // spawn heart particles around the player
            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.HEART, player.getX() + 1.5D, player.getEyeY(), player.getZ(), 1, 0, 0, 0, 0);
                sw.spawnParticles(ParticleTypes.HEART, player.getX() - 1.5D, player.getEyeY(), player.getZ(), 1, 0, 0, 0, 0);
                sw.spawnParticles(ParticleTypes.HEART, player.getX(), player.getEyeY(), player.getZ() + 1.5D, 1, 0, 0, 0, 0);
                sw.spawnParticles(ParticleTypes.HEART, player.getX(), player.getEyeY(), player.getZ() - 1.5D, 1, 0, 0, 0, 0);
            }

        }

        stack.setDamage(this.getMaterial().getDurability());
        return TypedActionResult.success(stack);
    }
}
