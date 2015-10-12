package com.demigodsrpg.norsedemigods;

import com.demigodsrpg.norsedemigods.listener.DDamage;
import com.demigodsrpg.norsedemigods.listener.DDeities;
import com.demigodsrpg.norsedemigods.listener.DPvP;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DFixes implements Listener {
    private static final Set<EntityDamageEvent> important = Collections.synchronizedSet(new HashSet<>());
    public static final Set<EntityDamageEvent> processed = Collections.synchronizedSet(new HashSet<>());

    @EventHandler(priority = EventPriority.HIGH)
    private void onImportantDamage(EntityDamageEvent event) {
        if (isProcessed(event))
            return;
        if (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Player && DMisc.isFullParticipant((Player) ((EntityDamageByEntityEvent) event).getDamager()))
            important.add(event);
        else if (event instanceof EntityDamageByEntityEvent && (((EntityDamageByEntityEvent) event).getDamager() instanceof Fireball || ((EntityDamageByEntityEvent) event).getDamager() instanceof Arrow))
            important.add(event);
        else if (event.getEntity() instanceof Player && DMisc.isFullParticipant((Player) event.getEntity()))
            important.add(event);

        triggerDownstream(event);
    }

    public static void checkAndCancel(EntityDamageEvent event) {
        if (important.contains(event)) {
            event.setCancelled(true);
            important.remove(event);
        }
    }

    private static boolean isProcessed(EntityDamageEvent event) {
        return processed.contains(event);
    }

    public static void setLastDamage(LivingEntity target, EntityDamageEvent.DamageCause cause, double amount) {
        EntityDamageEvent damage = new EntityDamageEvent(target, cause, amount);
        processed.add(damage);
        target.setLastDamageCause(damage);
    }

    public static void setLastDamageBy(LivingEntity source, LivingEntity target, EntityDamageByEntityEvent.DamageCause cause, double amount) {
        EntityDamageByEntityEvent damageBy = new EntityDamageByEntityEvent(source, target, cause, amount);
        processed.add(damageBy);
        target.setLastDamageCause(damageBy);
    }

    private static void deityDamageImmunity(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if ((DMisc.hasDeity(p, "Fire Giant") || DMisc.hasDeity(p, "Dwarf")) && (event.getCause() == EntityDamageEvent.DamageCause.FIRE) || (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || event.getCause() == EntityDamageEvent.DamageCause.LAVA)) {
                p.setFireTicks(0);
                DFixes.checkAndCancel(event);
                return;
            } else if (DMisc.hasDeity(p, "Thor") && (event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING)) {
                DFixes.checkAndCancel(event);
                return;
            } else if (DMisc.hasDeity(p, "Jormungand") && event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
                DFixes.checkAndCancel(event);
                return;
            } else if (DMisc.hasDeity(p, "Thrymr")) {
                if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    double reduction = (double) Math.round(Math.pow(DMisc.getDevotion(p, "Thrymr"), 0.115));
                    if (reduction > event.getDamage()) reduction = event.getDamage();
                    event.setDamage(event.getDamage() - reduction);
                } else if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    if (DMisc.getActiveEffectsList(p.getUniqueId()).contains("Unburden"))
                        event.setDamage(event.getDamage() / 3.0);
                }
            }
            if (event instanceof EntityDamageByEntityEvent) {
                try {
                    if (DMisc.getActiveEffectsList(p.getUniqueId()).contains("Ceasefire")) {
                        DFixes.checkAndCancel(event);
                        return;
                    }
                } catch (Exception ignored) {
                }
                EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;
                if (damageByEntityEvent.getDamager() instanceof Player) {
                    Player damager = (Player) damageByEntityEvent.getDamager();
                    if (DMisc.isFullParticipant(p)) {
                        try {
                            if (DMisc.getActiveEffectsList(damager.getUniqueId()).contains("Ceasefire")) {
                                DFixes.checkAndCancel(event);
                                return;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                if (DMisc.hasDeity(p, "Hel") && (damageByEntityEvent.getDamager() instanceof Zombie) || (damageByEntityEvent.getDamager() instanceof Skeleton)) {
                    DFixes.checkAndCancel(event);
                }
            }
        }
    }

    private static void triggerDownstream(EntityDamageEvent event) {
        DFixes.deityDamageImmunity(event);
        if (event.isCancelled()) return;
        if (event instanceof EntityDamageByEntityEvent) DPvP.pvpDamage((EntityDamageByEntityEvent) event);
        DDamage.onDamage(event);
        DDeities.onEntityDamage(event);
    }

    public static boolean isNoob(Player player) {
        return DMisc.getDevotion(player) <= Setting.NOOB_LEVEL;
    }
}
