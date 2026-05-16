package org.horse_loyalty;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class LoyaltyManager {

    private final JavaPlugin plugin;
    private final NamespacedKey loyaltyKey;
    private final NamespacedKey xpKey;
    private final NamespacedKey originalSpeedKey;

    public LoyaltyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.loyaltyKey = new NamespacedKey(plugin, "horse_loyalty");
        this.xpKey = new NamespacedKey(plugin, "horse_xp");
        this.originalSpeedKey = new NamespacedKey(plugin, "original_speed");
    }

    // ---------- LEALDADE (NÍVEL) ----------
    public int getLoyalty(Horse horse) {
        return horse.getPersistentDataContainer()
                .getOrDefault(loyaltyKey, PersistentDataType.INTEGER, 0);
    }

    public void setLoyalty(Horse horse, int level) {
        level = Math.max(0, Math.min(getMaxLevel(), level));
        horse.getPersistentDataContainer().set(loyaltyKey, PersistentDataType.INTEGER, level);
        applySpeed(horse);
    }

    // ---------- EXPERIÊNCIA ----------
    public int getXP(Horse horse) {
        return horse.getPersistentDataContainer()
                .getOrDefault(xpKey, PersistentDataType.INTEGER, 0);
    }

    public void setXP(Horse horse, int xp) {
        horse.getPersistentDataContainer().set(xpKey, PersistentDataType.INTEGER, xp);
    }

    /**
     * Adiciona XP e verifica se o cavalo sobe de nível.
     */
    public void addXP(Horse horse, int amount, Player player) {
        int currentXP = getXP(horse);
        int newXP = currentXP + amount;
        int currentLevel = getLoyalty(horse);
        int maxLevel = getMaxLevel();
        int nextLevel = currentLevel + 1;
        if (nextLevel > maxLevel) {
            setXP(horse, 0);
            return;
        }

        int requiredXP = getRequiredXPForLevel(nextLevel);
        while (newXP >= requiredXP && currentLevel < maxLevel) {
            newXP -= requiredXP;
            currentLevel++;
            setLoyalty(horse, currentLevel);
            // Som e mensagem
            if (player != null) {
                player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("level-up-sound")), 1.0f, 1.0f);
                String msg = plugin.getConfig().getString("level-up-message").replace("%level%", String.valueOf(currentLevel));
                player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', msg));
            }
            // Nome automático
            if (plugin.getConfig().getBoolean("auto-name.enabled") &&
                    currentLevel == plugin.getConfig().getInt("auto-name.level")) {
                if (horse.getCustomName() == null) {
                    String autoName = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("auto-name.name"));
                    horse.setCustomName(autoName);
                    horse.setCustomNameVisible(true);
                }
            }
            if (currentLevel >= maxLevel) {
                newXP = 0;
                break;
            }
            requiredXP = getRequiredXPForLevel(currentLevel + 1);
        }
        setXP(horse, newXP);
    }

    private int getRequiredXPForLevel(int level) {
        var list = plugin.getConfig().getIntegerList("level-xp-requirements");
        if (level < 1 || level > list.size()) return Integer.MAX_VALUE;
        return list.get(level - 1);
    }

    // ---------- VELOCIDADE COM PRESERVAÇÃO DA GENÉTICA ----------
    public void applySpeed(Horse horse) {
        // Armazena a velocidade original uma única vez
        if (!horse.getPersistentDataContainer().has(originalSpeedKey, PersistentDataType.DOUBLE)) {
            double current = horse.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
            horse.getPersistentDataContainer().set(originalSpeedKey, PersistentDataType.DOUBLE, current);
        }
        double original = horse.getPersistentDataContainer().get(originalSpeedKey, PersistentDataType.DOUBLE);
        int loyalty = getLoyalty(horse);
        double extra = loyalty * 0.1;
        horse.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(original + extra);
    }

    // ---------- PROPRIEDADE ----------
    public boolean isOwner(Player player, Horse horse) {
        return horse.isTamed() && horse.getOwner() != null && horse.getOwner().getUniqueId().equals(player.getUniqueId());
    }

    // ---------- CONFIGURAÇÕES DINÂMICAS ----------
    public int getMaxLevel() {
        return plugin.getConfig().getInt("max-level", 10);
    }

    public int getExclusiveMountLevel() {
        return plugin.getConfig().getInt("exclusive-mount-level", 5);
    }

    public int getCallLevel() {
        return plugin.getConfig().getInt("call-level", 8);
    }

    public int getFavoriteLevel() {
        return plugin.getConfig().getInt("favorite-level", 10);
    }
}