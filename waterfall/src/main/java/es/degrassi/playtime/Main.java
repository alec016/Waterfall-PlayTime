package es.degrassi.playtime;

import es.degrassi.common.Common;
import es.degrassi.common.data.PlayerData;
import es.degrassi.playtime.command.ConfigReload;
import es.degrassi.playtime.command.PlaytimeCommand;
import es.degrassi.playtime.command.PlaytimeReset;
import es.degrassi.playtime.command.PlaytimeResetAll;
import es.degrassi.playtime.command.PlaytimeTopCommand;
import es.degrassi.playtime.handler.ConfigHandler;
import es.degrassi.playtime.handler.UpdateHandler;
import es.degrassi.playtime.handler.cache.PlayerDataCacheHandler;
import es.degrassi.playtime.listener.PlaytimeEvents;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;

public class Main extends Plugin {
  public static Main instance;

  public List<PlayerData> playTimeCache = new LinkedList<>();

  // Handlers
  public ConfigHandler configHandler;
  public PlayerDataCacheHandler playerDataCacheHandler;
  public UpdateHandler updateHandler;

  // Commands
  public ConfigReload configReload;
  public PlaytimeCommand playtimeCommand;
  public PlaytimeTopCommand playtimeTopCommand;
  public PlaytimeReset playtimeReset;
  public PlaytimeResetAll playtimeResetAll;

  // Events
  public PlaytimeEvents playtimeEvents;

  public Main() {
    instance = this;
    init();
    configHandler.initConfig(getDataFolder().toPath());
    configHandler.makeNonChanging();
    configHandler.makeConfigCache();
  }

  @Override
  public void onLoad() {
    Common.registerDB(configHandler);
    playerDataCacheHandler.buildCache();
    registerUpdates();
    registerMessages();
    registerCache();
    registerBStats();
    registerListeners();
    registerScheduler();
    registerCommands();

    getLogger().info("Waterfall PlayTime Loaded.");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    getProxy().unregisterChannel(Common.MCI);
    getProxy().getPluginManager().unregisterCommands(this);
    getProxy().getPluginManager().unregisterListeners(this);
  }

  public void init() {
    initCache();
    initCommands();
    initEvents();
  }

  public void initCache() {
    configHandler = new ConfigHandler(this);
    playerDataCacheHandler = new PlayerDataCacheHandler(this, configHandler);
    updateHandler = new UpdateHandler(this);
  }

  public void initEvents() {
    playtimeEvents = new PlaytimeEvents(this);
  }

  public void initCommands() {
    configReload = new ConfigReload(configHandler);
    playtimeCommand = new PlaytimeCommand(this, configHandler);
    playtimeTopCommand = new PlaytimeTopCommand(this, playerDataCacheHandler, configHandler);
    playtimeReset = new PlaytimeReset(this, configHandler, playerDataCacheHandler);
    playtimeResetAll = new PlaytimeResetAll(this, configHandler);
  }

  public PlayerData getPlayTime(ProxiedPlayer player) {
    return playerDataCacheHandler.get(player);
  }

  public Optional<PlayerData> getPlayTimeOptional(ProxiedPlayer player) {
    return playerDataCacheHandler.getOptional(player);
  }

  public long calculatePlayTime(long rawValue, char v) {
    return switch (v) {
      case 'h' -> rawValue / 3600000;
      case 'm' -> (rawValue % 3600000) / 60000;
      case 's' -> ((rawValue % 3600000) % 60000) / 1000;
      default -> {
        getLogger().log(Level.SEVERE, "Error while Calculating Playtime.");
        yield 0;
      }
    };
  }

  private void registerCommands() {
    getProxy().getPluginManager().registerCommand(this, configReload);
    getProxy().getPluginManager().registerCommand(this, playtimeCommand);
    getProxy().getPluginManager().registerCommand(this, playtimeTopCommand);
    getProxy().getPluginManager().registerCommand(this, playtimeReset);
    getProxy().getPluginManager().registerCommand(this, playtimeResetAll);
  }

  private void registerBStats() {
    if(configHandler.isBSTATS()) {
      int pluginID = 23455;
      Metrics metrics = new Metrics(this, pluginID);
      metrics.addCustomChart(new SimplePie("uses_cache", () -> configHandler.isUSE_CACHE() ? "true" : "false"));
      metrics.addCustomChart(new SimplePie("perms_usage", () -> configHandler.getPermsUsageCount()));
      metrics.addCustomChart(new SimplePie("cachegen_time", () -> playerDataCacheHandler.getCacheGenTime() + " ms"));
      metrics.addCustomChart(new SimplePie("cacheupdate_interval", () -> String.valueOf(configHandler.getCACHE_UPDATE_INTERVAL())));
      metrics.addCustomChart(new SimplePie("autoupdater", () -> String.valueOf(configHandler.isBSTATS())));
      metrics.addCustomChart(new SimplePie("toplistLimit", () -> String.valueOf(configHandler.getTOPLIST_LIMIT())));
    }
  }

  private void registerCache() {
    if(configHandler.isUSE_CACHE()) {
      playerDataCacheHandler.buildCache();
      getProxy().getScheduler()
        .schedule(this, () -> playerDataCacheHandler.updateCache(), 0L, configHandler.getCACHE_UPDATE_INTERVAL(), TimeUnit.MILLISECONDS);
    }
  }

  private void registerScheduler() {
    getProxy().getScheduler()
      .schedule(this, () -> {
        for(ProxiedPlayer player : getProxy().getPlayers()) {
          final String name = player.getName();
          getPlayTimeOptional(player).ifPresentOrElse(playTime -> {
            playTime.increment(1000L);
            if (!configHandler.rewardsH.containsKey(player.getServer().getInfo().getName())) return;
            configHandler.rewardsH.get(player.getServer().getInfo().getName()).keySet().forEach(key -> {
              if(key == playTime.getTime()) {
                getProxy()
                  .getPluginManager()
                  .dispatchCommand(
                    getProxy().getConsole(),
                    configHandler
                      .rewardsH
                      .get(player.getServer().getInfo().getName().toLowerCase(Locale.ROOT))
                      .get(key)
                      .replace("%player%", name)
                  );
              }
            });
          }, () -> {
            PlayerData playTime = new PlayerData(player.getUniqueId(), player.getName(), player.getServer().getInfo().getName());
            playTimeCache.add(playTime);
          });
        }
      }, 0L, 1L, TimeUnit.SECONDS);
  }

  private void registerListeners() {
    getProxy().getPluginManager().registerListener(this, playtimeEvents);
  }

  private void registerUpdates() {
    if(configHandler.isCHECK_FOR_UPDATES())
      getProxy().getScheduler().runAsync(this, () -> updateHandler.checkForUpdates());
  }

  private void registerMessages() {
    getProxy().registerChannel(Common.MCI);
  }
}
