package es.degrassi.playtimeLink;

import es.degrassi.common.Common;
import es.degrassi.common.data.PlayerData;
import es.degrassi.playtimeLink.command.ConfigReload;
import java.util.LinkedList;
import java.util.List;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlaytimeLink extends JavaPlugin {

  public PlaceholderAPI placeholderAPI;
  public UpdateHandler updateHandler;
  public ConfigHandler configHandler;
  public ConfigReload configReload;

  public final List<PlayerData> playTimeCache = new LinkedList<>();

  @Override
  public void onEnable() {
    init();
    saveDefaultConfig();

    this.getServer().getCommandMap().register("playtimelinkreload", configReload);

    final boolean isUpdate = configHandler.isCHECK_FOR_UPDATES();
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      placeholderAPI.register();
    }
    if(configHandler.isBSTATS()) {
      Metrics metrics = new Metrics(this, 23495);
      metrics.addCustomChart(new SimplePie("updater", () -> String.valueOf(isUpdate)));
    }
    if(isUpdate)
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> updateHandler.checkForUpdates());

    getLogger().info("PlaytimeLink has been loaded.");
  }

  private void init() {
    configHandler = new ConfigHandler(this);
    configReload = new ConfigReload(configHandler);
    configHandler.initConfig(getDataFolder().toPath());
    configHandler.makeNonChanging();
    configHandler.makeConfigCache();
    Common.registerDB(configHandler);
    placeholderAPI = new PlaceholderAPI(this);
    updateHandler = new UpdateHandler(this);
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }

  public String calculatePlayTime(long rawValue, char v) {
    switch (v) {
      case 'h' -> {
        return String.valueOf(rawValue / 3600000);
      }
      case 'm' -> {
        return String.valueOf((rawValue % 3600000) / 60000);
      }
      case 's' -> {
        return String.valueOf(((rawValue % 3600000) % 60000) / 1000);
      }
    }
    return "ERR";
  }
}
