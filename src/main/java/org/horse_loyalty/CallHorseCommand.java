package org.horse_loyalty;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CallHorseCommand implements CommandExecutor {

    private final LoyaltyManager manager;

    public CallHorseCommand(LoyaltyManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("§cUse: /callhorse <UUID>");
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
        if (manager.getLoyalty(horse) < manager.getCallLevel()) {
            player.sendMessage("§cEste cavalo precisa de lealdade " + manager.getCallLevel() + " ou superior para ser chamado.");
            return true;
        }
        horse.teleport(player.getLocation());
        player.sendMessage("§aCavalo chamado com sucesso!");
        return true;
    }
}
