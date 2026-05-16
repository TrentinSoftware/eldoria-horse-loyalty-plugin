Claro! Vou reunir **todo o contexto do projeto**, incluindo a estrutura final dos arquivos, o estado atual do código e as últimas correções que fizemos. Assim você pode compartilhar diretamente com outro agente.

---

## 📦 Projeto: HorseLoyalty (Plugin Paper 1.21.4)

**Objetivo:** Sistema de lealdade para cavalos (`Horse`) com níveis (0–10), bônus de velocidade, restrição de montaria por dono (nível 5), XP por alimentação/tempo/distância, catálogo de cavalos via livro, renomeação, chamada de cavalos (nível 8) e favoritos (nível 10).

**Ferramentas:** Java 21, Gradle, Paper API 1.21.4-R0.1-SNAPSHOT.

---

## 📁 Estrutura de arquivos no diretório do projeto

```
C:\Users\Guilherme Trentin\IdeaProjects\untitled\
├── build.gradle
├── settings.gradle
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── seuplugin/
        │           ├── HorseLoyaltyPlugin.java
        │           ├── LoyaltyManager.java
        │           ├── HorseLealdadeCommand.java
        │           ├── HorseCatalogCommand.java
        │           ├── CallHorseCommand.java
        │           ├── FavoriteHorseCommand.java
        │           ├── VemCaPocotoCommand.java
        │           ├── HorseProtectListener.java
        │           ├── HorseRidingListener.java
        │           ├── HorseFeedListener.java
        │           ├── HorseInfoListener.java
        │           ├── HorseCatalogListener.java
        │           └── RenameChatListener.java
        └── resources/
            ├── plugin.yml
            └── config.yml
```

---

## 🔧 build.gradle

```gradle
plugins {
    id 'java'
}

group = 'com.seuplugin'
version = '1.0.0'

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = "https://repo.papermc.io/repository/maven-public/"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

def targetJavaVersion = 21
java {
    def javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
        options.release.set(targetJavaVersion)
    }
}

processResources {
    def props = [version: version]
    inputs.properties props
    filteringCharset 'UTF-8'
    filesMatching('plugin.yml') {
        expand props
    }
}
```

---

## 🧾 plugin.yml

```yaml
name: HorseLoyalty
version: '${version}'
main: com.seuplugin.HorseLoyaltyPlugin
api-version: '1.21'
author: SeuNome
description: Sistema de lealdade para cavalos.
commands:
  horselealdade:
    description: Gerencia a lealdade de um cavalo
    usage: /horselealdade <get|set|item|rename|help>
    permission: horselealdade.admin
  horsecatalog:
    description: Obtém o catálogo de cavalos
    usage: /horsecatalog
    permission: horselealdade.catalog
  callhorse:
    description: Chama um cavalo específico (nível 8+)
    usage: /callhorse <UUID>
    permission: horselealdade.call
  favoritehorse:
    description: Favorita um cavalo (nível 10)
    usage: /favoritehorse <UUID>
    permission: horselealdade.favorite
  vemcapocoto:
    description: Chama o cavalo favorito
    usage: /vemcapocoto
    permission: horselealdade.favorite
permissions:
  horselealdade.admin:
    default: op
  horselealdade.catalog:
    default: true
  horselealdade.call:
    default: true
  horselealdade.favorite:
    default: true
```

---

## ⚙️ config.yml

