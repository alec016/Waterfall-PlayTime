package es.degrassi.playtime.Listeners;

import es.degrassi.playtime.Handlers.ConfigHandler;
import es.degrassi.playtime.Main;

import java.util.Optional;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;

@SuppressWarnings("unused")
public class PlaytimeEvents implements Listener {
  private final Main main;
  private final ConfigHandler configHandler;

  public PlaytimeEvents(Main main, ConfigHandler configHandler) {
    this.main = main;
    this.configHandler = configHandler;
  }

  public void onConnect(PostLoginEvent e) {
    main.getProxy().getScheduler().runAsync(main, () -> {
      String playerName = e.getPlayer().getName();
      if(!main.playtimeCache.containsKey(playerName)) {
        Optional<Long> playtime = configHandler.getPtOptionalFromConfig(playerName);
        if (playtime.isPresent()) {
          main.playtimeCache.put(playerName, playtime.get());
        } else {
          main.playtimeCache.put(playerName, 0L);
        }
      }
    });
  }

  public void onLeave(PlayerDisconnectEvent e) {
    main.getProxy().getScheduler().runAsync(main, () -> {
      final String playerName = e.getPlayer().getName();
      final long playerTime = main.GetPlayTime(playerName);
      configHandler.savePlaytime(playerName, playerTime);
      if(!configHandler.isUSE_CACHE())
        main.playtimeCache.remove(playerName);
    });
  }
}
