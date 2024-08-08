package invmod.common.entity;

import java.util.stream.Stream;

import invmod.common.INotifyTask;

public interface ITerrainModify
{
  boolean isReadyForTask(INotifyTask notify);

  boolean requestTask(ModifyBlockEntry[] tasks, INotifyTask notify1, INotifyTask notify2);

  default boolean requestTask(INotifyTask notify1, INotifyTask notify2, ModifyBlockEntry...tasks) {
      return requestTask(tasks, notify1, notify2);
  }

  default boolean requestTask(INotifyTask notify1, INotifyTask notify2, Stream<ModifyBlockEntry> tasks) {
      return requestTask(tasks.toArray(ModifyBlockEntry[]::new), notify1, notify2);
  }

  ModifyBlockEntry getLastBlockModified();
}