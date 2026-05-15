package org.horse_loyalty;

import org.bukkit.ChatColor;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class RenameChatListener implements Listener {

    private final HorseLoyaltyPlugin plugin;

    public RenameChatListener(HorseLoyaltyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (!HorseLealdadeCommand.pendingRename.containsKey(playerId)) return;

        event.setCancelled(true);
        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancelar")) {
            HorseLealdadeCommand.pendingRename.remove(playerId);
            player.sendMessage("§cRenomeação cancelada.");
            return;
        }

        if (message.length() > 16) {
            player.sendMessage("§cO nome deve ter no máximo 16 caracteres.");
            return;
        }

        UUID horseUUID = HorseLealdadeCommand.pendingRename.remove(playerId);

        // Executa a alteração do nome na thread principal
        new BukkitRunnable() {
            @Override
            public void run() {
                Horse horse = null;
                for (var world : plugin.getServer().getWorlds()) {
                    for (Horse h : world.getEntitiesByClass(Horse.class)) {
                        if (h.getUniqueId().equals(horseUUID)) {
                            horse = h;
                            break;
                        }
                    }
                    if (horse != null) break;
                }

                if (horse == null) {
                    player.sendMessage("§cCavalo não encontrado.");
                    return;
                }

                String coloredName = ChatColor.translateAlternateColorCodes('&', message);
                horse.setCustomName(coloredName);
                horse.setCustomNameVisible(true);
                player.sendMessage("§aCavalo renomeado para: " + coloredName);
            }
        }.runTask(plugin);
    }
}
