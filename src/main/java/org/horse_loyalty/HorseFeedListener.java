package org.horse_loyalty;

import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class HorseFeedListener implements Listener {

    private final LoyaltyManager manager;
    private final HorseLoyaltyPlugin plugin;

    public HorseFeedListener(HorseLoyaltyPlugin plugin, LoyaltyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onFeed(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Horse horse)) return;
        Player player = event.getPlayer();
        if (!horse.isTamed() || !manager.isOwner(player, horse)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        int xp = 0;
        if (item.getType() == Material.APPLE) {
            xp = plugin.getConfig().getInt("feeding.apple", 1);
        } else if (item.getType() == Material.GOLDEN_APPLE) {
            xp = plugin.getConfig().getInt("feeding.golden_apple", 20);
        } else if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            xp = plugin.getConfig().getInt("feeding.enchanted_golden_apple", 200);
        }

        if (xp > 0) {
            // Consome o item (opcional: deixar o jogo normal acontecer e depois consumir)
            // Cancelamos o evento para evitar cura/breeding? Vamos deixar o evento normal e apenas adicionar XP.
            // Se quiser consumir o item, remova um da mão:
            item.setAmount(item.getAmount() - 1);
            manager.addXP(horse, xp, player);
            event.setCancelled(true); // evita que o cavalo seja alimentado também pelo vanilla (cura/breeding)
        }
    }
}