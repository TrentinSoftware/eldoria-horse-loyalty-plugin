package org.horse_loyalty;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class HorseProtectListener implements Listener {

    private final LoyaltyManager manager;

    public HorseProtectListener(LoyaltyManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return; // só mão principal
        if (!(event.getRightClicked() instanceof Horse horse)) return;
        Player player = event.getPlayer();

        // Impede montar se lealdade >= exclusive-mount-level e jogador não é dono
        if (manager.getLoyalty(horse) >= manager.getExclusiveMountLevel() && !isOwner(player, horse)) {
            event.setCancelled(true);
            player.sendMessage("§cEste cavalo é leal apenas ao seu dono.");
        }
    }

    @EventHandler
    public void onMount(EntityMountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getMount() instanceof Horse horse)) return;

        if (manager.getLoyalty(horse) >= manager.getExclusiveMountLevel() && !isOwner(player, horse)) {
            event.setCancelled(true);
            player.sendMessage("§cEste cavalo não permite que você monte.");
        }
    }

    @EventHandler
    public void onMountApplySpeed(EntityMountEvent event) {
        if (event.getMount() instanceof Horse horse) {
            manager.applySpeed(horse);
        }
    }

    private boolean isOwner(Player player, Horse horse) {
        // O dono é o jogador que domou o cavalo (getOwner())
        return horse.isTamed() && horse.getOwner() != null && horse.getOwner().getUniqueId().equals(player.getUniqueId());
    }
}