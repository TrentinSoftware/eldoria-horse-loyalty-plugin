package org.horse_loyalty;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

public final class HorseLoyaltyPlugin extends JavaPlugin {

    private LoyaltyManager loyaltyManager;

    @Override
    public void onEnable() {
        this.loyaltyManager = new LoyaltyManager(this);
        Objects.requireNonNull(getCommand("horselealdade")).setExecutor(new HorseLealdadeCommand(loyaltyManager));
        getServer().getPluginManager().registerEvents(new HorseProtectListener(loyaltyManager), this);
        getLogger().info("HorseLoyalty ativado!");
    }

    @Override
    public void onDisable() {
        getLogger().info("HorseLoyalty desativado.");
    }

    public LoyaltyManager getLoyaltyManager() {
        return loyaltyManager;
    }
}