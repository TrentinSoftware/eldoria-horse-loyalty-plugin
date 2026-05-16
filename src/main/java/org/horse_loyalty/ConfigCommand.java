package org.horse_loyalty;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Handles the /horselealdade config subcommand.
 * Allows OPs to read/write plugin configuration at runtime.
 *
 * Usage:
 *   /horselealdade config get <key>
 *   /horselealdade config set <key> <value>
 *   /horselealdade config add xp-requirements <value>
 *   /horselealdade config remove xp-requirements <index>
 */
public class ConfigCommand {

    // Integer keys that can be get/set
    static final Set<String> INT_KEYS = Set.of(
            "max-level",
            "exclusive-mount-level",
            "call-level",
            "favorite-level",
            "riding-xp-per-second",
            "riding-xp-per-block",
            "feeding.apple",
            "feeding.golden_apple",
            "feeding.enchanted_golden_apple",
            "call-cooldown.base-seconds"
    );

    // Boolean keys that can be get/set with true/false
    static final Set<String> BOOL_KEYS = Set.of(
            "call-cooldown.enabled",
            "call-range.enabled"
    );

    // List keys that support add/remove
    static final Set<String> LIST_KEYS = Set.of(
            "level-xp-requirements",
            "call-range.range-per-level"
    );

    static final List<String> ALL_KEYS;
    static {
        ALL_KEYS = new ArrayList<>();
        ALL_KEYS.addAll(INT_KEYS);
        ALL_KEYS.addAll(BOOL_KEYS);
        ALL_KEYS.addAll(LIST_KEYS);
        ALL_KEYS.sort(String::compareTo);
    }

    private ConfigCommand() {}

    /**
     * Handles /horselealdade config <get|set|add|remove> <key> [value]
     *
     * @param plugin the plugin instance
     * @param player the player executing the command
     * @param args   the arguments starting from the subcommand (e.g. ["get", "max-level"])
     */
    public static void handle(HorseLoyaltyPlugin plugin, Player player, String[] args) {
        if (!player.hasPermission("lealdadecavalo.admin")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando.");
            return;
        }

        if (args.length < 2) {
            sendConfigHelp(player);
            return;
        }

        String action = args[0].toLowerCase();
        String key = args[1].toLowerCase();
        FileConfiguration config = plugin.getConfig();

        switch (action) {
            case "get" -> handleGet(player, config, key);
            case "set" -> {
                if (args.length < 3) {
                    player.sendMessage("§cUso: /lealdadecavalo config set <chave> <valor>");
                    return;
                }
                handleSet(plugin, player, config, key, args[2]);
            }
            case "add" -> {
                if (args.length < 3) {
                    player.sendMessage("§cUso: /lealdadecavalo config add <chave-lista> <valor>");
                    return;
                }
                handleAdd(plugin, player, config, key, args[2]);
            }
            case "remove" -> {
                if (args.length < 3) {
                    player.sendMessage("§cUso: /lealdadecavalo config remove <chave-lista> <índice>");
                    return;
                }
                handleRemove(plugin, player, config, key, args[2]);
            }
            default -> sendConfigHelp(player);
        }
    }

    private static void handleGet(Player player, FileConfiguration config, String key) {
        if (!INT_KEYS.contains(key) && !BOOL_KEYS.contains(key) && !LIST_KEYS.contains(key)) {
            player.sendMessage("§cChave desconhecida: §e" + key);
            player.sendMessage("§7Chaves válidas: " + String.join(", ", ALL_KEYS));
            return;
        }

        if (LIST_KEYS.contains(key)) {
            List<Integer> list = config.getIntegerList(key);
            player.sendMessage("§6" + key + "§7: " + list);
        } else if (BOOL_KEYS.contains(key)) {
            boolean value = config.getBoolean(key);
            player.sendMessage("§6" + key + "§7 = §e" + value);
        } else {
            int value = config.getInt(key, -1);
            player.sendMessage("§6" + key + "§7 = §e" + value);
        }
    }

    private static void handleSet(HorseLoyaltyPlugin plugin, Player player, FileConfiguration config,
                                   String key, String rawValue) {
        if (BOOL_KEYS.contains(key)) {
            if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false")) {
                player.sendMessage("§cValor inválido. Use §etrue§c ou §efalse§c.");
                return;
            }
            boolean value = Boolean.parseBoolean(rawValue);
            config.set(key, value);
            plugin.saveConfig();
            player.sendMessage("§a" + key + " §7definido para §e" + value + "§7.");
            return;
        }

