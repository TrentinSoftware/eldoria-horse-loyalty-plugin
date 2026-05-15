package org.horse_loyalty;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Horse;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class LoyaltyManager {

    private final NamespacedKey loyaltyKey;
    private final JavaPlugin plugin;

    public LoyaltyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.loyaltyKey = new NamespacedKey(plugin, "horse_loyalty");
    }

    public int getLoyalty(Horse horse) {
        return horse.getPersistentDataContainer()
                .getOrDefault(loyaltyKey, PersistentDataType.INTEGER, 0);
    }

    public void setLoyalty(Horse horse, int level) {
        level = Math.max(0, Math.min(10, level)); // limita entre 0 e 10
        horse.getPersistentDataContainer().set(loyaltyKey, PersistentDataType.INTEGER, level);
        // Aplica a velocidade imediatamente se já estiver montado
        applySpeed(horse);
    }

    public void applySpeed(Horse horse) {
        int loyalty = getLoyalty(horse);
        double baseSpeed = 0.175; // velocidade base comum de cavalo (ajuste se quiser)
        double extraSpeed = loyalty * 0.1;
        horse.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED)
                .setBaseValue(baseSpeed + extraSpeed);
    }
}