```yaml
# Tempo e distância para ganhar XP montado
riding-xp-per-second: 1
riding-xp-per-block: 10

# XP por alimentação
feeding:
  apple: 1
  golden_apple: 20
  enchanted_golden_apple: 200

# XP necessário para cada nível (índice 0 = nível 1)
level-xp-requirements:
  - 20
  - 40
  - 60
  - 80
  - 100
  - 120
  - 140
  - 160
  - 180
  - 200

# Efeitos de level-up
level-up-sound: ENTITY_PLAYER_LEVELUP
level-up-message: "&bSeu cavalo subiu para o nível &l%level%&b!"

# Nome automático ao atingir determinado nível
auto-name:
  enabled: true
  level: 3
  name: "&eCavalo Leal"

# Comandos usados pelos botões do catálogo
call-command: "callhorse"
favorite-command: "favoritehorse"
rename-command: "horselealdade rename"

# Mensagens de ajuda
help-messages:
  - "&a/horselealdade get &7- Mostra lealdade e XP do cavalo"
  - "&a/horselealdade set <nível> &7- Define a lealdade (admin)"
  - "&a/horselealdade item &7- Obtém uma ficha de cavalo"
  - "&a/horselealdade rename <UUID> &7- Renomeia um cavalo (use após clicar no botão do catálogo)"
  - "&a/horsecatalog &7- Obtém o livro de catálogo"
  - "&a/callhorse <UUID> &7- Chama um cavalo (nível 8+)"
  - "&a/favoritehorse <UUID> &7- Favorita um cavalo (nível 10)"
  - "&a/vemcapocoto &7- Chama o cavalo favorito"
  - "&a/horselealdade help &7- Mostra esta ajuda"
```

---

## 🧠 Códigos Java (comentados e finais)

### 1. HorseLoyaltyPlugin.java (classe principal)

```java
package com.seuplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class HorseLoyaltyPlugin extends JavaPlugin {

    private LoyaltyManager loyaltyManager;
    private NamespacedKey catalogKey;
    private NamespacedKey favoriteKey;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // gera config.yml se não existir
        this.loyaltyManager = new LoyaltyManager(this);
        this.catalogKey = new NamespacedKey(this, "horse_catalog");
        this.favoriteKey = new NamespacedKey(this, "favorite_horse");

        // Comandos
        Objects.requireNonNull(getCommand("horselealdade")).setExecutor(new HorseLealdadeCommand(this, loyaltyManager));
        Objects.requireNonNull(getCommand("horsecatalog")).setExecutor(new HorseCatalogCommand(catalogKey));
        Objects.requireNonNull(getCommand("callhorse")).setExecutor(new CallHorseCommand(loyaltyManager));
        Objects.requireNonNull(getCommand("favoritehorse")).setExecutor(new FavoriteHorseCommand(loyaltyManager, favoriteKey));
        Objects.requireNonNull(getCommand("vemcapocoto")).setExecutor(new VemCaPocotoCommand(favoriteKey));

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
```

---

### 2. LoyaltyManager.java

```java
package com.seuplugin;

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

    // Nível
    public int getLoyalty(Horse horse) {
        return horse.getPersistentDataContainer().getOrDefault(loyaltyKey, PersistentDataType.INTEGER, 0);
    }

    public void setLoyalty(Horse horse, int level) {
        level = Math.max(0, Math.min(10, level));
        horse.getPersistentDataContainer().set(loyaltyKey, PersistentDataType.INTEGER, level);
        applySpeed(horse);
    }

    // XP
    public int getXP(Horse horse) {
        return horse.getPersistentDataContainer().getOrDefault(xpKey, PersistentDataType.INTEGER, 0);
    }

    public void setXP(Horse horse, int xp) {
        horse.getPersistentDataContainer().set(xpKey, PersistentDataType.INTEGER, xp);
    }

    public void addXP(Horse horse, int amount, Player player) {
        int currentXP = getXP(horse);
        int newXP = currentXP + amount;
        int currentLevel = getLoyalty(horse);
        int nextLevel = currentLevel + 1;
        if (nextLevel > 10) {
            setXP(horse, 0);
            return;
        }

        int requiredXP = getRequiredXPForLevel(nextLevel);
        while (newXP >= requiredXP && currentLevel < 10) {
            newXP -= requiredXP;
            currentLevel++;
            setLoyalty(horse, currentLevel);
            if (player != null) {
                player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("level-up-sound")), 1.0f, 1.0f);
                String msg = plugin.getConfig().getString("level-up-message").replace("%level%", String.valueOf(currentLevel));
                player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', msg));
            }
            // Nome automático
            if (plugin.getConfig().getBoolean("auto-name.enabled") && currentLevel == plugin.getConfig().getInt("auto-name.level")) {
                if (horse.getCustomName() == null) {
                    String autoName = org.bukkit.ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("auto-name.name"));
                    horse.setCustomName(autoName);
                    horse.setCustomNameVisible(true);
                }
            }
            if (currentLevel >= 10) {
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

    // Velocidade (preserva genética)
    public void applySpeed(Horse horse) {
        if (!horse.getPersistentDataContainer().has(originalSpeedKey, PersistentDataType.DOUBLE)) {
            double current = horse.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
            horse.getPersistentDataContainer().set(originalSpeedKey, PersistentDataType.DOUBLE, current);
        }
        double original = horse.getPersistentDataContainer().get(originalSpeedKey, PersistentDataType.DOUBLE);
        int loyalty = getLoyalty(horse);
        double extra = loyalty * 0.1;
        horse.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(original + extra);
    }

    // Dono
    public boolean isOwner(Player player, Horse horse) {
        return horse.isTamed() && horse.getOwner() != null && horse.getOwner().getUniqueId().equals(player.getUniqueId());
    }
}
```

