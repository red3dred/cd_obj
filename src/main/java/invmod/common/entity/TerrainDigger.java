package invmod.common.entity;

import java.util.Arrays;
import java.util.Objects;

import invmod.common.INotifyTask;
import invmod.common.util.IPosition;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class TerrainDigger implements ITerrainDig, INotifyTask {
    private ICanDig digger;
    private ITerrainModify modifier;
    private float digRate;

    public TerrainDigger(ICanDig digger, ITerrainModify modifier, float digRate) {
        this.digger = digger;
        this.modifier = modifier;
        this.digRate = digRate;
    }

    public void setDigRate(float digRate) {
        this.digRate = digRate;
    }

    public float getDigRate() {
        return this.digRate;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean askClearPosition(BlockPos pos, INotifyTask onFinished, float costMultiplier) {
        return this.modifier.requestTask(onFinished, this, Arrays.stream(digger.getBlockRemovalOrder(pos)).map(IPosition::toBlockPos).map(removal -> {
            BlockState state = digger.getTerrain().getBlockState(removal);
            if (!state.isAir() && !state.blocksMovement() && digger.canClearBlock(removal)) {
                return ModifyBlockEntry.ofDeletion(removal, (int) (costMultiplier * digger.getBlockRemovalCost(removal) / digRate));
            }

            return null;
        }).filter(Objects::nonNull));
    }

    @Override
    public boolean askRemoveBlock(BlockPos pos, INotifyTask onFinished, float costMultiplier) {
        return digger.canClearBlock(pos) && modifier.requestTask(onFinished, this, ModifyBlockEntry.ofDeletion(pos, (int) (costMultiplier * digger.getBlockRemovalCost(pos) / digRate)));
    }

    @Override
    public void notifyTask(Status result) {
        if (result == Status.SUCCESS) {
            ModifyBlockEntry entry = modifier.getLastBlockModified();
            digger.onBlockRemoved(entry.toBlockPos(), entry.getOldBlock());
        }
    }
}