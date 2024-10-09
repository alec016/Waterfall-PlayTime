package es.degrassi.playtime.listener;

import es.degrassi.common.data.PlayerData;
import es.degrassi.playtime.Main;
import java.util.Optional;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlaytimeEvents implements Listener {
  private final Main main;

  public PlaytimeEvents(Main main) {
    this.main = main;
  }

  @EventHandler
  public void onConnect(ServerConnectedEvent e) {
    ProxiedPlayer player = e.getPlayer();
    Optional<PlayerData> playtime = main.getPlayTimeOptional(player);
    if (playtime.isPresent()) {
      main.playTimeCache.add(playtime.get());
    } else {
      main.playTimeCache.add(new PlayerData(player.getUniqueId(), player.getName(), player.getServer().getInfo().getName()));
    }
    main.playerDataCacheHandler.updateCache();
  }

  @EventHandler
  public void onLeave(ServerDisconnectEvent e) {
    final ProxiedPlayer player = e.getPlayer();
    main.playerDataCacheHandler.save(player);
  }
}
