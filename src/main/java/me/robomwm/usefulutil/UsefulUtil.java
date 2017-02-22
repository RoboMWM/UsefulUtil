package me.robomwm.usefulutil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created on 2/22/2017.
 *
 * @author RoboMWM
 */
public class UsefulUtil
{
    public static UsefulPlayer getPlayer(String name)
    {
        Player player = Bukkit.getPlayer(name);
        if (player == null)
            return null;
        return new UsefulPlayer(player);
    }
    public static UsefulPlayer getPlayer(UUID uuid)
    {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return null;
        return new UsefulPlayer(player);
    }
}