---

### 3. HorseLealdadeCommand.java

```java
package com.seuplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class HorseLealdadeCommand implements CommandExecutor {

    private final LoyaltyManager manager;
    private final HorseLoyaltyPlugin plugin;
    public static final Map<UUID, UUID> pendingRename = new HashMap<>();

    public HorseLealdadeCommand(HorseLoyaltyPlugin plugin, LoyaltyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(player);
                return true;
            case "item":
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aFicha do Cavalo"));
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "Clique em um cavalo para ver suas informações."));
                NamespacedKey key = new NamespacedKey(plugin, "horse_info_item");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                paper.setItemMeta(meta);
                player.getInventory().addItem(paper);
                player.sendMessage("§aVocê recebeu uma Ficha do Cavalo.");
                return true;
            case "rename":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /horselealdade rename <UUID>");
                    return true;
                }
                UUID horseUUID;
                try {
                    horseUUID = UUID.fromString(args[1]);
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cUUID inválido.");
                    return true;
                }
                Horse target = null;
                for (var world : plugin.getServer().getWorlds()) {
                    for (Horse h : world.getEntitiesByClass(Horse.class)) {
                        if (h.getUniqueId().equals(horseUUID)) {
                            target = h;
                            break;
                        }
                    }
                    if (target != null) break;
                }
                if (target == null) {
                    player.sendMessage("§cCavalo não encontrado.");
                    return true;
                }
                if (!manager.isOwner(player, target)) {
                    player.sendMessage("§cVocê não é o dono deste cavalo.");
                    return true;
                }
                pendingRename.put(player.getUniqueId(), horseUUID);
                player.sendMessage("§aDigite no chat o novo nome para o cavalo. Digite 'cancelar' para cancelar.");
                return true;
        }

        Horse horse = getTargetHorse(player);
        if (horse == null) {
            player.sendMessage("§cVocê precisa estar olhando para um cavalo.");
            return true;
        }

        if (args[0].equalsIgnoreCase("get")) {
            int level = manager.getLoyalty(horse);
            int xp = manager.getXP(horse);
            int nextLevel = level + 1;
            String reqText;
            if (nextLevel > 10) {
                reqText = "MAX";
            } else {
                int required = plugin.getConfig().getIntegerList("level-xp-requirements").get(nextLevel - 1);
                reqText = xp + "/" + required;
            }
            player.sendMessage("§a=== Cavalo Leal ===");
            player.sendMessage("§bNível: " + level + "/10");
            player.sendMessage("§eXP: " + reqText);
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
                player.sendMessage("§cNível inválido.");
                return true;
            }
        }

        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6=== Ajuda HorseLoyalty ===");
        List<String> helpList = plugin.getConfig().getStringList("help-messages");
        if (helpList.isEmpty()) {
            player.sendMessage("§cNenhuma mensagem de ajuda configurada. Verifique o config.yml.");
            return;
        }
        for (String line : helpList) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    private Horse getTargetHorse(Player player) {
        var target = player.getTargetEntity(5);
        if (target instanceof Horse horse) {
            return horse;
        }
        return null;
    }
}
```

---

### 4. HorseCatalogCommand.java

