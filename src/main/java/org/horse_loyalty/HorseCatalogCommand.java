package org.horse_loyalty;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

public class HorseCatalogCommand implements CommandExecutor {

    private final NamespacedKey catalogKey;

    public HorseCatalogCommand(NamespacedKey catalogKey) {
        this.catalogKey = catalogKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("Catálogo de Cavalos");
        meta.setAuthor(player.getName());
        meta.addPage("Abra novamente para carregar seus cavalos...");
        meta.getPersistentDataContainer().set(catalogKey, PersistentDataType.BYTE, (byte)1);
        book.setItemMeta(meta);

        player.getInventory().addItem(book);
        player.sendMessage("§aVocê recebeu um Catálogo de Cavalos. Clique com o botão direito para atualizar.");
        return true;
    }
}
