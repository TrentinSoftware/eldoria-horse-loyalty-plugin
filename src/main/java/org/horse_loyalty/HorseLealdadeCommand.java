package org.horse_loyalty;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class HorseLealdadeCommand implements CommandExecutor {

    private final LoyaltyManager manager;
    private final HorseLoyaltyPlugin plugin;
    // Mapa para renomeação pendente: chave = UUID do jogador, valor = UUID do cavalo
    public static final Map<UUID, UUID> pendingRename = new HashMap<>();

    public HorseLealdadeCommand(HorseLoyaltyPlugin plugin, LoyaltyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        // Subcomandos que não exigem cavalo alvo
        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(player);
                return true;
            case "item":
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aFicha do Cavalo"));
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "Clique em um cavalo para ver suas informações."));
                NamespacedKey key = new NamespacedKey(plugin, "horse_info_item");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                paper.setItemMeta(meta);
                player.getInventory().addItem(paper);
                player.sendMessage("§aVocê recebeu uma Ficha do Cavalo.");
                return true;
            case "rename":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /horselealdade rename <UUID>");
                    return true;
                }
                UUID horseUUID;
                try {
                    horseUUID = UUID.fromString(args[1]);
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cUUID inválido.");
                    return true;
                }
                // Encontra o cavalo em todos os mundos
                Horse target = null;
                for (var world : plugin.getServer().getWorlds()) {
                    for (Horse h : world.getEntitiesByClass(Horse.class)) {
                        if (h.getUniqueId().equals(horseUUID)) {
                            target = h;
                            break;
                        }
                    }
                    if (target != null) break;
                }
                if (target == null) {
                    player.sendMessage("§cCavalo não encontrado.");
                    return true;
                }
                if (!manager.isOwner(player, target)) {
                    player.sendMessage("§cVocê não é o dono deste cavalo.");
                    return true;
                }
                pendingRename.put(player.getUniqueId(), horseUUID);
                player.sendMessage("§aDigite no chat o novo nome para o cavalo. Digite 'cancelar' para cancelar.");
                return true;
        }

        // Subcomandos que precisam de um cavalo olhado
        Horse horse = getTargetHorse(player);
        if (horse == null) {
            player.sendMessage("§cVocê precisa estar olhando para um cavalo.");
            return true;
        }

        if (args[0].equalsIgnoreCase("get")) {
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
            player.sendMessage("§a=== Cavalo Leal ===");
            player.sendMessage("§bNível: " + level + "/10");
            player.sendMessage("§eXP: " + reqText);
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 2) {
                player.sendMessage("§cUso: /horselealdade set <0-10>");
                return true;
            }
            try {
                int level = Integer.parseInt(args[1]);
                manager.setLoyalty(horse, level);
                player.sendMessage("§aLealdade definida para " + level + ".");
                return true;
            } catch (NumberFormatException e) {
                player.sendMessage("§cNível inválido.");
                return true;
            }
        }

        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6=== Ajuda HorseLoyalty ===");
        List<String> helpList = plugin.getConfig().getStringList("help-messages");
        if (helpList.isEmpty()) {
            player.sendMessage("§cNenhuma mensagem de ajuda configurada. Verifique o config.yml.");
            return;
        }
        for (String line : helpList) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    private Horse getTargetHorse(Player player) {
        var target = player.getTargetEntity(5);
        if (target instanceof Horse horse) {
            return horse;
        }
        return null;
    }
}
