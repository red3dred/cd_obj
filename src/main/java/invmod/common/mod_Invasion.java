package invmod.common;

import invmod.common.nexus.TileEntityNexus;

@Deprecated
public class mod_Invasion {
    private static TileEntityNexus focusNexus;
    private static TileEntityNexus activeNexus;
    private static boolean isInvasionActive = false;

    @Deprecated
    public static boolean isInvasionActive() {
        return isInvasionActive;
    }

    @Deprecated
    public static boolean tryGetInvasionPermission(TileEntityNexus nexus) {
        // TODO: Not implemented correctly
        if (nexus == activeNexus) {
            return true;
        }

        if (nexus == null) {
            InvasionMod.log("Nexus entity invalid");
            return false;
        }

        activeNexus = nexus;
        isInvasionActive = true;
        return true;
    }

    @Deprecated
    public static void setInvasionEnded(TileEntityNexus nexus) {
        if (activeNexus == nexus) {
            isInvasionActive = false;
        }
    }

    @Deprecated
    public static void setNexusUnloaded(TileEntityNexus nexus) {
        if (activeNexus == nexus) {
            nexus = null;
            isInvasionActive = false;
        }
    }

    @Deprecated
    public static void setNexusClicked(TileEntityNexus nexus) {
        focusNexus = nexus;
    }

    @Deprecated
    public static TileEntityNexus getActiveNexus() {
        return activeNexus;
    }

    @Deprecated
    public static TileEntityNexus getFocusNexus() {
        return focusNexus;
    }
}