```java
package com.seuplugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

public class HorseCatalogCommand implements CommandExecutor {

    private final NamespacedKey catalogKey;

    public HorseCatalogCommand(NamespacedKey catalogKey) {
        this.catalogKey = catalogKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("Catálogo de Cavalos");
        meta.setAuthor(player.getName());
        meta.addPage("Abra novamente para carregar seus cavalos...");
        meta.getPersistentDataContainer().set(catalogKey, PersistentDataType.BYTE, (byte)1);
        book.setItemMeta(meta);

        player.getInventory().addItem(book);
        player.sendMessage("§aVocê recebeu um Catálogo de Cavalos. Clique com o botão direito para atualizar.");
        return true;
    }
}
```

---

### 5. CallHorseCommand.java

```java
package com.seuplugin;

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
        if (manager.getLoyalty(horse) < 8) {
            player.sendMessage("§cEste cavalo precisa de lealdade 8 ou superior para ser chamado.");
            return true;
        }
        horse.teleport(player.getLocation());
        player.sendMessage("§aCavalo chamado com sucesso!");
        return true;
    }
}
```

---

### 6. FavoriteHorseCommand.java

```java
package com.seuplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class FavoriteHorseCommand implements CommandExecutor {

    private final LoyaltyManager manager;
    private final NamespacedKey favoriteKey;

    public FavoriteHorseCommand(LoyaltyManager manager, NamespacedKey favoriteKey) {
        this.manager = manager;
        this.favoriteKey = favoriteKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("§cUse: /favoritehorse <UUID>");
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
        if (manager.getLoyalty(horse) < 10) {
            player.sendMessage("§cEste cavalo precisa de lealdade 10 para ser favoritado.");
            return true;
        }

        player.getPersistentDataContainer().set(favoriteKey, PersistentDataType.STRING, uuid.toString());
        player.sendMessage("§aCavalo favoritado! Use /vemcapocoto para chamá-lo.");
        return true;
    }
}
```

---

### 7. VemCaPocotoCommand.java

```java
package com.seuplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class VemCaPocotoCommand implements CommandExecutor {

    private final NamespacedKey favoriteKey;

    public VemCaPocotoCommand(NamespacedKey favoriteKey) {
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
```

---

### 8. HorseProtectListener.java

```java
package com.seuplugin;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class HorseProtectListener implements Listener {

    private final LoyaltyManager manager;

    public HorseProtectListener(LoyaltyManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Horse horse)) return;
        Player player = event.getPlayer();

        if (manager.getLoyalty(horse) >= 5 && !manager.isOwner(player, horse)) {
            event.setCancelled(true);
            player.sendMessage("§cEste cavalo é leal apenas ao seu dono.");
        }
    }

    @EventHandler
    public void onMount(EntityMountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getMount() instanceof Horse horse)) return;

        if (manager.getLoyalty(horse) >= 5 && !manager.isOwner(player, horse)) {
            event.setCancelled(true);
            player.sendMessage("§cEste cavalo não permite que você monte.");
        }
    }
}
```

---

### 9. HorseRidingListener.java

```java
package com.seuplugin;

import org.bukkit.Location;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HorseRidingListener implements Listener {

    private final HorseLoyaltyPlugin plugin;
    private final LoyaltyManager manager;
    private final Map<UUID, Long> lastSecondXP = new HashMap<>();
    private final Map<UUID, Location> lastBlockCheck = new HashMap<>();

    public HorseRidingListener(HorseLoyaltyPlugin plugin, LoyaltyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onMount(EntityMountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getMount() instanceof Horse horse)) return;
        if (!horse.isTamed() || !manager.isOwner(player, horse)) return;

        lastSecondXP.put(player.getUniqueId(), System.currentTimeMillis());
        lastBlockCheck.put(player.getUniqueId(), player.getLocation());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isInsideVehicle() || !(player.getVehicle() instanceof Horse horse)) return;
        if (!manager.isOwner(player, horse)) return;

        UUID id = player.getUniqueId();
        if (lastBlockCheck.containsKey(id)) {
            Location from = lastBlockCheck.get(id);
            double dist = from.distance(event.getTo());
            int blocks = (int) dist;
            if (blocks > 0) {
                int xpPerBlock = plugin.getConfig().getInt("riding-xp-per-block", 10);
                int xpGained = blocks / xpPerBlock;
                if (xpGained > 0) {
                    manager.addXP(horse, xpGained, player);
                }
                lastBlockCheck.put(id, event.getTo());
            }
        }

        long now = System.currentTimeMillis();
        Long last = lastSecondXP.get(id);
        if (last == null) return;
        if (now - last >= 1000) {
            int xpPerSec = plugin.getConfig().getInt("riding-xp-per-second", 1);
            manager.addXP(horse, xpPerSec, player);
            lastSecondXP.put(id, now);
        }
    }
}
```

