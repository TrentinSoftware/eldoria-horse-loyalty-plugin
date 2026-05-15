package org.horse_loyalty;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class HorseInfoListener implements Listener {

    private final LoyaltyManager manager;
    private final HorseLoyaltyPlugin plugin;

    public HorseInfoListener(HorseLoyaltyPlugin plugin, LoyaltyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // Shift + left click (dano)
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Horse horse)) return;
        if (!player.isSneaking()) return;
        event.setCancelled(true);
        showHorseInfo(player, horse);
    }

    // Clique com o papel especial
    @EventHandler
    public void onPaperClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.PAPER) return;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "horse_info_item");
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) return;
        if (!(event.getRightClicked() instanceof Horse horse)) return;

        event.setCancelled(true); // bloqueia interação normal (montar etc)
        showHorseInfo(player, horse);
    }

    private void showHorseInfo(Player player, Horse horse) {
        if (!manager.isOwner(player, horse)) {
            player.sendMessage(Component.text("Você não é o dono deste cavalo.", NamedTextColor.RED));
            return;
        }

        int level = manager.getLoyalty(horse);
        int xp = manager.getXP(horse);
        int nextLevel = level + 1;
        String reqText;
        if (nextLevel > 10) {
            reqText = "MAX";
        } else {
            int required = plugin.getConfig().getIntegerList("level-xp-requirements").get(nextLevel - 1);
            reqText = xp + "/" + required;
        }

        player.sendMessage(Component.text("=== Cavalo Leal ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Nível: " + level + "/10", NamedTextColor.AQUA));
        player.sendMessage(Component.text("XP: " + reqText, NamedTextColor.YELLOW));
    }
}
