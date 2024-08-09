package invmod.common;

import invmod.common.nexus.TileEntityNexus;
import org.jetbrains.annotations.Nullable;

public class mod_Invasion {
    private static TileEntityNexus focusNexus;
    private static TileEntityNexus activeNexus;
    private static boolean isInvasionActive = false;

    public static boolean isInvasionActive() {
        return isInvasionActive;
    }

    public static boolean tryGetInvasionPermission(TileEntityNexus nexus) {
        if (nexus == activeNexus) {
            return true;
        }

        if (nexus == null) {
            log("Nexus entity invalid");
            return false;
        }

        activeNexus = nexus;
        isInvasionActive = true;
        return true;
    }

    public static void setInvasionEnded(TileEntityNexus nexus) {
        if (activeNexus == nexus) {
            isInvasionActive = false;
        }
    }

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

    public static void log(@Nullable String s) {
        if (InvasionMod.getConfig().enableLog && s != null) {
            InvasionMod.LOGGER.info(s);
        }
    }

}