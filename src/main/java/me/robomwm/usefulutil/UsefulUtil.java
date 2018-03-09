package me.robomwm.usefulutil;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
        Bukkit.getLogger().severe("[" + UsefulUtil.class.getPackage().getName() + "] " + error);
    }

    /**
     * Returns whether the entity is a hostile/potentially hostile mob
     * Does not include monsters of the Flying class (notably ghast, enderdragon, etc.)
     * Flying extends LivingEntity. LivingEntity does not have getTarget(). Creatures do.
     * @param entity
     * @return
     */
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
        return formatTime(getEpoch());
    }

    public static String formatTime(Long seconds) {
        return formatTime(seconds, 1);
    }

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

    public static long getEpoch() {
        return System.currentTimeMillis() / 1000;
    }

    public static YamlConfiguration loadOrCreateYamlFile(JavaPlugin plugin, String fileName)
    {
        File storageFile = new File(plugin.getDataFolder(), fileName);
        return YamlConfiguration.loadConfiguration(storageFile);
    }

    public static boolean saveYamlFile(JavaPlugin plugin, String fileName, YamlConfiguration yaml)
    {
        File storageFile = new File(plugin.getDataFolder(), fileName);
        if (yaml.getKeys(false).isEmpty())
        {
            storageFile.delete();
            return true;
        }
        try
        {
            storageFile.createNewFile();
            yaml.save(storageFile);
        }
        catch (Exception e)
        {
            plugin.getLogger().severe("Could not save " + fileName);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void saveYamlFileDelayed(JavaPlugin plugin, String fileName, YamlConfiguration yaml)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                saveYamlFile(plugin, fileName, yaml);
            }
        }.runTask(plugin);
    }
}
