package es.degrassi.playtime;

import es.degrassi.playtime.Handlers.CacheHandler;
import es.degrassi.playtime.Handlers.ConfigHandler;
import es.degrassi.playtime.Handlers.DataConverter;
import es.degrassi.playtime.Handlers.UpdateHandler;
import es.degrassi.playtime.Listeners.PlaytimeEvents;
import es.degrassi.playtime.Listeners.RequestHandler;
import es.degrassi.playtime.Commands.ConfigReload;
import es.degrassi.playtime.Commands.PlaytimeCommand;
import es.degrassi.playtime.Commands.PlaytimeReset;
import es.degrassi.playtime.Commands.PlaytimeResetAll;
import es.degrassi.playtime.Commands.PlaytimeTopCommand;
import java.util.logging.Level;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.charts.SimplePie;
import org.bstats.bungeecord.Metrics;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class Main extends Plugin {
  public ConfigHandler configHandler;
  public PlaytimeCommand playtimeCommand;
  public CacheHandler cacheHandler;
  public PlaytimeEvents playtimeEvents;
  public PlaytimeTopCommand playtimeTopCommand;
  public RequestHandler requestHandler;
  public DataConverter dataConverter;
  public ConfigReload configReload;
  public UpdateHandler updateHandler;
  public PlaytimeReset playtimeReset;
  public PlaytimeResetAll playtimeResetAll;
  public final String MCI = "waterfall:playtime";

  public void InitInstance() {
    configHandler = new ConfigHandler(this);
    playtimeCommand = new PlaytimeCommand(this, configHandler);
    cacheHandler = new CacheHandler(this, configHandler);
    playtimeEvents = new PlaytimeEvents(this, configHandler);
    playtimeTopCommand = new PlaytimeTopCommand(this, cacheHandler, configHandler);
    requestHandler = new RequestHandler(this, playtimeTopCommand);
    dataConverter = new DataConverter(this, configHandler);
    configReload = new ConfigReload(configHandler);
    updateHandler = new UpdateHandler(this);
    playtimeReset = new PlaytimeReset(this, configHandler, cacheHandler);
    playtimeResetAll = new PlaytimeResetAll(this, configHandler);
  }

  public final HashMap<String, Long> playtimeCache = new HashMap<>();


  private final Metrics metrics;

  public Main() {
    int pluginID = 22432;
    metrics = new Metrics(this, pluginID);
    InitInstance();

    configHandler.initConfig(getDataFolder().toPath());
    configHandler.makeNonChanging();
    configHandler.makeConfigCache();
  }

  @Override
  public void onLoad() {
    if(configHandler.isCHECK_FOR_UPDATES())
      getProxy().getScheduler().runAsync(this, () -> updateHandler.checkForUpdates());

    getProxy().registerChannel(MCI);

    if(!configHandler.isDataFileUpToDate())
      dataConverter.checkConfig();

    if(configHandler.isUSE_CACHE()) {
      cacheHandler.buildCache();
      getProxy().getScheduler()
        .schedule(this, () -> cacheHandler.updateCache(), configHandler.getCACHE_UPDATE_INTERVAL(), TimeUnit.MILLISECONDS);
    }

    if(configHandler.isBSTATS()) {

      metrics.addCustomChart(new SimplePie("uses_cache", () -> configHandler.isUSE_CACHE() ? "true" : "false"));
      metrics.addCustomChart(new SimplePie("perms_usage", () -> configHandler.getPermsUsageCount()));
      metrics.addCustomChart(new SimplePie("cachegen_time", () -> cacheHandler.getCacheGenTime() + " ms"));
      metrics.addCustomChart(new SimplePie("cacheupdate_interval", () -> String.valueOf(configHandler.getCACHE_UPDATE_INTERVAL())));
      metrics.addCustomChart(new SimplePie("autoupdater", () -> String.valueOf(configHandler.isBSTATS())));
      metrics.addCustomChart(new SimplePie("toplistLimit", () -> String.valueOf(configHandler.getTOPLIST_LIMIT())));
    }

    getProxy().getPluginManager().registerListener(this, playtimeEvents);
    getProxy().getPluginManager().registerListener(this, requestHandler);

    getProxy().getScheduler()
      .schedule(this, () -> {
        for(ProxiedPlayer player : getProxy().getPlayers()) {
          final String name = player.getName();
          final long playTime = GetPlayTime(name);
          playtimeCache.put(name, playTime + 1000L);
          configHandler.rewardsH.keySet().forEach(key -> {
            if(key == playTime) {
              getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), configHandler.rewardsH.get(key).replace("%player%", name));
            }
          });
        }
      }, 1L, TimeUnit.SECONDS);

    getProxy().getPluginManager().registerCommand(this, playtimeCommand);
    getProxy().getPluginManager().registerCommand(this, playtimeTopCommand);
    getProxy().getPluginManager().registerCommand(this, configReload);
    getProxy().getPluginManager().registerCommand(this, playtimeReset);
    getProxy().getPluginManager().registerCommand(this, playtimeResetAll);

    getLogger().info("Waterfall PlayTime Loaded.");
  }

  @Override
  public void onEnable() {
    super.onEnable();
  }

  @Override
  public void onDisable() {
    super.onDisable();
  }

  public long GetPlayTime(String playerName) {
    return playtimeCache.getOrDefault(playerName, 0L);
  }

  public long calculatePlayTime(long rawValue, char v) {
    switch (v) {
      case 'h' -> {
        return rawValue / 3600000;
      }
      case 'm' -> {
        return (rawValue % 3600000) / 60000;
      }
      case 's' -> {
        return ((rawValue % 3600000) % 60000) / 1000;
      }
    }
    getLogger().log(Level.SEVERE, "Error while Calculating Playtime.");
    return 0;
  }
}
