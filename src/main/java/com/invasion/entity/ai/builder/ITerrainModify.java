package com.invasion.entity.ai.builder;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.invasion.INotifyTask;

public interface ITerrainModify {
    boolean isReadyForTask(INotifyTask notify);

    boolean requestTask(Collection<ModifyBlockEntry> tasks, @Nullable INotifyTask onFinished, @Nullable INotifyTask onBlockChanged);

    default boolean requestTask(INotifyTask notify1, INotifyTask notify2, ModifyBlockEntry...tasks) {
        return requestTask(Arrays.asList(tasks), notify1, notify2);
    }

    default boolean requestTask(INotifyTask notify1, INotifyTask notify2, Stream<ModifyBlockEntry> tasks) {
        return requestTask(tasks.toList(), notify1, notify2);
    }

    ModifyBlockEntry getLastBlockModified();
}