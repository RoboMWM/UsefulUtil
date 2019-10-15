package com.robomwm.usefulutils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 8/3/2019.
 *
 * @author RoboMWM
 */
public final class FileUtils
{
    private static Lock saveLock = new ReentrantLock();

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
                saveLock.lock();

                if (contents == null || contents.isEmpty())
                {
                    storageFile.delete();
                    saveLock.unlock();
                    return;
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

                saveLock.unlock();
            }
        }.runTaskAsynchronously(plugin);
    }
}
