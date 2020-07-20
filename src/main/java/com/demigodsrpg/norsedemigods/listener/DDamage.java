package com.demigodsrpg.norsedemigods.listener;

import com.demigodsrpg.norsedemigods.DFixes;
import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Setting;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DDamage implements Listener {
    /*
     * This handler deals with non-Demigods damage (all of that will go directly to DMiscUtil's built in damage function) and converts it
     * to Demigods HP, using individual multipliers for balance purposes.
     *
     * The adjusted value should be around/less than 1 to adjust for the increased health, but not ridiculous
     */

    public static void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (!DMisc.isFullParticipant(p)) return;
        if (!DMisc.canTarget(p, p.getLocation())) {
            DFixes.checkAndCancel(e);
            return;
        }

        if (e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e;
            if (ee.getDamager() instanceof Player) {
                if (!Setting.FRIENDLY_FIRE && DMisc.areAllied(p, (Player) ee.getDamager())) {
                    if (Setting.FRIENDLY_FIRE_WARNING)
                        ee.getDamager().sendMessage(ChatColor.YELLOW + "No friendly fire.");
                    DFixes.checkAndCancel(e);
                    return;
                }
                if (!DMisc.canTarget(ee.getDamager(), ee.getDamager().getLocation())) {
                    DFixes.checkAndCancel(e);
                    return;
                }
                DMisc.damageDemigods((Player) ee.getDamager(), p, e.getDamage(), DamageCause.ENTITY_ATTACK);
                return;
            } else if (ee.getDamager() instanceof Projectile && ((Projectile) ee.getDamager()).getShooter() instanceof Player) {
                Projectile projectile = (Projectile) ee.getDamager();
                if (projectile.hasMetadata("how_do_I_shot_web")) {
                    DFixes.checkAndCancel(e);
                    double damage = e.getDamage() * (DMisc.getAscensions((Player) projectile.getShooter()) + 2);
                    DMisc.damageDemigods((LivingEntity) projectile.getShooter(), p, p.getHealth() - damage >= 1 ? damage : -(1 - p.getHealth()), DamageCause.ENTITY_EXPLOSION);
                }
                return;
            }
        }

        if (e.getCause() == DamageCause.LAVA) {
            DFixes.checkAndCancel(e);
            return;
        }

        if ((e.getCause() != DamageCause.ENTITY_ATTACK) && (e.getCause() != DamageCause.PROJECTILE))
            DMisc.damageDemigodsNonCombat(p, e.getDamage(), e.getCause());
    }

    @SuppressWarnings("incomplete-switch")
    public static int armorReduction(Player p) {
        if (p.getLastDamageCause() != null)
            if ((p.getLastDamageCause().getCause() == DamageCause.FIRE) || (p.getLastDamageCause().getCause() == DamageCause.FIRE_TICK) || (p.getLastDamageCause().getCause() == DamageCause.SUFFOCATION) || (p.getLastDamageCause().getCause() == DamageCause.LAVA) || (p.getLastDamageCause().getCause() == DamageCause.DROWNING) || (p.getLastDamageCause().getCause() == DamageCause.STARVATION) || (p.getLastDamageCause().getCause() == DamageCause.FALL) || (p.getLastDamageCause().getCause() == DamageCause.VOID) || (p.getLastDamageCause().getCause() == DamageCause.POISON) || (p.getLastDamageCause().getCause() == DamageCause.MAGIC) || (p.getLastDamageCause().getCause() == DamageCause.SUICIDE)) {
                return 0;
            }
        double reduction = 0.0;
        if ((p.getInventory().getBoots() != null) && (p.getInventory().getBoots().getType() != Material.AIR)) {
            switch (p.getInventory().getBoots().getType()) {
                case LEATHER_BOOTS:
                    reduction += 0.3;
                    break;
                case IRON_BOOTS:
                    reduction += 0.6;
                    break;
                case GOLDEN_BOOTS:
                    reduction += 0.5;
                    break;
                case DIAMOND_BOOTS:
                    reduction += 0.8;
                    break;
                case CHAINMAIL_BOOTS:
                    reduction += 0.7;
                    break;
            }
            p.getInventory().getBoots().setDurability((short) (p.getInventory().getBoots().getDurability() + 1));
            if (p.getInventory().getBoots().getDurability() > p.getInventory().getBoots().getType().getMaxDurability())
                p.getInventory().setBoots(null);
        }
        if ((p.getInventory().getLeggings() != null) && (p.getInventory().getLeggings().getType() != Material.AIR)) {
            switch (p.getInventory().getLeggings().getType()) {
                case LEATHER_LEGGINGS:
                    reduction += 0.5;
                    break;
                case IRON_LEGGINGS:
                    reduction += 1;
                    break;
                case GOLDEN_LEGGINGS:
                    reduction += 0.8;
                    break;
                case DIAMOND_LEGGINGS:
                    reduction += 1.4;
                    break;
                case CHAINMAIL_LEGGINGS:
                    reduction += 1.1;
                    break;
            }
            p.getInventory().getLeggings().setDurability((short) (p.getInventory().getLeggings().getDurability() + 1));
            if (p.getInventory().getLeggings().getDurability() > p.getInventory().getLeggings().getType().getMaxDurability())
                p.getInventory().setLeggings(null);
        }
        if ((p.getInventory().getChestplate() != null) && (p.getInventory().getChestplate().getType() != Material.AIR)) {
            switch (p.getInventory().getChestplate().getType()) {
                case LEATHER_CHESTPLATE:
                    reduction += 0.8;
                    break;
                case IRON_CHESTPLATE:
                    reduction += 1.6;
                    break;
                case GOLDEN_CHESTPLATE:
                    reduction += 1.4;
                    break;
                case DIAMOND_CHESTPLATE:
                    reduction += 2;
                    break;
                case CHAINMAIL_CHESTPLATE:
                    reduction += 1.8;
                    break;
            }
            p.getInventory().getChestplate().setDurability((short) (p.getInventory().getChestplate().getDurability() + 1));
            if (p.getInventory().getChestplate().getDurability() > p.getInventory().getChestplate().getType().getMaxDurability())
                p.getInventory().setChestplate(null);
        }
        if ((p.getInventory().getHelmet() != null) && (p.getInventory().getHelmet().getType() != Material.AIR)) {
            switch (p.getInventory().getHelmet().getType()) {
                case LEATHER_HELMET:
                    reduction += 0.4;
                    break;
                case IRON_HELMET:
                    reduction += 0.8;
                    break;
                case GOLDEN_HELMET:
                    reduction += 0.7;
                    break;
                case DIAMOND_HELMET:
                    reduction += 1.3;
                    break;
                case CHAINMAIL_HELMET:
                    reduction += 1;
                    break;
            }
            p.getInventory().getHelmet().setDurability((short) (p.getInventory().getHelmet().getDurability() + 1));
            if (p.getInventory().getHelmet().getDurability() > p.getInventory().getHelmet().getType().getMaxDurability())
                p.getInventory().setHelmet(null);
        }
        return (int) (Math.round(reduction));
    }

    public static double specialReduction(Player p, double amount) {
        if (DMisc.getActiveEffectsList(p.getUniqueId()).isEmpty()) return amount;
        if (DMisc.getActiveEffectsList(p.getUniqueId()).contains("Invincible")) {
            amount *= 0.5;
        }
        if (DMisc.getActiveEffectsList(p.getUniqueId()).contains("Ceasefire")) {
            amount *= 0;
        }
        return amount;
    }
}
