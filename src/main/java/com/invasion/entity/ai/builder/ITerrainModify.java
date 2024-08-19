package com.invasion.entity.ai.builder;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.invasion.Notifiable;

public interface ITerrainModify {
    boolean isReadyForTask(Notifiable notify);

    boolean requestTask(Collection<ModifyBlockEntry> tasks, @Nullable Notifiable onFinished, @Nullable Notifiable onBlockChanged);

    default boolean requestTask(Notifiable notify1, Notifiable notify2, ModifyBlockEntry...tasks) {
        return requestTask(Arrays.asList(tasks), notify1, notify2);
    }

    default boolean requestTask(Notifiable notify1, Notifiable notify2, Stream<ModifyBlockEntry> tasks) {
        return requestTask(tasks.toList(), notify1, notify2);
    }

    ModifyBlockEntry getLastBlockModified();
}