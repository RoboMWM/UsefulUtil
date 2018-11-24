package com.robomwm.usefulutil;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

/**
 * Created on 2/22/2017.
 *
 * @author RoboMWM
 */
public final class UsefulUtil
{
    private UsefulUtil(){}

    private static void log(String error)
    {
        Bukkit.getLogger().warning("[" + UsefulUtil.class.getPackage().getName() + "] " + error);
    }

    /**
     * Returns whether the entity is a hostile/potentially hostile mob that extends Creature.
     *
     * Mobs that extend Flying extend LivingEntity, which <i>does not have getTarget().</i> Creatures do have getTarget().
     * As such, monsters of the Flying class (notably ghast, enderdragon, etc.), are not included in this check.
     *
     * @param entity
     * @return
     * @deprecated 1.13 added Mob class which is where getTarget has been moved to (every monster has tracking now)
     * @see org.bukkit.entity.Mob
     */
    @Deprecated
    public static boolean isMonsterWithTracking(Entity entity)
    {
        if (!(entity instanceof Creature)) return false;
        if (entity instanceof Monster) return true;


        EntityType type = entity.getType();
        switch(type)
        {
            case MAGMA_CUBE:
            case SHULKER:
            case POLAR_BEAR:
                return true;
            case RABBIT:
                Rabbit rabbit = (Rabbit) entity;
                if (rabbit.getRabbitType() == Rabbit.Type.THE_KILLER_BUNNY) return true;
            default:
                return false;
        }
    }
    /**
     * Returns whether the entity is a hostile/potentially hostile mob
     * @param entity
     * @return
     */
    public static boolean isMonster(Entity entity)
    {
        if (entity instanceof Monster) return true;

        EntityType type = entity.getType();
        switch(type)
        {
            case GHAST: //extends Flying
            case ENDER_DRAGON: //extends ComplexLivingEntity
            case MAGMA_CUBE: //extends Slime
            case SHULKER: //extends Golem
            case POLAR_BEAR: //extends Animal - neutral until provoked (treating same classification as zombie pigmen)
                return true;
            case RABBIT:
                Rabbit rabbit = (Rabbit) entity;
                if (rabbit.getRabbitType() == Rabbit.Type.THE_KILLER_BUNNY) return true;
            default:
                return false;
        }
    }

    /**
     * Gets the entity that dealt the final blow
     * @param event
     * @return
     */
    public static Entity getKiller(EntityDeathEvent event)
    {
        return getKiller(event, false);
    }

    /**
     * Gets the entity that dealt the final blow
     * @param event
     * @param deleteProjectile whether to delete the projectile that dealt the final blow
     * @return
     */
    public static Entity getKiller(EntityDeathEvent event, boolean deleteProjectile)
    {
        LivingEntity entity = event.getEntity();
        Entity killer = event.getEntity().getKiller();

        if (killer != null)
            return killer;

        //Killed by non-player entity
        if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent)
            killer = getSourceAttacker(entity.getLastDamageCause(), deleteProjectile);

