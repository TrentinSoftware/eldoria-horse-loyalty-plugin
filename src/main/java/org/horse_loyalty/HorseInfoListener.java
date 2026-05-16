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
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class HorseInfoListener implements Listener {

    private final LoyaltyManager manager;
    private final HorseLoyaltyPlugin plugin;
    private final NamespacedKey catalogKey;

    public HorseInfoListener(HorseLoyaltyPlugin plugin, LoyaltyManager manager, NamespacedKey catalogKey) {
        this.plugin = plugin;
        this.manager = manager;
        this.catalogKey = catalogKey;
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

        event.setCancelled(true);
        showHorseInfo(player, horse);
    }

    // Shift + clique direito com o livro de catálogo em um cavalo → abre/atualiza catálogo
    @EventHandler
    public void onCatalogBookClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (!(event.getRightClicked() instanceof Horse)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.WRITTEN_BOOK) return;
        BookMeta meta = (BookMeta) item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(catalogKey, PersistentDataType.BYTE)) return;

        event.setCancelled(true);
        HorseCatalogListener.refreshAndOpenBook(player, item, plugin, manager, catalogKey);
    }

    private void showHorseInfo(Player player, Horse horse) {
        if (!manager.isOwner(player, horse)) {
            player.sendMessage(Component.text("Você não é o dono deste cavalo.", NamedTextColor.RED));
            return;
        }

        int level = manager.getLoyalty(horse);
        int xp = manager.getXP(horse);
        int nextLevel = level + 1;
        int maxLevel = manager.getMaxLevel();
        String reqText;
        if (nextLevel > maxLevel) {
            reqText = "MAX";
        } else {
            int required = plugin.getConfig().getIntegerList("level-xp-requirements").get(nextLevel - 1);
            reqText = xp + "/" + required;
        }

        player.sendMessage(Component.text("=== Cavalo Leal ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Nível: " + level + "/" + maxLevel, NamedTextColor.AQUA));
        player.sendMessage(Component.text("XP: " + reqText, NamedTextColor.YELLOW));
    }
}
