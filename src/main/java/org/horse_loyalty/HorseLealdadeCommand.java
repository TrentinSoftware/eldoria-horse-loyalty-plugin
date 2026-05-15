package org.horse_loyalty;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

public class HorseLealdadeCommand implements CommandExecutor {

    private final LoyaltyManager manager;

    public HorseLealdadeCommand(LoyaltyManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            return false;
        }

        // Verifica se está olhando para um cavalo (alcance ~5 blocos)
        Horse horse = getTargetHorse(player);
        if (horse == null) {
            player.sendMessage("§cVocê precisa estar olhando para um cavalo.");
            return true;
        }

        if (args[0].equalsIgnoreCase("get")) {
            int level = manager.getLoyalty(horse);
            player.sendMessage("§aLealdade do cavalo: " + level + "/10");
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
                player.sendMessage("§cNível inválido. Use um número de 0 a 10.");
                return true;
            }
        }

        return false;
    }

    private Horse getTargetHorse(Player player) {
        var target = player.getTargetEntity(5);
        if (target instanceof Horse horse) {
            return horse;
        }
        return null;
    }
}
