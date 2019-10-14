package com.robomwm.usefulutils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Created on 8/3/2019.
 *
 * @author RoboMWM
 */
public final class FileUtils
{
    private static BlockingQueue<Integer> saveQueue = new SynchronousQueue<>();

    /**
     * Asynchronsly save the specified string into a file within the plugin's data folder.
     *
     * E.g. saveStringToFile(plugin, "filename.yml", yaml.saveToString()
     *
     * @see FileUtils#saveStringToFile(Plugin, File, String)
     * @param plugin
     * @param fileName file name, relative to plugin's folder directory
     * @param contents
     */
    public static void saveStringToFile(Plugin plugin, String fileName, String contents)
    {
        saveStringToFile(plugin, new File(plugin.getDataFolder(), fileName), contents);
    }

    /**
     * Asynchronously save the specified string into a file
     *
     * TODO: return status via a future or something
     *
     * @param plugin
     * @param storageFile File to store contents in
     * @param contents
     */
    public static void saveStringToFile(Plugin plugin, File storageFile, String contents)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    saveQueue.put(this.hashCode());
                }
                catch (InterruptedException e)
                {
                    plugin.getLogger().severe("Could not save " + storageFile.toString());
                    e.printStackTrace();
                }


                if (contents == null || contents.isEmpty())
                {
                    storageFile.delete();
                }

                try
                {
                    storageFile.getParentFile().mkdirs();
                    storageFile.createNewFile();
                    Files.write(storageFile.toPath(), Collections.singletonList(contents), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                }
                catch (Exception e)
                {
                    plugin.getLogger().severe("Could not save " + storageFile.toString());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
