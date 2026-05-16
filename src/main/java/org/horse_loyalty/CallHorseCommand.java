package org.horse_loyalty;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CallHorseCommand implements CommandExecutor {

    private final HorseLoyaltyPlugin plugin;
    private final LoyaltyManager manager;

    // playerUUID → timestamp (ms) do último uso bem-sucedido
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public CallHorseCommand(HorseLoyaltyPlugin plugin, LoyaltyManager manager) {
        this.plugin = plugin;
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

        // ── Cooldown ──────────────────────────────────────────────────────────────
        if (plugin.getConfig().getBoolean("call-cooldown.enabled", false)) {
            int cooldownSecs = plugin.getConfig().getInt("call-cooldown.base-seconds", 10);
            long cooldownMs = cooldownSecs * 1000L;
            long elapsed = System.currentTimeMillis() - cooldowns.getOrDefault(player.getUniqueId(), 0L);
            if (elapsed < cooldownMs) {
                long remaining = (cooldownMs - elapsed + 999) / 1000; // arredonda pra cima
                player.sendMessage("§cAguarde §e" + remaining + "§c segundo(s) para chamar outro cavalo.");
                return true;
            }
        }

        // ── Alcance ───────────────────────────────────────────────────────────────
        if (plugin.getConfig().getBoolean("call-range.enabled", true)) {
            if (!horse.getWorld().equals(player.getWorld())) {
                player.sendMessage("§cSeu cavalo está em outra dimensão e não pode ser chamado.");
                return true;
            }
            List<Integer> rangePerLevel = plugin.getConfig().getIntegerList("call-range.range-per-level");
            if (!rangePerLevel.isEmpty()) {
                int loyalty = manager.getLoyalty(horse);
                int idx = Math.max(0, Math.min(loyalty - 1, rangePerLevel.size() - 1));
                int maxRange = rangePerLevel.get(idx);
                double distance = horse.getLocation().distance(player.getLocation());
                if (distance > maxRange) {
                    player.sendMessage("§cEste cavalo está muito longe! Alcance máximo: §e" + maxRange
                            + "§c blocos. (Distância atual: §e" + (int) distance + "§c)");
                    return true;
                }
            }
        }

        // ── Teleporte ─────────────────────────────────────────────────────────────
        horse.teleport(player.getLocation());
        player.sendMessage("§aCavalo chamado com sucesso!");

        // Registra timestamp somente após sucesso
        if (plugin.getConfig().getBoolean("call-cooldown.enabled", false)) {
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
        return true;
    }
}