---

### 10. HorseFeedListener.java

```java
package com.seuplugin;

import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class HorseFeedListener implements Listener {

    private final HorseLoyaltyPlugin plugin;
    private final LoyaltyManager manager;

    public HorseFeedListener(HorseLoyaltyPlugin plugin, LoyaltyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onFeed(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Horse horse)) return;
        Player player = event.getPlayer();
        if (!horse.isTamed() || !manager.isOwner(player, horse)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        int xp = 0;
        if (item.getType() == Material.APPLE) {
            xp = plugin.getConfig().getInt("feeding.apple", 1);
        } else if (item.getType() == Material.GOLDEN_APPLE) {
            xp = plugin.getConfig().getInt("feeding.golden_apple", 20);
        } else if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            xp = plugin.getConfig().getInt("feeding.enchanted_golden_apple", 200);
        }

        if (xp > 0) {
            item.setAmount(item.getAmount() - 1);
            manager.addXP(horse, xp, player);
            event.setCancelled(true);
        }
    }
}
```

---

### 11. HorseInfoListener.java

```java
package com.seuplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class HorseInfoListener implements Listener {

    private final HorseLoyaltyPlugin plugin;
    private final LoyaltyManager manager;

    public HorseInfoListener(HorseLoyaltyPlugin plugin, LoyaltyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // Shift + left click (bater)
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Horse horse)) return;
        if (!player.isSneaking()) return;
        event.setCancelled(true);
        showHorseInfo(player, horse);
    }

    // Clique com o papel especial (Ficha do Cavalo)
    @EventHandler
    public void onPaperClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.PAPER) return;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "horse_info_item");
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) return;
        if (!(event.getRightClicked() instanceof Horse horse)) return;

        event.setCancelled(true); // bloqueia interação padrão
        showHorseInfo(player, horse);
    }

    private void showHorseInfo(Player player, Horse horse) {
        if (!manager.isOwner(player, horse)) {
            player.sendMessage(Component.text("Você não é o dono deste cavalo.", NamedTextColor.RED));
            return;
        }

        int level = manager.getLoyalty(horse);
        int xp = manager.getXP(horse);
        int nextLevel = level + 1;
        String reqText;
        if (nextLevel > 10) {
            reqText = "MAX";
        } else {
            int required = plugin.getConfig().getIntegerList("level-xp-requirements").get(nextLevel - 1);
            reqText = xp + "/" + required;
        }

        player.sendMessage(Component.text("=== Cavalo Leal ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Nível: " + level + "/10", NamedTextColor.AQUA));
        player.sendMessage(Component.text("XP: " + reqText, NamedTextColor.YELLOW));
    }
}
```

---

### 12. HorseCatalogListener.java (com botões de renomear, chamar e favoritar)

