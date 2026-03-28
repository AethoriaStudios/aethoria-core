package com.aethoria.core;

import com.aethoria.core.chat.RankStyleService;
import com.aethoria.core.command.AethoriaCommand;
import com.aethoria.core.item.AethoriaItemFactory;
import com.aethoria.core.item.ItemKeys;
import com.aethoria.core.item.ItemRegistryService;
import com.aethoria.core.listener.ChatFormattingListener;
import com.aethoria.core.listener.ItemRestrictionListener;
import com.aethoria.core.listener.PlayerConnectionListener;
import com.aethoria.core.listener.PlayerNameFormattingListener;
import com.aethoria.core.listener.TestStaffAbilityListener;
import com.aethoria.core.listener.CombatProgressionListener;
import com.aethoria.core.service.ClassSwapService;
import com.aethoria.core.service.ClassItemSetService;
import com.aethoria.core.service.CurrencyService;
import com.aethoria.core.service.DungeonService;
import com.aethoria.core.service.GameplayStatService;
import com.aethoria.core.service.PlayerProfileService;
import com.aethoria.core.service.ProgressionService;
import com.aethoria.core.storage.MariaDbPlayerDataStore;
import com.aethoria.core.storage.PlayerDataStore;
import com.aethoria.core.storage.YamlPlayerDataStore;
import java.util.logging.Level;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class AethoriaCorePlugin extends JavaPlugin {
    private PlayerDataStore playerDataStore;
    private PlayerProfileService profileService;
    private CurrencyService currencyService;
    private ClassSwapService classSwapService;
    private DungeonService dungeonService;
    private ProgressionService progressionService;
    private GameplayStatService gameplayStatService;
    private ItemKeys itemKeys;
    private ItemRegistryService itemRegistryService;
    private AethoriaItemFactory itemFactory;
    private RankStyleService rankStyleService;
    private ClassItemSetService classItemSetService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        bootstrapServices();
        registerCommands();
        registerListeners();
        getLogger().info("Aethoria Core enabled.");
    }

    @Override
    public void onDisable() {
        shutdownServices();
        getLogger().info("Aethoria Core disabled.");
    }

    public ItemRegistryService.ReloadResult reloadAethoria() {
        reloadConfig();

        try {
            ItemRegistryService.ReloadResult result = itemRegistryService.reload();
            for (Player player : getServer().getOnlinePlayers()) {
                gameplayStatService.refreshLater(player);
            }
            return result;
        } catch (RuntimeException exception) {
            getLogger().log(Level.SEVERE, "Failed to reload Aethoria Core cleanly.", exception);
            return null;
        }
    }

    public PlayerProfileService getProfileService() {
        return profileService;
    }

    public CurrencyService getCurrencyService() {
        return currencyService;
    }

    public ClassSwapService getClassSwapService() {
        return classSwapService;
    }

    public DungeonService getDungeonService() {
        return dungeonService;
    }

    public ItemRegistryService getItemRegistryService() {
        return itemRegistryService;
    }

    public ProgressionService getProgressionService() {
        return progressionService;
    }

    public AethoriaItemFactory getItemFactory() {
        return itemFactory;
    }

    public GameplayStatService getGameplayStatService() {
        return gameplayStatService;
    }

    public ClassItemSetService getClassItemSetService() {
        return classItemSetService;
    }

    private void bootstrapServices() {
        itemKeys = new ItemKeys(this);
        itemRegistryService = new ItemRegistryService(this);
        itemRegistryService.initialize();
        classItemSetService = new ClassItemSetService(itemRegistryService);
        playerDataStore = createDataStore();
        profileService = new PlayerProfileService(this, playerDataStore);
        progressionService = new ProgressionService(profileService);
        currencyService = new CurrencyService(this, profileService);
        classSwapService = new ClassSwapService(this, profileService, currencyService);
        dungeonService = new DungeonService(this, profileService, currencyService);
        itemFactory = new AethoriaItemFactory(this, itemRegistryService, itemKeys);
        gameplayStatService = new GameplayStatService(this);
        rankStyleService = new RankStyleService();
        getLogger().info("Using " + playerDataStore.getStorageName() + " player data store.");
    }

    private PlayerDataStore createDataStore() {
        if (getConfig().getBoolean("database.enabled", false)) {
            MariaDbPlayerDataStore mariaDbStore = new MariaDbPlayerDataStore(this);
            try {
                mariaDbStore.initialize();
                return mariaDbStore;
            } catch (Exception exception) {
                getLogger().log(Level.SEVERE, "MariaDB store failed to initialize. Falling back to YAML storage.", exception);
                try {
                    mariaDbStore.close();
                } catch (Exception closeException) {
                    getLogger().log(Level.WARNING, "Failed to close MariaDB store after initialization error.", closeException);
                }
            }
        }

        YamlPlayerDataStore yamlStore = new YamlPlayerDataStore(this);
        try {
            yamlStore.initialize();
            return yamlStore;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not initialize YAML player data store.", exception);
        }
    }

    private void registerCommands() {
        PluginCommand command = getCommand("aethoria");
        if (command == null) {
            getLogger().warning("Command aethoria is missing from plugin.yml.");
            return;
        }

        AethoriaCommand executor = new AethoriaCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void registerListeners() {
        PlayerNameFormattingListener playerNameFormattingListener = new PlayerNameFormattingListener(this, rankStyleService);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(playerNameFormattingListener, this);
        getServer().getPluginManager().registerEvents(new ChatFormattingListener(rankStyleService), this);
        getServer().getPluginManager().registerEvents(new ItemRestrictionListener(this), this);
        getServer().getPluginManager().registerEvents(new TestStaffAbilityListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatProgressionListener(this), this);

        for (Player player : getServer().getOnlinePlayers()) {
            playerNameFormattingListener.apply(player);
        }
    }

    private void shutdownServices() {
        if (profileService != null) {
            profileService.saveAll();
        }

        if (playerDataStore != null) {
            try {
                playerDataStore.close();
            } catch (Exception exception) {
                getLogger().log(Level.WARNING, "Failed to close player data store cleanly.", exception);
            }
        }
    }
}
