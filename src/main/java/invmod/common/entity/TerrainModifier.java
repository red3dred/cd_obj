package invmod.common.entity;

import invmod.common.INotifyTask;
import invmod.common.InvasionMod;
import invmod.common.block.InvBlocks;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/**
 * AI component for building structures.
 * Takes a list of block changes and steps through them to place or break the blocks when they are in the entity's range.
 */
public final class TerrainModifier implements ITerrainModify {
    private final LivingEntity theEntity;

    private final float reach;

    private INotifyTask finishCallback = INotifyTask.NONE;
    private INotifyTask blockChangeCallback = INotifyTask.NONE;

    private List<ModifyBlockEntry> modList = new ArrayList<>();

    private @Nullable ModifyBlockEntry nextEntry;
    private @Nullable ModifyBlockEntry lastEntry;

    private int entryIndex;
    private int timer;

    private INotifyTask.Status lastStatus = INotifyTask.Status.SUCCESS;

    public TerrainModifier(LivingEntity entity, float defaultReach) {
        this.theEntity = entity;
        this.reach = MathHelper.square(defaultReach);
    }

    @Override
    public boolean isReadyForTask(INotifyTask asker) {
        return modList.isEmpty() || finishCallback == asker;
    }

    public boolean isBusy() {
        return timer > 0;
    }

    @Override
    public boolean requestTask(Collection<ModifyBlockEntry> entries, @Nullable INotifyTask onFinished, @Nullable INotifyTask onBlockChanged) {
        if (isReadyForTask(onFinished)) {
            modList.addAll(entries);
            finishCallback = onFinished == null ? INotifyTask.NONE : onFinished;
            blockChangeCallback = onBlockChanged == null ? INotifyTask.NONE : onBlockChanged;
            return true;
        }
        return false;
    }

    @Override
    public ModifyBlockEntry getLastBlockModified() {
        return lastEntry;
    }

    public void onUpdate() {
        if (timer > 1) {
            timer -= 1;
            return;
        }
        if (timer == 1) {
            entryIndex++;
            timer = 0;
            lastStatus = changeBlock(nextEntry);
            lastEntry = nextEntry;
            blockChangeCallback.notifyTask(lastStatus);
        }

        if (entryIndex < modList.size()) {
            nextEntry = modList.get(entryIndex);
            while (isTerrainIdentical(nextEntry)) {
                entryIndex++;
                if (entryIndex < modList.size()) {
                    nextEntry = modList.get(entryIndex);
                } else {
                    cancelTask();
                    return;
                }
            }

            timer = Math.max(nextEntry.getCost(), 1);
        } else if (!modList.isEmpty()) {
            cancelTask();
        }
    }

    public void cancelTask() {
        entryIndex = 0;
        timer = 0;
        modList.clear();
        finishCallback.notifyTask(lastStatus);
    }

    private INotifyTask.Status changeBlock(ModifyBlockEntry entry) {
        if (theEntity.getEyePos().squaredDistanceTo(entry.pos().toCenterPos()) > reach) {
            return INotifyTask.Status.OUT_OF_RANGE;
        }

        BlockState oldBlock = theEntity.getWorld().getBlockState(entry.pos());
        entry.setOldBlock(oldBlock);
        if (oldBlock.isOf(InvBlocks.NEXUS_CORE)) {
            return INotifyTask.Status.UNMODIFIABLE;
        }

        int flags = Block.NOTIFY_NEIGHBORS | Block.NOTIFY_LISTENERS;
        if (!InvasionMod.getConfig().destructedBlocksDrop) {
            flags |= Block.SKIP_DROPS;
        }

        boolean succeeded = entry.newBlock().isAir()
                ? theEntity.getWorld().breakBlock(entry.pos(), InvasionMod.getConfig().destructedBlocksDrop, theEntity)
                : theEntity.getWorld().setBlockState(entry.pos(), entry.newBlock(), flags);
        if (!succeeded) {
            return INotifyTask.Status.UNMODIFIABLE;
        }
        return INotifyTask.Status.SUCCESS;
    }

    private boolean isTerrainIdentical(ModifyBlockEntry entry) {
        return theEntity.getWorld().getBlockState(entry.pos()) == entry.newBlock();
    }
}