        if (!INT_KEYS.contains(key)) {
            if (LIST_KEYS.contains(key)) {
                player.sendMessage("§cUse §e/lealdadecavalo config add/remove§c para listas.");
            } else {
                player.sendMessage("§cChave desconhecida: §e" + key);
                player.sendMessage("§7Chaves válidas: " + String.join(", ", ALL_KEYS));
            }
            return;
        }

        int value;
        try {
            value = Integer.parseInt(rawValue);
        } catch (NumberFormatException e) {
            player.sendMessage("§cValor inválido. Use um número inteiro.");
            return;
        }

        if (value < 0) {
            player.sendMessage("§cO valor não pode ser negativo.");
            return;
        }

        config.set(key, value);
        plugin.saveConfig();
        player.sendMessage("§a" + key + " §7definido para §e" + value + "§7.");
    }

    private static void handleAdd(HorseLoyaltyPlugin plugin, Player player, FileConfiguration config,
                                   String key, String rawValue) {
        if (!LIST_KEYS.contains(key)) {
            if (INT_KEYS.contains(key)) {
                player.sendMessage("§cUse §e/lealdadecavalo config set§c para valores simples.");
            } else {
                player.sendMessage("§cChave desconhecida: §e" + key);
            }
            return;
        }

        int value;
        try {
            value = Integer.parseInt(rawValue);
        } catch (NumberFormatException e) {
            player.sendMessage("§cValor inválido. Use um número inteiro.");
            return;
        }

        if (value <= 0) {
            player.sendMessage("§cO valor deve ser positivo.");
            return;
        }

        List<Integer> list = new ArrayList<>(config.getIntegerList(key));
        list.add(value);
        config.set(key, list);
        plugin.saveConfig();
        player.sendMessage("§aAdicionado §e" + value + "§a ao índice §e" + list.size() + "§a de §6" + key + "§a.");
    }

    private static void handleRemove(HorseLoyaltyPlugin plugin, Player player, FileConfiguration config,
                                      String key, String rawIndex) {
        if (!LIST_KEYS.contains(key)) {
            player.sendMessage("§cChave não é uma lista: §e" + key);
            return;
        }

        int index;
        try {
            index = Integer.parseInt(rawIndex);
        } catch (NumberFormatException e) {
            player.sendMessage("§cÍndice inválido. Use um número inteiro (começa em 1).");
            return;
        }

        List<Integer> list = new ArrayList<>(config.getIntegerList(key));
        if (index < 1 || index > list.size()) {
            player.sendMessage("§cÍndice fora do intervalo. A lista tem §e" + list.size() + "§c elemento(s).");
            return;
        }

        int removed = list.remove(index - 1);
        config.set(key, list);
        plugin.saveConfig();
        player.sendMessage("§aRemovido elemento §e" + removed + "§a do índice §e" + index + "§a de §6" + key + "§a.");
        player.sendMessage("§7Nova lista: " + list);
    }

    private static void sendConfigHelp(Player player) {
        player.sendMessage("§6=== Configuração Dinâmica ===");
        player.sendMessage("§a/lealdadecavalo config get <chave> §7- Ver valor atual");
        player.sendMessage("§a/lealdadecavalo config set <chave> <valor> §7- Definir valor (inteiro ou true/false)");
        player.sendMessage("§a/lealdadecavalo config add <chave-lista> <valor> §7- Adicionar à lista");
        player.sendMessage("§a/lealdadecavalo config remove <chave-lista> <índice> §7- Remover da lista");
        player.sendMessage("§7Chaves inteiras: " + String.join(", ", INT_KEYS.stream().sorted().toList()));
        player.sendMessage("§7Chaves booleanas: " + String.join(", ", BOOL_KEYS.stream().sorted().toList()));
        player.sendMessage("§7Chaves de lista: " + String.join(", ", LIST_KEYS.stream().sorted().toList()));
    }

    /** Tab-completion suggestions for args starting after "config" */
    public static List<String> tabComplete(String[] args) {
        // args[0] = action, args[1] = key, args[2] = value
        if (args.length == 1) {
            return filterPrefix(Arrays.asList("get", "set", "add", "remove"), args[0]);
        }
        if (args.length == 2) {
            String action = args[0].toLowerCase();
            if (action.equals("get") || action.equals("set")) {
                List<String> writable = new ArrayList<>();
                writable.addAll(INT_KEYS);
                writable.addAll(BOOL_KEYS);
                return filterPrefix(writable, args[1]);
            }
            if (action.equals("add") || action.equals("remove")) {
                return filterPrefix(new ArrayList<>(LIST_KEYS), args[1]);
            }
            return filterPrefix(ALL_KEYS, args[1]);
        }
        return List.of();
    }

    private static List<String> filterPrefix(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream().filter(s -> s.startsWith(lower)).toList();
    }
}