```java
package com.seuplugin;

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

        Player player = event.getPlayer();
        List<Horse> horses = getPlayersHorses(player);
        List<Component> pages = new ArrayList<>();

        if (horses.isEmpty()) {
            pages.add(Component.text("Você não possui cavalos domesticados."));
        } else {
            for (int i = 0; i < horses.size(); i++) {
                pages.add(buildHorsePage(horses.get(i), i+1));
            }
        }

        meta.pages(pages);
        item.setItemMeta(meta);
        player.openBook(item);
        event.setCancelled(true);
    }

    private Component buildHorsePage(Horse horse, int index) {
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
                .append(Component.text(level + "/10", NamedTextColor.AQUA))
                .append(Component.newline())
                .append(Component.text("XP: ", NamedTextColor.GRAY))
                .append(Component.text(xp + "/" + getRequiredForNext(level), NamedTextColor.YELLOW))
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

        // Botão de chamar (nível >= 8)
        if (level >= 8) {
            String callCmd = plugin.getConfig().getString("call-command") + " " + uuid;
            page = page.append(Component.text("[Chamar]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/" + callCmd))
                    .hoverEvent(Component.text("Clique para chamar este cavalo até você")));
            page = page.append(Component.newline());
        }

        // Botão de favoritar (nível >= 10)
        if (level >= 10) {
            String favCmd = plugin.getConfig().getString("favorite-command") + " " + uuid;
            page = page.append(Component.text("[Favoritar]", NamedTextColor.LIGHT_PURPLE)
                    .clickEvent(ClickEvent.runCommand("/" + favCmd))
                    .hoverEvent(Component.text("Favorita este cavalo. Use /vemcapocoto para chamá-lo.")));
        }

        return page;
    }

    private int getRequiredForNext(int currentLevel) {
        var list = plugin.getConfig().getIntegerList("level-xp-requirements");
        if (currentLevel + 1 > list.size()) return 0;
        return list.get(currentLevel); // índice 0 = nível 1
    }

    private List<Horse> getPlayersHorses(Player player) {
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
```

---

### 13. RenameChatListener.java (versão corrigida thread-safe)

```java
package com.seuplugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class RenameChatListener implements Listener {

    private final HorseLoyaltyPlugin plugin;

    public RenameChatListener(HorseLoyaltyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (!HorseLealdadeCommand.pendingRename.containsKey(playerId)) return;

        event.setCancelled(true);
        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancelar")) {
            HorseLealdadeCommand.pendingRename.remove(playerId);
            player.sendMessage("§cRenomeação cancelada.");
            return;
        }

        if (message.length() > 16) {
            player.sendMessage("§cO nome deve ter no máximo 16 caracteres.");
            return;
        }

        UUID horseUUID = HorseLealdadeCommand.pendingRename.remove(playerId);

        new BukkitRunnable() {
            @Override
            public void run() {
                Horse horse = null;
                for (var world : plugin.getServer().getWorlds()) {
                    for (Horse h : world.getEntitiesByClass(Horse.class)) {
                        if (h.getUniqueId().equals(horseUUID)) {
                            horse = h;
                            break;
                        }
                    }
                    if (horse != null) break;
                }

                if (horse == null) {
                    player.sendMessage("§cCavalo não encontrado.");
                    return;
                }

                String coloredName = ChatColor.translateAlternateColorCodes('&', message);
                horse.setCustomName(coloredName);
                horse.setCustomNameVisible(true);
                player.sendMessage("§aCavalo renomeado para: " + coloredName);
            }
        }.runTask(plugin);
    }
}
```

---

## 🚀 Como compilar e testar

1. **Compilar:** `.\gradlew build` no diretório do projeto.
2. **Jar gerado:** `build/libs/untitled-1.0.0.jar` (pode renomear à vontade).
3. **Servidor local:** Baixe Paper 1.21.4, crie um servidor local com `java -jar paper.jar nogui`, aceite o EULA, coloque o plugin na pasta `plugins`.
4. **OP:** No console do servidor, execute `op SeuNick`.
5. **Teste:** Use `/horselealdade help` para ver a ajuda (agora funcional). Com o catálogo, clique em `[Renomear]`, digite o nome no chat e veja o cavalo ser renomeado.

## ⚠️ Observações

- O bônus de **dois passageiros** (lealdade 10) não foi implementado; apenas esboçado.
- A interação com o livro é via **botão direito no ar/bloco**; a ficha de papel e shift+esquerda são formas independentes de ver informações.
- Toda configuração agora vem do `config.yml`, inclusive as mensagens de ajuda e os comandos usados pelos botões.

---

Esse é o estado final do projeto. Qualquer dúvida ou necessidade de ajuste, o novo agente pode retomar daqui.