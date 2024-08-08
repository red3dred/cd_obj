package invmod.common.nexus;

import invmod.common.item.InvItems;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockNexus extends BlockWithEntity {
    private static final MapCodec<BlockNexus> CODEC = Block.createCodec(BlockNexus::new);
    public static final BooleanProperty LIT = BooleanProperty.of("lit");

    public BlockNexus(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(LIT, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!stack.isOf(InvItems.MATERIAL_PROBE) && !stack.isOf(InvItems.NEXUS_ADJUSTER) && !stack.getRegistryEntry().matchesKey(InvItems.DEBUG_WAND)) {
            if (world.getBlockEntity(pos) instanceof TileEntityNexus nexus) {
                player.openHandledScreen(new NamedScreenHandlerFactory() {
                    @Override
                    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                        return new ContainerNexus(syncId, playerInventory, nexus);
                    }

                    @Override
                    public Text getDisplayName() {
                        return getName();
                    }
                });
            }
            return ItemActionResult.SUCCESS;
        }

        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {

        if (!state.get(LIT)) {
            return;
        }

        for (int i = 0; i < 6; i++) {
            double y1 = pos.getY() + random.nextFloat();
            double y2 = (random.nextFloat() - 0.5D) * 0.5D;

            int direction = random.nextInt(2) * 2 - 1;
            double x2;
            double x1;
            double z1;
            double z2;
            if (random.nextInt(2) == 0) {
                z1 = pos.getZ() + 0.5D + 0.25D * direction;
                z2 = random.nextFloat() * 2.0F * direction;

                x1 = pos.getX() + random.nextFloat();
                x2 = (random.nextFloat() - 0.5D) * 0.5D;
            } else {
                x1 = pos.getX() + 0.5D + 0.25D * direction;
                x2 = random.nextFloat() * 2.0F * direction;
                z1 = pos.getZ() + random.nextFloat();
                z2 = (random.nextFloat() - 0.5D) * 0.5D;
            }

            world.addParticle(ParticleTypes.PORTAL, x1, y1, z1, x2, y2, z2);
        }
    }

    @Override
    public TileEntityNexus createBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityNexus(pos, state);
    }

    @Override
    protected float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        return state.get(LIT) ? -1 : super.calcBlockBreakingDelta(state, player, world, pos);
    }
}