package com.reussy.exodus.bw1058winstreak;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.server.ServerType;
import com.reussy.exodus.bw1058winstreak.cache.StreakCache;
import com.reussy.exodus.bw1058winstreak.cache.StreakProperties;
import com.reussy.exodus.bw1058winstreak.commads.StreakAdminCommand;
import com.reussy.exodus.bw1058winstreak.commads.StreakCommand;
import com.reussy.exodus.bw1058winstreak.commads.StreakCommandProxy;
import com.reussy.exodus.bw1058winstreak.configuration.FilesManager;
import com.reussy.exodus.bw1058winstreak.database.DatabaseManager;
import com.reussy.exodus.bw1058winstreak.database.MySQL;
import com.reussy.exodus.bw1058winstreak.database.SQLite;
import com.reussy.exodus.bw1058winstreak.integrations.PlaceholderAPIBuilder;
import com.reussy.exodus.bw1058winstreak.listeners.InGameStreakProperties;
import com.reussy.exodus.bw1058winstreak.listeners.PlayerStreakProperties;
import com.reussy.exodus.bw1058winstreak.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class WinStreakPlugin extends JavaPlugin {

    String pluginName = "BedWars1058-WinStreak";
    String pluginVersion = getDescription().getVersion();
    private BedWars bedWars;
    private com.andrei1058.bedwars.proxy.api.BedWars bedWarsProxy;
    private DatabaseManager databaseManager;
    private FilesManager filesManager;
    private StreakCache streakCache;
    private MessageUtils messageUtils;

    @Override
    public void onEnable() {

        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&r ----------------------------------------------"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "  &7Enabling &f" + this.pluginName + " v" + this.pluginVersion + " &7..."));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&r "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "  &7Developed by&f reussy"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "  &7Running Java &f" + System.getProperty("java.version")));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "  &7Running &f" + Bukkit.getServer().getName() + " &7fork &fv" + Bukkit.getServer().getBukkitVersion()));
        this.filesManager = new FilesManager(this);
        this.streakCache = new StreakCache();
        initHooks();
        initDatabase();
        initEvents();
        initCommands();
        this.messageUtils = new MessageUtils();
    }

    @Override
    public void onDisable() {
        //Perform a task for save currently progress in case of any crash or similar
        for (Player player : Bukkit.getOnlinePlayers()) {
            StreakProperties streakProperties = getStreakCache().get(player.getUniqueId());
            getDatabaseManager().saveStreakProperties(streakProperties);
        }
    }

    public boolean isBedWars1058Present() {
        return Bukkit.getPluginManager().getPlugin("BedWars1058") != null;
    }

    public boolean isBedWarsProxyPresent() {
        return Bukkit.getPluginManager().getPlugin("BedWarsProxy") != null;
    }

    private void initHooks() {

        if (isBedWars1058Present()) {

            this.bedWars = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&r "));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "  &fBedWars1058 &7found and hooked successfully."));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&r "));

        } else if (isBedWarsProxyPresent()) {
            this.bedWarsProxy = Bukkit.getServicesManager().getRegistration(com.andrei1058.bedwars.proxy.api.BedWars.class).getProvider();
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&r "));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "  &fBedWarsProxy &7found and hooked successfully."));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&r "));
        } else {
            Bukkit.getLogger().severe("There is no BedWars plugin installed!");
            Bukkit.getLogger().severe("Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIBuilder(this).register();
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&r "));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "  &fPlaceholderAPI &7found and hooked successfully."));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&r "));
        }
    }

    private void initDatabase() {

        if (isBedWars1058Present()) {
            this.databaseManager = getBedWarsAPI().getConfigs().getMainConfig().getBoolean("database.enable") ? new MySQL(this) : new SQLite(this);
        } else if (isBedWarsProxyPresent()) {
            File proxyConfig = new File("plugins/BedWarsProxy/config.yml");
            YamlConfiguration configYaml = YamlConfiguration.loadConfiguration(proxyConfig);
            this.databaseManager = configYaml.getBoolean("database.enable") ? new MySQL(this) : new SQLite(this);
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&r ----------------------------------------------"));
    }

    public void debug(String message) {

        if (getFilesManager().getPluginConfig().getBoolean("general.debug")) {
            Bukkit.getLogger().info("[BW1058-WinStreak DEBUG]: " + message);
        }
    }

    private void initEvents() {

        if (isBedWars1058Present()) {
            Bukkit.getPluginManager().registerEvents(new PlayerStreakProperties(this), this);
            Bukkit.getPluginManager().registerEvents(new InGameStreakProperties(this), this);
        } else if (isBedWarsProxyPresent()) {
            Bukkit.getPluginManager().registerEvents(new PlayerStreakProperties(this), this);
        }
    }

    private void initCommands() {

        if (isBedWars1058Present()) {

            Bukkit.getPluginCommand("ws").setExecutor(new StreakAdminCommand(this));
            if (getBedWarsAPI().getServerType() != ServerType.BUNGEE) {
                new StreakCommand(this, getBedWarsAPI().getBedWarsCommand(), "streak");
            }
        } else if (isBedWarsProxyPresent()) {
            Bukkit.getPluginCommand("ws").setExecutor(new StreakAdminCommand(this));
            Bukkit.getPluginCommand("streak").setExecutor(new StreakCommandProxy(this));
        }
    }

    public BedWars getBedWarsAPI() {
        return bedWars;
    }

    public com.andrei1058.bedwars.proxy.api.BedWars getBedWarsProxy() {
        return bedWarsProxy;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public FilesManager getFilesManager() {
        return filesManager;
    }

    public StreakCache getStreakCache() {
        return streakCache;
    }

    public MessageUtils getMessageUtils() {
        return messageUtils;
    }
}
