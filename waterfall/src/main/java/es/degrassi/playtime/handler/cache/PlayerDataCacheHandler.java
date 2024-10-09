package es.degrassi.playtime.handler.cache;

import es.degrassi.database.util.InvalidStateException;
import es.degrassi.playtime.Main;
import es.degrassi.common.data.PlayerData;
import es.degrassi.playtime.handler.ConfigHandler;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerDataCacheHandler extends CacheHandler<List<PlayerData>> {
  public PlayerDataCacheHandler(Main main, ConfigHandler configHandler) {
    super(main, configHandler);
  }

  @Override
  public void buildCache() {
    main.playTimeCache.clear();
    start = System.currentTimeMillis();
    try {
      PlayerData.load(main.playTimeCache);
    } catch (InvalidStateException | SQLException | ClassCastException e) {
      main.getLogger().log(Level.SEVERE, "Config initialize error. {0}", e);
      main.onDisable();
    }

    cacheGenTime = (System.currentTimeMillis() - start) + configHandler.getGenTime();
    main.getLogger().log(Level.INFO, "The player cache has been built, took: {0} ms", cacheGenTime);
  }

  @Override
  public List<PlayerData> generateTempCache() {
    return new LinkedList<>(main.playTimeCache);
  }

  @Override
  public void upd2(ProxiedPlayer player) {
    generateTempCache().forEach(PlayerData::save);
    buildCache();
  }

  @Override
  public void updateCache() {
    generateTempCache().forEach(PlayerData::save);
    buildCache();
  }

  public PlayerData get(ProxiedPlayer player) {
    return generateTempCache().stream().filter(data -> data.getPlayer().equals(player.getUniqueId()) &&
      data.getServer().equals(player.getServer().getInfo().getName())).findFirst().orElse(null);
  }

  public PlayerData get(UUID uuid) {
    ProxiedPlayer player = Main.instance.getProxy().getPlayer(uuid);
    return get(player);
  }

  public Optional<PlayerData> getOptional(ProxiedPlayer player) {
    return generateTempCache().stream().filter(data -> data.getPlayer().equals(player.getUniqueId()) &&
      data.getServer().equals(player.getServer().getInfo().getName())).findFirst();
  }

  @Override
  public void save() {
    main.playTimeCache.forEach(PlayerData::save);
  }

  public void save(ProxiedPlayer player) {
    main.playTimeCache.stream()
      .filter(playerData ->
        playerData.getPlayer().equals(player.getUniqueId()) &&
          player.getServer().getInfo().getName().equals(playerData.getServer())
      ).findFirst().ifPresent(PlayerData::save);
  }
}
