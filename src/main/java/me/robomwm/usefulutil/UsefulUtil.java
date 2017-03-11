package me.robomwm.usefulutil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

/**
 * Created on 2/22/2017.
 *
 * @author RoboMWM
 */
public final class UsefulUtil
{
    private UsefulUtil(){}

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
        {
            killer = ((EntityDamageByEntityEvent)entity.getLastDamageCause()).getDamager();

            //Killed by projectile
            if (killer != null && killer instanceof Projectile)
            {
                Projectile arrow = (Projectile)killer;
                if (arrow.getShooter() instanceof LivingEntity)
                    killer = (Entity) arrow.getShooter();
                if (deleteProjectile)
                    arrow.remove();
            }

            //Killed by TNT explosion
            else if (killer != null && killer instanceof TNTPrimed)
            {
                TNTPrimed tnt = (TNTPrimed)killer;
                killer = tnt.getSource();
            }
        }

        //TODO: track kills due to fire and other related environmental damage(?)

        return killer;
    }
}
