package com.invasion.entity.ai.builder;

import com.invasion.INotifyTask;

import net.minecraft.util.math.BlockPos;

public interface ITerrainDig {
    boolean askRemoveBlock(BlockPos pos, INotifyTask paramINotifyTask, float paramFloat);

    boolean askClearPosition(BlockPos pos, INotifyTask paramINotifyTask, float paramFloat);
}