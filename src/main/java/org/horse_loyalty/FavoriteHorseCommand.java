package org.horse_loyalty;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class FavoriteHorseCommand implements CommandExecutor {

    private final LoyaltyManager manager;
    private final NamespacedKey favoriteKey;

    public FavoriteHorseCommand(LoyaltyManager manager, NamespacedKey favoriteKey) {
        this.manager = manager;
        this.favoriteKey = favoriteKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("§cUse: /favoritehorse <UUID>");
            return true;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cUUID inválido.");
            return true;
        }
        Horse horse = player.getServer().getWorlds().stream()
                .flatMap(w -> w.getEntitiesByClass(Horse.class).stream())
                .filter(h -> h.getUniqueId().equals(uuid))
                .findFirst().orElse(null);
        if (horse == null) {
            player.sendMessage("§cCavalo não encontrado.");
            return true;
        }
        if (!manager.isOwner(player, horse)) {
            player.sendMessage("§cVocê não é o dono deste cavalo.");
            return true;
        }
        if (manager.getLoyalty(horse) < 10) {
            player.sendMessage("§cEste cavalo precisa de lealdade 10 para ser favoritado.");
            return true;
        }

        // Salva UUID no jogador
        player.getPersistentDataContainer().set(favoriteKey, PersistentDataType.STRING, uuid.toString());
        player.sendMessage("§aCavalo favoritado! Use /vemcapocoto para chamá-lo.");
        return true;
    }
}
