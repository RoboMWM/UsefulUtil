package me.robomwm.usefulutil.compat;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 3/1/2017.
 *
 * @author RoboMWM
 */
public class UsefulCompatEvent extends Event implements Cancellable
{
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }
    private boolean cancel = false;
    public boolean isCancelled()
    {
        return cancel;
    }
    public void setCancelled(boolean cancelled)
    {
        this.cancel = cancelled;
    }

    private Throwable rock;
    private String identifier;
    private JavaPlugin callingPlugin;

    UsefulCompatEvent(Throwable rock, String id, JavaPlugin plugin)
    {
        this.rock = rock;
        this.identifier = id;
        this.callingPlugin = plugin;
    }

    public Throwable getRock()
    {
        return rock;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public JavaPlugin getCallingPlugin()
    {
        return callingPlugin;
    }
}
