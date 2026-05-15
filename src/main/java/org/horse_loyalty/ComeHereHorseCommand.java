package org.horse_loyalty;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ComeHereHorseCommand implements CommandExecutor {

    private final NamespacedKey favoriteKey;

    public ComeHereHorseCommand(NamespacedKey favoriteKey) {
        this.favoriteKey = favoriteKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }
        String uuidStr = player.getPersistentDataContainer().get(favoriteKey, PersistentDataType.STRING);
        if (uuidStr == null) {
            player.sendMessage("§cVocê não tem um cavalo favorito. Use o catálogo para favoritar um.");
            return true;
        }
        UUID uuid = UUID.fromString(uuidStr);
        Horse horse = player.getServer().getWorlds().stream()
                .flatMap(w -> w.getEntitiesByClass(Horse.class).stream())
                .filter(h -> h.getUniqueId().equals(uuid))
                .findFirst().orElse(null);
        if (horse == null) {
            player.sendMessage("§cSeu cavalo favorito não foi encontrado.");
            return true;
        }
        horse.teleport(player.getLocation());
        player.sendMessage("§aSeu cavalo favorito veio até você!");
        return true;
    }
}
