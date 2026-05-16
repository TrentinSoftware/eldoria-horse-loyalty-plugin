package org.horse_loyalty;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class HorseCatalogListener implements Listener {

    private final NamespacedKey catalogKey;
    private final LoyaltyManager manager;
    private final HorseLoyaltyPlugin plugin;

    public HorseCatalogListener(HorseLoyaltyPlugin plugin, LoyaltyManager manager, NamespacedKey catalogKey) {
        this.plugin = plugin;
        this.manager = manager;
        this.catalogKey = catalogKey;
    }

    @EventHandler
    public void onBookOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() != org.bukkit.Material.WRITTEN_BOOK) return;
        BookMeta meta = (BookMeta) item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(catalogKey, PersistentDataType.BYTE)) return;

        event.setCancelled(true);
        refreshAndOpenBook(event.getPlayer(), item, plugin, manager, catalogKey);
    }

    /**
     * Atualiza as páginas do livro de catálogo e o abre para o jogador.
     * Pode ser chamado de outros listeners (ex: shift+clique no cavalo).
     */
    public static void refreshAndOpenBook(Player player, ItemStack book, HorseLoyaltyPlugin plugin,
                                          LoyaltyManager manager, NamespacedKey catalogKey) {
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) return;

        List<Horse> horses = getPlayersHorsesStatic(player, plugin);
        List<Component> pages = new ArrayList<>();

        if (horses.isEmpty()) {
            pages.add(Component.text("Você não possui cavalos domesticados."));
        } else {
            for (int i = 0; i < horses.size(); i++) {
                pages.add(buildHorsePageStatic(horses.get(i), i + 1, plugin, manager));
            }
        }

        meta.pages(pages);
        book.setItemMeta(meta);
        player.openBook(book);
    }

    private Component buildHorsePage(Horse horse, int index) {
        return buildHorsePageStatic(horse, index, plugin, manager);
    }

    private static Component buildHorsePageStatic(Horse horse, int index, HorseLoyaltyPlugin plugin, LoyaltyManager manager) {
        String name = horse.getCustomName() != null ? horse.getCustomName() : "Cavalo #" + index;
        int level = manager.getLoyalty(horse);
        int xp = manager.getXP(horse);
        double speed = horse.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
        double jump = horse.getAttribute(Attribute.JUMP_STRENGTH).getBaseValue();
        double maxHealth = horse.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        String ownerName = horse.getOwner() != null ? horse.getOwner().getName() : "Ninguém";
        String uuid = horse.getUniqueId().toString();

        Component page = Component.empty()
                .append(Component.text(name, NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("Dono: ", NamedTextColor.GRAY))
                .append(Component.text(ownerName, NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("Lealdade: ", NamedTextColor.GRAY))
                .append(Component.text(level + "/" + manager.getMaxLevel(), NamedTextColor.AQUA))
                .append(Component.newline())
                .append(Component.text("XP: ", NamedTextColor.GRAY))
                .append(Component.text(xp + "/" + getRequiredForNextStatic(level, plugin), NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.text("Velocidade: ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.3f", speed), NamedTextColor.GREEN))
                .append(Component.newline())
                .append(Component.text("Pulo: ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.3f", jump), NamedTextColor.GREEN))
                .append(Component.newline())
                .append(Component.text("Vida: ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.1f", maxHealth), NamedTextColor.RED))
                .append(Component.newline());

        // Botão de renomear (sempre disponível para o dono)
        String renameCmd = plugin.getConfig().getString("rename-command") + " " + uuid;
        page = page.append(Component.text("[Renomear]", NamedTextColor.GOLD)
                .clickEvent(ClickEvent.runCommand("/" + renameCmd))
                .hoverEvent(Component.text("Clique para renomear este cavalo")));
        page = page.append(Component.newline());

        // Botão de chamar (nível >= call-level)
        if (level >= manager.getCallLevel()) {
            String callCmd = plugin.getConfig().getString("call-command") + " " + uuid;
            page = page.append(Component.text("[Chamar]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/" + callCmd))
                    .hoverEvent(Component.text("Clique para chamar este cavalo até você")));
            page = page.append(Component.newline());
        }

        // Botão de favoritar (nível >= favorite-level)
        if (level >= manager.getFavoriteLevel()) {
            String favCmd = plugin.getConfig().getString("favorite-command") + " " + uuid;
            page = page.append(Component.text("[Favoritar]", NamedTextColor.LIGHT_PURPLE)
                    .clickEvent(ClickEvent.runCommand("/" + favCmd))
                    .hoverEvent(Component.text("Clique para favoritar este cavalo. Use /vemcapocoto para chamá-lo.")));
        }

        return page;
    }

    private int getRequiredForNext(int currentLevel) {
        return getRequiredForNextStatic(currentLevel, plugin);
    }

    private static int getRequiredForNextStatic(int currentLevel, HorseLoyaltyPlugin plugin) {
        var list = plugin.getConfig().getIntegerList("level-xp-requirements");
        if (currentLevel + 1 > list.size()) return 0;
        return list.get(currentLevel); // índice 0 = lvl 1
    }

    private List<Horse> getPlayersHorses(Player player) {
        return getPlayersHorsesStatic(player, plugin);
    }

    private static List<Horse> getPlayersHorsesStatic(Player player, HorseLoyaltyPlugin plugin) {
        List<Horse> result = new ArrayList<>();
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Horse horse && horse.isTamed() && horse.getOwner() != null &&
                        horse.getOwner().getUniqueId().equals(player.getUniqueId())) {
                    result.add(horse);
                }
            }
        }
        return result;
    }
}
