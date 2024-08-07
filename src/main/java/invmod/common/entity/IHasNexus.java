package invmod.common.entity;

import invmod.common.nexus.INexusAccess;

public interface IHasNexus {
  INexusAccess getNexus();

  void acquiredByNexus(INexusAccess paramINexusAccess);
}
