package org.horse_loyalty;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class HorseLoyaltyPlugin extends JavaPlugin {

    private LoyaltyManager loyaltyManager;
    private NamespacedKey catalogKey;
    private NamespacedKey favoriteKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.loyaltyManager = new LoyaltyManager(this);
        this.catalogKey = new NamespacedKey(this, "horse_catalog");
        this.favoriteKey = new NamespacedKey(this, "favorite_horse");

        // Comandos
        Objects.requireNonNull(getCommand("horselealdade")).setExecutor(new HorseLealdadeCommand(this, loyaltyManager));
        Objects.requireNonNull(getCommand("horsecatalog")).setExecutor(new HorseCatalogCommand(catalogKey));
        Objects.requireNonNull(getCommand("callhorse")).setExecutor(new CallHorseCommand(loyaltyManager));
        Objects.requireNonNull(getCommand("favoritehorse")).setExecutor(new FavoriteHorseCommand(loyaltyManager, favoriteKey));
        Objects.requireNonNull(getCommand("vemcapocoto")).setExecutor(new ComeHereHorseCommand(favoriteKey));

        // Listeners
        getServer().getPluginManager().registerEvents(new HorseProtectListener(loyaltyManager), this);
        getServer().getPluginManager().registerEvents(new HorseRidingListener(this, loyaltyManager), this);
        getServer().getPluginManager().registerEvents(new HorseFeedListener(this, loyaltyManager), this);
        getServer().getPluginManager().registerEvents(new HorseInfoListener(this, loyaltyManager), this);
        getServer().getPluginManager().registerEvents(new HorseCatalogListener(this, loyaltyManager, catalogKey), this);
        getServer().getPluginManager().registerEvents(new RenameChatListener(this), this);

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
