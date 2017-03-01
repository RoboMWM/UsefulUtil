package me.robomwm.usefulutil.compat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 3/1/2017.
 *
 * @author RoboMWM
 */
public class UsefulCompat
{
    private static Integer serverVersion = null;
    private static int currentVersion = 11; //TODO: use minor version from pom
    public static boolean isCurrentOrNewer()
    {
        return getVersion() >= currentVersion;
    }

    public static int getVersion()
    {
        if (serverVersion != null)
            return serverVersion;
        String version = Bukkit.getBukkitVersion();
        version = version.substring(2);
        version = version.substring(0, version.indexOf("."));
        int versionNumber;
        try
        {
            versionNumber = Integer.valueOf(version);
        }
        catch (Exception e)
        {
            Bukkit.getLogger().warning("[DeathSpectating] Was not able to determine bukkit version.");
            return -1;
        }
        serverVersion = versionNumber;
        return versionNumber;
    }

    public static boolean isOlder(int version)
    {
        return getVersion() < version;
    }

    /**
     * Calls a "UsefulCompatEvent" so listeners can decide if they can provide some form of compatibility
     *
     * General usage:
     *
     * catch (whatever e)
     * {
     *     if (!compatCall(e, "mymethodname", myplugin) throw e;
     * }
     *
     * @param rock
     * @param id
     * @param plugin
     * @return whether the event was handled (canceled)
     */
    public static boolean compatCall(Throwable rock, String id, JavaPlugin plugin, Object... objects)
    {
        //return false if we're on the current version
        if (isCurrentOrNewer())
            return false;
        UsefulCompatEvent event = new UsefulCompatEvent(rock, id, plugin, objects);
        plugin.getServer().getPluginManager().callEvent(event);
        return event.isCancelled();
    }
}
