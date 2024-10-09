package es.degrassi.playtime.handler.cache;

import es.degrassi.playtime.Main;
import es.degrassi.playtime.handler.ConfigHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class CacheHandler<T> {
  protected final Main main;
  protected final ConfigHandler configHandler;
  protected long cacheGenTime;
  protected long start;

  public CacheHandler(Main main, ConfigHandler configHandler) {
    this.main = main;
    this.configHandler = configHandler;
  }

  public long getCacheGenTime() {
    return cacheGenTime;
  }

  public abstract void buildCache();

  public abstract T generateTempCache();

  public void upd2(ProxiedPlayer player) {

  }

  public void updateCache() {

  }

  public void save() {

  }
}
