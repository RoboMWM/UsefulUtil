package com.robomwm.usefulutils;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 8/3/2019.
 *
 * @author RoboMWM
 */
public final class FileUtils
{
    private static ExecutorService savePool = Executors.newCachedThreadPool();

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
        savePool.execute(() ->
                {
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
        );
    }
}