        return killer;
    }

    /**
     * Returns the source entity responsible for this damage (e.g. the skeleton that fired the arrow)
     * @param damageEvent
     * @param deleteProjectile whether to delete the projectile that caused the damage (if applicable)
     * @return
     */
    public static Entity getSourceAttacker(EntityDamageEvent damageEvent, boolean deleteProjectile)
    {
        Entity damager = null;

        if (damageEvent instanceof EntityDamageByEntityEvent)
        {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)damageEvent;

            damager = event.getDamager();

            //damaged by projectile
            if (damager instanceof Projectile)
            {
                Projectile arrow = (Projectile)damager;
                if (arrow.getShooter() instanceof LivingEntity)
                    damager = (Entity) arrow.getShooter();
                if (deleteProjectile)
                    arrow.remove();
            }

            //damaged by TNT explosion
            else if (damager instanceof TNTPrimed)
            {
                TNTPrimed tnt = (TNTPrimed)damager;
                damager = tnt.getSource();
            }
        }

        //TODO: track kills due to fire and other related environmental damage(?)

        return damager;
    }

    public static String formatTime()
    {
        return formatTime(getCurrentSeconds());
    }

    public static String formatTime(Long seconds) {
        return formatTime(seconds, 1);
    }

    /**
     * Converts seconds into days, hours, minutes, seconds format
     *
     * @param seconds
     * @param depth Sets maximum levels of precision should be displayed.
     *              i.e. formatTime(3662, 1) will return 1 hour, 1 minute
     *              whereas formatTime(3662, 1) will return 1 hour, 1 minute, 2 seconds
     * @return
     */
    public static String formatTime(Long seconds, int depth) {
        if (seconds == null || seconds < 5) {
            return "moments";
        }

        if (seconds < 60) {
            return seconds + " seconds";
        }

        if (seconds < 3600) {
            Long count = (long) Math.ceil(seconds / 60);
            String res;
            if (count > 1) {
                res = count + " minutes";
            } else {
                res = "1 minute";
            }
            Long remaining = seconds % 60;
            if (depth > 0 && remaining >= 5) {
                return res + ", " + formatTime(remaining, --depth);
            }
            return res;
        }
        if (seconds < 86400) {
            Long count = (long) Math.ceil(seconds / 3600);
            String res;
            if (count > 1) {
                res = count + " hours";
            } else {
                res = "1 hour";
            }
            if (depth > 0) {
                return res + ", " + formatTime(seconds % 3600, --depth);
            }
            return res;
        }
        Long count = (long) Math.ceil(seconds / 86400); //Because 5 day teleport delay is needed.
        String res;
        if (count > 1) {
            res = count + " days";
        } else {
            res = "1 day";
        }
        if (depth > 0) {
            return res + ", " + formatTime(seconds % 86400, --depth);
        }
        return res;
    }

    public static long getCurrentSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    @Deprecated
    public static long getEpoch()
    {
        return getCurrentSeconds();
    }

    /**
     * Loads a yaml file with the specified pathSeparator
     * @param plugin
     * @param fileName
     * @param pathSeparator
     * @return
     */
    public static YamlConfiguration loadOrCreateYamlFile(Plugin plugin, String fileName, char pathSeparator)
    {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.options().pathSeparator(pathSeparator);
        File storageFile = new File(plugin.getDataFolder(), fileName);
        try
        {
            yamlConfiguration.load(storageFile);
        }
        catch (InvalidConfigurationException | IOException ignored) {}
        return yamlConfiguration;
    }

    /**
     * Lazy way to get a yamlconfiguration without having to make a file object
     * @param plugin
     * @param fileName
     * @return
     * @deprecated Literally can be a one-liner in your own plugin
     *             YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "/file.yml");
     */
    @Deprecated
    public static YamlConfiguration loadOrCreateYamlFile(Plugin plugin, String fileName)
    {
        File storageFile = new File(plugin.getDataFolder(), fileName);
        return YamlConfiguration.loadConfiguration(storageFile);
    }



    /**
     * @see UsefulUtil#saveStringToFile(Plugin, File, String)
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
                //delete file if empty
                if (contents == null || contents.isEmpty())
                {
                    storageFile.delete();
                }

                try
                {
                    storageFile.getParentFile().mkdirs();
                    storageFile.delete(); //always overwrite file
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

    /**
     * @see UsefulUtil#saveStringToFile(Plugin, String, String)
     * @param plugin
     * @param fileName
     * @param yaml
     * @deprecated use saveStringToFile(plugin, "filename.yml", yaml.saveToString());
     */
    @Deprecated
    public static void saveYamlFile(Plugin plugin, String fileName, YamlConfiguration yaml)
    {
        saveStringToFile(plugin, fileName, yaml.saveToString());
    }

    /**
     * Sets a source to a TNTPrimed entity
     *
     * TNTPrimed allows you to get the source, but there's no way to set one
     * This method allows you to do that though.
     *
     * @param tnt TNTPrimed entity you wish to apply a source to
     * @param source the LivingEntity to define as the source of this TNTPrimed entity
     * @return the tnt object, for chaining or whatever
     */
    public static TNTPrimed tntSetSource(TNTPrimed tnt, LivingEntity source)
    {
        try
        {
            final Class<? extends TNTPrimed> tntClass = tnt.getClass();
            Method getHandle = tntClass.getMethod("getHandle");
            final Object nmsTNT = getHandle.invoke(tnt);
            Field f = nmsTNT.getClass().getDeclaredField("source");
            f.setAccessible(true);

            final Class<? extends LivingEntity> sourceClass = source.getClass();
            getHandle = sourceClass.getMethod("getHandle");
            final Object nmsEntity = getHandle.invoke(source);

            f.set(nmsTNT, nmsEntity);

            return tnt;
        }
        catch (Exception e)
        {
            log("Unable to set source for TNT.");
            e.printStackTrace();
            return tnt;
        }
    }
}
