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

    private static YamlConfiguration inventorySnapshots;
    private static File inventorySnapshotsFile = new File(Bukkit.getWorldContainer().getPath() + File.separator + "plugins" + File.separator + "UsefulUtilData", "inventorySnapshots.data");

    private static void loadInventorySnapshots()
    {
        if (inventorySnapshots == null)
        {
            inventorySnapshotsFile.getParentFile().mkdirs();
            if (!inventorySnapshotsFile.exists())
            {
                try
                {
                    inventorySnapshotsFile.createNewFile();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }
            }
            inventorySnapshots = YamlConfiguration.loadConfiguration(inventorySnapshotsFile);
        }
    }

    private static void saveInventorySnapshots()
    {
        if (inventorySnapshots == null)
            return;
        try
        {
            inventorySnapshots.save(inventorySnapshotsFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

    private static ConfigurationSection getPlayerSnapshotSection(Player player)
    {
        loadInventorySnapshots();
        if (!inventorySnapshots.contains(player.getUniqueId().toString()))
            return inventorySnapshots.createSection(player.getUniqueId().toString());
        return inventorySnapshots.getConfigurationSection(player.getUniqueId().toString());
    }

    private static boolean deletePlayerSnapshotSection(Player player)
    {
        if (inventorySnapshots.contains(player.getUniqueId().toString()))
        {
            inventorySnapshots.set(player.getUniqueId().toString(), null);
            saveInventorySnapshots();
            return true;
        }
        return false;
    }

    public static boolean storeAndClearInventory(Player player)
    {
        player.closeInventory();

        ConfigurationSection snapshotSection = getPlayerSnapshotSection(player);
        if (snapshotSection.contains("items"))
            return false;

        snapshotSection.set("items", player.getInventory().getContents()); //ItemStack[]
        snapshotSection.set("armor", player.getInventory().getArmorContents()); //ItemStack[]
        snapshotSection.set("exp", player.getTotalExperience() + 1); //int //For our purposes, totalExperience is ok since experience can't be spent. We add 1 since exp can be more precise than an int...
        snapshotSection.set("health", player.getHealth()); //double
        snapshotSection.set("maxHealth", player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()); //double
        snapshotSection.set("foodLevel", player.getFoodLevel()); //int

        saveInventorySnapshots(); //TODO: schedule in a runnable instead (performance)? (Would need plugin instance)

        player.getInventory().clear();

        return true;
    }

    public static boolean restoreInventory(Player player)
    {
        player.closeInventory();

        ConfigurationSection snapshotSection = getPlayerSnapshotSection(player);
        if (!snapshotSection.contains("items"))
            return false;

        player.getInventory().setContents(snapshotSection.getList("items").toArray(new ItemStack[0]));
        player.getInventory().setArmorContents(snapshotSection.getList("armor").toArray(new ItemStack[0]));
        SetExpFix.setTotalExperience(player, snapshotSection.getInt("exp"));
        player.setHealth(snapshotSection.getDouble("health"));
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(snapshotSection.getDouble("maxHealth"));
        player.setFoodLevel(snapshotSection.getInt("foodLevel"));

        if (snapshotSection.contains("additionalExp"))
        {
            Bukkit.getPluginManager().callEvent(new PlayerExpChangeEvent(player, snapshotSection.getInt("additionalExp")));
        }

        deletePlayerSnapshotSection(player);

        return true;
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
        Long count = (long) Math.ceil(seconds / 86400); //Because 5 day teleport delay is needed. Jesus
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
}
