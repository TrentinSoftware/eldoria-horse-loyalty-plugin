package org.horse_loyalty;

import org.bukkit.Location;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HorseRidingListener implements Listener {

    private final HorseLoyaltyPlugin plugin;
    private final LoyaltyManager manager;
    private final Map<UUID, Long> lastSecondXP = new HashMap<>();
    private final Map<UUID, Location> lastBlockCheck = new HashMap<>();

    public HorseRidingListener(HorseLoyaltyPlugin plugin, LoyaltyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onMount(EntityMountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getMount() instanceof Horse horse)) return;
        if (!horse.isTamed() || !manager.isOwner(player, horse)) return;

        lastSecondXP.put(player.getUniqueId(), System.currentTimeMillis());
        lastBlockCheck.put(player.getUniqueId(), player.getLocation());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isInsideVehicle() || !(player.getVehicle() instanceof Horse horse)) return;
        if (!manager.isOwner(player, horse)) return;

        UUID id = player.getUniqueId();
        // XP por distância
        if (lastBlockCheck.containsKey(id)) {
            Location from = lastBlockCheck.get(id);
            double dist = from.distance(event.getTo());
            int blocks = (int) dist;
            if (blocks > 0) {
                int xpPerBlock = plugin.getConfig().getInt("riding-xp-per-block", 10);
                int xpGained = blocks / xpPerBlock;
                if (xpGained > 0) {
                    manager.addXP(horse, xpGained, player);
                }
                lastBlockCheck.put(id, event.getTo()); // update posição
            }
        }

        // XP por tempo (a cada segundo)
        long now = System.currentTimeMillis();
        Long last = lastSecondXP.get(id);
        if (last == null) return;
        if (now - last >= 1000) {
            int xpPerSec = plugin.getConfig().getInt("riding-xp-per-second", 1);
            manager.addXP(horse, xpPerSec, player);
            lastSecondXP.put(id, now);
        }
    }
}